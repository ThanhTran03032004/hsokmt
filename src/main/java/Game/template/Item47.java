package Game.template;

public class Item47 {
    public short id;
    public short quantity;
    public byte category;
    public long expiry;
    
    public Item47(){}
    public Item47(Item47 Origin){
        this.id = Origin.id;
        this.quantity = Origin.quantity;
        this.category = Origin.category;
    }
    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }
    public boolean isWingClan() {
        return 228 <= this.id && this.id <= 234;
    }
}
