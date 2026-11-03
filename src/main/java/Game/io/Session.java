package Game.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import Game.client.MessageHandler;
import Game.core.Manager;
import Game.core.ServerManager;
import Game.core.Util;
import Game.map.MapService;
import Game.Language.VietNam;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import Game.client.Clan;
import Game.client.Player;
import Game.core.CheckDDOS;
import Game.core.Log;
import Game.core.SQL;
import Game.core.SaveData;
import Game.core.SessionManager;

import java.security.SecureRandom;

import Game.map.Map;
import Game.security.AntiCheatService;
import Game.security.ClientAttestationService;
import Game.security.PacketRateLimiter;
import Game.security.SessionState;
import Game.security.ViolationType;
import Game.template.Part_player;

public class Session implements Runnable {

    public static final List<Session> client_entry = new LinkedList<>();
    public final Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Thread sendd;
    private Thread receiv;
    public boolean connected;
    private final BlockingQueue<Message> list_msg;
    private boolean sendKeyComplete;
    private byte curR;
    private byte curW;
    private final byte[] keys = "@HSO".getBytes();
    public int id;
    public String user;
    public String pass;
    public byte ac_admin = 0;
    public String ip;
    private final MessageHandler controller;
    public Player p;
    public String[] list_char;
    public byte zoomlv;
    public int version;
    public byte status;
    public boolean get_in4;
    public long timeConnect;
    public long tongnap;
    public long coin;
    public boolean isLogin;
    public long timeStartLogin;
    public byte typeLanguage;
    public VietNam language;
    public volatile byte[] attestationNonce;
    public volatile long attestationIssuedAt;
    public volatile boolean unityAttested;
    public volatile String clientBuildId = "";
    public volatile String clientPlatform = "";
    public volatile String clientPackageName = "";
    private volatile SessionState sessionState = SessionState.CONNECTED;
    private final PacketRateLimiter packetRateLimiter = new PacketRateLimiter();
    private int antiCheatScore;

    public Session(Socket socket) {
        timeConnect = System.currentTimeMillis();
        SecureRandom random = new SecureRandom();
        random.nextBytes(keys);
        this.socket = socket;
        this.list_msg = new LinkedBlockingQueue<>();
        this.sendKeyComplete = false;
        this.connected = false;
        this.controller = new MessageHandler(this);
        get_in4 = false;
    }

    public Session() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void init() {
        try {
            this.ip = this.socket.getInetAddress().getHostAddress();

            if (this.ip != null && !this.ip.equals("127.0.0.1") && (CheckDDOS.isIPExist(this.ip) || !CheckDDOS.canAccess(this.ip) || !CheckDDOS.checkCountIP(ip))) {
                this.socket.close();
                this.connected = false;
                Session.client_entry.remove(this);
                return;
            }
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.connected = true;
            this.get_in4 = false;
            this.sendd = new Thread(() -> {
                try {
                    while (connected) {
                        Message m = list_msg.poll(5, TimeUnit.SECONDS);
                        if (m != null) {
                            send_msg(m);
                            m.cleanup();
                        }
                    }
                } catch (InterruptedException e) {
                } catch (IOException e) {
                } finally {
//                     disconnect();
                }
            });
            this.receiv = new Thread(this);
            this.receiv.start();
            this.sendd.start();
            synchronized (Session.client_entry) {
                Session.client_entry.add(this);
            }
            System.out.println("accecpt ip " + ip + " - online : " + Session.client_entry.size());
        } catch (IOException e) {
            e.printStackTrace();
            this.connected = false;
        }
    }

    public void SaveIP() {
        String sql = "UPDATE `account` SET `last_ip` = '" + this.ip + "' WHERE id = " + this.id + ";";
        try (Connection connection = SQL.gI().getConnection(); Statement ps = connection.createStatement()) {
            if (ps.executeUpdate(sql) > 0) {
                connection.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            //return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        this.sessionState = SessionState.CLOSED;
        if (isLogin) {
            Manager.gI().time_login_client.put(this.user, (System.currentTimeMillis() + Manager.gI().time_login));
        }
        //
        //CheckDDOS.removeIp(ip);
        this.connected = false;
        this.sendd.interrupt();
        this.receiv.interrupt();
        if (this.p != null) {
            try {
                if (p.party != null && p.party.get_mems().size() > 1) {
                    p.party.remove_mems(p);
                    p.party.sendin4();
                    p.party.send_txt_notice(p.name + " rời nhóm");
                    p.party = null;
                }
                if (p.name_trade != null && !p.name_trade.equals("")) {
                    Player p0;
                    p0 = Map.get_player_by_name(p.name_trade);
                    if (p0 != null) {
                        Message m = new Message(36);
                        m.writer().writeByte(6);
                        p0.conn.addmsg(m);
                        m.cleanup();
                        p0.name_trade = "";
                        p0.lock_trade = false;
                        p0.money_trade = 0;
                        p0.accept_trade = false;
                        p0.list_item_trade = null;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            p.flush();
            MapService.leave(p.map, p);
            Log.gI().add_log(p.name, "Logout : [Vàng] : " + Util.number_format(p.get_vang()) + " : [Ngọc] : "
                    + Util.number_format(p.get_ngoc()));
        }
        //
        this.isLogin = false;
        try {
            if (this.socket != null && this.socket.isConnected()) {
                this.socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        synchronized (Session.client_entry) {
            Session.client_entry.remove(this);
        }
        System.out.println("disconnect session " + user + " - online : " + (Session.client_entry.size() - 1));
    }

    public void addmsg(Message m) {
        if (connected) {
            this.list_msg.add(m);
        }
    }

    @Override
    public void run() {
        try {
            while (connected) {
                Message m = read_msg();
                if (Manager.gI().antiCheatEnabled
                        && !packetRateLimiter.allow(m.cmd, this.p != null, Manager.gI().packetRatePerSecond)) {
                    int score = AntiCheatService.gI().record(this, ViolationType.PACKET_RATE, 3,
                            "command=" + m.cmd);
                    m.cleanup();
                    if (score >= 15) {
                        close();
                        break;
                    }
                    continue;
                }
                if (Manager.gI().unityOnly && sendKeyComplete && !unityAttested
                        && m.cmd != ClientAttestationService.COMMAND
                        && !(this.ip.equals("127.0.0.1") && m.cmd == -1)) {
                    int score = AntiCheatService.gI().record(this, ViolationType.CLIENT_ATTESTATION, 2,
                            "command before Unity attestation=" + m.cmd);
                    if (m.cmd == 1) {
                        sendLoginNotice("Server chi ho tro phien ban Unity moi nhat.");
                    }
                    m.cleanup();
                    if (score >= 6) {
                        close();
                        break;
                    }
                    continue;
                }
                if (this.ip.equals("127.0.0.1") && m.cmd == -1) {
                    ServerManager.gI().close();
                    System.out.println("Close server is processing....");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SaveData.process();
                            for (int k = Session.client_entry.size() - 1; k >= 0; k--) {
                                Session.client_entry.get(k).p = null;
                                try {
                                    Session.client_entry.get(k).close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            Manager.gI().close();
                        }
                    }).start();
                } else if (m.cmd == -40 && sessionState == SessionState.CONNECTED) {
                    sendkeys();
                } else if (sendKeyComplete && (this.p != null || m.cmd == ClientAttestationService.COMMAND
                        || m.cmd == 61 || m.cmd == 1 || m.cmd == 14 || m.cmd == 13)) {
                    try {
                        controller.process_msg(m);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (Manager.gI().antiCheatEnabled) {
                    AntiCheatService.gI().record(this, ViolationType.INVALID_SESSION_STATE, 2,
                            "command=" + m.cmd + ",state=" + sessionState);
                }
                m.cleanup();
            }
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            if (!this.socket.isClosed()) {
                disconnect();
            }
        }
    }

    private void send_msg(Message msg) throws IOException {
        byte[] data = msg.getData();
        if (msg.cmd == 25) {
            msg.cmd = 126;
        }
        if (sendKeyComplete) {
            byte b = writeKey(msg.cmd);
            dos.writeByte(b);
        } else {
            dos.writeByte(msg.cmd);
        }
        if (data != null) {
            int size = data.length;
            SessionManager.AddBandWidth(ip, size);
            if (msg.cmd == -51 || msg.cmd == -52 || msg.cmd == -54 || msg.cmd == 126) {
                if (msg.cmd == 126) {
                    byte bspec = writeKey((byte) 25);
                    dos.writeByte(bspec);
                }
                byte b4 = (byte) (size);
                byte b3 = (byte) ((byte) (size >> 8));
                byte b2 = (byte) ((byte) (size >> 16));
                byte b1 = (byte) ((byte) (size >> 24));
                final int byte4 = this.writeKey(b4);
                final int byte3 = this.writeKey(b3);
                final int byte2 = this.writeKey(b2);
                final int byte1 = this.writeKey(b1);
                this.dos.writeByte(byte1);
                this.dos.writeByte(byte2);
                this.dos.writeByte(byte3);
                this.dos.writeByte(byte4);
            } else if (sendKeyComplete) {
                int byte1 = writeKey((byte) (size >> 8));
                dos.writeByte(byte1);
                int byte2 = writeKey((byte) (size));
                dos.writeByte(byte2);
            } else {
                final int byte1 = (byte) (size & 0xFF00);
                this.dos.writeByte(byte1);
                final int byte2 = (byte) (size & 0xFF);
                this.dos.writeByte(byte2);
            }
            if (sendKeyComplete) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = writeKey(data[i]);
                }
            }
            dos.write(data);
        } else {
            final int byte1 = (byte) (0 & 0xFF00);
            this.dos.writeByte(byte1);
            final int byte2 = (byte) (0 & 0xFF);
            this.dos.writeByte(byte2);
        }
        dos.flush();
        msg.cleanup();
        Util.logconsole("___send msg : " + msg.cmd + " - size : " + data.length + " : " + user, 1, msg.cmd);
    }

    private Message read_msg() throws IOException {
        byte cmd = dis.readByte();
        if (sendKeyComplete) {
            cmd = readKey(cmd);
        }
        int size;
        if (sendKeyComplete) {
            byte b1 = dis.readByte();
            byte b2 = dis.readByte();
            size = (readKey(b1) & 255) << 8 | readKey(b2) & 255;
        } else {
            size = dis.readShort();
        }
        if (size < 0 || size > Manager.gI().maxInboundPacketSize) {
            if (Manager.gI().antiCheatEnabled) {
                AntiCheatService.gI().record(this, ViolationType.INVALID_PACKET, 10,
                        "command=" + cmd + ",size=" + size);
            }
            throw new IOException("Invalid inbound packet size: " + size);
        }
        byte data[] = new byte[size];
        dis.readFully(data);
        if (sendKeyComplete) {
            for (int i = 0; i < data.length; i++) {
                data[i] = readKey(data[i]);
            }
        }
        SessionManager.AddBandWidth(ip, size);
        Util.logconsole("Read msg : " + cmd + " - size : " + data.length, 0, cmd);
        return new Message(cmd, data);
    }

    private byte readKey(final byte b) {
        final byte curR = this.curR;
        this.curR = (byte) (curR + 1);
        final byte i = (byte) ((keys[curR] & 0xFF) ^ (b & 0xFF));
        if (this.curR >= keys.length) {
            this.curR %= (byte) keys.length;
        }
        return i;
    }

    private byte writeKey(final byte b) {
        final byte curW = this.curW;
        this.curW = (byte) (curW + 1);
        final byte i = (byte) ((keys[curW] & 0xFF) ^ (b & 0xFF));
        if (this.curW >= keys.length) {
            this.curW %= (byte) keys.length;
        }
        return i;
    }

    private void sendkeys() throws IOException {
        Message m = new Message(-40);
        m.writer().writeByte(keys.length);
        m.writer().writeByte(keys[0]);
        for (int i = 1; i < keys.length; i++) {
            m.writer().writeByte(keys[i] ^ keys[i - 1]);
        }
        send_msg(m);
        m.cleanup();
        sendKeyComplete = true;
        sessionState = SessionState.KEY_EXCHANGED;
    }

    public void getclientin4(Message m) throws IOException {
        if (Manager.gI().unityOnly && !unityAttested) {
            AntiCheatService.gI().record(this, ViolationType.CLIENT_ATTESTATION, 10,
                    "legacy login rejected");
            sendLoginNotice("Server chi ho tro phien ban Unity moi nhat.");
            close();
            return;
        }
        if (sessionState != SessionState.KEY_EXCHANGED && sessionState != SessionState.ATTESTED) {
            AntiCheatService.gI().record(this, ViolationType.INVALID_SESSION_STATE, 5,
                    "login in state=" + sessionState);
            return;
        }
        if (!CheckDDOS.checkCountIP(ip)) {
            noticelogin("Bạn đã đăng nhập quá giới hạn tài khoản trên cùng 1 mạng");
            return;
        }
        this.user = m.reader().readUTF().trim();
        this.pass = m.reader().readUTF().trim();
        
        this.version = Integer.parseInt(m.reader().readUTF().replace(".", "")); // version
        String clinePro = m.reader().readUTF(); // clinePro
        String pro = m.reader().readUTF(); // pro
        String agent = m.reader().readUTF(); // agent
        this.zoomlv = m.reader().readByte();
        byte device = m.reader().readByte(); // device
        int id = m.reader().readInt(); // id
        byte area = m.reader().readByte(); // area
        byte Main = m.reader().readByte(); // !Main.isPC ? 0 : 1
        byte IndexRes = m.reader().readByte(); // IndexRes
        byte indexInfoLogin = m.reader().readByte(); // indexInfoLogin
        byte fake = m.reader().readByte(); // fake byte
        short indexCharPar = m.reader().readShort();
        String stringPackageName = m.reader().readUTF(); // stringPackageName
        System.out.println(this.user + " login with " + version + " zoom " + zoomlv);
//        if (verion > 280) {
//            noticelogin("Hiện tại chưa hỗ trợ version trên 2.8.0");
//            return;
//        }
        //
        Pattern p = Pattern.compile("^[a-zA-Z0-9@.]{1,15}$");
        if ((!this.user.contains("knightauto_hsr_")) && (!p.matcher(user).matches() || !p.matcher(pass).matches())) {
            noticelogin("Ký tự nhập vào không hợp lệ!!");
            return;
        }
        long time_can_login = 0;
        if (Manager.gI().time_login_client.containsKey(this.user)) {
            time_can_login = Manager.gI().time_login_client.get(this.user) - System.currentTimeMillis();
        }

        int dem = 0;
        for (int i = 0; i < Session.client_entry.size(); i++) {
            if (Session.client_entry.get(i).ip.equals(this.ip)) {
                dem++;
            }
        }
        if (!this.ip.equals("127.0.0.1") && Manager.gI().login_limit_per_ip > 0) {
            int loginSlots = SessionManager.countLoginSlotsByIp(this.ip, this);
            if (loginSlots >= Manager.gI().login_limit_per_ip) {
                noticelogin("IP nay chi duoc dang nhap toi da " + Manager.gI().login_limit_per_ip + " nick");
                return;
            }
        }
        if (Manager.gI().isServerAdmin && !user.equals("admin1")) {
            noticelogin("Hiện tại server đang nâng cấp hệ thống. Chỉ admin mới có thể truy cập");
            return;
        }
        if (dem > Manager.gI().allow_ip_client) {
            noticelogin("Vượt quá số lượng ip có thể truy cập vào thời điểm này!");
            return;
        }
        if (pass.equals("1") && user.equals("1")) {
//            noticelogin("Vui lòng truy cập hsomobile.com để đăng ký");
//            return;
            user = "knightauto_hsr_" + String.valueOf(System.nanoTime());
            pass = "hsr_132";
            //
            try (Connection connnect = SQL.gI().getConnection(); Statement ps = connnect.createStatement()) {
                if (!ps.execute("INSERT INTO `account` (`user`, `pass`, `char`, `status`, `lock`, `coin`, `ip`, `email`, `ac_admin`) VALUES ('" + user
                        + "', '" + pass + "', '[]', 0, 0, 0, 0, 0, 0)")) {
                    connnect.commit();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                noticelogin("Vui lòng liên hệ admin để tạo tài khoản");
                return;
            }
            this.list_char = new String[3];
            for (int i = 0; i < 3; i++) {
                this.list_char[i] = "";
            }
        } else {
            //
            String query = "SELECT * FROM `account` WHERE `user` = ? AND `pass` = ? LIMIT 1";
            try (Connection conn = SQL.gI().getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, user);
                ps.setString(2, pass);
                try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    noticelogin("Thông tin đăng nhập không chính xác");
                    return;
                }
                this.id = rs.getInt("id");
                this.ac_admin = rs.getByte("ac_admin");
                this.status = rs.getByte("status");
                this.coin = rs.getLong("coin");
                this.tongnap = rs.getLong("tongnap");
                this.typeLanguage = rs.getByte("typeLanguage");
                //  this.topnap = rs.getInt("topnap");
                if (this.ac_admin <= 0 && time_can_login > 0 && !ip.equals("127.0.0.1")) {
                    float t_ = ((float) time_can_login) / 1000f;
                    noticelogin("sau " + String.format("%.1f", t_) + "s nữa mới có thể vào!");
                    return;
                }
                if (rs.getByte("lock") == 1 && !ip.equals("127.0.0.1")) {
                    noticelogin("Tài khoản đã bị khóa vì có hành vi xấu ảnh hưởng server");
                    return;
                }
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("char"));
                if (jsar == null) {
                    noticelogin("Có lỗi xảy ra, hãy thử lại!");
                    return;
                }
                this.list_char = new String[3];
                for (int i = 0; i < 3; i++) {
                    this.list_char[i] = "";
                }
                for (int i = 0; i < jsar.size(); i++) {
                    this.list_char[i] = jsar.get(i).toString();
                }
                    jsar.clear();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                noticelogin("Có lỗi xảy ra, hãy thử lại!");
                return;
            }
        }
        if (indexCharPar != Manager.gI().indexCharPar) {
            Message m13 = new Message(63);
            m13.writer().writeByte(Manager.gI().indexRes);
            addmsg(m13);
            m13.cleanup();
            //
            send_char_part();
        } else {
            send_listchar_board();
        }
        //
        Message md = new Message(31);
        md.writer().writeUTF(user);
        md.writer().writeUTF(pass);
        addmsg(md);
        md.cleanup();
        //
        for (int id0 = 10200; id0 < 10242; id0++) {
            Message m22 = new Message(-51);
            m22.writer().writeShort(id0);
            m22.writer().write(Util.loadfile("data/icon/x" + zoomlv + "/" + id0 + ".png"));
            addmsg(m22);
            m22.cleanup();
        }
        this.timeStartLogin = System.currentTimeMillis();
        this.get_in4 = true;
        this.sessionState = SessionState.ACCOUNT_AUTHENTICATED;
    }

    public void send_char_part() {
        new Thread(() -> {
            try {
                Message m = new Message(-57);
                m.writer().writeShort(Manager.gI().indexCharPar);
                m.writer().writeShort(1028);
                addmsg(m);
                m.cleanup();
                //
                for (int i = 0; i < 1028; i++) {
                    m = new Message(-52);
                    switch (zoomlv) {
                        case 1: {
                            m.writer().write(Manager.gI().data_part_char_x1[i]);
                            break;
                        }
                        case 2: {
                            m.writer().write(Manager.gI().data_part_char_x2[i]);
                            break;
                        }
                        case 3: {
                            m.writer().write(Manager.gI().data_part_char_x3[i]);
                            break;
                        }
                        case 4: {
                            m.writer().write(Manager.gI().data_part_char_x4[i]);
                            break;
                        }
                        default: {
                            return;
                        }
                    }
                    addmsg(m);
                    m.cleanup();
                }
                send_listchar_board();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void noticelogin(String s) throws IOException {
        Message m = new Message(2);
        m.writer().writeUTF(s);
        m.writer().writeByte(0);
        addmsg(m);
        m.cleanup();
    }

    public void sendLoginNotice(String message) throws IOException {
        noticelogin(message);
    }

    public boolean ownsCharacterId(int characterId) {
        if (list_char == null || characterId <= 0) {
            return false;
        }
        String query = "SELECT `name` FROM `player` WHERE `id` = ? LIMIT 1";
        try (Connection connection = SQL.gI().getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, characterId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return false;
                }
                String characterName = result.getString("name");
                for (String ownedName : list_char) {
                    if (ownedName != null && ownedName.equals(characterName)) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public SessionState getSessionState() {
        return sessionState;
    }

    public void setSessionState(SessionState state) {
        this.sessionState = state;
    }

    public synchronized int addAntiCheatScore(int score) {
        antiCheatScore += score;
        return antiCheatScore;
    }

    public synchronized int getAntiCheatScore() {
        return antiCheatScore;
    }

    public void send_listchar_board() throws IOException {
        //
        if (this.list_char == null) {
            this.disconnect();
            return;
        }
        String name_querry = String.format("`name` = '%s' OR `name` = '%s' OR `name` = '%s'", this.list_char[0],
                this.list_char[1], this.list_char[2]);
        try (Connection connection = SQL.gI().getConnection(); Statement ps = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet rs
                = ps.executeQuery("SELECT * FROM `player` WHERE " + name_querry + " ORDER BY `id` DESC LIMIT 3;");) {
            rs.last();
            int row = rs.getRow();
            rs.afterLast();
            Message m = new Message(13);
            m.writer().writeByte(row); // list char size
            while (rs.previous()) {
                m.writer().writeInt(rs.getInt("id"));
                String name = rs.getString("name");
                m.writer().writeUTF(name);
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("body"));
                if (jsar == null) {
                    return;
                }
                m.writer().writeByte(Byte.parseByte(jsar.get(0).toString())); // head
                m.writer().writeByte(Byte.parseByte(jsar.get(2).toString())); // hair
                m.writer().writeByte(Byte.parseByte(jsar.get(1).toString())); // eye
                //
                jsar.clear();
                List<Part_player> itemwear = new ArrayList<>();
                jsar = (JSONArray) JSONValue.parse(rs.getString("itemwear"));
                if (jsar == null) {
                    return;
                }
                for (int i3 = 0; i3 < jsar.size(); i3++) {
                    JSONArray jsar2 = (JSONArray) JSONValue.parse(jsar.get(i3).toString());
                    if (jsar2 == null) {
                        return;
                    }
                    byte index_wear = Byte.parseByte(jsar2.get(9).toString());
                    if (index_wear != 0 && index_wear != 1 && index_wear != 6 && index_wear != 7 && index_wear != 10) {
                        continue;
                    }
                    Part_player temp = new Part_player();
                    temp.type = Byte.parseByte(jsar2.get(2).toString());
                    temp.part = Byte.parseByte(jsar2.get(6).toString());
                    itemwear.add(temp);
                }
                jsar.clear();
                m.writer().writeByte(itemwear.size()); // size part
                for (int j = 0; j < itemwear.size(); j++) {
                    m.writer().writeByte(itemwear.get(j).type);
                    m.writer().writeByte(itemwear.get(j).part);
                }
                short level_ = rs.getShort("level");
                if (level_ > Manager.gI().lvmax) {
                    level_ = (short) Manager.gI().lvmax;
                }
                m.writer().writeShort(level_);
                m.writer().writeByte(Byte.parseByte(rs.getString("clazz")));
                m.writer().writeByte(1); // fake
                m.writer().writeByte(0); // fake
                Clan clan = Clan.get_clan_of_player(name);
                if (clan != null) {
                    m.writer().writeShort(clan.icon);
                    m.writer().writeUTF(clan.name_clan_shorted);
                    m.writer().writeByte(clan.get_mem_type(name));
                } else {
                    m.writer().writeShort(-1); // clan in4
                }
            }
            addmsg(m);
            m.cleanup();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void char_create(Message m) throws IOException {
        if (!Manager.gI().ip_create_char.containsKey(this.ip)) {
            Manager.gI().ip_create_char.put(this.ip, 0);
        }
        int count = Manager.gI().ip_create_char.get(this.ip);
        if (count > 1) {
            notice_create_char("Đã quá lượt tạo nhân vật hôm nay");
             return;
        }
        if (this.list_char == null || !this.list_char[2].isEmpty()) {
            return;
        }
//        if (!this.list_char[0].isEmpty()) {
//            notice_create_char("Chỉ tạo 1 nhân vật");
//            return;
//        }
        int charNumber = 0;
        for (String charName : this.list_char
        ) {
            if(!charName.equals("")) charNumber++;
        }
        if (charNumber>0) {
            notice_create_char("Chỉ được tạo 1 nhân vật thôi!!");
            return;
        }
        byte clazz = m.reader().readByte();
        String name = m.reader().readUTF().toLowerCase();
        byte hair = m.reader().readByte();
        byte eye = m.reader().readByte();
        byte head = m.reader().readByte();
        //
        Pattern p = Pattern.compile("^[a-zA-Z0-9]{6,10}$");
        if (!p.matcher(name).matches()) {
            notice_create_char("Tên không hợp lệ");
            return;
        }
        if (name != null && (name.contains("ad") || name.contains("server") || name.contains("sever") || name.contains("thongbao"))) {
            notice_create_char("Tên không hợp lệ");
            return;
        }
        try (Connection connnect = SQL.gI().getConnection(); PreparedStatement ps = connnect.prepareStatement(
                "INSERT INTO `player` (`name`, `body`, `clazz`, `site`, `vang`, `kimcuong`, `tiemnang`, `kynang`, " +
                        "`point1`, `point2`, `point3`, `point4`, `itemwear`, `item4`, `rms_save`, `date`, `diemdanh`, " +
                        "`skill`, `typeexp`, `medal_create_material`,`count_dungeon`,`item3`,`item7`,`itembox3`," +
                        "`itembox4`,`itembox7`,`itembag3`,`itembag4`,`itembag7`, `pet`, `friend`, `enemies`) " +
                        "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            ps.setNString(1, name);
            JSONArray jsar = new JSONArray();
            jsar.add(head);
            jsar.add(eye);
            jsar.add(hair);
            ps.setNString(2, jsar.toJSONString());
            jsar.clear();
            ps.setByte(3, clazz);
            ps.setNString(4, "[0,132,354]");
            ps.setLong(5, 50000L);
            ps.setInt(6, 5000);
            ps.setShort(7, (short) 5);
            ps.setShort(8, (short) 1);
            ps.setShort(9, (short) 5);
            ps.setShort(10, (short) 5);
            ps.setShort(11, (short) 5);
            ps.setShort(12, (short) 5);
            switch (clazz) {
                case 0: {
                    ps.setNString(13,
                            "[[0,0,8,1,0,0,0,0,[[0,54],[40,120]],0],[80,0,0,1,16,0,0,0,[[14,52],[16,100]],1],[120,0,1,1,24,0,0,0,[[14,18],[25,3]],7]]");
                    break;
                }
                case 1: {
                    ps.setNString(13,
                            "[[5,1,9,1,1,0,0,0,[[0,54],[40,120]],0],[105,1,0,1,21,0,1,0,[[14,52],[20,100]],1],[145,1,1,1,29,0,1,0,[[14,18],[24,3]],7]]");
                    break;
                }
                case 2: {
                    ps.setNString(13,
                            "[[10,2,11,1,2,0,0,0,[[0,50],[40,120]],0],[90,2,0,1,18,0,2,0,[[14,42],[16,200]],1],[50,2,2,1,10,0,2,0,[[7,200],[14,12]],6],[130,2,1,1,26,0,2,0,[[14,12],[26,4]],7]]");
                    break;
                }
                default: {
                    ps.setNString(13,
                            "[[15,3,10,1,3,0,0,0,[[0,50],[40,120]],0],[95,3,0,1,19,0,3,0,[[14,44],[16,200]],1],[55,3,2,1,11,0,3,0,[[7,200],[14,14]],6],[135,3,1,1,27,0,3,0,[[14,14],[24,4]],7]]");
                    break;
                }
            }
            ps.setNString(14, "[[2,999],[5,999],[205,100],[206,100],[207,100]]");
            ps.setNString(15, "[[],[]]");
            ps.setNString(16, Date.from(Instant.now()).toString());
            ps.setInt(17, 1);
            jsar.add(1);
            for (int i = 0; i < 20; i++) {
                jsar.add(0);
            }
            ps.setNString(18, jsar.toJSONString());

            ps.setInt(19, 1);
            ps.setNString(20, "[295,261,318,328,341,249,285,321,329,344,284,280,316,327,344,288,280,317,327,342]");
            ps.setInt(21, 10);
            ps.setNString(22, "[]");
            ps.setNString(23, "[]");
            ps.setNString(24, "[]");
            ps.setNString(25, "[]");
            ps.setNString(26, "[]");
            ps.setNString(27, "[]");
            ps.setNString(28, "[]");
            ps.setNString(29, "[]");
            ps.setNString(30, "[]");
            ps.setNString(31, "[]");
            ps.setNString(32, "[]");
            jsar.clear();
            if (!ps.execute()) {
                connnect.commit();
            }
            //
            for (int i = 0; i < this.list_char.length; i++) {
                if (this.list_char[i].isEmpty()) {
                    this.list_char[i] = name;
                    break;
                }
            }
            Manager.gI().ip_create_char.replace(this.ip, count, (count + 1));
            send_listchar_board();
            flush();
        } catch (SQLException e) {
//            e.printStackTrace();
            notice_create_char("Tên này đã sử dụng, hãy thử lại");
        }
    }

    private void flush() {
        if (list_char != null) {
            String query = "[";
            for (int i = 0; i < list_char.length; i++) {
                if (!this.list_char[i].isEmpty()) {
                    query += "\"" + this.list_char[i] + "\",";
                }
            }
            query = query.substring(0, query.length() - 1);
            query += "]";
            try (Connection conn = SQL.gI().getConnection(); Statement statement = conn.createStatement()) {
                if (statement.executeUpdate(
                        "UPDATE `account` SET `char` = '" + query + "' WHERE `user` = '" + this.user + "';") > 0) {
                    conn.commit();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void notice_create_char(String s) throws IOException {
        Message m = new Message(37);
        m.writer().writeUTF(s);
        m.writer().writeUTF("");
        m.writer().writeByte(0);
        addmsg(m);
        m.cleanup();
    }
    public static void banAcc(String name) throws IOException {
        try (Connection conn = SQL.gI().getConnection(); Statement statement = conn.createStatement()) {
            if (statement.executeUpdate(
                    "UPDATE `account` SET `lock` = '" + 1 + "' WHERE `user` = '" + name + "';") > 0) {
                conn.commit();
            }
            Player p = Map.get_player_by_name(name);
            if (p != null) {
                p.conn.disconnect();
            }
            Manager.gI().notifierBot.sendNotification("Đã khoá tài khoản " + name);
        } catch (SQLException e) {
            e.printStackTrace();
            Manager.gI().notifierBot.sendNotification("Lỗi khoá tài khoản " + name);
        }
    }
    public void close() throws IOException {
        this.disconnect();
    }
    
}
