package server.alanbecker.net;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ChatClear implements CommandExecutor {
    private final Plugin plugin;

    public ChatClear(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("abmcswear.clear")) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                for (int i = 0; i < 150; i++) {
                    player.sendMessage("");
                }
            }
            plugin.getServer().broadcastMessage(ChatColor.RED + "Excuse us while we make the chat squeaky clean! Chat has been cleared by an administrator.");
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return false;
        }
    }
}
//This is just a test