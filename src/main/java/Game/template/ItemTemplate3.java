package Game.template;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemTemplate3 {
	public static final List<ItemTemplate3> item = new ArrayList<>();
	private short id;
	private String name;
	private byte type;
	private byte part;
	private byte clazz;
	private short icon;
	private short level;
	private List<Option> op;
	private byte color;
}
