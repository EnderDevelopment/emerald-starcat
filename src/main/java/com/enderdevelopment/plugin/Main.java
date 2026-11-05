package com.enderdevelopment.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {
    private static final double ALIEN_CAT_HEALTH = 150.0;
    private static final int TAME_EMERALDS = 5;
    private NamespacedKey alienKey;
    private final Map<UUID, UUID> shipByCat = new HashMap<>();
    private BukkitTask protectionTask;

    @Override
    public void onEnable() {
        alienKey = new NamespacedKey(this, "alien_cat");
        Bukkit.getPluginManager().registerEvents(this, this);
        if (getCommand("aliencat") != null) {
            getCommand("aliencat").setExecutor((sender, command, label, args) -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can summon the alien cat.");
                    return true;
                }
                summonAlienCat(player);
                return true;
            });
        }
        protectionTask = Bukkit.getScheduler().runTaskTimer(this, this::protectOwners, 20L, 20L);
    }

    @Override
    public void onDisable() {
        if (protectionTask != null) {
            protectionTask.cancel();
        }
    }

    private void summonAlienCat(Player player) {
        Location landing = player.getLocation().clone().add(0, 0, 4);
        World world = landing.getWorld();
        if (world == null) {
            return;
        }
        Entity ship = world.spawnEntity(landing.clone().add(0, 2, 0), EntityType.MINECART);
        ship.customName(Component.text("Aerospace Ship").color(NamedTextColor.AQUA));
        ship.setCustomNameVisible(true);
        ship.getPersistentDataContainer().set(new NamespacedKey(this, "alien_ship"), PersistentDataType.BYTE, (byte) 1);
        ship.setInvulnerable(true);

        Cat cat = (Cat) world.spawnEntity(landing, EntityType.CAT);
        cat.customName(Component.text("Alien Cat").color(NamedTextColor.LIGHT_PURPLE));
        cat.setCustomNameVisible(true);
        cat.getPersistentDataContainer().set(alienKey, PersistentDataType.BYTE, (byte) 1);
        cat.setSitting(false);
        setHealth(cat, ALIEN_CAT_HEALTH);
        cat.setRemoveWhenFarAway(false);
        ship.addPassenger(cat);
        shipByCat.put(cat.getUniqueId(), ship.getUniqueId());

        player.getInventory().addItem(new ItemStack(Material.EMERALD, TAME_EMERALDS));
        player.sendMessage(Component.text("An alien cat has landed! You received 5 emeralds. Feed them to the cat to tame it.").color(NamedTextColor.GREEN));
        world.strikeLightningEffect(landing);
    }

    private void setHealth(LivingEntity entity, double health) {
        AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
        if (attribute != null) {
            attribute.setBaseValue(ALIEN_CAT_HEALTH);
            entity.setHealth(health);
        }
    }

    @EventHandler
    public void onCatInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Cat cat) || !isAlienCat(cat)) {
            return;
        }
        Player player = event.getPlayer();
        if (cat.isTamed()) {
            return;
        }
        ItemStack held = player.getInventory().getItem(event.getHand());
        if (held.getType() != Material.EMERALD) {
            player.sendMessage(Component.text("The alien cat needs an emerald to trust you.").color(NamedTextColor.YELLOW));
            return;
        }
        held.setAmount(held.getAmount() - 1);
        cat.setOwner(player);
        cat.setSitting(false);
        player.sendMessage(Component.text("The alien cat is now your loyal defender!").color(NamedTextColor.AQUA));
        event.setCancelled(true);
    }

    @EventHandler
    public void onDefend(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player owner) || !(event.getDamager() instanceof LivingEntity attacker)) {
            return;
        }
        for (Entity entity : owner.getNearbyEntities(24, 12, 24)) {
            if (entity instanceof Cat cat && isAlienCat(cat) && cat.isTamed() && owner.equals(cat.getOwner())) {
                cat.setTarget(attacker);
                return;
            }
        }
    }

    private void protectOwners() {
        for (World world : Bukkit.getWorlds()) {
            for (Cat cat : world.getEntitiesByClass(Cat.class)) {
                if (!isAlienCat(cat) || !cat.isTamed() || !(cat.getOwner() instanceof Player owner)) {
                    continue;
                }
                if (cat.getLocation().distanceSquared(owner.getLocation()) > 576) {
                    cat.teleport(owner.getLocation().clone().add(1, 0, 1));
                }
                if (cat.getTarget() != null && !cat.getTarget().isValid()) {
                    cat.setTarget(null);
                }
            }
        }
    }

    private boolean isAlienCat(Cat cat) {
        return cat.getPersistentDataContainer().has(alienKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Cat cat) || !isAlienCat(cat)) {
            return;
        }
        UUID shipId = shipByCat.remove(cat.getUniqueId());
        if (shipId == null) {
            return;
        }
        Entity ship = Bukkit.getEntity(shipId);
        if (ship != null && ship.isValid()) {
            ship.remove();
        }
    }
}
