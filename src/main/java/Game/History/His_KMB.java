
package Game.History;

import Game.template.Item3;
import Game.template.Item47;
import Game.core.SQL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class His_KMB {
    public String namePSell;
    public String namePBuy;
    public long lastGoldSell;
    public long lastGoldBuy;
    
    public long affterGoldSell;
    public long affterGoldBuy;
    public Item3 tem3;
    public Item47 tem47;
    
    public His_KMB(String namesell, String namebuy, long lastGoldsell, long lastGoldbuy){
        this.namePSell = namesell;
        this.namePBuy = namebuy;
        this.lastGoldSell = lastGoldsell;
        this.lastGoldBuy = lastGoldbuy;
    }
    
    public void UpdateGold(long lastGoldsell, long lastGoldbuy){
        this.affterGoldSell = lastGoldsell;
        this.affterGoldBuy = lastGoldbuy;
    }
    
    public void Flus(){
        String it3 = "[]";
        String it47 = "[]";
        if(tem3!=null)
            it3 = "["+tem3.id+","+tem3.name+","+tem3.tier+","+tem3.tierStar+","+tem3.color+"]";
        if(tem47 != null)
            it47 = "["+tem47.id+","+tem47.quantity+","+tem47.category+"]";
        String query
                = "INSERT INTO `history_kmb2` (`name_player_sell`, `name_player_buy`, `last_gold_sell` , `affter_gold_sell`, `last_gold_buy`"
                + ", `affter_gold_buy`, `item3`, `item47`) VALUES ('"
                + this.namePSell + "', '" + this.namePBuy + "', '" + this.lastGoldSell + "', '" + this.affterGoldSell + "', '" + this.lastGoldBuy + 
                "', '" + this.affterGoldBuy + "', '" + it3 + "', '" + it47  + "')";
        try ( Connection connection = SQL.gI().getConnection();  Statement statement = connection.createStatement();) {
            if (statement.executeUpdate(query) > 0) {
                connection.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
