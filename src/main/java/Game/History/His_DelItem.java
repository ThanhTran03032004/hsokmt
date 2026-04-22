
package Game.History;

import Game.template.Item3;
import Game.template.Item47;
import Game.core.SQL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class His_DelItem {
    public String namePlayer;
    public Item3 tem3;
    public Item47 tem47;
    public String Logger;
    public His_DelItem(String name){
        this.namePlayer = name;
    }
    
    public void Flus(){
        String it3 = "[]";
        String it47 = "[]";
        if(tem3!=null)
            it3 = "["+tem3.id+","+tem3.name+","+tem3.tier+","+tem3.tierStar+","+tem3.color+"]";
        if(tem47 != null)
            it47 = "["+tem47.id+","+tem47.quantity+","+tem47.category+"]";
        String query
                = "INSERT INTO `history_del_item` (`name_player`, `item3`, `item47` , `logger`) VALUES ('"
                + this.namePlayer + "', '"  + it3 + "', '" + it47  + "', '"  + this.Logger +  "')";
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
