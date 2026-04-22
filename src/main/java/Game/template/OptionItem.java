package Game.template;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OptionItem {
	public static final List<OptionItem> entry = new ArrayList<>();
	private String name;
	private byte color;
	private byte ispercent;
}
