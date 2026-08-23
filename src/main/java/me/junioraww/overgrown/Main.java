package me.junioraww.overgrown;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.junioraww.overgrown.commands.OvergrownCommand;
import me.junioraww.overgrown.listeners.ChunkEvents;
import me.junioraww.overgrown.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Main extends JavaPlugin {
  private static Main plugin;
  private ChunkEvents chunkEvents;

  private final Random random = new Random();
  private final BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

  public static Main getPlugin() {
    return plugin;
  }

  @Override
  public void onEnable() {
    plugin = this;

    saveDefaultConfig();
    loadConfiguration();
    Config.setLastUpdateKey(
            new NamespacedKey(this, "last_growth_time")
    );

    chunkEvents = new ChunkEvents();
    getServer().getPluginManager().registerEvents(chunkEvents, this);
    getCommand("overgrown").setExecutor(new OvergrownCommand(this));
  }

  @Override
  public void onDisable() {
    plugin = null;
    chunkEvents.stop();
  }

  public void loadConfiguration() {
    reloadConfig();
    Config.setMinY(getConfig().getInt("settings.min-y", 40));
    Config.setIntervalMillis(getConfig().getLong("settings.growth-interval-seconds", 60) * 1000L);
    Config.setBlocksPerCycle(getConfig().getInt("settings.blocks-per-cycle", 10));
    Config.setMaxCatchupCycles(getConfig().getInt("settings.max-catchup-cycles", 100));

    Config.getWhitelistedWorlds().clear();
    Config.getWhitelistedWorlds().addAll(getConfig().getStringList("whitelisted-worlds"));

    Config.getGrowthChances().clear();
    if (getConfig().contains("chances")) {
      for (String key : getConfig().getConfigurationSection("chances").getKeys(false)) {
        Material mat = Material.matchMaterial(key);
        if (mat != null) Config.getGrowthChances().put(mat, getConfig().getDouble("chances." + key));
      }
    }

    Config.getTargetBlocks().clear();
    for (String key : getConfig().getStringList("target-blocks")) {
      if (key.startsWith("#")) {
        String tagName = key.substring(1).replace("minecraft:", "");
        Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(tagName), Material.class);
        if (tag != null) Config.getTargetBlocks().addAll(tag.getValues());
      } else {
        Material mat = Material.matchMaterial(key);
        if (mat != null) Config.getTargetBlocks().add(mat);
      }
    }

    Config.getReplaceBlocks().clear();
    if (getConfig().contains("replace-blocks")) {
      for (String key : getConfig().getConfigurationSection("replace-blocks").getKeys(false)) {
        Material from = Material.matchMaterial(key);
        Material to = Material.matchMaterial(getConfig().getString("replace-blocks." + key));
        if (from != null && to != null) Config.getReplaceBlocks().put(from, to);
      }
    }
  }

  public void processOvergrowth(Chunk chunk, int totalBlocksToCheck) {
    if (totalBlocksToCheck <= 0) return;

    for (int i = 0; i < totalBlocksToCheck; i++) {
      int x = random.nextInt(16);
      int z = random.nextInt(16);

      int highestY = chunk.getWorld().getHighestBlockYAt(chunk.getX() * 16 + x, chunk.getZ() * 16 + z);
      if (highestY < Config.getMinY()) continue;

      int y = Config.getMinY() + random.nextInt(Math.max(1, highestY - Config.getMinY() + 1));
      Block block = chunk.getBlock(x, y, z);
      Material type = block.getType();

      if (Config.getTargetBlocks().contains(type)) {
        if (Config.getReplaceBlocks().containsKey(type) && random.nextBoolean()) {
          block.setType(Config.getReplaceBlocks().get(type), false);
          continue;
        }

        BlockFace randomFace = faces[random.nextInt(faces.length)];
        Block adjacent = block.getRelative(randomFace);

        if (adjacent.getType().isAir()) {
          Material toPlace = getRandomGrowthMaterial();
          if (toPlace != null) {
            BlockData blockData = toPlace.createBlockData();

            // Обработка листвы (запрещаем гнить)
            if (blockData instanceof Leaves leaves) {
              leaves.setPersistent(true);
            }
            // Обработка лиан и других блоков, зависящих от стороны (приклеиваем к блоку)
            else if (blockData instanceof MultipleFacing facing) {
              BlockFace attachFace = randomFace.getOppositeFace();
              // Проверяем, может ли этот блок существовать на этой стороне
              if (facing.getAllowedFaces().contains(attachFace)) {
                facing.setFace(attachFace, true);
              } else {
                continue; // Пропускаем попытку (например, лианы не ставятся на пол)
              }
            }

            adjacent.setBlockData(blockData, false);
          }
        }
      }
    }
  }

  private Material getRandomGrowthMaterial() {
    double rand = random.nextDouble() * 100.0;
    double current = 0;
    for (Map.Entry<Material, Double> entry : Config.getGrowthChances().entrySet()) {
      current += entry.getValue();
      if (rand <= current) return entry.getKey();
    }
    return null;
  }
}