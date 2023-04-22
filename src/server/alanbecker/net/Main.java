package server.alanbecker.net;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
    }

    public static class ChatListener implements Listener {

        private final Map<UUID, Long> playerChatTimestamps = new HashMap<>();
        private final Map<UUID, Long> playerCommandTimestamps = new HashMap<>();
        private final Map<UUID, String> playerLastMessages = new HashMap<>();
        private static final int CHAT_COOLDOWN = 2000; // 2000 milliseconds or 2 seconds
        private static final int COMMAND_COOLDOWN = 2000; // 2000 milliseconds or 2 seconds

        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();

            if (!player.hasPermission("abmcchat.bypass")) {
                if (playerChatTimestamps.containsKey(playerId)) {
                    long elapsedTime = currentTime - playerChatTimestamps.get(playerId);
                    if (elapsedTime < CHAT_COOLDOWN) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Please wait " + (CHAT_COOLDOWN - elapsedTime) / 1000 + " seconds before sending another message.");
                        return;
                    }
                }
                playerChatTimestamps.put(playerId, currentTime);

                String message = event.getMessage();

                // Check for repeated messages
                if (playerLastMessages.containsKey(playerId) && playerLastMessages.get(playerId).equalsIgnoreCase(message)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot send the same message twice.");
                    return;
                }
                playerLastMessages.put(playerId, message);

                String filteredMessage = filterProfanity(message);
                TextComponent textComponent = new TextComponent(filteredMessage);
                event.setMessage(textComponent.toLegacyText());
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();

            if (!player.hasPermission("abmcchat.bypass")) {
                if (playerCommandTimestamps.containsKey(playerId)) {
                    long elapsedTime = currentTime - playerCommandTimestamps.get(playerId);
                    if (elapsedTime < COMMAND_COOLDOWN) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Please wait " + (COMMAND_COOLDOWN - elapsedTime) / 1000 + " seconds before sending another command.");
                        return;
                    }
                }
                playerCommandTimestamps.put(playerId, currentTime);
            }
        }

        private String filterProfanity(String message) {
            String[][] swearWordsAndPatterns = {
                    {"f+(\\W|\\d|_)*u+(\\W|\\d|_)*c+(\\W|\\d|_)*k+"},
                    {"p+(\\W|\\d|_)*u+(\\W|\\d|_)*s+(\\W|\\d|_)*s+(\\W|\\d|_)*y+"},
                    {"n+(\\W|\\d|_)*i+(\\W|\\d|_)*g+(\\W|\\d|_)*g+(\\W|\\d|_)*e+(\\W|\\d|_)*r+"},
                    {"p+(\\W|\\d|_)*o+(\\W|\\d|_)*r+(\\W|\\d|_)*n+(\\W|\\d|_)*"},
                    {"f+(\\W|\\d|_)*a+(\\W|\\d|_)*g+(\\W|\\d|_)*g+(\\W|\\d|_)*o+(\\W|\\d|_)*t+"},
                    {"n+(\\W|\\d|_)*i+(\\W|\\d|_)*g+(\\W|\\d|_)*g+(\\W|\\d|_)*a+"},
                    {"b+(\\W|\\d|_)*i+(\\W|\\d|_)*t+(\\W|\\d|_)*c+(\\W|\\d|_)*h+"},
                    {"p+(\\W|\\d|_)*e+(\\W|\\d|_)*n+(\\W|\\d|_)*i+(\\W|\\d|_)*s+"},
                    {"c+(\\W|\\d|_)*o+(\\W|\\d|_)*c+(\\W|\\d|_)*k+"},
                    {"a+(\\W|\\d|_)*s+(\\W|\\d|_)*s+(\\W|\\d|_)*h+(\\W|\\d|_)*o+(\\W|\\d|_)*l+(\\W|\\d|_)*e+"},
                    {"s+(\\W|\\d|_)*h+(\\W|\\d|_)*i+(\\W|\\d|_)*t+"}
            };

            for (String[] swearAndPattern : swearWordsAndPatterns) {
                String pattern = "(?i)" + swearAndPattern[0];
                Matcher matcher = Pattern.compile(pattern).matcher(message);
                StringBuffer result = new StringBuffer();

                while (matcher.find()) {
                    String replacement = repeatString("*", matcher.group().length());
                    matcher.appendReplacement(result, replacement);
                }

                matcher.appendTail(result);
                message = result.toString();
            }

            return message;
        }





        private String repeatString(String str, int times) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < times; i++) {
                builder.append(str);
            }
            return builder.toString();
        }
    }
}
