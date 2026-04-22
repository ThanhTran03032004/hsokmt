package Game.template;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemTemplate7 {
	public static final List<ItemTemplate7> item = new ArrayList<>();
	private short id;
	private short icon;
	private long price;
	private String name;
	private String content;
	private byte type;
	private byte pricetype;
	private byte sell;
	private short value;
	private byte trade;
	private byte color;
	public static String get_name_by_id(short id) {
		for (int i = 0; i < item.size(); i++) {
			if (id == item.get(i).getId()) {
				return item.get(i).getName();
			}
		}
		return "";
	}
}
