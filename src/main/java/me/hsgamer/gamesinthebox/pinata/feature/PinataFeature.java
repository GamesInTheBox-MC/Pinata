package me.hsgamer.gamesinthebox.pinata.feature;

import com.google.common.base.Enums;
import me.hsgamer.gamesinthebox.game.feature.EntityFeature;
import me.hsgamer.gamesinthebox.game.feature.GameConfigFeature;
import me.hsgamer.gamesinthebox.game.simple.SimpleGameArena;
import me.hsgamer.gamesinthebox.util.Util;
import me.hsgamer.hscore.common.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PinataFeature extends EntityFeature {
    private final SimpleGameArena arena;
    private List<String> nameTags = Collections.emptyList();
    private EntityType entityType = EntityType.SHEEP;
    private boolean hasAI = true;

    public PinataFeature(SimpleGameArena arena) {
        this.arena = arena;
    }

    @Override
    public void postInit() {
        GameConfigFeature config = arena.getFeature(GameConfigFeature.class);

        nameTags = Optional.ofNullable(config.get("pinata.name-tag"))
                .map(CollectionUtils::createStringListFromObject)
                .orElse(nameTags);

        entityType = Optional.ofNullable(config.get("pinata.type"))
                .map(Objects::toString)
                .map(String::toUpperCase)
                .flatMap(s -> Enums.getIfPresent(EntityType.class, s).toJavaUtil())
                .filter(EntityType::isAlive)
                .orElse(entityType);

        hasAI = Optional.ofNullable(config.get("pinata.ai"))
                .map(Objects::toString)
                .map(String::toLowerCase)
                .map(Boolean::parseBoolean)
                .orElse(hasAI);
    }

    @Override
    protected @Nullable Entity createEntity(Location location) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);
        String nameTag = Util.getRandomColorizedString(nameTags, "");
        if (!nameTag.isEmpty()) {
            entity.setCustomName(nameTag);
            entity.setCustomNameVisible(true);
        }
        entity.setAI(hasAI);
        return entity;
    }

    public List<String> getNameTags() {
        return nameTags;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public boolean isHasAI() {
        return hasAI;
    }
}
