
package Game.template;


public class BoxItem {
    public short id;
    public short quantity;
    public byte catagory;
    public BoxItem(short id, short quant, byte cat){
        this.id= id;
        this.quantity =quant;
        this.catagory = cat;
    }
}
