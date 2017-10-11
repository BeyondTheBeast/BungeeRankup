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

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeRankup extends Plugin
{
	private static BungeeRankup instance;
	private Configuration config;
	private ScheduledTask task;
	
	public void onEnable() {		
		instance = this;
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
		} catch (IOException e) {
			ProxyServer.getInstance().getLogger().warning("Unable to create configuration!");
			e.printStackTrace();
		}
		
		startScheduler();
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new BRCommand("br"));
	}
	
	public void startScheduler(){
		task = ProxyServer.getInstance().getScheduler().schedule(this, new Timer(), config.getLong("sync_delay"), TimeUnit.MINUTES);
	}
	
	public void stopScheduler(){
		ProxyServer.getInstance().getScheduler().cancel(task);
	}
	
	public void check(){
		try {
			if(ProxyServer.getInstance().getPlayers().size() == 0){
				if(config.getBoolean("log")) ProxyServer.getInstance().getLogger().info("No players online, not checking");
				return;
			}
			if(config.getBoolean("log")) ProxyServer.getInstance().getLogger().info(" -- Starting rankup check -- ");
			
			if(ProxyServer.getInstance().getPlayers().size() != 0){
				for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()){
					for(String s : config.getSection("ranks").getKeys()){
						Configuration sec = config.getSection("ranks").getSection(s);

						int i = BungeeOnlineTime.mysql.getOnlineTime(p.getUniqueId());
							
						boolean proceed = false;
						int posAmount = 0;
						int negAmount = 0;
						for(String str : sec.getStringList("postivePermissions")){
							if(p.hasPermission(str)) posAmount++;
						}
						
						for(String str : sec.getStringList("negativePermissions")){
							if(!p.hasPermission(str)) negAmount++;
						}
						
						if(sec.getBoolean("requireAllPositivePermissions")){
							if(sec.getStringList("positivePermissions").size() == posAmount){
								proceed = true;
							}else{
								proceed = false;
							}
						}else{
							if(posAmount > 0){
								proceed = true;
							}else{
								proceed = false;
							}
						}
						
						if(sec.getBoolean("requireAllNegativePermissions")){
							if(sec.getStringList("negativePermissions").size() == negAmount){
								proceed = true;
							}else{
								proceed = false;
							}
						}else{
							if(negAmount > 0){
								proceed = true;
							}else{
								proceed = false;
							}
						}
						
						if(!proceed) continue;
						
						if(i >= sec.getDouble("timeRequired")){
							if(config.getBoolean("log")) ProxyServer.getInstance().getLogger().info(p.getName() + " met the conditions. Time for a rankup!");
							for(String str : sec.getStringList("commands")){
								ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), str.replace("%name%", p.getName()));
								ProxyServer.getInstance().getLogger().info(str.replace("%name%", p.getName()));
							}
						}
					}
				}
			}
			
			if(config.getBoolean("log")) ProxyServer.getInstance().getLogger().info(" -- Ended rankup check -- ");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static BungeeRankup getInstance(){ return instance; }
	public Configuration getConfig(){ return config; }
}
