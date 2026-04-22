package Game.template;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemTemplate4 {
	public static final List<ItemTemplate4> item = new ArrayList<>();
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
}
