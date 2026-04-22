package Game.NPC;


import java.util.ArrayList;
import java.util.List;

public class NpcTemplate {
    public static final List<NpcTemplate> npcTemplates = new ArrayList<>();
    public String name;
    public String name_gt;
    public String infoObject;
    public byte ID_Image;
    public byte wBlock;
    public byte hBlock;
    public byte nFrame;
    public byte IdBigAvatar;
    public byte isPerson;
    public byte isShowHP;

    public short id;

    public static NpcTemplate getNpcById(short id) {
        for (NpcTemplate npcTemplate : npcTemplates) {
            if (id == npcTemplate.id) {
                return npcTemplate;
            }
        }
        return null;
    }
}
