package me.junioraww.overgrown.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigMigratorTest {

  @Test
  public void testMigrateV1ToV2PreservesExistingAndAddsNewValues(@TempDir Path tempDir) throws Exception {
    String legacyConfigYaml = """
        settings:
          min-y: 75
          growth-interval-seconds: 120
          blocks-per-cycle: 5
          max-catchup-cycles: 50
        whitelisted-worlds:
          - "my_custom_world"
        chances:
          OAK_LEAVES: 60
          VINE: 40
        target-blocks:
          - "#minecraft:planks"
          - "minecraft: cobblestone"
        replace-blocks:
          STONE_BRICKS: CRACKED_STONE_BRICKS
        restoration:
          enabled: false
        """;

    File testConfigFile = tempDir.resolve("config.yml").toFile();
    YamlConfiguration userConfig = new YamlConfiguration();
    userConfig.loadFromString(legacyConfigYaml);
    userConfig.save(testConfigFile);

    InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml");
    assertNotNull(in, "Default config.yml resource should exist in classpath");

    YamlConfiguration defaultConfig = new YamlConfiguration();
    try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      defaultConfig.load(reader);
    }

    Method migrateMethod = ConfigMigrator.class.getDeclaredMethod("migrateV1ToV2", YamlConfiguration.class, YamlConfiguration.class);
    migrateMethod.setAccessible(true);
    migrateMethod.invoke(null, userConfig, defaultConfig);
    userConfig.set("config-version", 2);

    assertEquals(75, userConfig.getInt("settings.min-y"));
    assertEquals(120, userConfig.getLong("settings.growth-interval-seconds"));
    assertEquals(List.of("my_custom_world"), userConfig.getStringList("whitelisted-worlds"));
    assertFalse(userConfig.getBoolean("restoration.enabled"));

    List<String> targetBlocks = userConfig.getStringList("target-blocks");
    assertTrue(targetBlocks.contains("minecraft:cobblestone"));
    assertFalse(targetBlocks.contains("minecraft: cobblestone"));

    assertTrue(userConfig.contains("inactivity"));
    assertEquals(3, userConfig.getInt("inactivity.days"));
    assertEquals(0, userConfig.getInt("inactivity.hours"));
    assertTrue(userConfig.getBoolean("inactivity.reset-actions.block-place"));
    assertTrue(userConfig.getBoolean("inactivity.reset-actions.block-break"));
    assertTrue(userConfig.getBoolean("inactivity.reset-actions.chest-interact"));
    assertFalse(userConfig.getBoolean("inactivity.reset-actions.chest-open"));
    assertFalse(userConfig.getBoolean("inactivity.reset-actions.door-open"));

    assertEquals(2, userConfig.getInt("config-version"));
  }
}
