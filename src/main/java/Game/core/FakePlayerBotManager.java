package Game.core;

import Game.client.Friend;
import Game.client.Item;
import Game.client.Pet;
import Game.client.Player;
import Game.io.Message;
import Game.io.Session;
import Game.gamble.LuckyDraw;
import Game.map.Map;
import Game.map.MapService;
import Game.map.Mob_in_map;
import Game.map.NpcMap;
import Game.template.Item3;
import Game.template.Item47;
import Game.template.ItemTemplate3;
import Game.template.Option;
import Game.template.Part_fashion;
import Game.template.Player_store;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FakePlayerBotManager {
    private static final FakePlayerBotManager INSTANCE = new FakePlayerBotManager();
    private static final int FIRST_ID = 32000;
    private static final int MAX_BOTS = 5000;
    private static final int MAX_VISIBLE_PER_VIEW = 24;
    private static final int MAX_VISIBLE_FARM_PER_VIEW = 12;
    private static final int MAX_FARM_BOTS_PER_ZONE = 4;
    private static final int BOT_SHOP_PRICE_MIN = 10_000_000;
    private static final int BOT_SHOP_PRICE_MAX = 500_000_000;
    private static final long MOVE_PRESENCE_DELAY = 3000L;
    private static final long DEBUG_FLUSH_INTERVAL = 5000L;
    private static final int DEBUG_PRESENCE_WARN_THRESHOLD = 10;
    private static final int DEBUG_RECIPIENT_WARN_THRESHOLD = 8;
    private static final int MOB_ATTACK_VIEW_RANGE = 220;
    private static final int FARM_ATTACK_DELAY_MIN = 1800;
    private static final int FARM_ATTACK_DELAY_MAX = 4200;
    private static final int BOSS_ATTACK_DELAY_MIN = 900;
    private static final int BOSS_ATTACK_DELAY_MAX = 1800;
    private static final String DEFAULT_PREFIX = "Bot";
    private static final int MARKET_MAP_ID = 82;
    private static final int VIP_MAP_ID = 7;
    private static final int LEGACY_VIP_MAP_ID = 1;
    private static final short PIRATE_NPC_ID = -34;
    private static final int[] MANDATORY_PROFILE_SLOTS = {0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 14};
    private static final short[] BOT_PET_IDS = {2943, 2944, 2939, 3269, 3616, 4617, 4626, 4631, 4699, 4761, 4762, 4768, 4788};
    private static final byte[][] FULL_WEAR_PLAN = {
            {8, 0}, {0, 1}, {3, 2}, {4, 3}, {5, 4},
            {2, 6}, {1, 7}, {6, 8}, {4, 9}, {7, 10},
            {15, 11}, {16, 12}, {21, 13}, {22, 14}, {23, 15},
            {25, 16}, {24, 17}, {26, 18}, {27, 19}, {28, 20}
    };
    private static final byte[] WEAPON_TYPES = {8, 9, 10, 11};
    private static final String[] NAMES = {
            "AnhDung", "BaoAn", "MinhQuan", "GiaBao", "ThanhPhong", "NgocLam",
            "HaiDang", "QuocBao", "TuanKiet", "DucHuy", "NhatMinh", "HoangNam",
            "KhueVan", "ThienAn", "BaoLong", "HuuTin", "GiaHuy", "ManhCuong",
            "QuangKhai", "TrongNhan", "DuyKhang", "PhucLam", "KhanhLinh",
            "MaiAnh", "BaoNgoc", "MinhChau", "TuongVy", "HoaiNam", "LamSon",
            "DaiNghia", "TanPhat", "HienMinh", "KienVan", "ThanhTam", "BinhAn"
    };
    private static final String[] FARM_CHAT = {
            "up quai ti", "treo xiu roi ve", "map nay quai on", "kiem them it vang",
            "ai pt khong", "dang train lv", "sap len cap roi"
    };
    private static final String[] MARKET_CHAT = {
            "do dep gia cao nha", "treo cho vui thoi", "can thi xem shop",
            "hang suu tam, gia khong re", "ai du vang thi muc"
    };
    private static final String[] VIP_CHAT = {
            "quay vip tiep nao", "hom nay hoi den", "them vong nua thoi",
            "co ai trung lon chua", "giu vang quay tiep"
    };
    private static final String[] BOSS_CHAT = {
            "boss ra roi", "qua boss nhanh", "giu boss di", "san boss nao",
            "map nay co boss", "can them nguoi dame boss"
    };

    private final Random random = new Random();
    private final ConcurrentHashMap<Integer, Long> lastMovePresence = new ConcurrentHashMap<>();
    private final HashMap<String, DebugMapStats> debugStats = new HashMap<>();
    private final ScheduledExecutorService botTicker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "fake-player-bots");
        thread.setDaemon(true);
        return thread;
    });

    private volatile boolean enabled;
    private volatile boolean autoDistribute = true;
    private volatile String prefix = DEFAULT_PREFIX;
    private volatile int mapId = 1;
    private volatile int zoneId = 0;
    private volatile int baseX = 432;
    private volatile int baseY = 354;
    private volatile List<RealBot> bots = Collections.emptyList();
    private volatile int farmerCount;
    private volatile int sellerCount;
    private volatile int vipCount;
    private volatile int bossHunterCount;
    private volatile long nextDebugFlushAt;

    private FakePlayerBotManager() {
        botTicker.scheduleAtFixedRate(this::safeTick, 2L, 2L, TimeUnit.SECONDS);
        nextDebugFlushAt = System.currentTimeMillis() + DEBUG_FLUSH_INTERVAL;
    }

    public static FakePlayerBotManager gI() {
        return INSTANCE;
    }

    public synchronized void configure(boolean enabled, int count, String prefix, int mapId, int zoneId, int baseX, int baseY) {
        configure(enabled, count, prefix, mapId, zoneId, baseX, baseY, false);
    }

    public synchronized void configure(boolean enabled, int count, String prefix, int mapId, int zoneId, int baseX, int baseY, boolean autoDistribute) {
        removeBots(this.bots);

        this.enabled = enabled;
        this.autoDistribute = autoDistribute;
        this.prefix = normalizePrefix(prefix);
        this.mapId = mapId;
        this.zoneId = Math.max(0, zoneId);
        this.baseX = clamp(baseX, 24, 5000);
        this.baseY = clamp(baseY, 24, 5000);
        this.lastMovePresence.clear();
        this.farmerCount = 0;
        this.sellerCount = 0;
        this.vipCount = 0;
        this.bossHunterCount = 0;

        this.bots = enabled && count > 0
                ? Collections.unmodifiableList(createBots(Math.min(MAX_BOTS, count), autoDistribute))
                : Collections.emptyList();

        sendPresenceToOnlinePlayers();
    }

    public int getCount() {
        return enabled ? bots.size() : 0;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getMapId() {
        return mapId;
    }

    public int getZoneId() {
        return zoneId;
    }

    public int getBaseX() {
        return baseX;
    }

    public int getBaseY() {
        return baseY;
    }

    public boolean isAutoDistribute() {
        return autoDistribute;
    }

    public int getFarmerCount() {
        return farmerCount;
    }

    public int getSellerCount() {
        return sellerCount;
    }

    public int getVipCount() {
        return vipCount;
    }

    public int getBossHunterCount() {
        return bossHunterCount;
    }

    public void sendPresence(Player viewer) {
        sendPresence(viewer, false);
    }

    public void sendPresenceOnMove(Player viewer) {
        if (viewer == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = lastMovePresence.get(viewer.ID);
        if (last != null && now - last < MOVE_PRESENCE_DELAY) {
            return;
        }
        lastMovePresence.put(viewer.ID, now);
        sendPresence(viewer, true);
    }

    public boolean sendInfo(Player viewer, int id) throws IOException {
        if (!enabled || viewer == null || viewer.conn == null || viewer.map == null) {
            return false;
        }
        RealBot bot = findBot(id);
        if (bot == null || bot.player == null || bot.player.map != viewer.map) {
            return false;
        }
        MapService.send_in4_other_char(viewer.map, viewer, bot.player);
        Service.send_view_other_player_in4(viewer.conn, bot.player);
        return true;
    }

    public boolean isFakeBot(Player player) {
        return player != null && player.conn instanceof BotSession;
    }

    public Player findBotPlayer(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (RealBot bot : bots) {
            if (bot != null && bot.player != null && name.equals(bot.player.name)) {
                return bot.player;
            }
        }
        return null;
    }

    public Player findBotPlayer(int id) {
        if (id <= 0) {
            return null;
        }
        for (RealBot bot : bots) {
            if (bot != null && bot.player != null && bot.player.ID == id) {
                return bot.player;
            }
        }
        return null;
    }

    public int countBotsInMap(Map map) {
        if (!enabled || map == null) {
            return 0;
        }
        int count = 0;
        for (RealBot bot : bots) {
            if (bot == null || bot.player == null || bot.player.map != map) {
                continue;
            }
            count++;
        }
        return count;
    }

    private List<RealBot> createBots(int count, boolean autoDistribute) {
        ArrayList<RealBot> result = new ArrayList<>(count);
        ArrayList<SpawnPoint> farmPoints = collectFarmPoints();
        HashMap<String, Integer> farmZoneUsage = new HashMap<>();
        int sellers = autoDistribute ? sellerTarget(count) : 0;
        int vip = autoDistribute ? vipTarget(count, sellers) : 0;
        int bossHunters = autoDistribute ? bossTarget(count, sellers, vip) : 0;

        for (int i = 0; i < count; i++) {
            Role role;
            SpawnPoint spawn;
            if (i < sellers) {
                role = Role.MARKET;
                spawn = marketSpawn(i);
            } else if (i < sellers + vip) {
                role = Role.VIP;
                spawn = vipSpawn(i - sellers);
            } else if (i < sellers + vip + bossHunters) {
                role = Role.BOSS;
                spawn = farmSpawn(farmPoints, i - sellers - vip, farmZoneUsage);
            } else {
                role = Role.FARM;
                spawn = autoDistribute
                        ? farmSpawn(farmPoints, i - sellers - vip - bossHunters, farmZoneUsage)
                        : manualSpawn(i);
            }
            RealBot bot = createBot(i, role, spawn);
            if (bot != null) {
                result.add(bot);
                addRoleCount(role);
            }
        }
        return result;
    }

    private RealBot createBot(int index, Role role, SpawnPoint spawn) {
        if (spawn == null || spawn.map == null) {
            return null;
        }
        try {
            int id = FIRST_ID - index;
            BotSession session = new BotSession(id, botAccountName(index));
            Player player = new Player(session, id);
            session.p = player;
            initPlayer(player, index, role, spawn);
            session.connected = true;
            RealBot bot = new RealBot(player, role, spawn);
            if (role == Role.MARKET) {
                setupStore(bot, index);
            } else {
                setupWear(player, index, role);
            }
            return bot;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initPlayer(Player p, int index, Role role, SpawnPoint spawn) throws IOException {
        p.name = botDisplayName(index);
        p.map = spawn.map;
        p.map_id = spawn.map.map_id;
        p.zone_id = spawn.map.zone_id;
        p.x = spawn.x;
        p.y = spawn.y;
        p.x_old = p.x;
        p.y_old = p.y;
        p.time_move = System.currentTimeMillis();
        p.timeCantChangeMap = System.currentTimeMillis() + 86_400_000L;
        p.clazz = (byte) (index % 4);
        p.head = (byte) (index % 3);
        p.eye = (byte) ((index / 3) % 2);
        p.hair = (byte) (32 + (index % 3));
        int maxLevel = Math.max(1, Manager.gI().lvmax);
        int botLevel = spawn.level > 0 ? spawn.level + random.nextInt(6) : 35 + (index % 85);
        if (role == Role.BOSS) {
            botLevel = Math.max(botLevel, maxLevel - random.nextInt(0, Math.min(18, maxLevel) + 1));
        } else if (role == Role.VIP) {
            botLevel = Math.max(botLevel, Math.min(maxLevel, 90 + random.nextInt(0, Math.max(1, maxLevel - 89))));
        }
        p.level = (short) clamp(botLevel, 1, maxLevel);
        p.exp = 0;
        p.typepk = -1;
        p.hieuchien = 0;
        p.isdie = false;
        p.isOwner = true;
        p.owner = p;
        p.isSquire = false;
        p.isLiveSquire = false;
        p.already_setup = false;
        p.type_exp = 1;
        p.vang = (role == Role.VIP || role == Role.BOSS) ? 200_000_000L : 0;
        p.kimcuong = role == Role.VIP ? 20_000 : 0;
        p.tiemnang = 0;
        p.kynang = 0;
        p.point1 = (short) (40 + p.level);
        p.point2 = (short) (40 + p.level);
        p.point3 = (short) (50 + p.level);
        p.point4 = (short) (35 + p.level);
        p.skill_point = new byte[21];
        p.skill_110 = new byte[2];
        p.time_delay_skill = new long[21];
        p.maxInventory = 80;
        p.maxBox = 80;
        p.maxBag = 80;
        p.item = new Item(p);
        p.item.wear = new Item3[24];
        p.item.inventory3 = new Item3[p.maxInventory];
        p.item.box3 = new Item3[p.maxBox];
        p.item.bag3 = new Item3[p.maxBag];
        p.item.inventory47 = new ArrayList<>();
        p.item.box47 = new ArrayList<>();
        p.item.bag47 = new ArrayList<>();
        p.giftcode = new ArrayList<>();
        p.rms_save = new byte[][]{new byte[0], new byte[0]};
        p.mypet = new ArrayList<>();
        p.list_friend = new ArrayList<Friend>();
        p.list_enemies = new ArrayList<>();
        p.other_player_inside = new HashMap<>();
        p.other_mob_inside = new HashMap<>();
        p.other_mob_inside_update = new HashMap<>();
        p.fashion = new short[]{-1, -1, -1, -1, -1, -1, -1};
        p.my_store = new ArrayList<>();
        p.my_store_name = "";
        p.Store_Sell_ToPL = "";
        p.name_trade = "";
        p.list_item_trade = new short[0];
        p.quest_daily = new int[]{-1, 0, 0, 0};
        p.time_use_item_arena = new long[]{0, 0, 0};
        p.id_henshin = -1;
        p.id_horse = -1;
        p.pet_follow_id = -1;
        p.type_use_horse = -1;
        p.id_item_rebuild = -1;
        p.id_use_mayman = -1;
        p.item_replace = -1;
        p.item_replace2 = -1;
        p.id_buffer_126 = -1;
        p.id_temp_byte = -1;
        p.id_medal_is_created = -1;
        p.fusion_material_medal_id = -1;
        p.id_remove_time_use = -1;
        p.id_wing_split = -1;
        p.id_select_mo_ly = -1;
        p.id_hop_ngoc = -1;
        p.list_thao_kham_ngoc = new ArrayList<>();
        p.date = new Date();
        p.suckhoe = 30_000;
        p.isUpExp = role == Role.FARM;
        p.add_EffDefault(-127, 1, System.currentTimeMillis() + 86_400_000L);
        p.add_EffDefault(-128, 1, System.currentTimeMillis() + 86_400_000L);
        p.hp = p.body.get_HpMax();
        p.mp = p.body.get_MpMax();
    }

    private void setupWear(Player player, int seed, Role role) {
        int minColor = minWearColor(role);
        for (int i = 0; i < FULL_WEAR_PLAN.length; i++) {
            byte type = FULL_WEAR_PLAN[i][0];
            int slot = FULL_WEAR_PLAN[i][1];
            ItemTemplate3 template = slot == 0
                    ? pickWeaponTemplate(player.clazz, player.level, seed + i * 17, minColor)
                    : pickTemplate(type, player.clazz, player.level, seed + i * 17, minColor, requirePartAssetForSlot(slot));
            if (template == null && minColor > 0) {
                template = slot == 0
                        ? pickWeaponTemplate(player.clazz, player.level, seed + i * 17, 0)
                        : pickTemplate(type, player.clazz, player.level, seed + i * 17, 0, requirePartAssetForSlot(slot));
            }
            putWear(player, slot, template, role, seed + i * 17);
        }
        ensureMandatoryProfileWear(player, seed, role);
        setupPet(player, seed, role);
        player.fashion = Part_fashion.get_part(player);
        player.hp = player.body.get_HpMax();
        player.mp = player.body.get_MpMax();
    }

    private void putWear(Player player, int slot, ItemTemplate3 template, Role role, int seed) {
        if (template == null || slot < 0 || slot >= player.item.wear.length) {
            return;
        }
        Item3 item = createItem(template, true, tierForRole(role));
        if (item != null) {
            tuneWearItem(item, slot, role, seed);
            player.item.wear[slot] = item;
        }
    }

    private void ensureMandatoryProfileWear(Player player, int seed, Role role) {
        int minColor = minWearColor(role);
        for (int i = 0; i < MANDATORY_PROFILE_SLOTS.length; i++) {
            int slot = MANDATORY_PROFILE_SLOTS[i];
            Item3 current = slot < player.item.wear.length ? player.item.wear[slot] : null;
            if (isMandatorySlotSatisfied(slot, current)) {
                continue;
            }
            ItemTemplate3 template = pickMandatoryTemplate(player, slot, seed + i * 53, minColor);
            putWear(player, slot, template, role, seed + i * 53);
        }
    }

    private boolean isMandatorySlotSatisfied(int slot, Item3 current) {
        if (current == null) {
            return false;
        }
        if (slot == 11) {
            return Part_fashion.fashions.contains(current.id);
        }
        if (slot == 14) {
            return isVisibleWingId(current.id);
        }
        return true;
    }

    private ItemTemplate3 pickMandatoryTemplate(Player player, int slot, int seed, int minColor) {
        return switch (slot) {
            case 0 -> pickWeaponTemplateRelaxed(player.clazz, player.level, seed, minColor);
            case 11 -> pickFashionTemplate((byte) 15, player.clazz, player.level, seed, minColor);
            case 14 -> pickWingTemplate(player.clazz, player.level, seed, minColor);
            default -> pickTemplateRelaxed(slotType(slot), player.clazz, player.level, seed, minColor, requirePartAssetForSlot(slot));
        };
    }

    private byte slotType(int slot) {
        return switch (slot) {
            case 1 -> 0;
            case 7 -> 1;
            case 6 -> 2;
            case 2 -> 3;
            case 3, 9 -> 4;
            case 4 -> 5;
            case 8 -> 6;
            case 10 -> 7;
            case 11 -> 15;
            case 12 -> 16;
            case 13 -> 21;
            case 14 -> 22;
            case 15 -> 23;
            case 16 -> 25;
            case 17 -> 24;
            case 18 -> 26;
            case 19 -> 27;
            case 20 -> 28;
            default -> -1;
        };
    }

    private ItemTemplate3 pickTemplateRelaxed(byte type, byte clazz, short level, int seed, int minColor, boolean requirePartAsset) {
        ItemTemplate3 template = pickTemplate(type, clazz, level, seed, minColor, requirePartAsset);
        if (template != null) {
            return template;
        }
        if (minColor > 0) {
            template = pickTemplate(type, clazz, level, seed, 0, requirePartAsset);
            if (template != null) {
                return template;
            }
        }
        template = pickTemplateLoose(type, clazz, level, seed, 0, requirePartAsset, false, false);
        if (template != null) {
            return template;
        }
        return requirePartAsset ? pickTemplateLoose(type, clazz, level, seed, 0, false, false, false) : null;
    }

    private ItemTemplate3 pickWeaponTemplateRelaxed(byte clazz, short level, int seed, int minColor) {
        ItemTemplate3 template = pickWeaponTemplate(clazz, level, seed, minColor);
        if (template != null) {
            return template;
        }
        if (minColor > 0) {
            template = pickWeaponTemplate(clazz, level, seed, 0);
            if (template != null) {
                return template;
            }
        }
        ArrayList<ItemTemplate3> choices = new ArrayList<>();
        for (byte type : WEAPON_TYPES) {
            collectTemplates(choices, type, clazz, level, 0, true, false, false, null);
        }
        if (!choices.isEmpty()) {
            return pick(choices, seed);
        }
        for (byte type : WEAPON_TYPES) {
            collectTemplates(choices, type, clazz, level, 0, false, false, false, null);
        }
        return pick(choices, seed);
    }

    private ItemTemplate3 pickFashionTemplate(byte type, byte clazz, short level, int seed, int minColor) {
        ArrayList<ItemTemplate3> choices = new ArrayList<>();
        collectTemplates(choices, type, clazz, level, minColor, false, true, true, null);
        if (choices.isEmpty() && minColor > 0) {
            collectTemplates(choices, type, clazz, level, 0, false, true, true, null);
        }
        if (choices.isEmpty()) {
            collectTemplates(choices, type, clazz, level, 0, false, false, false, Part_fashion.fashions);
        }
        return pick(choices, seed);
    }

    private ItemTemplate3 pickWingTemplate(byte clazz, short level, int seed, int minColor) {
        ArrayList<ItemTemplate3> choices = new ArrayList<>();
        collectTemplates(choices, (byte) 22, clazz, level, minColor, false, true, true, null);
        if (choices.isEmpty() && minColor > 0) {
            collectTemplates(choices, (byte) 22, clazz, level, 0, false, true, true, null);
        }
        if (choices.isEmpty()) {
            collectTemplates(choices, (byte) 22, clazz, level, 0, false, false, false, null);
        }
        return pick(choices, seed);
    }

    private ItemTemplate3 pickTemplateLoose(byte type, byte clazz, short level, int seed, int minColor,
                                            boolean requirePartAsset, boolean strictLevel, boolean strictClass) {
        ArrayList<ItemTemplate3> choices = new ArrayList<>();
        collectTemplates(choices, type, clazz, level, minColor, requirePartAsset, strictLevel, strictClass, null);
        return pick(choices, seed);
    }

    private void collectTemplates(List<ItemTemplate3> choices, byte type, byte clazz, short level, int minColor,
                                  boolean requirePartAsset, boolean strictLevel, boolean strictClass, List<Short> requiredIds) {
        for (ItemTemplate3 template : ItemTemplate3.item) {
            if (template == null || template.getType() != type) {
                continue;
            }
            if (strictClass && template.getClazz() != 4 && template.getClazz() != clazz) {
                continue;
            }
            if (strictLevel && (template.getLevel() > level || Math.abs(template.getLevel() - level) > 40)) {
                continue;
            }
            if (template.getColor() < minColor) {
                continue;
            }
            if (requirePartAsset && !hasPartAsset(template.getType(), template.getPart())) {
                continue;
            }
            if (requiredIds != null && !requiredIds.contains(template.getId())) {
                continue;
            }
            if (type == 15 && !Part_fashion.fashions.contains(template.getId())) {
                continue;
            }
            if (type == 22 && !isVisibleWingId(template.getId())) {
                continue;
            }
            choices.add(template);
        }
    }

    private boolean isVisibleWingId(short id) {
        return (id >= 4638 && id <= 4648)
                || id == 4707
                || id == 4712
                || id == 4713
                || id == 4773
                || id == 4774
                || id == 4789
                || id == 4790
                || (id >= 4793 && id <= 4797);
    }

    private void setupPet(Player player, int seed, Role role) {
        player.mypet.clear();
        short petId = BOT_PET_IDS[Math.floorMod(seed + random.nextInt(BOT_PET_IDS.length), BOT_PET_IDS.length)];
        Pet pet = Pet.newPet(petId, 0);
        if (pet == null) {
            player.pet_follow_id = -1;
            return;
        }
        pet.is_follow = true;
        pet.is_hatch = false;
        pet.time_born = System.currentTimeMillis() - 86_400_000L;
        pet.level = petLevelForRole(role);
        pet.exp = 0;
        pet.maxgrown = 300;
        pet.grown = (short) (role == Role.BOSS ? 300 : role == Role.VIP ? 260 : 220);
        pet.sucmanh = petBaseStatForRole(role);
        pet.kheoleo = petBaseStatForRole(role);
        pet.theluc = petBaseStatForRole(role);
        pet.tinhthan = petBaseStatForRole(role);
        pet.maxpoint = 20_000;
        pet.setPoint();
        player.mypet.add(pet);
        player.pet_follow_id = pet.get_id();
    }

    private short petLevelForRole(Role role) {
        return switch (role) {
            case BOSS -> 30;
            case VIP -> 25;
            case MARKET -> 20;
            case FARM -> 18;
        };
    }

    private short petBaseStatForRole(Role role) {
        return switch (role) {
            case BOSS -> 16_000;
            case VIP -> 13_000;
            case MARKET -> 9_000;
            case FARM -> 8_000;
        };
    }

    private void setupStore(RealBot bot, int seed) {
        Player player = bot.player;
        setupWear(player, seed, Role.MARKET);
        int totalItems = 5 + random.nextInt(0, 4);
        for (int i = 0; i < totalItems && i < player.item.inventory3.length; i++) {
            ItemTemplate3 template = pickStoreTemplate(player.level, seed + i * 11);
            if (template == null) {
                continue;
            }
            if (!isPurpleOrOrange(template)) {
                continue;
            }
            Item3 item = createItem(template, false, (byte) random.nextInt(0, 6));
            if (item == null) {
                continue;
            }
            item.index = (short) i;
            player.item.inventory3[i] = item;
            Player_store store = new Player_store();
            store.it_id = (short) i;
            store.it_type = 3;
            store.it_quant = 1;
            store.it_price = random.nextInt(BOT_SHOP_PRICE_MIN, BOT_SHOP_PRICE_MAX + 1);
            player.my_store.add(store);
        }
        player.my_store_name = "KMB tim cam " + player.name;
        bot.nextChatAt = System.currentTimeMillis() + random.nextInt(5_000, 30_000);
    }

    private Item3 createItem(ItemTemplate3 template, boolean locked, byte tier) {
        if (template == null) {
            return null;
        }
        Item3 item = new Item3();
        item.id = template.getId();
        item.name = template.getName();
        item.clazz = template.getClazz();
        item.type = template.getType();
        item.level = template.getLevel();
        item.icon = template.getIcon();
        item.color = template.getColor();
        item.part = template.getPart();
        item.tier = tier;
        item.tierStar = 0;
        item.islock = locked;
        item.time_use = 0;
        item.expiry_date = 0;
        item.op = new ArrayList<>();
        if (template.getOp() != null) {
            for (Option option : template.getOp()) {
                if (option != null) {
                    item.op.add(new Option(option.id, option.param, item.id));
                }
            }
        }
        item.opMedal = new ArrayList<>();
        item.UpdateName();
        return item;
    }

    private ItemTemplate3 pickTemplate(byte type, byte clazz, short level, int seed) {
        return pickTemplate(type, clazz, level, seed, 0, true);
    }

    private ItemTemplate3 pickTemplate(byte type, byte clazz, short level, int seed, int minColor, boolean requirePartAsset) {
        ArrayList<ItemTemplate3> choices = new ArrayList<>();
        for (ItemTemplate3 template : ItemTemplate3.item) {
            if (template == null || template.getType() != type) {
                continue;
            }
            if (template.getClazz() != 4 && template.getClazz() != clazz) {
                continue;
            }
            if (template.getLevel() > level || Math.abs(template.getLevel() - level) > 40) {
                continue;
            }
            if (template.getColor() < minColor) {
                continue;
            }
            if (requirePartAsset && !hasPartAsset(template.getType(), template.getPart())) {
                continue;
            }
            choices.add(template);
        }
        return pick(choices, seed);
    }

    private ItemTemplate3 pickWeaponTemplate(byte clazz, short level, int seed, int minColor) {
        ArrayList<ItemTemplate3> choices = new ArrayList<>();
        for (byte type : WEAPON_TYPES) {
            for (ItemTemplate3 template : ItemTemplate3.item) {
                if (template == null || template.getType() != type) {
                    continue;
                }
                if (template.getClazz() != 4 && template.getClazz() != clazz) {
                    continue;
                }
                if (template.getLevel() > level || Math.abs(template.getLevel() - level) > 40) {
                    continue;
                }
                if (template.getColor() < minColor) {
                    continue;
                }
                if (!hasPartAsset(template.getType(), template.getPart())) {
                    continue;
                }
                choices.add(template);
            }
        }
        return pick(choices, seed);
    }

    private boolean requirePartAssetForSlot(int slot) {
        return slot == 0 || slot == 1 || slot == 6 || slot == 7 || slot == 10;
    }

    private int minWearColor(Role role) {
        return switch (role) {
            case VIP, BOSS, MARKET -> 3;
            case FARM -> 2;
        };
    }

    private byte tierForRole(Role role) {
        return switch (role) {
            case BOSS -> (byte) random.nextInt(11, 16);
            case VIP -> (byte) random.nextInt(9, 16);
            case MARKET -> (byte) random.nextInt(7, 13);
            case FARM -> (byte) random.nextInt(4, 12);
        };
    }

    private void tuneWearItem(Item3 item, int slot, Role role, int seed) {
        if (isSecondaryWearSlot(slot)) {
            item.tier = 0;
            item.tierStar = 0;
            item.UpdateName();
            return;
        }
        if (role == Role.VIP || role == Role.BOSS) {
            item.tierStar = (byte) random.nextInt(0, 4);
            if (item.color < 4) {
                item.color = 4;
            }
            addOptionIfMissing(item, 27, 300 + random.nextInt(0, 701));
            addOptionIfMissing(item, 28, 300 + random.nextInt(0, 701));
            if (role == Role.BOSS) {
                addOptionIfMissing(item, -128, 150 + random.nextInt(0, 351));
                addOptionIfMissing(item, 99, 200 + random.nextInt(0, 401));
            }
        } else if (role == Role.MARKET && item.color < 3) {
            item.color = 3;
        }
        item.UpdateName();
    }

    private boolean isSecondaryWearSlot(int slot) {
        return slot >= 11 && slot <= 20;
    }

    private void addOptionIfMissing(Item3 item, int id, int param) {
        for (Option option : item.op) {
            if (option.id == (byte) id) {
                return;
            }
        }
        item.op.add(new Option(id, param, item.id));
    }

    private ItemTemplate3 pickStoreTemplate(short level, int seed) {
        ArrayList<ItemTemplate3> choices = new ArrayList<>();
        for (ItemTemplate3 template : ItemTemplate3.item) {
            if (template == null || !isTradableEquipmentType(template.getType())) {
                continue;
            }
            if (template.getLevel() < 20 || template.getLevel() > Math.max(20, level + 20)) {
                continue;
            }
            if (!isPurpleOrOrange(template)) {
                continue;
            }
            if (!hasPartAsset(template.getType(), template.getPart())) {
                continue;
            }
            choices.add(template);
        }
        return pick(choices, seed);
    }

    private boolean isTradableEquipmentType(byte type) {
        return (type >= 0 && type <= 4) || (type >= 6 && type <= 9);
    }

    private boolean isPurpleOrOrange(ItemTemplate3 template) {
        return template.getColor() == 3 || template.getColor() == 4;
    }

    private boolean hasPartAsset(byte type, byte part) {
        if (part < 0) {
            return false;
        }
        String key = type + "_" + part;
        return Files.exists(Path.of("data", "part_char", "imgver", "x1", "Data", key))
                && Files.exists(Path.of("data", "part_char", "imgver", "x1", "Img", key + ".png"));
    }

    private <T> T pick(List<T> values, int seed) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        int index = Math.floorMod(seed + random.nextInt(values.size()), values.size());
        return values.get(index);
    }

    private ArrayList<SpawnPoint> collectFarmPoints() {
        ArrayList<SpawnPoint> result = new ArrayList<>();
        for (Map[] zones : Map.entrys) {
            if (zones == null || zones.length == 0) {
                continue;
            }
            for (Map map : zones) {
                if (map == null || map.mobs == null || map.mobs.length == 0
                        || Map.is_map_cant_save_site(map.map_id) || map.ismaplang || isReservedAutoMap(map.map_id)) {
                    continue;
                }
                Mob_in_map mob = firstLiveMob(map);
                if (mob == null) {
                    continue;
                }
                result.add(new SpawnPoint(map, mob.x, mob.y, mob.level));
            }
        }
        if (result.isEmpty()) {
            result.add(manualSpawn(0));
        }
        return result;
    }

    private SpawnPoint manualSpawn(int index) {
        Map[] maps = Map.get_map_by_id(mapId);
        if (maps == null || maps.length == 0) {
            maps = Map.get_map_by_id(1);
        }
        if (maps == null || maps.length == 0) {
            return null;
        }
        int zoneStep = Math.floorDiv(Math.max(0, index), MAX_FARM_BOTS_PER_ZONE);
        int safeZone = Math.floorMod(zoneId + zoneStep, maps.length);
        Map map = maps[safeZone];
        short x = (short) (baseX + ((index % 10) - 4) * 42);
        short y = (short) (baseY + (((index / 10) % 10) - 4) * 30);
        return new SpawnPoint(map, x, y, (short) 35);
    }

    private SpawnPoint farmSpawn(List<SpawnPoint> points, int index, HashMap<String, Integer> zoneUsage) {
        if (points == null || points.isEmpty()) {
            return manualSpawn(index);
        }
        int size = points.size();
        int start = Math.floorMod(index, size);
        for (int offset = 0; offset < size; offset++) {
            SpawnPoint base = points.get((start + offset) % size);
            String key = farmZoneKey(base.map);
            int used = zoneUsage.getOrDefault(key, 0);
            if (used >= MAX_FARM_BOTS_PER_ZONE) {
                continue;
            }
            zoneUsage.put(key, used + 1);
            short x = (short) (base.x + random.nextInt(-70, 71));
            short y = (short) (base.y + random.nextInt(-35, 36));
            return new SpawnPoint(base.map, x, y, base.level);
        }
        return null;
    }

    private SpawnPoint marketSpawn(int index) {
        Map[] maps = Map.get_map_by_id(MARKET_MAP_ID);
        if (maps == null || maps.length == 0) {
            return manualSpawn(index);
        }
        int zoneIndex = index % maps.length;
        Map map = maps[zoneIndex];
        int localIndex = index / maps.length;
        int centerX = clamp(map.mapW * 12, 220, 860);
        int centerY = clamp(map.mapH * 12, 220, 620);
        int col = localIndex % 4;
        int row = (localIndex / 4) % 4;
        short x = (short) (centerX + (col - 1) * 92 + (zoneIndex % 2) * 18);
        short y = (short) (centerY + (row - 1) * 62);
        return new SpawnPoint(map, x, y, (short) 75);
    }

    private SpawnPoint vipSpawn(int index) {
        Map[] maps = Map.get_map_by_id(VIP_MAP_ID);
        if (maps == null || maps.length == 0) {
            return manualSpawn(index);
        }
        int zoneIndex = index % maps.length;
        Map map = maps[zoneIndex];
        NpcMap npc = findNpc(map, PIRATE_NPC_ID);
        int localIndex = index / maps.length;
        short baseX = npc != null ? npc.x : 816;
        short baseY = npc != null ? npc.y : 408;
        int col = localIndex % 4;
        int row = (localIndex / 4) % 3;
        short x = (short) (baseX + 28 + (col - 1) * 18);
        short y = (short) (baseY - 18 + row * 16);
        return new SpawnPoint(map, x, y, (short) 60);
    }

    private NpcMap findNpc(Map map, short npcId) {
        if (map == null) {
            return null;
        }
        NpcMap npc = findNpc(map.npc_0, npcId);
        if (npc != null) {
            return npc;
        }
        npc = findNpc(map.npc_1, npcId);
        if (npc != null) {
            return npc;
        }
        return findNpc(map.npc_2, npcId);
    }

    private NpcMap findNpc(NpcMap[] npcs, short npcId) {
        if (npcs == null) {
            return null;
        }
        for (NpcMap npc : npcs) {
            if (npc != null && npc.npcTemplate != null && npc.npcTemplate.id == npcId) {
                return npc;
            }
        }
        return null;
    }

    private boolean isReservedAutoMap(int mapId) {
        return mapId == MARKET_MAP_ID || mapId == VIP_MAP_ID || mapId == LEGACY_VIP_MAP_ID;
    }

    private Mob_in_map firstLiveMob(Map map) {
        if (map == null || map.mobs == null) {
            return null;
        }
        for (Mob_in_map mob : map.mobs) {
            if (mob != null && !mob.isdie && mob.template != null) {
                return mob;
            }
        }
        return null;
    }

    private int sellerTarget(int count) {
        if (count < 20) {
            return 0;
        }
        return Math.max(1, Math.round(count * 0.05f));
    }

    private int vipTarget(int count, int sellers) {
        if (count < 45 || count - sellers < 6) {
            return 0;
        }
        return Math.max(1, Math.round(count * 0.02f));
    }

    private int bossTarget(int count, int sellers, int vip) {
        if (count - sellers - vip < 8) {
            return 0;
        }
        return Math.max(1, count / 14);
    }

    private void addRoleCount(Role role) {
        switch (role) {
            case MARKET -> sellerCount++;
            case VIP -> vipCount++;
            case BOSS -> bossHunterCount++;
            case FARM -> farmerCount++;
        }
    }

    private void sendPresence(Player viewer, boolean onlyNearby) {
        if (!enabled || viewer == null || viewer.conn == null || viewer.map == null || viewer.conn instanceof BotSession) {
            return;
        }
        try {
            Message m = new Message(4);
            int sent = 0;
            int farmSent = 0;
            ArrayList<RealBot> sameMap = new ArrayList<>();
            for (RealBot bot : bots) {
                if (bot.player == null || bot.player.map != viewer.map) {
                    continue;
                }
                if (!isVisibleFor(viewer, bot.player)) {
                    continue;
                }
                if (hasLoadedForViewer(viewer, bot.player.ID)) {
                    continue;
                }
                if (bot.role == Role.FARM && farmSent >= MAX_VISIBLE_FARM_PER_VIEW) {
                    continue;
                }
                writePresence(m, bot.player);
                markLoadedForViewer(viewer, bot.player.ID);
                sameMap.add(bot);
                sent++;
                if (bot.role == Role.FARM) {
                    farmSent++;
                }
                if (sent >= MAX_VISIBLE_PER_VIEW) {
                    break;
                }
            }
            if (m.writer().size() > 0) {
                viewer.conn.addmsg(m);
            }
            m.cleanup();
            for (RealBot bot : sameMap) {
                sendStoreLabel(viewer, bot.player);
            }
            recordPresenceDebug(viewer, sent, farmSent, onlyNearby);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writePresence(Message m, Player player) throws IOException {
        m.writer().writeByte(0);
        m.writer().writeShort(0);
        m.writer().writeShort(player.ID);
        m.writer().writeShort(player.x);
        m.writer().writeShort(player.y);
        m.writer().writeByte(-1);
    }

    private void sendStoreLabel(Player viewer, Player seller) {
        if (viewer == null || viewer.conn == null || seller == null || seller.my_store_name == null || seller.my_store_name.isEmpty()) {
            return;
        }
        try {
            Message m = new Message(-102);
            m.writer().writeByte(1);
            m.writer().writeShort(seller.ID);
            m.writer().writeUTF(seller.my_store_name);
            viewer.conn.addmsg(m);
            m.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPresenceToOnlinePlayers() {
        for (Session session : onlineSessions()) {
            if (session != null && !(session instanceof BotSession)) {
                sendPresence(session.p);
            }
        }
    }

    private void removeBots(List<RealBot> oldBots) {
        if (oldBots == null || oldBots.isEmpty()) {
            return;
        }
        for (RealBot bot : oldBots) {
            if (bot == null || bot.player == null || bot.player.map == null) {
                continue;
            }
            try {
                sendRemovePacket(bot.player.map, bot.player);
                bot.player.conn.connected = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<Session> onlineSessions() {
        synchronized (Session.client_entry) {
            return new ArrayList<>(Session.client_entry);
        }
    }

    private RealBot findBot(int id) {
        for (RealBot bot : bots) {
            if (bot.player != null && bot.player.ID == id) {
                return bot;
            }
        }
        return null;
    }

    private boolean isVisibleFor(Player viewer, Player bot) {
        return viewer.map != null && bot.map == viewer.map
                && (Math.abs(viewer.x - bot.x) < 1500 || Map.is_map__load_board_player(viewer.map.map_id))
                && (Math.abs(viewer.y - bot.y) < 1500 || Map.is_map__load_board_player(viewer.map.map_id));
    }

    private void safeTick() {
        try {
            tickBots();
            flushDebugStatsIfNeeded();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tickBots() {
        if (!enabled || bots.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        List<BossTarget> activeBosses = null;
        int actions = 0;
        for (RealBot bot : bots) {
            if (bot == null || bot.player == null || bot.player.map == null || now < bot.nextActionAt) {
                continue;
            }
            switch (bot.role) {
                case FARM -> farmAction(bot, now);
                case MARKET -> marketAction(bot, now);
                case VIP -> vipAction(bot, now);
                case BOSS -> {
                    if (activeBosses == null) {
                        activeBosses = collectActiveBosses();
                    }
                    bossAction(bot, now, activeBosses);
                }
            }
            actions++;
            if (actions >= 150) {
                break;
            }
        }
    }

    private void farmAction(RealBot bot, long now) {
        Player player = bot.player;
        Mob_in_map mob = nearestMob(player.map, player.x, player.y);
        if (mob != null) {
            syncCombatLevel(player, mob.level + 3);
            moveNear(player, mob.x, mob.y, 90);
            if (now >= bot.nextAttackAt) {
                if (sendFakeMobAttack(player, mob)) {
                    bot.nextAttackAt = now + random.nextInt(FARM_ATTACK_DELAY_MIN, FARM_ATTACK_DELAY_MAX + 1);
                } else {
                    bot.nextAttackAt = now + random.nextInt(700, 1401);
                }
            }
            if (now >= bot.nextChatAt && random.nextInt(100) < 18) {
                sendBotChat(player, FARM_CHAT[random.nextInt(FARM_CHAT.length)]);
                bot.nextChatAt = now + random.nextInt(35_000, 120_000);
            }
        } else {
            smallMove(player);
        }
        bot.nextActionAt = now + random.nextInt(1_000, 2_201);
    }

    private void marketAction(RealBot bot, long now) {
        if (now >= bot.nextChatAt && random.nextInt(100) < 35) {
            sendBotChat(bot.player, MARKET_CHAT[random.nextInt(MARKET_CHAT.length)]);
            bot.nextChatAt = now + random.nextInt(45_000, 160_000);
        }
        bot.nextActionAt = now + random.nextInt(8_000, 18_000);
    }

    private void vipAction(RealBot bot, long now) {
        moveAround(bot.player, bot.spawn.x, bot.spawn.y, 46, 28);
        if (now >= bot.nextLuckyJoinAt && random.nextInt(100) < 45) {
            tryJoinLuckyDraw(bot, now);
        }
        if (now >= bot.nextChatAt && random.nextInt(100) < 28) {
            sendBotChat(bot.player, VIP_CHAT[random.nextInt(VIP_CHAT.length)]);
            bot.nextChatAt = now + random.nextInt(35_000, 120_000);
        }
        bot.nextActionAt = now + random.nextInt(5_000, 12_000);
    }

    private void bossAction(RealBot bot, long now, List<BossTarget> activeBosses) {
        BossTarget target = selectBossTarget(bot, activeBosses, now);
        if (target == null) {
            if (bot.player.map != bot.spawn.map) {
                changeBotMap(bot.player, bot.spawn.map, bot.spawn.x, bot.spawn.y);
            }
            farmAction(bot, now);
            return;
        }

        Player player = bot.player;
        syncCombatLevel(player, target.mob.level + 5);
        if (player.map != target.map) {
            short x = (short) (target.mob.x + random.nextInt(-80, 81));
            short y = (short) (target.mob.y + random.nextInt(-36, 37));
            changeBotMap(player, target.map, x, y);
            if (now >= bot.nextChatAt) {
                sendBotChat(player, BOSS_CHAT[random.nextInt(BOSS_CHAT.length)]);
                bot.nextChatAt = now + random.nextInt(25_000, 80_000);
            }
        } else {
            moveNear(player, target.mob.x, target.mob.y, 95);
            if (now >= bot.nextAttackAt) {
                if (sendFakeMobAttack(player, target.mob)) {
                    bot.nextAttackAt = now + random.nextInt(BOSS_ATTACK_DELAY_MIN, BOSS_ATTACK_DELAY_MAX + 1);
                } else {
                    bot.nextAttackAt = now + random.nextInt(600, 1201);
                }
            }
            if (now >= bot.nextChatAt && random.nextInt(100) < 32) {
                sendBotChat(player, BOSS_CHAT[random.nextInt(BOSS_CHAT.length)]);
                bot.nextChatAt = now + random.nextInt(25_000, 80_000);
            }
        }
        bot.nextActionAt = now + random.nextInt(700, 1_601);
    }

    private void tryJoinLuckyDraw(RealBot bot, long now) {
        Manager manager = Manager.gI();
        LuckyDraw draw = random.nextInt(100) < 80 ? manager.lucky_draw_vip : manager.lucky_draw_normal;
        if (draw == null || Manager.isLockVX
                || manager.lucky_draw_vip.getPlayer(bot.player.ID) != null
                || manager.lucky_draw_normal.getPlayer(bot.player.ID) != null) {
            bot.nextLuckyJoinAt = now + random.nextInt(90_000, 220_000);
            return;
        }
        int maxBotJoin = Math.max(draw.min_join, draw.max_join / 5);
        int amount = random.nextInt(draw.min_join, maxBotJoin + 1);
        if (bot.player.vang < amount) {
            bot.player.vang = Math.max(200_000_000L, (long) amount + draw.max_join);
        }
        try {
            draw.join_lucky_draw(bot.player, amount);
            if (random.nextInt(100) < 35) {
                String type = draw == manager.lucky_draw_vip ? "vip" : "thuong";
                sendBotChat(bot.player, "vao vx " + type + " " + Util.number_format(amount));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bot.nextLuckyJoinAt = now + random.nextInt(120_000, 300_000);
    }

    private List<BossTarget> collectActiveBosses() {
        ArrayList<BossTarget> result = new ArrayList<>();
        for (Map[] zones : Map.entrys) {
            if (zones == null) {
                continue;
            }
            for (Map map : zones) {
                if (map == null) {
                    continue;
                }
                for (Mob_in_map mob : map.bossInMaps) {
                    if (isActiveBoss(mob)) {
                        result.add(new BossTarget(map, mob));
                    }
                }
                if (map.mobs == null) {
                    continue;
                }
                for (Mob_in_map mob : map.mobs) {
                    if (isActiveBoss(mob)) {
                        result.add(new BossTarget(map, mob));
                    }
                }
            }
        }
        return result;
    }

    private BossTarget selectBossTarget(RealBot bot, List<BossTarget> activeBosses, long now) {
        if (activeBosses == null || activeBosses.isEmpty()) {
            return null;
        }
        int botIndex = Math.max(0, FIRST_ID - bot.player.ID);
        int rotation = 0;
        if (bossHunterCount > 0 && activeBosses.size() > bossHunterCount) {
            rotation = (int) ((now / 45_000L) % activeBosses.size());
        }
        int targetIndex = Math.floorMod(botIndex + rotation, activeBosses.size());
        return activeBosses.get(targetIndex);
    }

    private boolean isActiveBoss(Mob_in_map mob) {
        return mob != null && mob.template != null && mob.isBoss() && mob.is_boss_active && !mob.isdie && mob.hp > 0;
    }

    private Mob_in_map nearestMob(Map map, short x, short y) {
        if (map == null || map.mobs == null || map.mobs.length == 0) {
            return null;
        }
        Mob_in_map best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Mob_in_map mob : map.mobs) {
            if (mob == null || mob.isdie || mob.template == null) {
                continue;
            }
            int distance = Math.abs(mob.x - x) + Math.abs(mob.y - y);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = mob;
            }
        }
        if (best != null && bestDistance < 550) {
            return best;
        }
        return firstLiveMob(map);
    }

    private void moveNear(Player player, short targetX, short targetY, int radius) {
        short newX = (short) (targetX + random.nextInt(-radius, radius + 1));
        short newY = (short) (targetY + random.nextInt(-35, 36));
        moveBot(player, newX, newY);
    }

    private void smallMove(Player player) {
        short newX = (short) (player.x + random.nextInt(-45, 46));
        short newY = (short) (player.y + random.nextInt(-22, 23));
        moveBot(player, newX, newY);
    }

    private void moveAround(Player player, short centerX, short centerY, int radiusX, int radiusY) {
        short newX = (short) (centerX + random.nextInt(-radiusX, radiusX + 1));
        short newY = (short) (centerY + random.nextInt(-radiusY, radiusY + 1));
        moveBot(player, newX, newY);
    }

    private void moveBot(Player player, short x, short y) {
        if (player == null || player.map == null) {
            return;
        }
        x = (short) clamp(x, 24, Math.max(48, player.map.mapW * 24 - 24));
        y = (short) clamp(y, 24, Math.max(48, player.map.mapH * 24 - 24));
        if (Math.abs(player.x - x) < 5 && Math.abs(player.y - y) < 5) {
            return;
        }
        player.x_old = player.x;
        player.y_old = player.y;
        player.x = x;
        player.y = y;
        player.time_move = System.currentTimeMillis();
        List<Player> viewers = collectNearbyRealViewers(player.map, player.x, player.y, player.ID);
        if (viewers.isEmpty()) {
            return;
        }
        ArrayList<Player> loadedViewers = new ArrayList<>();
        int lateSpawnCount = 0;
        for (Player viewer : viewers) {
            if (hasLoadedForViewer(viewer, player.ID)) {
                loadedViewers.add(viewer);
            } else {
                sendPresenceToViewer(viewer, player);
                lateSpawnCount++;
            }
        }
        recordMoveDebug(player, loadedViewers.size(), lateSpawnCount);
        if (loadedViewers.isEmpty()) {
            return;
        }
        try {
            Message m = new Message(4);
            writePresence(m, player);
            for (Player viewer : loadedViewers) {
                viewer.conn.addmsg(m);
            }
            m.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeBotMap(Player player, Map targetMap, short x, short y) {
        if (player == null || targetMap == null) {
            return;
        }
        Map oldMap = player.map;
        if (oldMap == targetMap) {
            moveBot(player, x, y);
            return;
        }
        if (oldMap != null) {
            sendRemovePacket(oldMap, player);
        }
        player.map = targetMap;
        player.map_id = targetMap.map_id;
        player.zone_id = targetMap.zone_id;
        player.other_player_inside.clear();
        player.other_mob_inside.clear();
        player.other_mob_inside_update.clear();
        player.x = (short) clamp(x, 24, Math.max(48, targetMap.mapW * 24 - 24));
        player.y = (short) clamp(y, 24, Math.max(48, targetMap.mapH * 24 - 24));
        player.x_old = player.x;
        player.y_old = player.y;
        player.time_move = System.currentTimeMillis();
        sendArrivePacket(player);
    }

    private void sendRemovePacket(Map map, Player player) {
        List<Player> viewers = collectNearbyRealViewers(map, player.x, player.y, player.ID);
        if (viewers.isEmpty()) {
            return;
        }
        try {
            Message m = new Message(8);
            m.writer().writeShort(player.ID);
            for (Player viewer : viewers) {
                if (viewer.other_player_inside != null) {
                    viewer.other_player_inside.remove(player.ID);
                }
                viewer.conn.addmsg(m);
            }
            m.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendArrivePacket(Player player) {
        if (player == null || player.map == null) {
            return;
        }
        for (Player viewer : collectNearbyRealViewers(player.map, player.x, player.y, player.ID)) {
            sendPresenceToViewer(viewer, player);
        }
    }

    private void syncCombatLevel(Player player, int targetLevel) {
        if (player == null) {
            return;
        }
        int maxLevel = Math.max(1, Manager.gI().lvmax);
        int level = clamp(targetLevel, 1, maxLevel);
        player.level = (short) level;
        player.exp = 0;
        player.point1 = (short) (40 + level);
        player.point2 = (short) (40 + level);
        player.point3 = (short) (50 + level);
        player.point4 = (short) (35 + level);
        if (player.body != null) {
            player.hp = player.body.get_HpMax();
            player.mp = player.body.get_MpMax();
        }
    }

    private int calculateBotDamage(Player player, Mob_in_map mob, Role role) {
        if (player == null || mob == null) {
            return 0;
        }
        long currentHp = Math.max(1, mob.hp);
        long baseDamage;
        long pressureDamage;
        if (mob.isBoss() || role == Role.BOSS) {
            baseDamage = (long) player.level * random.nextInt(2200, 4201);
            pressureDamage = currentHp * random.nextInt(18, 41) / 1000L;
        } else {
            baseDamage = (long) player.level * random.nextInt(1400, 2801);
            pressureDamage = currentHp * random.nextInt(250, 451) / 1000L;
        }
        long damage = Math.max(baseDamage, pressureDamage);
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, damage));
    }

    private boolean sendFakeMobAttack(Player player, Mob_in_map mob) {
        if (player == null || player.map == null || mob == null || mob.isdie || mob.hp <= 0) {
            return false;
        }
        Role role = resolveRole(player);
        List<Player> viewers = collectMobAttackViewers(player, mob);
        int hpBefore = Math.max(0, mob.hp);
        int damage = calculateBotDamage(player, mob, role);
        int appliedDamage = Math.min(hpBefore, damage);
        if (appliedDamage <= 0) {
            return false;
        }
        mob.setHp(player, appliedDamage);
        if (mob.isBoss()) {
            if (!mob.top_dame.containsKey(player.name)) {
                mob.top_dame.put(player.name, (long) appliedDamage);
            } else {
                long totalDamage = appliedDamage + mob.top_dame.get(player.name);
                mob.top_dame.put(player.name, totalDamage);
            }
        }
        int hpAfter = Math.max(0, mob.hp);
        try {
            if (!viewers.isEmpty()) {
                Message m = new Message(9);
                m.writer().writeShort(player.ID);
                m.writer().writeByte(random.nextInt(0, 5));
                m.writer().writeByte(1);
                m.writer().writeShort(mob.ID);
                m.writer().writeInt(appliedDamage);
                m.writer().writeInt(hpAfter);
                m.writer().writeByte(1);
                m.writer().writeByte(random.nextInt(100) < 20 ? 4 : 0);
                m.writer().writeInt(appliedDamage);
                m.writer().writeInt(player.hp);
                m.writer().writeInt(player.mp);
                m.writer().writeByte(11);
                m.writer().writeInt(0);
                for (Player viewer : viewers) {
                    viewer.conn.addmsg(m);
                }
                m.cleanup();
            }
            recordAttackDebug(player, mob, viewers.size(), false);
            if (mob.hp <= 0 && !mob.isdie) {
                mob.SetDie(player.map, player);
                MapService.MainObj_Die(player.map, null, mob, true);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendBotChat(Player player, String text) {
        if (player == null || player.map == null || text == null || text.isEmpty()) {
            return;
        }
        List<Player> viewers = collectLoadedBotViewers(player);
        if (viewers.isEmpty()) {
            return;
        }
        try {
            Message m = new Message(27);
            m.writer().writeShort(player.ID);
            m.writer().writeByte(0);
            m.writer().writeUTF(text);
            for (Player viewer : viewers) {
                viewer.conn.addmsg(m);
            }
            m.cleanup();
            recordChatDebug(player, viewers.size(), text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String botDisplayName(int index) {
        String name = NAMES[index % NAMES.length];
        if (index >= NAMES.length) {
            name += (index / NAMES.length + 1);
        }
        if (DEFAULT_PREFIX.equalsIgnoreCase(prefix)) {
            return name;
        }
        String safePrefix = prefix.replaceAll("[^a-zA-Z0-9]", "");
        if (safePrefix.isEmpty()) {
            return name;
        }
        return (safePrefix + name);
    }

    private String botAccountName(int index) {
        return "bot_" + index + "_" + System.nanoTime();
    }

    private String normalizePrefix(String value) {
        String text = value == null ? "" : value.trim();
        return text.isEmpty() ? DEFAULT_PREFIX : text;
    }

    private String farmZoneKey(Map map) {
        if (map == null) {
            return "null";
        }
        return map.map_id + ":" + map.zone_id;
    }

    private boolean hasLoadedForViewer(Player viewer, int botId) {
        Boolean loaded = viewer.other_player_inside.get(botId);
        return loaded != null && !loaded;
    }

    private void markLoadedForViewer(Player viewer, int botId) {
        viewer.other_player_inside.put(botId, false);
    }

    private boolean hasRealViewerNearby(Player player) {
        return player != null && hasRealViewerNearby(player.map, player.x, player.y, player.ID);
    }

    private boolean hasRealViewerNearby(Map map, short x, short y, int excludedId) {
        if (map == null) {
            return false;
        }
        for (Player other : map.players) {
            if (other == null || other.ID == excludedId || other.isSquire || other.conn == null || other.conn instanceof BotSession) {
                continue;
            }
            if ((Math.abs(other.x - x) < 1500 && Math.abs(other.y - y) < 1500)
                    || Map.is_map__load_board_player(map.map_id)) {
                return true;
            }
        }
        return false;
    }

    private List<Player> collectMobAttackViewers(Player attacker, Mob_in_map mob) {
        if (attacker == null || attacker.map == null || mob == null) {
            return Collections.emptyList();
        }
        Map map = attacker.map;
        boolean boardMap = Map.is_map__load_board_player(map.map_id);
        ArrayList<Player> viewers = new ArrayList<>();
        for (Player other : map.players) {
            if (other == null || other.isSquire || other.conn == null || other.conn instanceof BotSession) {
                continue;
            }
            if (!boardMap) {
                if (Math.abs(other.x - attacker.x) >= 1500 || Math.abs(other.y - attacker.y) >= 1500) {
                    continue;
                }
                if (Math.abs(other.x - mob.x) >= MOB_ATTACK_VIEW_RANGE || Math.abs(other.y - mob.y) >= MOB_ATTACK_VIEW_RANGE) {
                    continue;
                }
                if (!other.other_mob_inside.containsKey(mob.ID)) {
                    continue;
                }
            }
            if (!hasLoadedForViewer(other, attacker.ID)) {
                continue;
            }
            viewers.add(other);
        }
        return viewers;
    }

    private List<Player> collectLoadedBotViewers(Player player) {
        if (player == null || player.map == null) {
            return Collections.emptyList();
        }
        ArrayList<Player> viewers = new ArrayList<>();
        for (Player viewer : collectNearbyRealViewers(player.map, player.x, player.y, player.ID)) {
            if (hasLoadedForViewer(viewer, player.ID)) {
                viewers.add(viewer);
            }
        }
        return viewers;
    }

    private List<Player> collectNearbyRealViewers(Map map, short x, short y, int excludedId) {
        if (map == null) {
            return Collections.emptyList();
        }
        boolean boardMap = Map.is_map__load_board_player(map.map_id);
        ArrayList<Player> viewers = new ArrayList<>();
        for (Player other : map.players) {
            if (other == null || other.ID == excludedId || other.isSquire || other.conn == null || other.conn instanceof BotSession) {
                continue;
            }
            if (!boardMap && (Math.abs(other.x - x) >= 1500 || Math.abs(other.y - y) >= 1500)) {
                continue;
            }
            viewers.add(other);
        }
        return viewers;
    }

    private void sendPresenceToViewer(Player viewer, Player botPlayer) {
        if (viewer == null || viewer.conn == null || botPlayer == null || botPlayer.map != viewer.map) {
            return;
        }
        if (hasLoadedForViewer(viewer, botPlayer.ID)) {
            return;
        }
        try {
            Message m = new Message(4);
            writePresence(m, botPlayer);
            viewer.conn.addmsg(m);
            m.cleanup();
            markLoadedForViewer(viewer, botPlayer.ID);
            sendStoreLabel(viewer, botPlayer);
            recordDirectSpawnDebug(viewer, botPlayer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isDebugMode() {
        return Manager.gI() != null && Manager.gI().debug;
    }

    private void flushDebugStatsIfNeeded() {
        if (!isDebugMode()) {
            clearDebugStats();
            return;
        }
        long now = System.currentTimeMillis();
        if (now < nextDebugFlushAt) {
            return;
        }
        ArrayList<String> lines = new ArrayList<>();
        synchronized (debugStats) {
            nextDebugFlushAt = now + DEBUG_FLUSH_INTERVAL;
            for (DebugMapStats stats : debugStats.values()) {
                if (stats.hasActivity()) {
                    lines.add(stats.summary());
                }
            }
            debugStats.clear();
        }
        for (String line : lines) {
            System.out.println(line);
        }
    }

    private void clearDebugStats() {
        synchronized (debugStats) {
            debugStats.clear();
            nextDebugFlushAt = System.currentTimeMillis() + DEBUG_FLUSH_INTERVAL;
        }
    }

    private void recordPresenceDebug(Player viewer, int sent, int farmSent, boolean onMove) {
        if (!isDebugMode() || viewer == null || viewer.map == null || sent <= 0) {
            return;
        }
        synchronized (debugStats) {
            DebugMapStats stats = debugStatsFor(viewer.map);
            stats.presenceCalls++;
            stats.presenceEntries += sent;
            stats.presenceFarmEntries += farmSent;
            if (onMove) {
                stats.movePresenceCalls++;
            } else {
                stats.loadPresenceCalls++;
            }
        }
        if (sent >= DEBUG_PRESENCE_WARN_THRESHOLD || farmSent >= MAX_VISIBLE_FARM_PER_VIEW) {
            debugWarn("presence viewer=" + viewer.name + " map=" + viewer.map.map_id + "/" + viewer.map.zone_id
                    + " sent=" + sent + " farm=" + farmSent + " onMove=" + onMove);
        }
    }

    private void recordDirectSpawnDebug(Player viewer, Player botPlayer) {
        if (!isDebugMode() || viewer == null || viewer.map == null || botPlayer == null) {
            return;
        }
        Role role = resolveRole(botPlayer);
        synchronized (debugStats) {
            DebugMapStats stats = debugStatsFor(viewer.map);
            stats.directSpawns++;
            if (role == Role.FARM) {
                stats.directFarmSpawns++;
            }
        }
    }

    private void recordMoveDebug(Player player, int loadedRecipients, int lateSpawns) {
        if (!isDebugMode() || player == null || player.map == null) {
            return;
        }
        Role role = resolveRole(player);
        synchronized (debugStats) {
            DebugMapStats stats = debugStatsFor(player.map);
            stats.moveMessages++;
            stats.moveRecipients += loadedRecipients;
            stats.moveLateSpawns += lateSpawns;
            if (role == Role.FARM) {
                stats.farmMoveMessages++;
                stats.farmMoveRecipients += loadedRecipients;
                stats.farmMoveLateSpawns += lateSpawns;
            }
        }
        if ((loadedRecipients + lateSpawns) >= DEBUG_RECIPIENT_WARN_THRESHOLD) {
            debugWarn("move role=" + roleName(role) + " bot=" + player.name + " map=" + player.map.map_id + "/" + player.map.zone_id
                    + " loaded=" + loadedRecipients + " lateSpawn=" + lateSpawns);
        }
    }

    private void recordAttackDebug(Player player, Mob_in_map mob, int recipients, boolean skippedNoViewer) {
        if (!isDebugMode() || player == null || player.map == null) {
            return;
        }
        Role role = resolveRole(player);
        synchronized (debugStats) {
            DebugMapStats stats = debugStatsFor(player.map);
            if (skippedNoViewer) {
                stats.attackSkippedNoViewer++;
                if (role == Role.FARM) {
                    stats.farmAttackSkippedNoViewer++;
                }
            } else {
                stats.attackMessages++;
                stats.attackRecipients += recipients;
                if (role == Role.FARM) {
                    stats.farmAttackMessages++;
                    stats.farmAttackRecipients += recipients;
                }
            }
        }
        if (!skippedNoViewer && recipients >= DEBUG_RECIPIENT_WARN_THRESHOLD) {
            debugWarn("attack role=" + roleName(role) + " bot=" + player.name + " map=" + player.map.map_id + "/" + player.map.zone_id
                    + " mob=" + (mob != null ? mob.ID : -1) + " recipients=" + recipients);
        }
    }

    private void recordChatDebug(Player player, int recipients, String text) {
        if (!isDebugMode() || player == null || player.map == null || recipients <= 0) {
            return;
        }
        Role role = resolveRole(player);
        synchronized (debugStats) {
            DebugMapStats stats = debugStatsFor(player.map);
            stats.chatMessages++;
            stats.chatRecipients += recipients;
            if (role == Role.FARM) {
                stats.farmChatMessages++;
                stats.farmChatRecipients += recipients;
            }
        }
        if (recipients >= DEBUG_RECIPIENT_WARN_THRESHOLD) {
            debugWarn("chat role=" + roleName(role) + " bot=" + player.name + " map=" + player.map.map_id + "/" + player.map.zone_id
                    + " recipients=" + recipients + " text=" + text);
        }
    }

    private DebugMapStats debugStatsFor(Map map) {
        String key = map.map_id + ":" + map.zone_id;
        DebugMapStats stats = debugStats.get(key);
        if (stats == null) {
            stats = new DebugMapStats(map.map_id, map.zone_id);
            debugStats.put(key, stats);
        }
        return stats;
    }

    private Role resolveRole(Player player) {
        if (player == null) {
            return null;
        }
        RealBot bot = findBot(player.ID);
        return bot != null ? bot.role : null;
    }

    private String roleName(Role role) {
        return role == null ? "UNKNOWN" : role.name();
    }

    private void debugWarn(String text) {
        if (isDebugMode()) {
            System.out.println("[BOT-DEBUG] " + text);
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private enum Role {
        FARM,
        MARKET,
        VIP,
        BOSS
    }

    private static final class RealBot {
        private final Player player;
        private final Role role;
        private final SpawnPoint spawn;
        private long nextActionAt;
        private long nextAttackAt;
        private long nextChatAt;
        private long nextLuckyJoinAt;

        private RealBot(Player player, Role role, SpawnPoint spawn) {
            this.player = player;
            this.role = role;
            this.spawn = spawn;
            long now = System.currentTimeMillis();
            this.nextActionAt = now + 1000L;
            this.nextAttackAt = now + 2000L;
            this.nextChatAt = now + 15_000L;
            this.nextLuckyJoinAt = now + 10_000L;
        }
    }

    private static final class BossTarget {
        private final Map map;
        private final Mob_in_map mob;

        private BossTarget(Map map, Mob_in_map mob) {
            this.map = map;
            this.mob = mob;
        }
    }

    private static final class SpawnPoint {
        private final Map map;
        private final short x;
        private final short y;
        private final short level;

        private SpawnPoint(Map map, short x, short y, short level) {
            this.map = map;
            this.x = x;
            this.y = y;
            this.level = level;
        }
    }

    private static final class DebugMapStats {
        private final int mapId;
        private final int zoneId;
        private long loadPresenceCalls;
        private long movePresenceCalls;
        private long presenceCalls;
        private long presenceEntries;
        private long presenceFarmEntries;
        private long directSpawns;
        private long directFarmSpawns;
        private long moveMessages;
        private long moveRecipients;
        private long moveLateSpawns;
        private long farmMoveMessages;
        private long farmMoveRecipients;
        private long farmMoveLateSpawns;
        private long attackMessages;
        private long attackRecipients;
        private long attackSkippedNoViewer;
        private long farmAttackMessages;
        private long farmAttackRecipients;
        private long farmAttackSkippedNoViewer;
        private long chatMessages;
        private long chatRecipients;
        private long farmChatMessages;
        private long farmChatRecipients;

        private DebugMapStats(int mapId, int zoneId) {
            this.mapId = mapId;
            this.zoneId = zoneId;
        }

        private boolean hasActivity() {
            return presenceCalls > 0 || directSpawns > 0 || moveMessages > 0 || attackMessages > 0
                    || attackSkippedNoViewer > 0 || chatMessages > 0;
        }

        private String summary() {
            return "[BOT-DEBUG] map=" + mapId + "/" + zoneId
                    + " presence(load/move)=" + loadPresenceCalls + "/" + movePresenceCalls
                    + " entries=" + presenceEntries + " farm=" + presenceFarmEntries
                    + " directSpawn=" + directSpawns + " farm=" + directFarmSpawns
                    + " move=" + moveMessages + "->" + moveRecipients + " late=" + moveLateSpawns
                    + " farmMove=" + farmMoveMessages + "->" + farmMoveRecipients + " late=" + farmMoveLateSpawns
                    + " attack=" + attackMessages + "->" + attackRecipients + " skip=" + attackSkippedNoViewer
                    + " farmAttack=" + farmAttackMessages + "->" + farmAttackRecipients + " skip=" + farmAttackSkippedNoViewer
                    + " chat=" + chatMessages + "->" + chatRecipients
                    + " farmChat=" + farmChatMessages + "->" + farmChatRecipients;
        }
    }

    private static final class BotSession extends Session {
        private BotSession(int id, String user) {
            super(new Socket());
            this.id = id;
            this.user = user;
            this.pass = "";
            this.ip = "bot";
            this.status = 0;
            this.version = 280;
            this.zoomlv = 2;
            this.connected = true;
            this.get_in4 = true;
            this.isLogin = false;
        }

        @Override
        public void addmsg(Message m) {
            // Bot sessions do not write to sockets. Callers clean shared messages.
        }

        @Override
        public void disconnect() {
            this.connected = false;
        }

        @Override
        public void close() {
            this.connected = false;
        }
    }
}
