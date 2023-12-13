package me.hsgamer.gamesinthebox.pinata.feature;

import me.hsgamer.gamesinthebox.game.feature.BoundingOffsetFeature;
import me.hsgamer.gamesinthebox.game.simple.SimpleGameArena;
import me.hsgamer.minigamecore.base.Feature;
import org.bukkit.entity.Entity;

import java.util.concurrent.atomic.AtomicReference;

public class SpawnFeature implements Feature {
    private final SimpleGameArena arena;
    private final AtomicReference<Entity> entity = new AtomicReference<>();
    private PinataFeature pinataFeature;
    private BoundingOffsetFeature boundingOffsetFeature;

    public SpawnFeature(SimpleGameArena arena) {
        this.arena = arena;
    }

    @Override
    public void init() {
        this.pinataFeature = this.arena.getFeature(PinataFeature.class);
        this.boundingOffsetFeature = this.arena.getFeature(BoundingOffsetFeature.class);
    }

    public void checkAndSpawn() {
        Entity entity = this.entity.get();
        if (entity != null && entity.isValid()) return;
        pinataFeature.spawn(boundingOffsetFeature.getRandomLocation(), this.entity::set);
    }
}
