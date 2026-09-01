package me.junioraww.overgrown.listeners;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.junioraww.overgrown.Main;
import me.junioraww.overgrown.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkEvents implements Listener {
  private final Map<String, ScheduledTask> activeChunkTasks = new ConcurrentHashMap<>();

  public void stop() {
    activeChunkTasks.values().forEach(ScheduledTask::cancel);
    activeChunkTasks.clear();
  }

  @EventHandler
  public void onChunkLoad(ChunkLoadEvent event) {
    if (event.isNewChunk()) return;
    Chunk chunk = event.getChunk();
    if (!Config.getWhitelistedWorlds().contains(chunk.getWorld().getName())) return;

    long now = System.currentTimeMillis();
    Long lastTime = chunk.getPersistentDataContainer().get(Config.getLastUpdateKey(), PersistentDataType.LONG);

    if (lastTime == null) {
      chunk.getPersistentDataContainer().set(Config.getLastUpdateKey(), PersistentDataType.LONG, now);
    } else {
      long elapsed = now - lastTime;
      long cyclesToRun = elapsed / Config.getIntervalMillis();

      if (cyclesToRun > 0) {
        long actualCycles = Math.min(cyclesToRun, Config.getMaxCatchupCycles());
        Main.getPlugin().processOvergrowth(chunk, (int) (actualCycles * Config.getBlocksPerCycle()));

        long newTime = lastTime + (cyclesToRun * Config.getIntervalMillis());
        chunk.getPersistentDataContainer().set(Config.getLastUpdateKey(), PersistentDataType.LONG, newTime);
      }
    }

    startActiveChunkTask(chunk);
  }

  @EventHandler
  public void onChunkUnload(ChunkUnloadEvent event) {
    Chunk chunk = event.getChunk();
    String chunkId = getChunkId(chunk);
    ScheduledTask task = activeChunkTasks.remove(chunkId);
    if (task != null) task.cancel();
  }

  private void startActiveChunkTask(Chunk chunk) {
    String chunkId = getChunkId(chunk);
    long intervalTicks = Config.getIntervalMillis() / 50L;

    ScheduledTask task = Bukkit.getRegionScheduler().runAtFixedRate(Main.getPlugin(), chunk.getWorld(), chunk.getX(), chunk.getZ(),
            scheduledTask -> {
              if (!Config.getWhitelistedWorlds().contains(chunk.getWorld().getName())) {
                scheduledTask.cancel();
                activeChunkTasks.remove(chunkId);
                return;
              }
              Main.getPlugin().processOvergrowth(chunk, Config.getBlocksPerCycle());
              chunk.getPersistentDataContainer().set(Config.getLastUpdateKey(), PersistentDataType.LONG, System.currentTimeMillis());
            },
            intervalTicks, intervalTicks
    );

    activeChunkTasks.put(chunkId, task);
  }

  private String getChunkId(Chunk chunk) {
    return chunk.getWorld().getUID() + "_" + chunk.getX() + "_" + chunk.getZ();
  }
}
