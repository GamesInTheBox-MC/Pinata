package me.hsgamer.gamesinthebox.pinata;

import me.hsgamer.gamesinthebox.game.simple.feature.SimplePointFeature;
import me.hsgamer.gamesinthebox.game.template.TemplateGame;
import me.hsgamer.gamesinthebox.game.template.TemplateGameArena;
import me.hsgamer.gamesinthebox.game.template.TemplateGameArenaLogic;
import me.hsgamer.gamesinthebox.game.template.TemplateGameEditor;
import me.hsgamer.gamesinthebox.game.template.expansion.TemplateGameExpansion;
import me.hsgamer.gamesinthebox.util.UpdateUtil;
import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.hscore.config.proxy.ConfigGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Pinata extends TemplateGameExpansion {
    public static final SimplePointFeature.PointValue POINT_HIT = new SimplePointFeature.PointValue("hit", 1, false);
    private final MessageConfig messageConfig = ConfigGenerator.newInstance(MessageConfig.class, new BukkitConfig(new File(getDataFolder(), "messages.yml")));

    @Override
    protected @NotNull String @NotNull [] getGameType() {
        return new String[]{"pinata"};
    }

    @Override
    public TemplateGameArenaLogic createArenaLogic(TemplateGameArena templateGameArena) {
        return new GameArenaLogic(this, templateGameArena);
    }

    @Override
    public TemplateGameEditor getEditor(TemplateGame game) {
        return new GameEditor(game);
    }

    @Override
    public List<SimplePointFeature.PointValue> getPointValues() {
        return Collections.singletonList(POINT_HIT);
    }

    @Override
    public List<String> getDefaultHologramLines(String name) {
        return Optional.ofNullable(messageConfig.getDefaultHologramLines().get(name))
                .map(CollectionUtils::createStringListFromObject)
                .orElseGet(() -> super.getDefaultHologramLines(name));
    }

    @Override
    public String getDisplayName() {
        return messageConfig.getDisplayName();
    }

    @Override
    public void onReload() {
        super.onReload();
        messageConfig.reloadConfig();
    }

    @Override
    protected void enable() {
        UpdateUtil.notifyUpdate(this, "GamesInTheBox-MC/Pinata");
    }
}
