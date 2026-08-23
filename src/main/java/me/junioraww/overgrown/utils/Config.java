package me.junioraww.overgrown.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Config {
  private static int minY;
  private static long intervalMillis;
  private static int blocksPerCycle;
  private static int maxCatchupCycles;

  private static Map<Material, Double> growthChances = new HashMap<>();
  private static Set<Material> targetBlocks = new HashSet<>();
  private static Map<Material, Material> replaceBlocks = new HashMap<>();
  public static Set<String> whitelistedWorlds = new HashSet<>();

  private static NamespacedKey lastUpdateKey;

  public static long getIntervalMillis() {
    return intervalMillis;
  }

  public static int getBlocksPerCycle() {
    return blocksPerCycle;
  }

  public static int getMaxCatchupCycles() {
    return maxCatchupCycles;
  }

  public static int getMinY() {
    return minY;
  }

  public static Map<Material, Double> getGrowthChances() {
    return growthChances;
  }

  public static Map<Material, Material> getReplaceBlocks() {
    return replaceBlocks;
  }

  public static Set<Material> getTargetBlocks() {
    return targetBlocks;
  }

  public static Set<String> getWhitelistedWorlds() {
    return whitelistedWorlds;
  }

  public static NamespacedKey getLastUpdateKey() {
    return lastUpdateKey;
  }

  public static void setBlocksPerCycle(int value) {
    blocksPerCycle = value;
  }

  public static void setIntervalMillis(long value) {
    intervalMillis = value;
  }

  public static void setMaxCatchupCycles(int value) {
    maxCatchupCycles = value;
  }

  public static void setMinY(int value) {
    minY = value;
  }

  public static void setGrowthChances(Map<Material, Double> growthChances) {
    Config.growthChances = growthChances;
  }

  public static void setReplaceBlocks(Map<Material, Material> replaceBlocks) {
    Config.replaceBlocks = replaceBlocks;
  }

  public static void setTargetBlocks(Set<Material> targetBlocks) {
    Config.targetBlocks = targetBlocks;
  }

  public static void setWhitelistedWorlds(Set<String> whitelistedWorlds) {
    Config.whitelistedWorlds = whitelistedWorlds;
  }

  public static void setLastUpdateKey(NamespacedKey lastUpdateKey) {
    Config.lastUpdateKey = lastUpdateKey;
  }
}
