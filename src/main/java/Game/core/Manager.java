package Game.core;

import Game.Boss.BossServer;
import Game.NPC.NpcTemplate;
import Game.Quest.QuestTemplate;
import Game.ai.BotManager;
import Game.map.*;
import Game.template.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import Game.event.EventManager;
import Game.event.Noel;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Game.activities.KingCupManager;
import Game.activities.Lottery;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import Game.client.Clan;
import Game.event.Event_2;
import Game.event.Event_3;
import Game.activities.ChiemMo;
import Game.activities.ChienTruong;

import Game.gamble.LuckyDraw;
import Game.io.Message;

import org.json.simple.JSONObject;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Manager {
    public BotManager notifierBot;

    public int size_mob;
    private static Manager instance;
    public final HashMap<String, Integer> ip_create_char = new HashMap<>();
    public final HashMap<String, Long> time_login_client = new HashMap<>();
    public final byte[] msg_25_new;
    public final byte[] msg_29_chienbinh;
    public final byte[] msg_29_satthu;
    public final byte[] msg_29_phapsu;
    public final byte[] msg_29_xathu;
    public final byte[] msg_1;
    public final byte[] msg_61;
    public final byte[] msg_26;
    public final byte[] msg_eff_70;
    public final byte[] msg_eff_71;
    public final byte[] msg_eff_109;
    public final byte[] msg_eff_105;
    public boolean debug;
    public String mysql_host;
    public int mysql_port;
    public String mysql_database;
    public String mysql_user;
    public String mysql_pass;
    public boolean mysql_auto_start;
    public String mysql_start_bin;
    public String mysql_start_ini;
    public int mysql_start_timeout_ms;
    public boolean isServerAdmin;
    public int event;
    public int server_port;
    public byte indexRes;
    public int indexCharPar;
    public int exp;
    public int lvmax;
    public int allow_ip_client;
    public int login_limit_per_ip;
    public String admin_tool_bind;
    public int admin_tool_port;
    public String admin_tool_token;
    public boolean antiCheatEnabled;
    public boolean unityOnly;
    public int unityProtocol;
    public int maxInboundPacketSize;
    public int packetRatePerSecond;
    public int movementUnitsPerSecond;
    public int movementGraceUnits;
    public final HashMap<String, String> unityBuildSecrets = new HashMap<>();
    public final Set<String> unityPackages = new HashSet<>();
    public int time_login;
    public List<ItemSell3[]> itemSellTB;
    public short[] itemPoitionSell;
    public short[] item7sell;
    public LuckyDraw lucky_draw_vip;
    public LuckyDraw lucky_draw_normal;
    public ArrayList<BossServer> listBossServers = new ArrayList<>();
    public int size_mob_now = -20;
    public ChiemMo chiem_mo;
    public static boolean isLockVX = false;
    public static boolean isTrade = false;
    public static boolean isKmb = true;
    public static boolean isServerTest;
    public static boolean BuffAdmin = true;
    public static boolean BuffAdminMaterial = true;
    public static int timeRemoveClient = 1000 * 60;
    public static boolean logErrorLogin = false;
    public static byte thue = 5;
    public static String nameClanThue;
    public static Clan ClanThue;
    public int vua_chien_truong = -1;
    public long time_x2_server = 0;
    public short mo_ly = 0;
    public static final List<String> PlayersWinCThanh = new ArrayList<>();
    public static HashMap<Integer, Integer> gold_offline = new HashMap<>();
    public HashMap<String, Integer> thanh_tich = new HashMap<>();
    public List<String> ty_phu = new ArrayList<>();
    public List<String> trieu_phu = new ArrayList<>();
    public List<String> dai_gia = new ArrayList<>();
    public final byte[][] data_part_char_x1;
    public final byte[][] data_part_char_x2;
    public final byte[][] data_part_char_x3;
    public final byte[][] data_part_char_x4;
    public static HashMap<Byte, List<Short>> item_sell;
    public static int hour, minute, second, millisecond;

    public static void setClanThue() {
        if (nameClanThue == null || nameClanThue.isEmpty()) {
            ResetCThanh();
            return;
        }
        for (Clan c : Clan.entries) {
            if (c.name_clan.equals(nameClanThue)) {
                ClanThue = c;
                return;
            }
        }
        if (ClanThue == null) {
            ResetCThanh();
        }
    }

    public static void ResetCThanh() {
        PlayersWinCThanh.clear();
        nameClanThue = null;
        thue = 5;
        ClanThue = null;
    }

    private byte[] load_msg26() throws IOException {
        ByteArrayInputStream bais = new java.io.ByteArrayInputStream(Util.loadfile("data/msg/msg_26"));
        DataInputStream dis = new java.io.DataInputStream(bais);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DataOutputStream ou = new DataOutputStream(os);
        short size = dis.readShort();
        ou.writeShort(size);
        for (int i = 0; i < size; i++) {
            short id = dis.readShort();
            String name = dis.readUTF();
            byte lv = dis.readByte();
            int maxhp = dis.readInt();
            byte b = dis.readByte();
            if (id == 151 || id == 152) {
                b = 17;
            }
            ou.writeShort(id);
            ou.writeUTF(name);
            ou.writeByte(lv);
            ou.writeInt(maxhp);
            ou.writeByte(b);
        }
        ou.write(bais.readAllBytes());
        return os.toByteArray();
    }

    public Manager() throws IOException {
        this.msg_25_new = Util.loadfile("data/msg/msg_25_new");
        this.msg_29_chienbinh = Util.loadfile("data/msg/msg_29_chienbinh");
        this.msg_29_satthu = Util.loadfile("data/msg/msg_29_satthu");
        this.msg_29_phapsu = Util.loadfile("data/msg/msg_29_phapsu");
        this.msg_29_xathu = Util.loadfile("data/msg/msg_29_xathu");
        this.msg_1 = Util.loadfile("data/msg/msg_1");
        this.msg_61 = Util.loadfile("data/msg/msg_61");
        this.msg_26 = load_msg26();
        this.msg_eff_70 = Util.loadfile("data/msg_eff/70");
        this.msg_eff_71 = Util.loadfile("data/msg_eff/71");
        this.msg_eff_109 = Util.loadfile("data/msg_eff/109");
        this.msg_eff_105 = Util.loadfile("data/msg_eff/105");

        data_part_char_x1 = new byte[1028][];
        data_part_char_x2 = new byte[1028][];
        data_part_char_x3 = new byte[1028][];
        data_part_char_x4 = new byte[1028][];
        for (int i = 0; i < 1028; i++) {
            data_part_char_x1[i] = Util.loadfile("data/part_char/x1/" + i);
            data_part_char_x2[i] = Util.loadfile("data/part_char/x2/" + i);
            data_part_char_x3[i] = Util.loadfile("data/part_char/x3/" + i);
            data_part_char_x4[i] = Util.loadfile("data/part_char/x4/" + i);
        }
    }

    public static Manager gI() {
        if (instance == null) {
            try {
                instance = new Manager();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("create cache fail!");
                System.exit(0);
            }
        }
        return instance;
    }

    public void init() {
        try {
            load_config();
            LocalMySqlBootstrap.ensureAvailable(
                    this.mysql_host,
                    this.mysql_port,
                    this.mysql_auto_start || LocalMySqlBootstrap.isDebuggerAttached(),
                    this.mysql_start_bin,
                    this.mysql_start_ini,
                    this.mysql_start_timeout_ms
            );
            this.lucky_draw_vip = new LuckyDraw(LuckyDraw.VIP);
            this.lucky_draw_normal = new LuckyDraw(LuckyDraw.NORMAL);
            if (!load_database()) {                               
                System.err.println("load database err");
                System.exit(0);
                return;
            }
            chiem_mo = new ChiemMo();
            chiem_mo.init();
            if (!chiem_mo.LoadData()) {
                System.err.println("load database err");
                System.exit(0);
                return;
            }
            System.out.println("cache loaded!");
            ChienTruong.get_king_battlefield();
            Lottery.setItem();
            Log.gI().start_log();
            for (Map[] temp : Map.entrys) {
                for (Map temp2 : temp) {
                    temp2.start_map();
                }
            }
            if (!isServerTest) {
                try {
                    TelegramBotsApi tele = new TelegramBotsApi(DefaultBotSession.class);
                    notifierBot = new BotManager();
                    tele.registerBot(notifierBot);
                    notifierBot.runBot();
                    System.err.println("CREATED BOT");
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.err.println("load database err");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("load database err");
            System.exit(0);
        }
    }

    private boolean load_database() {
        try {
            // load item3
            Connection conn = SQL.gI().getConnection();
            Statement ps = conn.createStatement();
            ResultSet rs;
            String query = "SELECT * FROM `item3`;";
            rs = ps.executeQuery(query);
            while (rs.next()) {
                ItemTemplate3 temp = new ItemTemplate3();
                temp.setId(rs.getShort("id"));
                temp.setName(rs.getString("name"));
                temp.setType(rs.getByte("type"));
                temp.setPart(rs.getByte("part"));
                temp.setClazz(rs.getByte("clazz"));
                temp.setIcon(rs.getShort("iconid"));
                temp.setLevel(rs.getShort("level"));
                temp.setColor(rs.getByte("color"));
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("data"));
                if (jsar == null) {
                    return false;
                }
                List<Option> buffer = new ArrayList<>();
                for (int i = 0; i < jsar.size(); i++) {
                    JSONArray jsar2 = (JSONArray) JSONValue.parse(jsar.get(i).toString());
                    if (jsar2 == null) {
                        return false;
                    }
                    buffer.add(new Option(Byte.parseByte(jsar2.get(0).toString()), Integer.parseInt(jsar2.get(1).toString()), (short) 0));
                }
                temp.setOp(buffer);
                // set type leave
                if (temp.getType() < 12 && temp.getType() != 7) {
                    // load item leave
                    switch (temp.getLevel()) {
                        case 1: {
                            LeaveItemMap.item0x.add(temp.getId());
                            break;
                        }
                        case 10: {
                            LeaveItemMap.item1x.add(temp.getId());
                            break;
                        }
                        case 20: {
                            LeaveItemMap.item2x.add(temp.getId());
                            break;
                        }
                        case 30: {
                            LeaveItemMap.item3x.add(temp.getId());
                            break;
                        }
                        case 40: {
                            LeaveItemMap.item4x.add(temp.getId());
                            break;
                        }
                        case 50: {
                            LeaveItemMap.item5x.add(temp.getId());
                            break;
                        }
                        case 60: {
                            LeaveItemMap.item6x.add(temp.getId());
                            break;
                        }
                        case 70: {
                            LeaveItemMap.item7x.add(temp.getId());
                            break;
                        }
                        case 80: {
                            LeaveItemMap.item8x.add(temp.getId());
                            break;
                        }
                        case 90: {
                            LeaveItemMap.item9x.add(temp.getId());
                            break;
                        }
                        case 100: {
                            LeaveItemMap.item10x.add(temp.getId());
                            break;
                        }
                        case 110: {
                            LeaveItemMap.item11x.add(temp.getId());
                            break;
                        }
                        case 120: {
                            LeaveItemMap.item12x.add(temp.getId());
                            break;
                        }
                        case 130: {
                            LeaveItemMap.item13x.add(temp.getId());
                            break;
                        }
                    }
                }
                if (temp.getColor() == 2) {
                    temp.setLevel((short) (temp.getLevel() + 2));
                } else if (temp.getColor() == 3) {
                    temp.setLevel((short) (temp.getLevel() + 4));
                } else if (temp.getColor() == 4) {
                    temp.setLevel((short) (temp.getLevel() + 5));
                }
                ItemTemplate3.item.add(temp);
            }
            rs.close();

            // load item4
            query = "SELECT * FROM `item4`;";
            rs = ps.executeQuery(query);
            while (rs.next()) {
                ItemTemplate4 temp = new ItemTemplate4();
                temp.setId(rs.getShort("id"));
                temp.setIcon(rs.getShort("icon"));
                if (temp.getId() >= 113 && temp.getId() <= 116) {
                    temp.setPrice(50);
                } else {
                    temp.setPrice(rs.getLong("price"));
                }

                temp.setName(rs.getString("name"));
                temp.setContent(rs.getString("content"));
                temp.setType(rs.getByte("typepotion"));
                temp.setPricetype(rs.getByte("moneytype"));
                temp.setSell(rs.getByte("sell"));
                temp.setValue(rs.getShort("value"));
                temp.setTrade(rs.getByte("canTrade"));
                ItemTemplate4.item.add(temp);
            }
            rs.close();

            // load item7
            query = "SELECT * FROM `item7`;";
            rs = ps.executeQuery(query);
            while (rs.next()) {
                ItemTemplate7 temp = new ItemTemplate7();
                temp.setId(rs.getShort("id"));
                temp.setIcon(rs.getShort("imgid"));
                temp.setPrice(rs.getLong("price"));
                temp.setName(rs.getString("name"));
                temp.setContent(rs.getString("content"));
                temp.setType(rs.getByte("type"));
                temp.setPricetype(rs.getByte("pricetype"));
                temp.setSell(rs.getByte("sell"));
                temp.setValue(rs.getShort("value"));
                temp.setTrade(rs.getByte("trade"));
                temp.setColor(rs.getByte("setcolorname"));
                ItemTemplate7.item.add(temp);
            }
            // load item medal
            for (int i = 0; i < 10; i++) {
                MaterialMedal.m_blue[i] = (short) (i + 116);
                MaterialMedal.m_yellow[i] = (short) (i + 126);
                MaterialMedal.m_violet[i] = (short) (i + 136);
            }
            MaterialMedal.m_white = new short[7][];
            int dem = 46;
            for (int i = 0; i < 7; i++) {
                MaterialMedal.m_white[i] = new short[10];
                for (int j = 0; j < 10; j++) {
                    MaterialMedal.m_white[i][j] = (short) (dem++);
                }
            }
            //
            rs.close();
            // load item option
            query = "SELECT * FROM `itemoption`;";
            rs = ps.executeQuery(query);
            while (rs.next()) {
                OptionItem temp = new OptionItem();
                temp.setName(rs.getString("name"));
                temp.setColor(rs.getByte("colorInfoItem"));
                temp.setIspercent(rs.getByte("isPercentInfoItem"));
                OptionItem.entry.add(temp);
            }
            rs.close();
            // load item sell
            query = "SELECT * FROM `itemsell`;";
            rs = ps.executeQuery(query);
            itemSellTB = new ArrayList<>();
            item_sell = new HashMap<>();
            item_sell.put(Service.SHOP_POTION, new ArrayList<>());
            item_sell.put(Service.SHOP_ITEM, new ArrayList<>());
            item_sell.put(Service.SHOP_MATERIAL, new ArrayList<>());
            while (rs.next()) {
                byte type = rs.getByte("id");
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("data"));
                if (jsar == null) {
                    return false;
                }
                switch (type) {
                    case 0: {
                        switch (this.event) {
                            case 1: {
                                itemPoitionSell = new short[jsar.size() + 4];
                                for (int i = 0; i < jsar.size(); i++) {
                                    itemPoitionSell[i] = Short.parseShort(jsar.get(i).toString());
                                    item_sell.get(Service.SHOP_POTION).add(itemPoitionSell[i]);
                                }
                                itemPoitionSell[itemPoitionSell.length - 4] = 113;
                                itemPoitionSell[itemPoitionSell.length - 3] = 114;
                                itemPoitionSell[itemPoitionSell.length - 2] = 115;
                                itemPoitionSell[itemPoitionSell.length - 1] = 116;
                                item_sell.get(Service.SHOP_POTION).add((short) 113);
                                item_sell.get(Service.SHOP_POTION).add((short) 114);
                                item_sell.get(Service.SHOP_POTION).add((short) 115);
                                item_sell.get(Service.SHOP_POTION).add((short) 116);
                                break;
                            }
                            case 2: {
                                itemPoitionSell = new short[jsar.size() + 3];
                                for (int i = 0; i < jsar.size(); i++) {
                                    itemPoitionSell[i] = Short.parseShort(jsar.get(i).toString());
                                    item_sell.get(Service.SHOP_POTION).add(itemPoitionSell[i]);
                                }
                                itemPoitionSell[itemPoitionSell.length - 3] = 253;
                                itemPoitionSell[itemPoitionSell.length - 2] = 252;
                                itemPoitionSell[itemPoitionSell.length - 1] = 141;
                                item_sell.get(Service.SHOP_POTION).add((short) 252);
                                item_sell.get(Service.SHOP_POTION).add((short) 253);
                                item_sell.get(Service.SHOP_POTION).add((short) 141);
                                break;
                            }
                            case 3: {
                                itemPoitionSell = new short[jsar.size() + 1];
                                for (int i = 0; i < jsar.size(); i++) {
                                    itemPoitionSell[i] = Short.parseShort(jsar.get(i).toString());
                                    item_sell.get(Service.SHOP_POTION).add(itemPoitionSell[i]);
                                }
                                itemPoitionSell[itemPoitionSell.length - 1] = 303;
                                item_sell.get(Service.SHOP_POTION).add((short) 303);
                                break;
                            }
                            default: {
                                itemPoitionSell = new short[jsar.size()];
                                for (int i = 0; i < itemPoitionSell.length; i++) {
                                    itemPoitionSell[i] = Short.parseShort(jsar.get(i).toString());
                                    item_sell.get(Service.SHOP_POTION).add(itemPoitionSell[i]);
                                }
                                break;
                            }
                        }
                        break;
                    }
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16: {
                        int size = jsar.size();
                        if (Manager.gI().event == 2 && type <= 4) {
                            size += 6;
                        } else if (Manager.gI().event == 2 && type >= 5 && type <= 8) {
                            size += 4;
                        }
                        ItemSell3[] itemsell3 = new ItemSell3[size];
                        for (int i = 0; i < jsar.size(); i++) {
                            itemsell3[i] = new ItemSell3();
                            JSONArray jsar2 = (JSONArray) JSONValue.parse(jsar.get(i).toString());
                            itemsell3[i].id = Short.parseShort(jsar2.get(0).toString());
                            item_sell.get(Service.SHOP_ITEM).add(itemsell3[i].id);
                            itemsell3[i].clazz = Byte.parseByte(jsar2.get(1).toString());
                            itemsell3[i].type = Byte.parseByte(jsar2.get(2).toString());
                            itemsell3[i].price = Long.parseLong(jsar2.get(3).toString());
                            itemsell3[i].level = Short.parseShort(jsar2.get(4).toString());
                            itemsell3[i].color = Byte.parseByte(jsar2.get(5).toString());
                            itemsell3[i].option = new ArrayList<>();
                            JSONArray jsar3 = (JSONArray) JSONValue.parse(jsar2.get(6).toString());
                            for (Object o : jsar3) {
                                JSONArray jsar4 = (JSONArray) JSONValue.parse(o.toString());
                                itemsell3[i].option.add(new Option(Byte.parseByte(jsar4.get(0).toString()),
                                        Integer.parseInt(jsar4.get(1).toString()), itemsell3[i].id));
                            }
                            itemsell3[i].pricetype = Byte.parseByte(jsar2.get(7).toString());
                        }
                        if (Manager.gI().event == 2 && type >= 1 && type <= 4) {
                            short[] id_ = new short[]{4714, 4715, 4769, 4770, 4771, 4772};
                            for (int i = 0; i < id_.length; i++) {
                                ItemTemplate3 temp = ItemTemplate3.item.get(id_[i]);
                                itemsell3[i + jsar.size()] = new ItemSell3();
                                itemsell3[i + jsar.size()].id = id_[i];
                                item_sell.get(Service.SHOP_ITEM).add(itemsell3[i + jsar.size()].id);
                                itemsell3[i + jsar.size()].clazz = temp.getClazz();
                                itemsell3[i + jsar.size()].type = temp.getType();
                                itemsell3[i + jsar.size()].price = 50;
                                itemsell3[i + jsar.size()].level = temp.getLevel();
                                itemsell3[i + jsar.size()].color = temp.getColor();
                                itemsell3[i + jsar.size()].option = new ArrayList<>();
                                itemsell3[i + jsar.size()].option.addAll(temp.getOp());
                                itemsell3[i + jsar.size()].pricetype = 1;
                            }
                        } else if (Manager.gI().event == 2 && type >= 5 && type <= 8) {
                            short[] id_ = new short[]{4716, 4717, 4718, 4719};
                            for (int i = 0; i < id_.length; i++) {
                                ItemTemplate3 temp = ItemTemplate3.item.get(id_[i]);
                                itemsell3[i + jsar.size()] = new ItemSell3();
                                itemsell3[i + jsar.size()].id = id_[i];
                                item_sell.get(Service.SHOP_ITEM).add(itemsell3[i + jsar.size()].id);
                                itemsell3[i + jsar.size()].clazz = temp.getClazz();
                                itemsell3[i + jsar.size()].type = temp.getType();
                                itemsell3[i + jsar.size()].price = 50;
                                itemsell3[i + jsar.size()].level = temp.getLevel();
                                itemsell3[i + jsar.size()].color = temp.getColor();
                                itemsell3[i + jsar.size()].option = new ArrayList<>();
                                itemsell3[i + jsar.size()].option.addAll(temp.getOp());
                                itemsell3[i + jsar.size()].pricetype = 1;
                            }
                        }
                        itemSellTB.add(itemsell3);
                        break;
                    }
                    case 17: {
                        item7sell = new short[jsar.size()];
                        for (int i = 0; i < item7sell.length; i++) {
                            item7sell[i] = Short.parseShort(jsar.get(i).toString());
                            item_sell.get(Service.SHOP_MATERIAL).add(item7sell[i]);
                        }
                        for (short i = 246; i < 346; i++) {
                            item_sell.get(Service.SHOP_MATERIAL).add(i);
                        }
                        break;
                    }
                }
            }
            rs.close();
            //
            System.out.println("item loaded!");
            // load mob temp
            query = "SELECT * FROM `mobs`;";
            rs = ps.executeQuery(query);
            while (rs.next()) {
                Mob temp = new Mob();
                temp.mob_id = Short.parseShort(rs.getString("id"));
                temp.name = rs.getString("name");
                temp.level = Short.parseShort(rs.getString("level"));
                temp.hpmax = Integer.parseInt(rs.getString("hp"));
                temp.typemove = Byte.parseByte(rs.getString("typemove"));
                temp.is_boss = rs.getBoolean("is_boss");
                Mob.entry.add(temp);
            }
            rs.close();
            query = "SELECT * FROM `npc`;";
            rs = ps.executeQuery(query);
            while (rs.next()) {
                NpcTemplate temp = new NpcTemplate();
                temp.id = Byte.parseByte(rs.getString("IDItem"));
                temp.name = rs.getString("name");
                temp.name_gt = rs.getString("namegt");
                temp.infoObject = rs.getString("infoObject");
                temp.ID_Image = Byte.parseByte(rs.getString("IDImage"));
                temp.wBlock = Byte.parseByte(rs.getString("wBlock"));
                temp.hBlock = Byte.parseByte(rs.getString("hBlock"));
                temp.nFrame = Byte.parseByte(rs.getString("nFrame"));
                temp.IdBigAvatar = Byte.parseByte(rs.getString("IdBigAvatar"));
                temp.isPerson = Byte.parseByte(rs.getString("isPerson"));
                temp.isShowHP = Byte.parseByte(rs.getString("isShowHP"));
                NpcTemplate.npcTemplates.add(temp);
            }
            rs.close();
            // load map
            query = "SELECT * FROM `maps`;";
            rs = ps.executeQuery(query);
            int index_mob = 0;
            while (rs.next()) {
                byte maxzone = rs.getByte("maxzone");
                Map[] temp_all_zone = new Map[maxzone + 1];
                short map_id = rs.getShort("id");
                String name = rs.getString("name");
                int request_level = rs.getInt("request_level");
                //
                List<Vgo> vgo_temp = new ArrayList<>();
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("vgos"));
                if (jsar == null) {
                    return false;
                }
                for (int i = 0; i < jsar.size(); i++) {
                    JSONArray jsar2 = (JSONArray) JSONValue.parse(jsar.get(i).toString());
                    Vgo vgo = new Vgo();
                    vgo.id_map_go = Byte.parseByte(jsar2.get(0).toString());
                    vgo.x_old = Short.parseShort(jsar2.get(1).toString());
                    vgo.y_old = Short.parseShort(jsar2.get(2).toString());
                    vgo.name_map_go = jsar2.get(3).toString();
                    vgo.x_new = Short.parseShort(jsar2.get(4).toString());
                    vgo.y_new = Short.parseShort(jsar2.get(5).toString());
                    vgo_temp.add(vgo);
                }
                jsar.clear();
                //
                jsar = (JSONArray) JSONValue.parse(rs.getString("npc_0"));
                if (jsar == null) {
                    return false;
                }
                NpcMap[] npc_0 = new NpcMap[jsar.size()];
                for (int i = 0; i < jsar.size(); i++) {
                    JSONArray jsar2 = (JSONArray) JSONValue.parse(jsar.get(i).toString());
                    NpcMap npc = new NpcMap();
                    byte id = Byte.parseByte(jsar2.get(0).toString());
                    npc.npcTemplate = NpcTemplate.getNpcById(id);
                    if (npc.npcTemplate == null) {
                        continue;
                    }
                    npc.x = Short.parseShort(jsar2.get(1).toString());
                    npc.y = Short.parseShort(jsar2.get(2).toString());
                    npc_0[i] = npc;
                }
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("npc_1"));
                if (jsar == null) {
                    return false;
                }
                NpcMap[] npc_1 = new NpcMap[jsar.size()];
                for (int i = 0; i < jsar.size(); i++) {
                    JSONArray jsar2 = (JSONArray) JSONValue.parse(jsar.get(i).toString());
                    NpcMap npc = new NpcMap();
                    byte id = Byte.parseByte(jsar2.get(0).toString());
                    npc.npcTemplate = NpcTemplate.getNpcById(id);
                    if (npc.npcTemplate == null) {
                        continue;
                    }
                    npc.x = Short.parseShort(jsar2.get(1).toString());
                    npc.y = Short.parseShort(jsar2.get(2).toString());
                    npc_1[i] = npc;
                }
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("npc_2"));
                if (jsar == null) {
                    return false;
                }
                NpcMap[] npc_2 = new NpcMap[jsar.size()];
                for (int i = 0; i < jsar.size(); i++) {
                    JSONArray jsar2 = (JSONArray) JSONValue.parse(jsar.get(i).toString());
                    NpcMap npc = new NpcMap();
                    byte id = Byte.parseByte(jsar2.get(0).toString());
                    npc.npcTemplate = NpcTemplate.getNpcById(id);
                    if (npc.npcTemplate == null) {
                        continue;
                    }
                    npc.x = Short.parseShort(jsar2.get(1).toString());
                    npc.y = Short.parseShort(jsar2.get(2).toString());
                    npc_2[i] = npc;
                }
                jsar.clear();
               
                //
                jsar = (JSONArray) JSONValue.parse(rs.getString("mobs"));
                if (jsar == null) {
                    return false;
                }
                List<Mob_in_map> mob_in_map = new ArrayList<>();
                for (int i = 0; i < jsar.size(); i++) {
                    JSONArray jsar2 = (JSONArray) JSONValue.parse(jsar.get(i).toString());
                    Mob_in_map mob = new Mob_in_map();
                    short id = Short.parseShort(jsar2.get(0).toString());
                    mob.template = Mob.entry.get(id);
                    mob.name = mob.template.name;
                    mob.x = Short.parseShort(jsar2.get(1).toString());
                    mob.y = Short.parseShort(jsar2.get(2).toString());
                    mob.level = mob.template.level;
                    mob.map_id = map_id;
                    mob.isdie = false;
                    mob.time_back = System.currentTimeMillis() + 4_000L;
                    mob.color_name = 0;
                    mob.Set_hpMax(Mob.entry.get(id).hpmax);
                    mob.hp = mob.get_HpMax();
                    mob_in_map.add(mob);
                }
                jsar.clear();
                //
                byte typemap = rs.getByte("type");
                byte maxplayer = rs.getByte("maxplayer");
                boolean ismaplang = rs.getByte("ismaplang") == 1;
                boolean showhs = rs.getByte("showhs") == 1;
                for (int i = 0; i < maxzone + 1; i++) {
                    Map m = null;
                    try {
                        m = new Map(map_id, i, name, typemap, ismaplang, showhs, maxplayer, maxzone, vgo_temp, request_level);
                        m.npc_0 = npc_0;
                        m.npc_1 = npc_1;
                        m.npc_2 = npc_2;
                    } catch (IOException e) {
                        System.err.println("load data map err " + map_id);
                        System.exit(0);
                    }
                    //
                    // Zone 5 chỉ loại quái nếu là map đi buôn thực sự
                    boolean isMapBuon = (map_id == 8 || map_id == 7 || (map_id >= 15 && map_id <= 18)
                            || (map_id >= 20 && map_id <= 25) || map_id == 33 || map_id == 34
                            || (map_id >= 37 && map_id <= 39) || (map_id >= 42 && map_id <= 45) || map_id == 52);
                    if (i != 5 || !isMapBuon) {
                        m.mobs = new Mob_in_map[mob_in_map.size()];
                        int idxmob = 1;
                        for (int i1 = 0; i1 < mob_in_map.size(); i1++) {
                            Mob_in_map mob = new Mob_in_map();
                            mob.ID = idxmob++;
                            index_mob++;
                            mob.template = mob_in_map.get(i1).template;
                            mob.name = mob_in_map.get(i1).name;
                            mob.x = mob_in_map.get(i1).x;
                            mob.y = mob_in_map.get(i1).y;
                            mob.Set_hpMax(mob_in_map.get(i1).hp);
                            mob.hp = mob.get_HpMax();
                            mob.level = mob_in_map.get(i1).level;
                            mob.map_id = map_id;
                            mob.zone_id = (byte) i;
                            mob.isdie = mob_in_map.get(i1).isdie;
                            mob.color_name = mob_in_map.get(i1).color_name;
                            mob.is_boss = mob.template.is_boss;
                            mob.time_back = mob_in_map.get(i1).time_back;
                            mob.is_boss_active = false;
                            mob.map = m;
                            mob.MainEff = new ArrayList<>();
                            m.mobs[i1] = mob;
                            if (mob.is_boss) {
                                mob.isdie = true;
                                if (m.map_id == 61) {
                                    mob.level = 45;
                                } else {
                                    m.bossInMaps.add(mob);
                                }
                            }
                        }
                    } else {
                        m.mobs = new Mob_in_map[0];
                    }
                    //
                    temp_all_zone[i] = m;
                }
                Map.entrys.add(temp_all_zone);
            }
            rs.close();
            this.size_mob = index_mob;
            //
            System.out.println("map loaded, mob size " + (this.size_mob));
            // load level
            query = "SELECT * FROM `level`;";
            rs = ps.executeQuery(query);
            while (rs.next()) {
                Level temp2 = new Level();
                temp2.level = rs.getShort("level");
                temp2.exp = rs.getLong("exp");
                temp2.tiemnang = rs.getShort("tiemnang");
                temp2.kynang = rs.getShort("kynang");
                Level.entry.add(temp2);
            }
            if (lvmax > Level.entry.size()) {
                for (int i = Level.entry.size() + 1; i < lvmax + 2; i++) {
                    Level temp2 = new Level();
                    temp2.level = (short) i;
                    temp2.exp = Level.entry.get(i - 2).exp;
                    temp2.tiemnang = 5;
                    temp2.kynang = 1;
                    Level.entry.add(temp2);
                }
            }
            rs.close();
            // load part fashion temp
            query = "SELECT * FROM `fashiontemplate`;";
            rs = ps.executeQuery(query);
            while (rs.next()) {
                Part_fashion temp = new Part_fashion();
                temp.id = (short) rs.getInt("id");
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("part"));
                if (jsar == null) {
                    return false;
                }
                temp.part = new short[jsar.size()];
                for (int i = 0; i < temp.part.length; i++) {
                    temp.part[i] = Short.parseShort(jsar.get(i).toString());
                }
                Part_fashion.fashions.add(temp.id);
                Part_fashion.entry.add(temp);
            }
            System.out.println("part_fashion loaded!");
            rs.close();
            // load clan
            query = "SELECT * FROM `clan`;";
            rs = ps.executeQuery(query);
            List<Clan> clan_list = new ArrayList<>();
            while (rs.next()) {
                Clan temp = new Clan();
                temp.ID = rs.getInt("id");
                temp.name_clan = rs.getString("name");
                temp.name_clan_shorted = rs.getString("name_short");
                temp.icon = rs.getShort("icon");
                temp.level = rs.getShort("level");
                temp.exp = rs.getLong("exp");
                temp.slogan = rs.getString("slogan");
                temp.rule = rs.getString("rule");
                temp.notice = rs.getString("notice");
                temp.setVang(rs.getLong("vang"));
                temp.setKimcuong(rs.getInt("kimcuong"));
                temp.max_mem = rs.getShort("max_mem");
                temp.max_mem = Clan.get_mem_by_level(temp.level);
                //
                temp.item_clan = new ArrayList<>();
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("item"));
                if (jsar == null) {
                    return false;
                }
                for (int i = 0; i < jsar.size(); i++) {
                    JSONArray js2 = (JSONArray) JSONValue.parse(jsar.get(i).toString());
                    Item47 item = new Item47();
                    item.id = Short.parseShort(js2.get(0).toString());
                    item.quantity = Short.parseShort(js2.get(1).toString());
                    item.expiry = Long.parseLong(js2.get(2).toString());
                    if (item.expiry < System.currentTimeMillis() && item.isWingClan()) {
                        continue;
                    }
                    temp.item_clan.add(item);
                }
                //
                jsar.clear();

                temp.time_items = new ArrayList<>();
                jsar = (JSONArray) JSONValue.parse(rs.getString("time_item"));
                if (jsar == null) {
                    return false;
                }
                for (Object object : jsar) {
                    JSONArray js2 = (JSONArray) JSONValue.parse(object.toString());
                    EffTemplate effTemplate = new EffTemplate(Integer.parseInt(js2.get(0).toString()), 0,
                            Long.parseLong(js2.get(1).toString()));
                    temp.time_items.add(effTemplate);
                }
                //
                jsar.clear();

                jsar = (JSONArray) JSONValue.parse(rs.getString("mems"));
                if (jsar == null) {
                    return false;
                }
                temp.mems = new ArrayList<>();
                for (Object o : jsar) {
                    JSONArray jsar2 = (JSONArray) JSONValue.parse(o.toString());
                    MemberClan mem = new MemberClan();
                    mem.name = jsar2.get(0).toString();
                    mem.mem_type = Byte.parseByte(jsar2.get(1).toString());
                    mem.kimcuong = Integer.parseInt(jsar2.get(2).toString());
                    mem.vang = Long.parseLong(jsar2.get(3).toString());
                    mem.head = Byte.parseByte(jsar2.get(4).toString());
                    mem.eye = Byte.parseByte(jsar2.get(5).toString());
                    mem.hair = Byte.parseByte(jsar2.get(6).toString());
                    mem.level = Short.parseShort(jsar2.get(7).toString());
                    mem.itemwear = new ArrayList<>();
                    JSONArray jsar3 = (JSONArray) JSONValue.parse(jsar2.get(8).toString());
                    for (int j = 0; j < jsar3.size(); j++) {
                        JSONArray jsar4 = (JSONArray) JSONValue.parse(jsar3.get(j).toString());
                        Part_player part = new Part_player();
                        part.part = Byte.parseByte(jsar4.get(0).toString());
                        part.type = Byte.parseByte(jsar4.get(1).toString());
                        mem.itemwear.add(part);
                    }
                    temp.mems.add(mem);
                }
                temp.sortMember();
                temp.mo_tai_nguyen = new ArrayList<>();
                clan_list.add(temp);
            }
            Clan.set_clan(clan_list);
            System.out.println("clan loaded!");
            rs.close();
            // load event
            if (this.event == 0) {
                query = "SELECT * FROM `event` WHERE `id` = 0;";
                rs = ps.executeQuery(query);
                while (rs.next()) {
                    JSONObject jsob = (JSONObject) JSONValue.parse(rs.getString("data"));
                    if (jsob != null) {
                        EventManager.loadDatabase(jsob);
                    }
                }
            }
            if (this.event == 1) {
                query = "SELECT * FROM `event` WHERE `id` = 1;";
                rs = ps.executeQuery(query);
                long t_ = System.currentTimeMillis();
                while (rs.next()) {
                    JSONObject jsob = (JSONObject) JSONValue.parse(rs.getString("data"));
                    Noel.LoadDB(jsob);
                }
            } else if (this.event == 2) {
                query = "SELECT * FROM `event` WHERE `id` = 2;";
                rs = ps.executeQuery(query);
                long t_ = System.currentTimeMillis();
                while (rs.next()) {
                    JSONObject jsob = (JSONObject) JSONValue.parse(rs.getString("data"));
                    Event_2.LoadDB(jsob);
                }
            } else if (this.event == 3) {
                query = "SELECT * FROM `event` WHERE `id` = 3;";
                rs = ps.executeQuery(query);
                long t_ = System.currentTimeMillis();
                while (rs.next()) {
                    JSONObject jsob = (JSONObject) JSONValue.parse(rs.getString("data"));
                    Event_3.LoadDB(jsob);
                }
            }
            query = "SELECT * FROM `config_server`;";
            rs = ps.executeQuery(query);
            while (rs.next()) {
                this.vua_chien_truong = rs.getInt("king_battlefield");
                ClanThue = Clan.get_clan_by_id(rs.getInt("chiem_thanh"));
                thue = rs.getByte("thue");
                time_x2_server = rs.getLong("time_x2_server");
                mo_ly = rs.getShort("mo_ly");
                try {
                    String json = rs.getString("gold_offline");
                    if (json == null) {
                        continue;
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    gold_offline = objectMapper.readValue(json, new TypeReference<>() {
                    });
                    json = rs.getString("thanh_tich");
                    if (json == null) {
                        continue;
                    }
                    objectMapper = new ObjectMapper();
                    thanh_tich = objectMapper.readValue(json, new TypeReference<>() {
                    });
                    Admin.setThanhTich();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            rs.close();

            query = "SELECT * FROM `time_boss`;";
            rs = ps.executeQuery(query);
            while (rs.next()) {
                BossServer bossServer = new BossServer();
                bossServer.idMap = rs.getInt("map_id");
                bossServer.bossName = rs.getString("boss_name");
                bossServer.time_refresh_z1 = rs.getLong("zone_1");
                bossServer.time_refresh_z2 = rs.getLong("zone_2");
                bossServer.time_refresh_z3 = rs.getLong("zone_3");
                bossServer.time_refresh_z4 = rs.getLong("zone_4");
                bossServer.time_refresh_z5 = rs.getLong("zone_5");
                if (bossServer.idMap != 26 || event != -1) {
                    this.listBossServers.add(bossServer);
                }
            }
            rs.close();
            // load loi dai data
            query = "SELECT * FROM `king_cup`;";
            rs = ps.executeQuery(query);
            while (rs.next()) {
                KingCupManager.TURN_KING_CUP = rs.getInt("turn_king_cup");
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("group_65_74"));
                for (int i = 0; i < jsar.size(); i++) {
                    KingCupManager.group_65_74.add(jsar.get(i).toString());
                }
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("group_75_84"));
                for (int i = 0; i < jsar.size(); i++) {
                    KingCupManager.group_75_84.add(jsar.get(i).toString());
                }
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("group_85_94"));
                for (int i = 0; i < jsar.size(); i++) {
                    KingCupManager.group_85_94.add(jsar.get(i).toString());
                }
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("group_95_104"));
                for (int i = 0; i < jsar.size(); i++) {
                    KingCupManager.group_95_104.add(jsar.get(i).toString());
                }
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("group_105_114"));
                for (int i = 0; i < jsar.size(); i++) {
                    KingCupManager.group_105_114.add(jsar.get(i).toString());
                }
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("group_115_124"));
                for (int i = 0; i < jsar.size(); i++) {
                    KingCupManager.group_115_124.add(jsar.get(i).toString());
                }
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("group_125_139"));
                for (int i = 0; i < jsar.size(); i++) {
                    KingCupManager.group_125_139.add(jsar.get(i).toString());
                }
                jsar.clear();
                KingCupManager.list_name.addAll(KingCupManager.group_65_74);
                KingCupManager.list_name.addAll(KingCupManager.group_75_84);
                KingCupManager.list_name.addAll(KingCupManager.group_85_94);
                KingCupManager.list_name.addAll(KingCupManager.group_95_104);
                KingCupManager.list_name.addAll(KingCupManager.group_105_114);
                KingCupManager.list_name.addAll(KingCupManager.group_115_124);
                KingCupManager.list_name.addAll(KingCupManager.group_125_139);
            }
            rs.close();

            // Nhiệm vụ
            query = "SELECT * FROM `task`;";
            rs = ps.executeQuery(query);
            while (rs.next()) {
                QuestTemplate temp = new QuestTemplate();
                temp.ID = Short.parseShort(rs.getString("id"));
                temp.name = rs.getString("name");
                temp.idNPC_From = Short.parseShort(rs.getString("idNpcFrom"));
                temp.idNPC_To = Integer.parseInt(rs.getString("idNpcTo"));
                temp.isMain = rs.getBoolean("isMainTask");
                temp.typeItem = rs.getByte("typeItem");
                temp.strDetailHelp = rs.getString("strDetailHelp");
                temp.strDetailTalk = rs.getString("strDetailTalk");
                temp.strDetailTalkFinish = rs.getString("strDetailTalkFinish");
                temp.strDetailHelpFinish = rs.getString("strDetailHelpFinish");
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("infoQuest"));
                temp.arrQuest = new short[jsar.size()][3];
                for (int i = 0; i < jsar.size(); i++) {
                    JSONArray innerArray = (JSONArray) jsar.get(i);
                    for (int j = 0; j < innerArray.size(); j++) {
                        temp.arrQuest[i][j] = ((Long) innerArray.get(j)).shortValue();
                    }
                }

                QuestTemplate.questMains.add(temp);
            }
            rs.close();

            ps.close();
            conn.close();
            ShopCustom.setShopGems();
            add_item_sell_other();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void load_config() throws IOException {
        final byte[] ab = Util.loadfile("hso.conf");
        if (ab == null) {
            System.out.println("Config file not found!");
            System.exit(0);
        }
        final String data = new String(ab);
        final HashMap<String, String> configMap = new HashMap<String, String>();
        final StringBuilder sbd = new StringBuilder();
        boolean bo = false;
        for (int i = 0; i <= data.length(); ++i) {
            final char es;
            if (i == data.length() || (es = data.charAt(i)) == '\n') {
                bo = false;
                final String sbf = sbd.toString().trim();
                if (sbf != null && !sbf.equals("") && sbf.charAt(0) != '#') {
                    final int j = sbf.indexOf(58);
                    if (j > 0) {
                        final String key = sbf.substring(0, j).trim();
                        final String value = sbf.substring(j + 1).trim();
                        configMap.put(key, value);
                        System.out.println("config: " + key + ": " + value);
                    }
                }
                sbd.setLength(0);
            } else {
                if (es == '#') {
                    bo = true;
                }
                if (!bo) {
                    sbd.append(es);
                }
            }
        }
        if (configMap.containsKey("timeRemoveClient")) {
            timeRemoveClient = Integer.parseInt(configMap.get("timeRemoveClient"));
        }
        if (configMap.containsKey("port")) {
            this.server_port = Integer.parseInt(configMap.get("port"));
        } else {
            this.server_port = 19129;
        }
        if (configMap.containsKey("server_admin")) {
            this.isServerAdmin = Boolean.parseBoolean(configMap.get("server_admin"));
        } else {
            this.isServerAdmin = false;
        }
        if (configMap.containsKey("debug")) {
            this.debug = Boolean.parseBoolean(configMap.get("debug"));
        } else {
            this.debug = false;
        }
        this.mysql_host = configMap.getOrDefault("mysql-host", "127.0.0.1");
        this.mysql_port = Integer.parseInt(configMap.getOrDefault("mysql-port", "3306"));
        this.mysql_user = configMap.getOrDefault("mysql-user", "root");
        String rawPass = configMap.getOrDefault("mysql-password", "12345678");
        this.mysql_pass = rawPass != null ? rawPass.trim() : "";
        this.mysql_database = configMap.getOrDefault("mysql-database", "hso");
        this.mysql_auto_start = Boolean.parseBoolean(configMap.getOrDefault("mysql-auto-start", "false"));
        this.mysql_start_bin = configMap.getOrDefault("mysql-start-bin", "C:\\AppServ\\MySQL\\bin\\mysqld.exe");
        this.mysql_start_ini = configMap.getOrDefault("mysql-start-ini", "C:\\AppServ\\MySQL\\my.ini");
        this.mysql_start_timeout_ms = Integer.parseInt(configMap.getOrDefault("mysql-start-timeout-ms", "20000"));

        if (configMap.containsKey("indexRes")) {
            this.indexRes = Byte.parseByte(configMap.get("indexRes"));
        } else {
            this.indexRes = 60;
        }
        if (configMap.containsKey("indexCharPar")) {
            this.indexCharPar = Short.parseShort(configMap.get("indexCharPar"));
        } else {
            this.indexCharPar = -1667;
        }
        if (configMap.containsKey("exp")) {
            this.exp = Integer.parseInt(configMap.get("exp"));
        } else {
            this.exp = 1;
        }
        if (configMap.containsKey("lvmax")) {
            this.lvmax = Short.parseShort(configMap.get("lvmax"));
        } else {
            this.lvmax = 150;
        }
        if (configMap.containsKey("allow_ip_client")) {
            this.allow_ip_client = Integer.parseInt(configMap.get("allow_ip_client"));
        } else {
            this.allow_ip_client = 3;
        }
        if (configMap.containsKey("login_limit_per_ip")) {
            this.login_limit_per_ip = Integer.parseInt(configMap.get("login_limit_per_ip"));
        } else {
            this.login_limit_per_ip = 3;
        }
        this.admin_tool_bind = configMap.getOrDefault("admin_tool_bind", "127.0.0.1");
        this.admin_tool_port = Integer.parseInt(configMap.getOrDefault("admin_tool_port", "19130"));
        this.admin_tool_token = configMap.getOrDefault("admin_tool_token", "admin123");
        this.antiCheatEnabled = Boolean.parseBoolean(configMap.getOrDefault("anticheat-enabled", "true"));
        this.unityOnly = Boolean.parseBoolean(configMap.getOrDefault("unity-only", "false"));
        this.unityProtocol = Integer.parseInt(configMap.getOrDefault("unity-protocol", "2"));
        this.maxInboundPacketSize = Math.max(1024,
                Math.min(65535, Integer.parseInt(configMap.getOrDefault("max-inbound-packet-size", "32768"))));
        this.packetRatePerSecond = Math.max(30,
                Integer.parseInt(configMap.getOrDefault("packet-rate-per-second", "120")));
        this.movementUnitsPerSecond = Math.max(100,
                Integer.parseInt(configMap.getOrDefault("movement-units-per-second", "600")));
        this.movementGraceUnits = Math.max(20,
                Integer.parseInt(configMap.getOrDefault("movement-grace-units", "80")));

        this.unityPackages.clear();
        for (String packageName : configMap.getOrDefault("unity-packages", "com.hso.kmt").split(",")) {
            if (!packageName.isBlank()) {
                this.unityPackages.add(packageName.trim());
            }
        }
        this.unityBuildSecrets.clear();
        String configuredBuilds = configMap.getOrDefault("unity-build-secrets", "");
        for (String entry : configuredBuilds.split(",")) {
            String[] pair = entry.trim().split("=", 2);
            if (pair.length == 2 && !pair[0].isBlank() && !pair[1].isBlank()) {
                this.unityBuildSecrets.put(pair[0].trim(), pair[1].trim());
            }
        }
        String environmentBuildId = System.getenv("HSO_UNITY_BUILD_ID");
        String environmentBuildSecret = System.getenv("HSO_UNITY_BUILD_SECRET");
        if (environmentBuildId != null && !environmentBuildId.isBlank()
                && environmentBuildSecret != null && !environmentBuildSecret.isBlank()) {
            this.unityBuildSecrets.put(environmentBuildId.trim(), environmentBuildSecret.trim());
        }
        if (this.unityOnly && this.unityBuildSecrets.isEmpty()) {
            System.err.println("UNITY-ONLY is enabled but no Unity build secret is configured.");
        }
        if (configMap.containsKey("time_login")) {
            this.time_login = Integer.parseInt(configMap.get("time_login"));
        } else {
            this.time_login = 10000;
        }
        if (configMap.containsKey("event")) {
            this.event = Integer.parseInt(configMap.get("event"));
        } else {
            this.event = -1;
        }
        if (configMap.containsKey("server_test")) {
            isServerTest = Boolean.parseBoolean(configMap.get("server_test"));
        }
    }

    public void chatKTGprocess(String s) throws IOException {
        Message m = new Message(53);
        m.writer().writeUTF(s);
        m.writer().writeByte(1);
        for (Map[] map : Map.entrys) {
            for (Map map0 : map) {
                for (int i = 0; i < map0.players.size(); i++) {
                    map0.players.get(i).conn.addmsg(m);
                }
            }
        }
        m.cleanup();
    }

    public void chatKTGWhite(String s) throws IOException {
        Message m = new Message(53);
        m.writer().writeUTF(s);
        m.writer().writeByte(0);
        for (Map[] map : Map.entrys) {
            for (Map map0 : map) {
                for (int i = 0; i < map0.players.size(); i++) {
                    map0.players.get(i).conn.addmsg(m);
                }
            }
        }
        m.cleanup();
    }

    public void close() {
        lucky_draw_vip.close();
        lucky_draw_normal.close();
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(SQL.gI().url, Manager.gI().mysql_user, Manager.gI().mysql_pass);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        save_config_database(conn);
        Log.gI().close_log();
        //
        for (int i = 0; i < Map.entrys.size(); i++) {
            for (int j = 0; j < Map.entrys.get(i).length; j++) {
                Map.entrys.get(i)[j].stop_map();
            }
        }
    }

    public static void save_config_database(Connection conn) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String gold_off = objectMapper.writeValueAsString(gold_offline);
            String thanh_tich = objectMapper.writeValueAsString(Manager.gI().thanh_tich);
            String sql = "UPDATE `config_server` SET `gold_offline` = ?, `time_x2_server` = ?, `mo_ly` = ?, `thanh_tich` = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, gold_off);
                ps.setLong(2, Manager.gI().time_x2_server);
                ps.setLong(3, Manager.gI().mo_ly);
                ps.setString(4, thanh_tich);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized int get_index_mob_new() {
        if (this.size_mob_now < -5000) {
            this.size_mob_now = -20;
        }
        return (--this.size_mob_now);
    }

    public static void sendMessageToBot(String message) {
        if (Manager.gI().notifierBot != null) {
            Manager.gI().notifierBot.sendNotification(message);
        }
    }

    public static void add_item_sell_other() {
        item_sell.get(Service.SHOP_POTION).add((short) 48);
        item_sell.get(Service.SHOP_POTION).add((short) 49);
        item_sell.get(Service.SHOP_POTION).add((short) 50);
        item_sell.get(Service.SHOP_POTION).add((short) 51);
        item_sell.get(Service.SHOP_POTION).add((short) 84);
        item_sell.get(Service.SHOP_POTION).add((short) 86);
        item_sell.get(Service.SHOP_POTION).add((short) 205);
        item_sell.get(Service.SHOP_POTION).add((short) 206);
        item_sell.get(Service.SHOP_POTION).add((short) 207);
        item_sell.get(Service.SHOP_POTION).add((short) 244);
        item_sell.get(Service.SHOP_POTION).add((short) 245);

        item_sell.get(Service.SHOP_ITEM).add((short) 2943);
        item_sell.get(Service.SHOP_ITEM).add((short) 2944);
        item_sell.get(Service.SHOP_ITEM).add((short) 3590);
        item_sell.get(Service.SHOP_ITEM).add((short) 3591);
        item_sell.get(Service.SHOP_ITEM).add((short) 3592);
        for (Integer key : ShopCustom.items_gems.keySet()) {
            item_sell.get(Service.SHOP_ITEM).add(key.shortValue());
        }
    }
}
