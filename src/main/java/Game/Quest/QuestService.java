package Game.Quest;

import Game.client.Player;
import Game.core.Service;
import Game.io.Message;
import Game.io.Session;

import java.io.IOException;

public class QuestService {
    public static void sendQuestList(Session conn) {
        try {
            Player p = conn.p;
            Message msg = new Message(52);
            msg.writer().writeByte(0);
            msg.writer().writeByte(p.quests.size()); // b2
            for (int i = 0; i < p.quests.size(); i++) {
                QuestTemplate quest = p.quests.get(i);
                msg.writer().writeShort(quest.ID);
                msg.writer().writeBoolean(quest.isMain);
                msg.writer().writeUTF(quest.name);
                msg.writer().writeByte(quest.idNPC_From);
                msg.writer().writeUTF(quest.strDetailTalk);
                msg.writer().writeByte(quest.typeItem);
                msg.writer().writeUTF(quest.strDetailHelp);
            }
            conn.addmsg(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendQuestFinish(Session conn) {
        try {
            Player p = conn.p;
            Message msg = new Message(52);
            msg.writer().writeByte(1);
            msg.writer().writeByte(p.questsFinish.size()); // b2
            for (int i = 0; i < p.questsFinish.size(); i++) {
                QuestTemplate quest = p.questsFinish.get(i);
                if (quest != null) {
                    if (quest.strDetailTalkFinish.equals("") || quest.strDetailHelpFinish.equals("")) continue;
                    msg.writer().writeShort(quest.ID);
                    msg.writer().writeBoolean(quest.isMain);
                    msg.writer().writeUTF(quest.name);
                    msg.writer().writeByte(quest.idNPC_To);
                    msg.writer().writeUTF(quest.strDetailTalkFinish);
                    msg.writer().writeUTF(quest.strDetailHelpFinish);
                }
            }
            conn.addmsg(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendQuestDoing(Session conn) {
        try {
            Player p = conn.p;
            Message msg = new Message(52);
            msg.writer().writeByte(2);
            msg.writer().writeByte(p.questsDoing.size()); // b2
            for (int i = 0; i < p.questsDoing.size(); i++) {
                QuestTemplate quest = p.questsDoing.get(i);
                if (quest != null) {
                    msg.writer().writeShort(quest.ID);
                    msg.writer().writeBoolean(quest.isMain);
                    msg.writer().writeUTF(quest.name);
                    msg.writer().writeByte(quest.typeItem); // typeQuest
                    msg.writer().writeUTF(quest.strDetailHelp);
                    msg.writer().writeUTF(quest.strDetailHelp);
                    msg.writer().writeByte(quest.idNPC_To);
                    if (quest.typeItem == 0) { // Nhặt item
                        msg.writer().writeByte(quest.arrQuest.length);
                        for (int j = 0; j < quest.arrQuest.length; j++) {
                            msg.writer().writeShort(quest.arrQuest[j][0]); // 0 là id, 1 là đã làm được, 2 là số lượng cần
                            msg.writer().writeShort(quest.arrQuest[j][1]);
                            msg.writer().writeShort(quest.arrQuest[j][2]);
                        }
                    } else if (quest.typeItem == 1) { // không nhặt item
                        msg.writer().writeByte(quest.arrQuest.length);
                        for (int j = 0; j < quest.arrQuest.length; j++) {
                            msg.writer().writeShort(quest.arrQuest[j][0]);
                            msg.writer().writeShort(quest.arrQuest[j][1]);
                            msg.writer().writeShort(quest.arrQuest[j][2]);
                        }
                    } else if (quest.typeItem == 2) { // nói chuyện
                        msg.writer().writeByte(quest.idNPC_To); // typeQuest
                        msg.writer().writeUTF(quest.strShortDetail);
                    }
                }
            }
            conn.addmsg(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void receiveQuest(Session conn, short taskId, byte type, short[][] info) throws IOException {
        QuestTemplate quest = new QuestTemplate(taskId, -8, false, "Tiêu diệt quái vật", type, info);
        quest.strDetailTalk = "Quái vật";
        quest.strDetailHelp = "Quái vật help";
        conn.p.questsDoing.add(quest);
        sendQuestDoing(conn);
    }

    public static void receiveQuest(Session conn, short taskId, byte main_sub) {
        try {
            QuestTemplate quest = QuestTemplate.getQuest(conn.p.quests, taskId, true);
            if (quest != null) {
                if (quest.typeItem == 2) {
                    conn.p.questsFinish.add(quest);
                } else {
                    conn.p.questsDoing.add(quest);
                }
                conn.p.removeQuest(conn.p.quests, taskId, true);

                sendQuestFinish(conn);
                sendQuestDoing(conn);
                sendQuestList(conn);
                Service.send_notice_nobox_white(conn, "Đã nhận nhiệm vụ " + quest.name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void finishQuest(Session conn, short taskId, byte main_sub) {
        try {
            QuestTemplate questTemplate = QuestTemplate.getQuest(QuestTemplate.questMains, taskId + 1, true);
            if (questTemplate != null) {
                conn.p.quests.add(questTemplate);

                conn.p.removeQuest(conn.p.questsFinish, taskId, true);
                sendQuestFinish(conn);
                sendQuestList(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cancelQuest(Session conn, short taskId, byte main_sub) {
        try {
            QuestTemplate quest = QuestTemplate.getQuest(QuestTemplate.questMains, taskId, true);
            conn.p.removeQuest(conn.p.questsDoing, taskId, true);
            conn.p.quests.add(quest);

            sendQuestFinish(conn);
            sendQuestDoing(conn);
            sendQuestList(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
