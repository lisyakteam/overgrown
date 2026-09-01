package me.junioraww.overgrown.commands;

import me.junioraww.overgrown.Main;
import me.junioraww.overgrown.utils.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class OvergrownCommand implements CommandExecutor {
  private final Main plugin;

  public OvergrownCommand(Main plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      sender.sendRichMessage("<red>Usage: /overgrown world <add|remove|list> [world]");
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
}