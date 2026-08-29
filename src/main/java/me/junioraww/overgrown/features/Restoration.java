package me.junioraww.overgrown.features;

import me.junioraww.overgrown.Main;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Restoration implements Listener {
  private static final Map<Location, RestorationState> activeRestorations = new HashMap<>();
  private static final Random random = new Random();

  @EventHandler
  public void clayClick(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    if (event.getHand() != EquipmentSlot.HAND) return;

    Block block = event.getClickedBlock();
    if (block == null || block.getType() != Material.CRACKED_STONE_BRICKS) return;

    ItemStack item = event.getItem();
    if (item == null || item.getType() != Material.CLAY_BALL) return;

    Location loc = block.getLocation();
    if (activeRestorations.containsKey(loc)) return;

    item.setAmount(item.getAmount() - 1);

    BlockDisplay display = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
    display.setBlock(Material.CRACKED_STONE_BRICKS.createBlockData());

    block.setType(Material.BARRIER);

    RestorationState state = new RestorationState(display, event.getBlockFace());
    activeRestorations.put(loc, state);

    block.getWorld().playSound(claySound, block.getLocation());
    event.getPlayer().swingMainHand();

    state.animationTask = new BukkitRunnable() {
      @Override
      public void run() {
        float dx = (random.nextFloat() * 0.1f) - 0.05f;
        float dy = (random.nextFloat() * 0.1f) - 0.05f;
        float dz = (random.nextFloat() * 0.1f) - 0.05f;

        Transformation trans = display.getTransformation();
        display.setTransformation(new Transformation(
                new Vector3f(dx, dy, dz),
                trans.getLeftRotation(),
                trans.getScale(),
                trans.getRightRotation()
        ));
        display.setInterpolationDuration(2);
        display.setInterpolationDelay(0);
      }
    }.runTaskTimer(Main.getPlugin(), 0L, 2L);

    state.timeoutTask = new BukkitRunnable() {
      @Override
      public void run() {
        endRestoration(loc, false);
      }
    }.runTaskLater(Main.getPlugin(), 100L);
  }

  public static final Sound shovelSound = Sound.sound(
          Key.key("minecraft:item.shovel.flatten"), Sound.Source.PLAYER, 1f, 1f
  );

  public static final Sound claySound = Sound.sound(
          Key.key("minecraft:item.hoe.till"), Sound.Source.PLAYER, 1f, 1f
  );

  @EventHandler
  public void shovelClick(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    if (event.getHand() != EquipmentSlot.HAND) return;

    Block block = event.getClickedBlock();
    if (block == null || block.getType() != Material.BARRIER) return;

    ItemStack item = event.getItem();
    if (item == null || !item.getType().name().endsWith("_SHOVEL")) return;

    Location loc = block.getLocation();
    if (!activeRestorations.containsKey(loc)) return;

    endRestoration(loc, true);

    block.getWorld().playSound(shovelSound, block.getLocation());
    event.getPlayer().swingMainHand();

    ItemMeta meta = item.getItemMeta();
    if (meta instanceof Damageable damageable) {
      damageable.setDamage(damageable.getDamage() + 1);
      item.setItemMeta(damageable);

      if (damageable.getDamage() >= item.getType().getMaxDurability()) {
        item.setAmount(0);
      }
    }
  }

  private void endRestoration(Location loc, boolean success) {
    RestorationState state = activeRestorations.remove(loc);
    if (state == null) return;

    state.animationTask.cancel();
    state.timeoutTask.cancel();
    state.display.remove();

    Block block = loc.getBlock();
    if (success) {
      block.setType(Material.STONE_BRICKS);
    } else {
      block.setType(Material.CRACKED_STONE_BRICKS);
      Location dropLoc = loc.clone().add(0.5, 0.5, 0.5).add(state.face.getDirection());
      loc.getWorld().dropItemNaturally(dropLoc, new ItemStack(Material.CLAY_BALL));
    }
  }

  private static class RestorationState {
    BlockDisplay display;
    BlockFace face;
    BukkitTask animationTask;
    BukkitTask timeoutTask;

    RestorationState(BlockDisplay display, BlockFace face) {
      this.display = display;
      this.face = face;
    }
  }
}