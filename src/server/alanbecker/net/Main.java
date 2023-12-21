package server.alanbecker.net;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getCommand("abmcswear").setExecutor(new SwearVisibility(this));
        getCommand("abmcclear").setExecutor(new ChatClear(this));


    }

    public class ChatListener implements Listener {
    	

        private final Map<UUID, Long> playerChatTimestamps = new HashMap<>();
        private final Map<UUID, String> playerLastMessages = new HashMap<>();
        private static final int CHAT_COOLDOWN = 2000; // 2000 milliseconds or 2 seconds
        private static final int COMMAND_COOLDOWN = 2000; // 2000 milliseconds or 2 seconds

        private final Map<UUID, Long> playerCommandTimestamps = new HashMap<>();
        protected final HashSet<UUID> playersAllowedSwearing = new HashSet<>();
        

        public void toggleSwearingVisibility(Player player) {
            UUID playerId = player.getUniqueId();
            if (playersAllowedSwearing.contains(playerId)) {
                playersAllowedSwearing.remove(playerId);
                player.sendMessage(ChatColor.RED + "Swearing visibility toggled off. You will no longer see swear words.");
            } else {
                playersAllowedSwearing.add(playerId);
                player.sendMessage(ChatColor.GREEN + "Swearing visibility toggled on.");
            }
        }
        
        private String filterHardcodedProfanity(String message) {
            String[] hardcodedProfanities = {
                "n+(\\W|\\d|_)*i+(\\W|\\d|_)*g+(\\W|\\d|_)*g+(\\W|\\d|_)*e+(\\W|\\d|_)*r+",
                "n+(\\\\W|\\\\d|_)*i+(\\\\W|\\\\d|_)*g+(\\\\W|\\\\d|_)*g+(\\\\W|\\\\d|_)*a+",
                "f+(\\W|\\d|_)*u+(\\W|\\d|_)*c+(\\W|\\d|_)*k+",
                "p+(\\W|\\d|_)*o+(\\W|\\d|_)*r+(\\W|\\d|_)*n+(\\W|\\d|_)*",
                "f+(\\W|\\d|_)*a+(\\W|\\d|_)*g+(\\W|\\d|_)*g+(\\W|\\d|_)*o+(\\W|\\d|_)*t+",
                "\\\\b(n+(\\\\W|\\\\d|_)*i+(\\\\W|\\\\d|_)*b+(\\\\W|\\\\d|_)*b+(\\\\W|\\\\d|_)*a+(\\\\W|\\\\d|_)*)",
                
            };

            for (String profanity : hardcodedProfanities) {
                String pattern = "(?i)" + profanity;
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


        @EventHandler(priority = EventPriority.LOWEST)
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

                // Filter the message for all players
                String filteredMessage = filterProfanity(event.getMessage());
                String alwaysFilteredMessage = filterHardcodedProfanity(event.getMessage());

                Set<Player> recipientsToRemove = new HashSet<>();
                for (Player recipient : event.getRecipients()) {
                    UUID recipientId = recipient.getUniqueId();
                    if (!playersAllowedSwearing.contains(recipientId)) {
                        // Apply both filters for players who have swearing visibility off
                        String combinedFilteredMessage = filterHardcodedProfanity(filteredMessage);
                        recipient.sendMessage(String.format(event.getFormat(), event.getPlayer().getDisplayName(), combinedFilteredMessage));
                        recipientsToRemove.add(recipient);
                    } else {
                        // Apply only the hardcoded filter for players who have swearing visibility on
                        recipient.sendMessage(String.format(event.getFormat(), event.getPlayer().getDisplayName(), alwaysFilteredMessage));
                        recipientsToRemove.add(recipient);
                    }
                }
                event.getRecipients().removeAll(recipientsToRemove);
                event.setCancelled(true);
            }
        }




        @EventHandler(priority = EventPriority.LOWEST)
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
                    {"\\b(h+(\\W|\\d|_)*o+(\\W|\\d|_)*r+(\\W|\\d|_)*n+(\\W|\\d|_)*y+(\\W|\\d|_)*)"},
                    {"\\b(r+(\\W|\\d|_)*a+(\\W|\\d|_)*p+(\\W|\\d|_)*e+(\\W|\\d|_)*)\\b"},
                    {"\\b(r+(\\W|\\d|_)*a+(\\W|\\d|_)*p+(\\W|\\d|_)*i+(\\W|\\d|_)*s+(\\W|\\d|_)*t+(\\W|\\d|_)*)"},
                    {"\\b(c+(\\W|\\d|_)*u+(\\W|\\d|_)*n+(\\W|\\d|_)*t+(\\W|\\d|_)*)"},
                    {"\\b(a+(\\W|\\d|_)*n+(\\W|\\d|_)*a+(\\W|\\d|_)*l+(\\W|\\d|_)*)\\b"},
                    {"\\b(n+(\\W|\\d|_)*i+(\\W|\\d|_)*b+(\\W|\\d|_)*b+(\\W|\\d|_)*a+(\\W|\\d|_)*)"},
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