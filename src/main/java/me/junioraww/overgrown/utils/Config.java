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
  private static boolean recoveryEnabled;

  private static int inactivityDays = 3;
  private static int inactivityHours = 0;
  private static long inactivityMillis = 3 * 24L * 60 * 60 * 1000L;
  private static boolean resetOnBlockPlace = true;
  private static boolean resetOnBlockBreak = true;
  private static boolean resetOnChestInteract = true;
  private static boolean resetOnChestOpen = false;
  private static boolean resetOnDoorOpen = false;

  private static final Map<Material, Double> growthChances = new HashMap<>();
  private static final Set<Material> targetBlocks = new HashSet<>();
  private static final Map<Material, Material> replaceBlocks = new HashMap<>();
  public static Set<String> whitelistedWorlds = new HashSet<>();

  private static NamespacedKey lastUpdateKey;
  private static NamespacedKey lastActivityKey;

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

  public static boolean isRecoveryEnabled() {
    return recoveryEnabled;
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

  public static NamespacedKey getLastActivityKey() {
    return lastActivityKey;
  }

  public static int getInactivityDays() {
    return inactivityDays;
  }

  public static int getInactivityHours() {
    return inactivityHours;
  }

  public static long getInactivityMillis() {
    return inactivityMillis;
  }

  public static boolean isResetOnBlockPlace() {
    return resetOnBlockPlace;
  }

  public static boolean isResetOnBlockBreak() {
    return resetOnBlockBreak;
  }

  public static boolean isResetOnChestInteract() {
    return resetOnChestInteract;
  }

  public static boolean isResetOnChestOpen() {
    return resetOnChestOpen;
  }

  public static boolean isResetOnDoorOpen() {
    return resetOnDoorOpen;
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

  public static void setLastUpdateKey(NamespacedKey lastUpdateKey) {
    Config.lastUpdateKey = lastUpdateKey;
  }

  public static void setLastActivityKey(NamespacedKey lastActivityKey) {
    Config.lastActivityKey = lastActivityKey;
  }

  public static void setInactivityDuration(int days, int hours) {
    inactivityDays = Math.max(0, days);
    inactivityHours = Math.max(0, hours);
    inactivityMillis = (inactivityDays * 24L + inactivityHours) * 3600L * 1000L;
  }

  public static void setResetOnBlockPlace(boolean value) {
    resetOnBlockPlace = value;
  }

  public static void setResetOnBlockBreak(boolean value) {
    resetOnBlockBreak = value;
  }

  public static void setResetOnChestInteract(boolean value) {
    resetOnChestInteract = value;
  }

  public static void setResetOnChestOpen(boolean value) {
    resetOnChestOpen = value;
  }

  public static void setResetOnDoorOpen(boolean value) {
    resetOnDoorOpen = value;
  }

  public static void setRecoveryEnabled(boolean value) {
    Config.recoveryEnabled = value;
  }
}
