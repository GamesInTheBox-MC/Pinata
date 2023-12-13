package me.hsgamer.gamesinthebox.pinata;

import me.hsgamer.gamesinthebox.game.feature.BoundingFeature;
import me.hsgamer.gamesinthebox.game.feature.PointFeature;
import me.hsgamer.gamesinthebox.game.simple.feature.SimpleBoundingFeature;
import me.hsgamer.gamesinthebox.game.simple.feature.SimpleBoundingOffsetFeature;
import me.hsgamer.gamesinthebox.game.simple.feature.SimpleRewardFeature;
import me.hsgamer.gamesinthebox.game.template.TemplateGameArena;
import me.hsgamer.gamesinthebox.game.template.TemplateGameArenaLogic;
import me.hsgamer.gamesinthebox.pinata.feature.ListenerFeature;
import me.hsgamer.gamesinthebox.pinata.feature.PinataFeature;
import me.hsgamer.gamesinthebox.pinata.feature.SpawnFeature;
import me.hsgamer.minigamecore.base.Feature;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameArenaLogic extends TemplateGameArenaLogic {
    private final Pinata expansion;

    public GameArenaLogic(Pinata expansion, TemplateGameArena arena) {
        super(arena);
        this.expansion = expansion;
    }

    @Override
    public void forceEnd() {
        PinataFeature pinataFeature = arena.getFeature(PinataFeature.class);
        pinataFeature.setClearAllEntities(false);
        pinataFeature.stopTask();
        pinataFeature.clearAllEntities();

        arena.getFeature(ListenerFeature.class).unregister();
    }

    @Override
    public List<Feature> loadFeatures() {
        SimpleBoundingFeature boundingFeature = new SimpleBoundingFeature(arena);
        return Arrays.asList(
                boundingFeature,
                new SimpleBoundingOffsetFeature(arena, boundingFeature, true),
                new PinataFeature(arena),
                new ListenerFeature(expansion, arena),
                new SpawnFeature(arena)
        );
    }

    @Override
    public void postInit() {
        BoundingFeature boundingFeature = arena.getFeature(BoundingFeature.class);
        arena.getFeature(PinataFeature.class).addEntityClearCheck(entity -> !boundingFeature.checkBounding(entity.getLocation(), true));
    }

    @Override
    public void onInGameStart() {
        arena.getFeature(ListenerFeature.class).register();
        arena.getFeature(PinataFeature.class).startTask();
    }

    @Override
    public void onInGameUpdate() {
        arena.getFeature(SpawnFeature.class).checkAndSpawn();
    }

    @Override
    public void onEndingStart() {
        List<UUID> topList = arena.getFeature(PointFeature.class).getTopUUID().collect(Collectors.toList());
        arena.getFeature(SimpleRewardFeature.class).tryReward(topList);

        arena.getFeature(PinataFeature.class).setClearAllEntities(true);
    }

    @Override
    public boolean isEndingOver() {
        return super.isEndingOver() && arena.getFeature(PinataFeature.class).isAllEntityCleared();
    }

    @Override
    public void onEndingOver() {
        PinataFeature pinataFeature = arena.getFeature(PinataFeature.class);
        pinataFeature.setClearAllEntities(false);
        pinataFeature.stopTask();
        pinataFeature.clearAllEntities();

        arena.getFeature(ListenerFeature.class).unregister();
    }
}
