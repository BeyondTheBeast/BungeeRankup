package com.Vankka.bungeerankup;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import lu.r3flexi0n.bungeeonlinetime.BungeeOnlineTime;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public class BRCommand extends Command
{	
	public BRCommand(String name) {
		super(name);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender commandSender, String[] strs) {
	    List<String> strings = Arrays.asList(strs);
	    
	    if(strings.size() == 1){
		    if(commandSender.hasPermission("bungeerankup.start") && strings.get(0).equalsIgnoreCase("start")){
		    	BungeeRankup.getInstance().startScheduler();
		    	commandSender.sendMessage("Started.");
		    }
		    
		    if(commandSender.hasPermission("bungeerankup.stop") && strings.get(0).equalsIgnoreCase("stop")){
		    	BungeeRankup.getInstance().stopScheduler();
		    	commandSender.sendMessage("Stopped.");
		    }
		    
		    if(commandSender.hasPermission("bungeerankup.restart") && strings.get(0).equalsIgnoreCase("restart")){
		    	BungeeRankup.getInstance().stopScheduler();
		    	BungeeRankup.getInstance().startScheduler();
		    	commandSender.sendMessage("Restarted.");
		    }
		    
		    if(commandSender.hasPermission("bungeerankup.check") && strings.get(0).equalsIgnoreCase("check")){
		    	BungeeRankup.getInstance().check();
		    	commandSender.sendMessage("Checked.");
		    }
		    
		    if(strings.get(0).equalsIgnoreCase("help")){
		    	commandSender.sendMessage(
		    			ChatColor.GOLD + "BungeeRankup commands:\n" +
		    			ChatColor.AQUA + "/br start " + ChatColor.RED + "Starts the timer\n" +
		    			ChatColor.AQUA + "/br stop " + ChatColor.RED + "Stops the timer\n" +
		    			ChatColor.AQUA + "/br restart " + ChatColor.RED + "Restarts the timer\n" +
		    			ChatColor.AQUA + "/br check " + ChatColor.RED + "Checks, ignoring the timer\n" +
		    			ChatColor.AQUA + "/br time <name> " + ChatColor.RED + "Checks player\'s playtime\n" +
		    			ChatColor.AQUA + "/br help " + ChatColor.RED + "This list");
		    }
	    }else if(strings.size() == 2){
	    	if(commandSender.hasPermission("bungeerankup.time") && strings.get(0).equalsIgnoreCase("time")){
		    	try {
		    		if(ProxyServer.getInstance().getPlayer(strings.get(1)) != null){
		    			if(ProxyServer.getInstance().getPlayer(strings.get(1)).isConnected()){
			    			commandSender.sendMessage(strings.get(1) + "'s playtime is: " + BungeeOnlineTime.mysql.getOnlineTime(ProxyServer.getInstance().getPlayer(strings.get(1)).getUniqueId()));
			    		}else{
			    			commandSender.sendMessage("Player not online.");
			    		}
		    		}else{
		    			commandSender.sendMessage("Player not online.");
		    		}
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}
		    }
	    }else{
	    	commandSender.sendMessage(
	    			ChatColor.AQUA + "This server is running BungeeRankup " + ChatColor.RED + "1.0\n" +
	    		    ChatColor.GOLD + "/br help for the list of commands");
	    }
	}
}
