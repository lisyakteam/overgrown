package me.junioraww.overgrown.listeners;

import me.junioraww.overgrown.utils.ChunkActivityManager;
import me.junioraww.overgrown.utils.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PlayerActivityEvents implements Listener {
  private final ChunkActivityManager activityManager;

  public PlayerActivityEvents(ChunkActivityManager activityManager) {
    this.activityManager = activityManager;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    if (!Config.isResetOnBlockPlace()) return;
    Block block = event.getBlock();
    if (!Config.getWhitelistedWorlds().contains(block.getWorld().getName())) return;

    activityManager.recordActivity(block.getChunk());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    if (!Config.isResetOnBlockBreak()) return;
    Block block = event.getBlock();
    if (!Config.getWhitelistedWorlds().contains(block.getWorld().getName())) return;

    activityManager.recordActivity(block.getChunk());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    if (event.getHand() != EquipmentSlot.HAND) return;

    Block block = event.getClickedBlock();
    if (block == null) return;
    if (!Config.getWhitelistedWorlds().contains(block.getWorld().getName())) return;

    if (Config.isResetOnDoorOpen()) {
      BlockData data = block.getBlockData();
      if ((data instanceof Door || data instanceof TrapDoor || data instanceof Gate)
              && block.getType() != Material.IRON_DOOR
              && block.getType() != Material.IRON_TRAPDOOR) {
        activityManager.recordActivity(block.getChunk());
        return;
      }
    }

    if (Config.isResetOnChestOpen() && block.getType() == Material.ENDER_CHEST) {
      activityManager.recordActivity(block.getChunk());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInventoryOpen(InventoryOpenEvent event) {
    if (!Config.isResetOnChestOpen()) return;

    Inventory inv = event.getInventory();
    if (!isChestInventory(inv)) return;

    Location loc = inv.getLocation();
    if (loc != null && !Config.getWhitelistedWorlds().contains(loc.getWorld().getName())) return;

    activityManager.recordChestActivity(inv.getHolder(), loc);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    if (!Config.isResetOnChestInteract()) return;

    Inventory top = event.getView().getTopInventory();
    if (!isChestInventory(top)) return;

    Location loc = top.getLocation();
    if (loc != null && !Config.getWhitelistedWorlds().contains(loc.getWorld().getName())) return;

    Inventory clicked = event.getClickedInventory();
    if (clicked == null) return;

    // chest interaction
    if (clicked.equals(top)) {
      activityManager.recordChestActivity(top.getHolder(), loc);
    } else if (event.isShiftClick()) {
      activityManager.recordChestActivity(top.getHolder(), loc);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInventoryDrag(InventoryDragEvent event) {
    if (!Config.isResetOnChestInteract()) return;

    Inventory top = event.getView().getTopInventory();
    if (!isChestInventory(top)) return;

    Location loc = top.getLocation();
    if (loc != null && !Config.getWhitelistedWorlds().contains(loc.getWorld().getName())) return;

    int topSize = top.getSize();
    boolean draggedIntoTop = event.getRawSlots().stream().anyMatch(slot -> slot < topSize);
    if (draggedIntoTop) {
      activityManager.recordChestActivity(top.getHolder(), loc);
    }
  }

  private boolean isChestInventory(Inventory inventory) {
    if (inventory == null) return false;
    InventoryHolder holder = inventory.getHolder();
    if (holder instanceof Chest || holder instanceof DoubleChest) return true;
    if (holder instanceof org.bukkit.block.Barrel || holder instanceof ShulkerBox) return true;

    Location loc = inventory.getLocation();
    if (loc != null && loc.getWorld() != null) {
      Material type = loc.getBlock().getType();
      return type == Material.CHEST || type == Material.TRAPPED_CHEST
              || type == Material.BARREL || type.name().endsWith("SHULKER_BOX");
    }
    return false;
  }
}
