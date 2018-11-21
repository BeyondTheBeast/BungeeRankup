package github.vankka.bungeerankup;

import java.io.*;
import java.util.concurrent.TimeUnit;
import lu.r3flexi0n.bungeeonlinetime.BungeeOnlineTime;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeRankup extends Plugin {
    private Configuration configuration;
    private final String header =
            ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + "["
            + ChatColor.GREEN + ChatColor.BOLD.toString() + "B" + ChatColor.BLACK + ChatColor.BOLD.toString() + "R"
            + ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + "]" + ChatColor.RESET + " ";

    public void onEnable() {
        if (!reloadConfiguration()) {
            getLogger().severe("Unable to load configuration");
            return;
        }

        getProxy().getScheduler().schedule(this, new Thread(this::check), 0, configuration.getLong("sync_delay"), TimeUnit.MINUTES);
        getProxy().getPluginManager().registerCommand(this, new BungeeRankupCommand());
    }

    private boolean reloadConfiguration() {
        try {
            File configFile = new File(getDataFolder(), "config.yaml");

            if (!getDataFolder().exists())
                getDataFolder().mkdir();
            if (!configFile.exists()) {
                configFile.createNewFile();
                InputStream inputStream = getResourceAsStream("config.yaml");
                OutputStream outputStream = new FileOutputStream(configFile);

                int bit;
                while ((bit = inputStream.read()) != -1)
                    outputStream.write(bit);

                inputStream.close();
                outputStream.close();
            }

            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void check() {
        try {
            long startTime = System.currentTimeMillis();

            if (ProxyServer.getInstance().getPlayers().size() == 0) {
                if (configuration.getBoolean("log"))
                    ProxyServer.getInstance().getLogger().info("No players online, not checking");
                return;
            }

            for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
                for (String rank : configuration.getSection("ranks").getKeys()) {
                    Configuration section = configuration.getSection("ranks").getSection(rank);

                    boolean proceed;

                    int posAmount = 0;
                    for (String str : section.getStringList("positivePermissions")) {
                        if (proxiedPlayer.hasPermission(str))
                            posAmount++;
                    }

                    if (section.getBoolean("requireAllPositivePermissions")) {
                        proceed = section.getStringList("positivePermissions").size() == posAmount;
                    } else {
                        proceed = posAmount > 0;
                    }

                    if (!proceed)
                        continue;

                    int negAmount = 0;
                    for (String str : section.getStringList("negativePermissions")) {
                        if (!proxiedPlayer.hasPermission(str))
                            negAmount++;
                    }

                    if (section.getBoolean("requireAllNegativePermissions")) {
                        proceed = section.getStringList("negativePermissions").size() == negAmount;
                    } else {
                        proceed = negAmount > 0;
                    }

                    if (!proceed)
                        continue;

                    int time = (int) (BungeeOnlineTime.mysql.getOnlineTime(proxiedPlayer.getUniqueId(), 0L) % 3600L / 60L);
                    if (time >= section.getInt("timeRequired")) {
                        if (configuration.getBoolean("log"))
                            ProxyServer.getInstance().getLogger().info(proxiedPlayer.getName() + " met the conditions. Time for a rankup!");

                        for (String str : section.getStringList("commands")) {
                            ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), str.replace("%name%", proxiedPlayer.getName()));
                            ProxyServer.getInstance().getLogger().info(str.replace("%name%", proxiedPlayer.getName()));
                        }
                    }
                }
            }

            if (configuration.getBoolean("log"))
                ProxyServer.getInstance().getLogger().info("Rankup check completed in; " + (System.currentTimeMillis() - startTime) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class BungeeRankupCommand extends Command {
        BungeeRankupCommand() {
            super("bungeerankup", null, "br");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (check(sender, args))
                return;
            if (reload(sender, args))
                return;
            if (help(sender, args))
                return;

            sender.sendMessage(new TextComponent(header + ChatColor.AQUA + ChatColor.BOLD.toString() + " Running version "
                                                 + ChatColor.GOLD + ChatColor.BOLD.toString() + getDescription().getVersion()));
        }

        private boolean check(CommandSender sender, String[] args) {
            if (args.length < 1)
                return false;
            if (!args[0].equalsIgnoreCase("check"))
                return false;

            if (sender.hasPermission("bungeerankup.check")) {
                BungeeRankup.this.check();
                sender.sendMessage(new TextComponent(header + ChatColor.translateAlternateColorCodes('&', configuration.getString("checkCompleted"))));
            } else {
                sender.sendMessage(new TextComponent(header + ChatColor.translateAlternateColorCodes('&', configuration.getString("noPermissionMessage"))));
            }

            return true;
        }

        private boolean reload(CommandSender sender, String[] args) {
            if (args.length < 1)
                return false;
            if (!args[0].equalsIgnoreCase("reload"))
                return false;

            if (sender.hasPermission("bungeerankup.reload"))
                if (reloadConfiguration())
                    sender.sendMessage(new TextComponent(header + ChatColor.translateAlternateColorCodes('&', configuration.getString("configReloadSuccessMessage"))));
                else
                    sender.sendMessage(new TextComponent(header + ChatColor.translateAlternateColorCodes('&', configuration.getString("configReloadFailedMessage"))));
            else
                sender.sendMessage(new TextComponent(header + ChatColor.translateAlternateColorCodes('&', configuration.getString("noPermissionMessage"))));
            return true;
        }

        private boolean help(CommandSender sender, String[] args) {
            if (args.length < 1)
                return false;
            if (!args[0].equalsIgnoreCase("help"))
                return false;

            sender.sendMessage(new TextComponent(header + ChatColor.AQUA + ChatColor.BOLD.toString() + " /br check " + ChatColor.RED + "Runs rankup check"));
            sender.sendMessage(new TextComponent(header + ChatColor.AQUA + ChatColor.BOLD.toString() + " /br reload " + ChatColor.RED + "Reload the configuration"));
            return true;
        }
    }
}
