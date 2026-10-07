package me.junioraww.overgrown.commands;

import me.junioraww.overgrown.Main;
import me.junioraww.overgrown.utils.Config;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class OvergrownCommand implements CommandExecutor {
  private final Main plugin;

  public OvergrownCommand(Main plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      sender.sendRichMessage("<gold>Overgrown Commands:<newline>"
              + "<yellow>/overgrown world <add|remove|list> [world]<newline>"
              + "<yellow>/overgrown reload<newline>"
              + "<yellow>/overgrown info");
      return true;
    }

    if (args[0].equalsIgnoreCase("reload")) {
      plugin.loadConfiguration();
      sender.sendRichMessage("<green>Configuration reloaded successfully!<newline>"
              + "<gray>Inactivity threshold: <white>" + Config.getInactivityDays() + "d "
              + Config.getInactivityHours() + "h (" + (Config.getInactivityMillis() / 1000L) + "s)");
      return true;
    }

    if (args[0].equalsIgnoreCase("info")) {
      if (!(sender instanceof Player player)) {
        sender.sendRichMessage("<red>This command can only be executed by a player in a chunk.");
        return true;
      }

      Chunk chunk = player.getLocation().getChunk();
      Long lastActivity = plugin.getActivityManager().getLastActivity(chunk);
      boolean whitelisted = Config.getWhitelistedWorlds().contains(chunk.getWorld().getName());

      sender.sendRichMessage("<gold>--- Chunk Status [" + chunk.getX() + ", " + chunk.getZ() + "] in " + chunk.getWorld().getName() + " ---");
      sender.sendRichMessage("<gray>World whitelisted: " + (whitelisted ? "<green>Yes" : "<red>No"));

      if (lastActivity == null) {
        sender.sendRichMessage("<gray>Last player activity: <white>None (untouched)");
        sender.sendRichMessage("<gray>Overgrowth status: <green>Eligible (Untouched)");
      } else {
        long now = System.currentTimeMillis();
        long elapsed = now - lastActivity;
        long remaining = Config.getInactivityMillis() - elapsed;

        sender.sendRichMessage("<gray>Last player activity: <white>" + formatDuration(elapsed) + " ago");
        if (remaining > 0) {
          sender.sendRichMessage("<gray>Overgrowth status: <red>Protected (resumes in " + formatDuration(remaining) + ")");
        } else {
          sender.sendRichMessage("<gray>Overgrowth status: <green>Eligible (Inactivity threshold passed)");
        }
      }
      return true;
    }

    if (args[0].equalsIgnoreCase("world")) {
      if (args.length < 2) {
        sender.sendRichMessage("<red>Specify an action: add, remove, list");
        return true;
      }

      String action = args[1].toLowerCase();
      List<String> worldsList = plugin.getConfig().getStringList("whitelisted-worlds");

      switch (action) {
        case "list":
          sender.sendRichMessage("<green>Whitelisted worlds: <white>" + String.join(", ", Config.getWhitelistedWorlds()));
          break;

        case "add":
          if (args.length < 3) {
            sender.sendRichMessage("<red>Specify a world name.");
            return true;
          }

          String worldToAdd = args[2];
          if (Config.getWhitelistedWorlds().add(worldToAdd)) {
            worldsList.add(worldToAdd);
            plugin.getConfig().set("whitelisted-worlds", worldsList);
            plugin.saveConfig();
            sender.sendRichMessage("<green>World " + worldToAdd + " has been added to the whitelist.");
          } else {
            sender.sendRichMessage("<red>This world is already in the whitelist.");
          }
          break;

        case "remove":
          if (args.length < 3) {
            sender.sendRichMessage("<red>Specify a world name.");
            return true;
          }

          String worldToRemove = args[2];
          if (Config.getWhitelistedWorlds().remove(worldToRemove)) {
            worldsList.remove(worldToRemove);
            plugin.getConfig().set("whitelisted-worlds", worldsList);
            plugin.saveConfig();
            sender.sendRichMessage("<green>World " + worldToRemove + " has been removed from the whitelist.");
          } else sender.sendRichMessage("<red>This world is not in the whitelist.");
          break;

        default:
          sender.sendRichMessage("<red>Unknown action.");
          break;
      }
      return true;
    }

    return false;
  }

  private String formatDuration(long millis) {
    long seconds = millis / 1000L;
    long days = seconds / 86400L;
    long hours = (seconds % 86400L) / 3600L;
    long minutes = (seconds % 3600L) / 60L;
    long secs = seconds % 60L;

    if (days > 0) return days + "d " + hours + "h " + minutes + "m";
    if (hours > 0) return hours + "h " + minutes + "m " + secs + "s";
    if (minutes > 0) return minutes + "m " + secs + "s";
    return secs + "s";
  }
}
