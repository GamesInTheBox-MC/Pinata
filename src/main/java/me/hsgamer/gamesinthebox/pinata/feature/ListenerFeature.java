package me.hsgamer.gamesinthebox.pinata.feature;

import me.hsgamer.gamesinthebox.game.feature.GameConfigFeature;
import me.hsgamer.gamesinthebox.game.simple.SimpleGameArena;
import me.hsgamer.gamesinthebox.game.simple.feature.SimplePointFeature;
import me.hsgamer.gamesinthebox.pinata.Pinata;
import me.hsgamer.minigamecore.base.Feature;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Optional;

public class ListenerFeature implements Feature, Listener {
    private final Pinata expansion;
    private final SimpleGameArena arena;
    private PinataFeature pinataFeature;
    private SimplePointFeature pointFeature;
    private boolean damageAsScore = false;

    public ListenerFeature(Pinata expansion, SimpleGameArena arena) {
        this.expansion = expansion;
        this.arena = arena;
    }

    @Override
    public void init() {
        this.pinataFeature = this.arena.getFeature(PinataFeature.class);
        this.pointFeature = this.arena.getFeature(SimplePointFeature.class);
    }

    @Override
    public void postInit() {
        GameConfigFeature gameConfigFeature = arena.getFeature(GameConfigFeature.class);

        if (gameConfigFeature != null) {
            damageAsScore = Optional.ofNullable(gameConfigFeature.getString("damage-as-score"))
                    .map(Boolean::parseBoolean)
                    .orElse(false);
        }
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, expansion.getPlugin());
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    public boolean isDamageAsScore() {
        return damageAsScore;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPinataDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!pinataFeature.contains(entity)) return;
        event.setCancelled(true);

        Entity damager = event.getDamager();
        if (damager instanceof Player) {
            Player player = (Player) damager;

            if (damageAsScore) {
                pointFeature.applyPoint(player.getUniqueId(), (int) event.getFinalDamage());
            } else {
                pointFeature.applyPoint(player.getUniqueId(), Pinata.POINT_HIT);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPinataDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!pinataFeature.contains(entity)) return;

        event.setDroppedExp(0);
        event.getDrops().clear();
    }
}
