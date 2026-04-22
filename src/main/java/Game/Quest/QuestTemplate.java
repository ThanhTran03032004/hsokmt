package Game.Quest;

import java.util.ArrayList;
import java.util.List;

public class QuestTemplate {
    public static List<QuestTemplate> questMains = new ArrayList<>();
    public int ID;
    public int idNPC_To;
    public int idNPC_From;
    public boolean isMain;
    public String name;
    public String strShortDetail;
    public String strDetailTalk;
    public String strDetailTalkFinish;
    public String strDetailHelpFinish;
    public String strDetailHelp;
    public short[][] arrQuest;
    public byte typeQuest;
    public byte typeItem;

    public QuestTemplate(int ID, int idNPC_To, boolean isMain, String name, byte typeItem, short[][] arrQuest) {
        this.ID = ID;
        this.idNPC_To = idNPC_To;
        this.isMain = isMain;
        this.name = name;
        this.typeItem = typeItem;
        this.arrQuest = arrQuest;
    }

    public QuestTemplate() {

    }

    public static QuestTemplate getQuest(List<QuestTemplate> quests, int id, boolean isMain) {
        for (QuestTemplate quest : quests) {
            if (quest.ID == id && isMain == quest.isMain) {
                return quest;
            }
        }
        return null;
    }
}
