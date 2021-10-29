package com.github.ukraine1449.claimplugin;

import com.github.ukraine1449.claimplugin.Events.playerJoinEvent;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;

public final class ClaimPlugin extends JavaPlugin {
ArrayList<Location> cache = new ArrayList<Location>();
    @Override
    public void onEnable() {
        try {
            createTableClaims();
            createTableUserdata();
        } catch (Exception e) {
            e.printStackTrace();
        }

        getServer().getPluginManager().registerEvents(new playerJoinEvent(this), this);


        getConfig().options().copyDefaults();
        saveDefaultConfig();
        //getCommand("profileset").setExecutor(new description(this));


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }



    public Connection getConnection() throws Exception{
        String ip = getConfig().getString("ip");
        String password = getConfig().getString("password");
        String username = getConfig().getString("username");
        String dbn = getConfig().getString("database name");
        try{
            String driver = "com.mysql.jdbc.Driver";
            String url = "jdbc:mysql://"+ ip + ":3306/" + dbn;
            System.out.println(url);
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected");
            return conn;
        }catch(Exception e){}
        return null;
    }

    public void createTableClaims()throws Exception{
        try{
            Connection con = getConnection();
            PreparedStatement create = con.prepareStatement("CREATE TABLE IF NOT EXISTS claimData(CID varchar(255), world varchar(255), x1 int,x2 int,z1 int,z2 int,UUID varchar(255), name varchar(255), PRIMARY KEY (CID))");
            create.executeUpdate();

        }catch(Exception e){}
    }
    public void createTableUserdata()throws Exception{
        try{
            Connection con = getConnection();
            PreparedStatement create = con.prepareStatement("CREATE TABLE IF NOT EXISTS userClaimData(UUID varchar(255), claimMax BIGINT, totalClaims int, PRIMARY KEY (UUID))");
            create.executeUpdate();

        }catch(Exception e){}
    }
    public void postUD(String UUID, int updateOrNew, long newLimit, int oldClaims) throws Exception{

        // 0 is create new, 1 is update claim limit of player, 2 is update player claims total.

        if(updateOrNew ==0){
            try{
                Connection con = getConnection();
                PreparedStatement posted = con.prepareStatement("INSERT INTO userClaimData(UUID, claimMax, totalClaims) VALUES ('"+UUID+", "+getConfig().getString("maxClaimStart")+", 0')");
                posted.executeUpdate();
            }catch(Exception e){}
        }else if (updateOrNew == 1){
            try{
                Connection con = getConnection();
                PreparedStatement posted = con.prepareStatement("UPDATE userClaimData SET claimMax="+newLimit+" WHERE UUID="+UUID+"");
                posted.executeUpdate();
            }catch(Exception e){}
        }else{
            int newClaims = oldClaims+1;
            try{
                Connection con = getConnection();
                PreparedStatement posted = con.prepareStatement("UPDATE userClaimData SET totalClaims="+newClaims+" WHERE UUID="+UUID+"");
                posted.executeUpdate();
            }catch(Exception e){}
        }
    }
    public void postCD(String UUID, int updateOrNew, String CID, String world, int x1, int x2, int z1, int z2, String newUUID, String name, String newName, int oldClaims) throws Exception{
        if(updateOrNew == 0){
            try{
                Connection con = getConnection();
                PreparedStatement posted = con.prepareStatement("INSERT INTO claimData(CID, world, x1, x2, z1, z2 UUID, name) VALUES ('"+CID+", "+world+", "+x1+", "+x2+","+z1+", "+z2+", "+UUID+", "+name+"')");
                posted.executeUpdate();
            }catch(Exception e){}
            for(int i = 0; i< cache.size(); i++){
                cache.remove(i);
            }
        }else if(updateOrNew == 1){
            try{
                Connection con = getConnection();
                PreparedStatement posted = con.prepareStatement("UPDATE claimData SET UUID="+newUUID+" WHERE UUID="+UUID+"");
                posted.executeUpdate();
            }catch(Exception e){}
        }else if(updateOrNew == 2){
            try{
                Connection con = getConnection();
                PreparedStatement posted = con.prepareStatement("UPDATE claimData SET name="+newName+" WHERE UUID="+UUID+" AND name="+name+"");
                posted.executeUpdate();
            }catch(Exception e){}
        }
        else{
            for(int i = 0; i< cache.size(); i++){
                cache.remove(i);
            }
            int newClaims = oldClaims-1;
            try{
                Connection con = getConnection();
                PreparedStatement posted = con.prepareStatement("DELETE FROM claimData WHERE UUID="+UUID+" AND CN="+name+"");
                posted.executeUpdate();
            }catch(Exception e){}
            try{
                Connection con = getConnection();
                PreparedStatement posted = con.prepareStatement("UPDATE userClaimData SET totalClaims="+newClaims+" WHERE UUID="+UUID+"");
                posted.executeUpdate();
            }catch(Exception e){}
        }

    }
    public long selectUD(String UUID, int getWhat) throws Exception {
        long tbr = 0;
        if(getWhat == 0){
            Connection con = getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT claimMax FROM userStats WHERE UUID="+UUID+"");
            ResultSet result = statement.executeQuery();
            while(result.next()){
                tbr = result.getLong("claimMax");
            }
        }
        else if (getWhat == 1){
            Connection con = getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT totalClaims FROM userStats WHERE UUID="+UUID+"");
            ResultSet result = statement.executeQuery();
            while(result.next()){
                tbr = result.getLong("totalClaims");
            }
        }return tbr;
    }
    public ArrayList<Integer> selectCD(int x1, int x2, int z1, int z2, String world, String CID) throws Exception {
            ArrayList<Integer> coords = new ArrayList<Integer>();
            Connection con = getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT x1,x2,z1,z2 FROM userStats WHERE CID="+CID+" AND world="+world+"");
            ResultSet result = statement.executeQuery();
            int x11 = 0;
            int x22 = 0;
            int z11 = 0;
            int z22 = 0;
                x11 = result.getInt("x1");
                coords.add(x11);
                x22 = result.getInt("x2");
                coords.add(x22);
                z11 = result.getInt("z1");
                coords.add(z11);
                z22 = result.getInt("z2");
                coords.add(z22);
                return coords;
        }
}