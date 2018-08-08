package eu.beyondthebeast.bungeerankup;

import java.util.Arrays;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class BungeeRankupCommand extends Command {
    BungeeRankupCommand() {
        super("bungeerankup", null, "br");
    }

    @Override
    public void execute(CommandSender sender, String[] strs) {
        List<String> strings = Arrays.asList(strs);

        if (strings.size() == 1) {
            if (sender.hasPermission("bungeerankup.start") && strings.get(0).equalsIgnoreCase("start")) {
                BungeeRankup.getInstance().startScheduler();
                sender.sendMessage(new TextComponent("Started."));
            } else if (sender.hasPermission("bungeerankup.stop") && strings.get(0).equalsIgnoreCase("stop")) {
                BungeeRankup.getInstance().stopScheduler();
                sender.sendMessage(new TextComponent("Stopped."));
            } else if (sender.hasPermission("bungeerankup.restart") && strings.get(0).equalsIgnoreCase("restart")) {
                BungeeRankup.getInstance().stopScheduler();
                BungeeRankup.getInstance().startScheduler();
                sender.sendMessage(new TextComponent("Restarted."));
            } else if (sender.hasPermission("bungeerankup.check") && strings.get(0).equalsIgnoreCase("check")) {
                BungeeRankup.getInstance().check();
                sender.sendMessage(new TextComponent("Checked."));
            } else if (sender.hasPermission("bungeerankup.reload") && strings.get(0).equalsIgnoreCase("reload")) {
                BungeeRankup.getInstance().reloadConfig();
                sender.sendMessage(new TextComponent("Config reloaded"));
            } else if (strings.get(0).equalsIgnoreCase("help")) {
                sender.sendMessage(
                        new TextComponent(
                                ChatColor.GOLD + "BungeeRankup commands:\n" +
                                ChatColor.AQUA + "/br start " + ChatColor.RED + "Starts the timer\n" +
                                ChatColor.AQUA + "/br stop " + ChatColor.RED + "Stops the timer\n" +
                                ChatColor.AQUA + "/br restart " + ChatColor.RED + "Restarts the timer\n" +
                                ChatColor.AQUA + "/br check " + ChatColor.RED + "Checks, ignoring the timer\n" +
                                ChatColor.AQUA + "/br reload " + ChatColor.RED + "Reloads the config\n" +
                                ChatColor.AQUA + "/br help " + ChatColor.RED + "This list"
                        )
                );
            }
        } else {
            sender.sendMessage(new TextComponent(
                    ChatColor.AQUA + "This server is running BungeeRankup " + ChatColor.RED + "1.0\n" +
                    ChatColor.GOLD + "/br help for the list of commands"));
        }
    }
}
