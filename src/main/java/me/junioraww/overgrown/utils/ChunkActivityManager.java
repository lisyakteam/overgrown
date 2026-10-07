package me.junioraww.overgrown.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkActivityManager {
  private static final long THROTTLE_MILLIS = 5000L;
  private final Map<ChunkCoord, Long> activityThrottleCache = new ConcurrentHashMap<>();

  public record ChunkCoord(UUID worldUid, int x, int z) {}

  public void recordActivity(Chunk chunk) {
    if (chunk == null) return;
    if (!Config.getWhitelistedWorlds().contains(chunk.getWorld().getName())) return;

    long now = System.currentTimeMillis();
    ChunkCoord coord = new ChunkCoord(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());

    Long lastRecorded = activityThrottleCache.get(coord);
    if (lastRecorded != null && (now - lastRecorded) < THROTTLE_MILLIS) {
      return;
    }

    activityThrottleCache.put(coord, now);
    chunk.getPersistentDataContainer().set(Config.getLastActivityKey(), PersistentDataType.LONG, now);
  }

  public void recordChestActivity(InventoryHolder holder, Location loc) {
    if (holder instanceof DoubleChest doubleChest) {
      if (doubleChest.getLeftSide() instanceof Chest left) {
        recordActivity(left.getLocation().getChunk());
      }
      if (doubleChest.getRightSide() instanceof Chest right) {
        recordActivity(right.getLocation().getChunk());
      }
    } else if (loc != null && loc.getWorld() != null) {
      recordActivity(loc.getChunk());
    }
  }

  public boolean isChunkProtectedByActivity(Chunk chunk, long now) {
    if (chunk == null) return false;
    long inactivityMillis = Config.getInactivityMillis();
    if (inactivityMillis <= 0) return false;

    Long lastActivity = getLastActivity(chunk);
    if (lastActivity == null) return false;

    return (now - lastActivity) < inactivityMillis;
  }

  public Long getLastActivity(Chunk chunk) {
    if (chunk == null) return null;
    return chunk.getPersistentDataContainer().get(Config.getLastActivityKey(), PersistentDataType.LONG);
  }

  public void clearChunkCache(UUID worldUid, int x, int z) {
    activityThrottleCache.remove(new ChunkCoord(worldUid, x, z));
  }

  public void clearAllCache() {
    activityThrottleCache.clear();
  }
}
