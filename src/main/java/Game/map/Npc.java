package Game.map;

import java.io.IOException;
import Game.client.Player;
import Game.io.Message;

public class Npc {
	public static void chat(Map map, String txt, int id) throws IOException {
		Message m = new Message(23);
		m.writer().writeUTF(txt);
		m.writer().writeByte(id);
		for (int j = 0; j < map.players.size(); j++) {
			Player p0 = map.players.get(j);
			if (p0 != null && p0.map.equals(map)) {
				p0.conn.addmsg(m);
			}
		}
		m.cleanup();
	}
}
