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
                    {"\\b(f+(\\W|\\d|_)*u+(\\W|\\d|_)*c+(\\W|\\d|_)*k+(\\W|\\d|_)*)"},
                    {"\\b(p+(\\W|\\d|_)*u+(\\W|\\d|_)*s+(\\W|\\d|_)*s+(\\W|\\d|_)*y+(\\W|\\d|_)*)"},
                    {"\\b(n+(\\W|\\d|_)*i+(\\W|\\d|_)*g+(\\W|\\d|_)*g+(\\W|\\d|_)*e+(\\W|\\d|_)*r+(\\W|\\d|_)*)"},
                    {"\\b(p+(\\W|\\d|_)*o+(\\W|\\d|_)*r+(\\W|\\d|_)*n+(\\W|\\d|_)*)\\b"},
                    {"\\b(f+(\\W|\\d|_)*a+(\\W|\\d|_)*g+(\\W|\\d|_)*g+(\\W|\\d|_)*o+(\\W|\\d|_)*t+(\\W|\\d|_)*)"},
                    {"\\b(n+(\\W|\\d|_)*i+(\\W|\\d|_)*g+(\\W|\\d|_)*g+(\\W|\\d|_)*a+(\\W|\\d|_)*)"},
                    {"\\b(b+(\\W|\\d|_)*i+(\\W|\\d|_)*t+(\\W|\\d|_)*c+(\\W|\\d|_)*h+(\\W|\\d|_)*)"},
                    {"\\b(p+(\\W|\\d|_)*e+(\\W|\\d|_)*n+(\\W|\\d|_)*i+(\\W|\\d|_)*s+(\\W|\\d|_)*)"},
                    {"\\b(c+(\\W|\\d|_)*o+(\\W|\\d|_)*c+(\\W|\\d|_)*k+(\\W|\\d|_)*)"},
                    {"\\b(a+(\\W|\\d|_)*s+(\\W|\\d|_)*s+(\\W|\\d|_)*h+(\\W|\\d|_)*o+(\\W|\\d|_)*l+(\\W|\\d|_)*e+(\\W|\\d|_)*)"},
                    {"\\b(s+(\\W|\\d|_)*h+(\\W|\\d|_)*i+(\\W|\\d|_)*t+(\\W|\\d|_)*)"}
            };

            for (String[] swearAndPattern : swearWordsAndPatterns) {
                String pattern = swearAndPattern[0];
                Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(message);
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

//release