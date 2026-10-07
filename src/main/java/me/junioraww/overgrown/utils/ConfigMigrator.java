package me.junioraww.overgrown.utils;

import me.junioraww.overgrown.Main;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ConfigMigrator {
  public static final int CURRENT_VERSION = 2;

  public static void migrate(Main plugin) {
    File configFile = new File(plugin.getDataFolder(), "config.yml");
    if (!configFile.exists()) {
      return;
    }

    YamlConfiguration fileConfig = YamlConfiguration.loadConfiguration(configFile);
    int version = fileConfig.getInt("config-version", 1);

    if (version >= CURRENT_VERSION) {
      return;
    }

    plugin.getLogger().info("Detected outdated config.yml (version " + version + "). Starting migration to version " + CURRENT_VERSION + "...");

    createBackup(plugin, configFile, version);

    InputStream defaultStream = plugin.getResource("config.yml");
    if (defaultStream == null) {
      plugin.getLogger().warning("Could not find default config.yml in jar resources. Aborting migration.");
      return;
    }

    try (Reader reader = new InputStreamReader(defaultStream, StandardCharsets.UTF_8)) {
      YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);

      if (version < 2) {
        migrateV1ToV2(fileConfig, defaultConfig);
      }

      fileConfig.set("config-version", CURRENT_VERSION);
      fileConfig.setComments("config-version", List.of("Configuration version. Do not modify manually."));

      fileConfig.save(configFile);
      plugin.getLogger().info("config.yml successfully migrated to version " + CURRENT_VERSION + "!");
    } catch (Exception e) {
      plugin.getLogger().severe("Failed to migrate config.yml: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void migrateV1ToV2(YamlConfiguration fileConfig, YamlConfiguration defaultConfig) {
    if (fileConfig.contains("target-blocks")) {
      List<String> targetBlocks = fileConfig.getStringList("target-blocks");
      List<String> updatedBlocks = new ArrayList<>();
      boolean modified = false;

      for (String block : targetBlocks) {
        if ("minecraft: cobblestone".equals(block)) {
          updatedBlocks.add("minecraft:cobblestone");
          modified = true;
        } else {
          updatedBlocks.add(block);
        }
      }

      if (modified) {
        fileConfig.set("target-blocks", updatedBlocks);
      }
    }

    for (String key : defaultConfig.getKeys(true)) {
      if (!fileConfig.contains(key)) {
        fileConfig.set(key, defaultConfig.get(key));
      }

      List<String> comments = defaultConfig.getComments(key);
      if (comments != null && !comments.isEmpty() && fileConfig.getComments(key).isEmpty()) {
        fileConfig.setComments(key, comments);
      }

      List<String> inlineComments = defaultConfig.getInlineComments(key);
      if (inlineComments != null && !inlineComments.isEmpty() && fileConfig.getInlineComments(key).isEmpty()) {
        fileConfig.setInlineComments(key, inlineComments);
      }
    }
  }

  private static void createBackup(Main plugin, File configFile, int oldVersion) {
    try {
      File backupFile = new File(plugin.getDataFolder(), "config.yml.v" + oldVersion + ".bak");
      Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      plugin.getLogger().info("Safety backup of old configuration created at: " + backupFile.getName());
    } catch (Exception e) {
      plugin.getLogger().warning("Could not create backup of config.yml: " + e.getMessage());
    }
  }
}
