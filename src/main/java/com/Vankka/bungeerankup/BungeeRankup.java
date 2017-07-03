package com.Vankka.bungeerankup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.google.common.io.ByteStreams;

import lu.r3flexi0n.bungeeonlinetime.BungeeOnlineTime;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.exceptions.ObjectAlreadyHasException;
import me.lucko.luckperms.exceptions.ObjectLacksException;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

@SuppressWarnings("deprecation")
public class BungeeRankup extends Plugin
{
	public static Configuration config;
	
	public void onEnable() {		
		File configFile = new File(getDataFolder(), "config.yml");
		
		try {
			if (!getDataFolder().exists()) getDataFolder().mkdir();
			if (!configFile.exists()) {
				ProxyServer.getInstance().getLogger().info("Config not found, creating!");
				configFile.createNewFile();
				InputStream in = getResourceAsStream("config.yml");
				OutputStream out = new FileOutputStream(configFile);
				ByteStreams.copy(in, out);				
			} else {
				ProxyServer.getInstance().getLogger().info("Config found, loading!");
			}
			
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile); 
			
			switch(config.getInt("configVersion")){
				default:
					ProxyServer.getInstance().getLogger().info("Configuration is up-to-date, no migration required!");
			}
			
		} catch (IOException e) {
			ProxyServer.getInstance().getLogger().warning("Unable to create configuration!");
			e.printStackTrace();
		}
		
		
		startScheduler();
	}
	
	private void startScheduler(){
		ProxyServer.getInstance().getScheduler().schedule(this, new Runnable() {
			public void run() {
				try {
					if(ProxyServer.getInstance().getPlayers().size() == 0) return;
					if(config.getBoolean("log")) ProxyServer.getInstance().getLogger().info(" -- Starting rankup check -- ");
					
					LuckPermsApi lpapi = null;
					if(LuckPerms.getApiSafe().isPresent()) lpapi = LuckPerms.getApiSafe().get();
					if(lpapi == null){
						ProxyServer.getInstance().getLogger().warning("\n\nLuckPerms API is not present!\n");
						return;
					}
					for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()){
						int i = BungeeOnlineTime.mysql.getOnlineTime(p.getUniqueId());
						User user = null;
						if(lpapi.getUserSafe(p.getUniqueId()).isPresent()) user = lpapi.getUserSafe(p.getUniqueId()).get();
						if(user == null){
							ProxyServer.getInstance().getLogger().warning("\n\nLuckPerms: player not present!\n");
							return;
						}
						
						for(String s : config.getSection("ranks").getKeys()){
							Configuration sec = config.getSection("ranks").getSection(s);
							
							boolean hasGroup = false;
							for(String str : lpapi.getUser(p.getUniqueId()).getGroupNames()){
								if(str.equalsIgnoreCase(sec.getString("rank_from"))) hasGroup = true;
							}
							if(!hasGroup) return;
							
							if(i >= sec.getDouble("time_required") && hasGroup){
								user.addGroup(lpapi.getGroup(sec.getString("rank_to")));
								user.setPrimaryGroup(sec.getString("rank_to"));
								user.removeGroup(lpapi.getGroup(sec.getString("rank_from")));
								lpapi.getStorage().saveUser(user);
								if(config.getBoolean("log")) ProxyServer.getInstance().getLogger().info(p.getName() + " ranked up to " + sec.getString("rank_to") + " from " + sec.getString("rank_from"));
							}
						}
						
						if(config.getBoolean("log")) ProxyServer.getInstance().getLogger().info(" -- Ended rankup check -- ");
					}
				} catch (ClassNotFoundException | SQLException | ObjectAlreadyHasException | ObjectLacksException e) {
					e.printStackTrace();
				}
			}
		}, config.getLong("sync_delay"), TimeUnit.MINUTES);
	}
}
