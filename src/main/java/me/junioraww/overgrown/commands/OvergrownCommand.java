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
      sender.sendMessage("§cИспользование: /overgrown world <add|remove|list> [мир]");
      return true;
    }

    if (args[0].equalsIgnoreCase("world")) {
      if (args.length < 2) {
        sender.sendMessage("§cУкажите действие: add, remove, list");
        return true;
      }

      String action = args[1].toLowerCase();
      List<String> worldsList = plugin.getConfig().getStringList("whitelisted-worlds");

      switch (action) {
        case "list":
          sender.sendMessage("§aМиры в вайтлисте: §f" + String.join(", ", Config.getWhitelistedWorlds()));
          break;

        case "add":
          if (args.length < 3) {
            sender.sendMessage("§cУкажите название мира.");
            return true;
          }
          String worldToAdd = args[2];
          if (Config.getWhitelistedWorlds().add(worldToAdd)) {
            worldsList.add(worldToAdd);
            plugin.getConfig().set("whitelisted-worlds", worldsList);
            plugin.saveConfig();
            sender.sendMessage("§aМир " + worldToAdd + " добавлен в вайтлист.");
          } else {
            sender.sendMessage("§cЭтот мир уже в вайтлисте.");
          }
          break;

        case "remove":
          if (args.length < 3) {
            sender.sendMessage("§cУкажите название мира.");
            return true;
          }
          String worldToRemove = args[2];
          if (Config.getWhitelistedWorlds().remove(worldToRemove)) {
            worldsList.remove(worldToRemove);
            plugin.getConfig().set("whitelisted-worlds", worldsList);
            plugin.saveConfig();
            sender.sendMessage("§aМир " + worldToRemove + " удален из вайтлиста.");
          } else {
            sender.sendMessage("§cЭтого мира нет в вайтлисте.");
          }
          break;

        default:
          sender.sendMessage("§cНеизвестное действие.");
          break;
      }
      return true;
    }

    return false;
  }
}