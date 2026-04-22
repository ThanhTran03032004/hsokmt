package Game.template;

public class OptionPet {
	public int param;
	public int maxdame;
	public byte id;

	public OptionPet(int id, int param, int maxdame) {
		this.param = param;
		this.maxdame = maxdame;
		this.id = (byte) id;
	}
}
