package server.alanbecker.net;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.command.Command;
import server.alanbecker.net.Main.ChatListener;

public class SwearVisibility implements CommandExecutor {
    private Main plugin;

    public SwearVisibility(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("abmc.swear")) {
                ChatListener chatListener = HandlerList.getRegisteredListeners(plugin).stream()
                        .filter(registeredListener -> registeredListener.getListener() instanceof ChatListener)
                        .map(RegisteredListener::getListener)
                        .map(listener -> (ChatListener) listener)
                        .findFirst()
                        .orElse(null);

                if (chatListener != null) {
                    chatListener.toggleSwearingVisibility(player);
                    if (chatListener.playersAllowedSwearing.contains(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED+ "Viewer discretion is advised!");
                    } else {
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Error: ChatListener not found.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
        }
        return true;
    }
}
