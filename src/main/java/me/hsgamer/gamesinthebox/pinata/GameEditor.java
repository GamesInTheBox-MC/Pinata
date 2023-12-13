package me.hsgamer.gamesinthebox.pinata;

import com.google.common.base.Enums;
import me.hsgamer.gamesinthebox.game.GameArena;
import me.hsgamer.gamesinthebox.game.simple.action.ValueAction;
import me.hsgamer.gamesinthebox.game.simple.feature.SimpleBoundingFeature;
import me.hsgamer.gamesinthebox.game.simple.feature.SimpleBoundingOffsetFeature;
import me.hsgamer.gamesinthebox.game.template.TemplateGame;
import me.hsgamer.gamesinthebox.game.template.TemplateGameArenaLogic;
import me.hsgamer.gamesinthebox.game.template.TemplateGameEditor;
import me.hsgamer.gamesinthebox.game.template.feature.ArenaLogicFeature;
import me.hsgamer.gamesinthebox.pinata.feature.ListenerFeature;
import me.hsgamer.gamesinthebox.pinata.feature.PinataFeature;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class GameEditor extends TemplateGameEditor {
    private final SimpleBoundingFeature.Editor simpleBoundingFeatureEditor = SimpleBoundingFeature.editor(true);
    private final SimpleBoundingOffsetFeature.Editor simpleBoundingOffsetFeatureEditor = SimpleBoundingOffsetFeature.editor();
    private final List<String> nameTags = new ArrayList<>();
    private EntityType entityType = EntityType.SHEEP;
    private boolean damageAsScore = false;

    public GameEditor(@NotNull TemplateGame game) {
        super(game);
    }

    @Override
    protected @NotNull Map<String, SimpleAction> createActionMap() {
        Map<String, SimpleAction> map = super.createActionMap();

        map.putAll(simpleBoundingFeatureEditor.getActions());
        map.putAll(simpleBoundingOffsetFeatureEditor.getActions());

        map.put("add-name-tag", new SimpleAction() {
            @Override
            public @NotNull String getDescription() {
                return "Add a name tag to the list";
            }

            @Override
            public boolean performAction(@NotNull CommandSender sender, @NotNull String... args) {
                if (args.length == 0) {
                    return false;
                }
                nameTags.add(String.join(" ", args));
                return true;
            }

            @Override
            public @NotNull String getArgsUsage() {
                return "<name tag>";
            }
        });
        map.put("clear-name-tags", new SimpleAction() {
            @Override
            public @NotNull String getDescription() {
                return "Clear the name tags list";
            }

            @Override
            public boolean performAction(@NotNull CommandSender sender, @NotNull String... args) {
                nameTags.clear();
                return true;
            }
        });
        map.put("set-pinata-type", new ValueAction<EntityType>() {
            @Override
            public @NotNull String getDescription() {
                return "Set the pinata type";
            }

            @Override
            protected boolean performAction(@NotNull CommandSender sender, @NotNull EntityType value, String... args) {
                entityType = value;
                return true;
            }

            @Override
            protected int getValueArgCount() {
                return 1;
            }

            @Override
            protected Optional<EntityType> parseValue(@NotNull CommandSender sender, String... args) {
                return Optional.ofNullable(args[0])
                        .map(String::toUpperCase)
                        .flatMap(s -> Enums.getIfPresent(EntityType.class, s).toJavaUtil())
                        .filter(EntityType::isAlive);
            }

            @Override
            protected @NotNull List<String> getValueArgs(@NotNull CommandSender sender, String... args) {
                return Arrays.stream(EntityType.values())
                        .filter(EntityType::isAlive)
                        .map(Enum::name)
                        .collect(Collectors.toList());
            }
        });
        map.put("set-damage-as-score", new ValueAction<Boolean>() {
            @Override
            protected boolean performAction(@NotNull CommandSender sender, @NotNull Boolean value, String... args) {
                damageAsScore = value;
                return true;
            }

            @Override
            protected int getValueArgCount() {
                return 1;
            }

            @Override
            protected Optional<Boolean> parseValue(@NotNull CommandSender sender, String... args) {
                return Optional.of(Boolean.parseBoolean(args[0]));
            }

            @Override
            protected @NotNull List<String> getValueArgs(@NotNull CommandSender sender, String... args) {
                return Arrays.asList("true", "false");
            }

            @Override
            public @NotNull String getDescription() {
                return "Set whether to use damage as score";
            }

            @Override
            public @NotNull String getArgsUsage() {
                return "<true/false>";
            }
        });

        return map;
    }

    @Override
    protected @NotNull List<@NotNull SimpleEditorStatus> createEditorStatusList() {
        List<@NotNull SimpleEditorStatus> list = super.createEditorStatusList();
        list.add(simpleBoundingFeatureEditor.getStatus());
        list.add(simpleBoundingOffsetFeatureEditor.getStatus());
        list.add(new SimpleEditorStatus() {
            @Override
            public void sendStatus(@NotNull CommandSender sender) {
                MessageUtils.sendMessage(sender, "&6&lPinata");
                MessageUtils.sendMessage(sender, "&6Type: &f" + entityType.name());
                MessageUtils.sendMessage(sender, "&6Damage As Score: &f" + damageAsScore);
                MessageUtils.sendMessage(sender, "&6Name Tags: ");
                nameTags.forEach(nameTag -> MessageUtils.sendMessage(sender, "&f- " + nameTag));
            }

            @Override
            public void reset(@NotNull CommandSender sender) {
                nameTags.clear();
                entityType = EntityType.SHEEP;
                damageAsScore = false;
            }

            @Override
            public boolean canSave(@NotNull CommandSender sender) {
                return true;
            }

            @Override
            public Map<String, Object> toPathValueMap(@NotNull CommandSender sender) {
                Map<String, Object> map = new LinkedHashMap<>();
                if (!nameTags.isEmpty()) {
                    map.put("pinata.name-tag", nameTags);
                }
                if (entityType != EntityType.SHEEP) {
                    map.put("pinata.type", entityType.name());
                }
                if (damageAsScore) {
                    map.put("damage-as-score", true);
                }
                return map;
            }
        });
        return list;
    }

    @Override
    public boolean migrate(@NotNull CommandSender sender, @NotNull GameArena gameArena) {
        ArenaLogicFeature arenaLogicFeature = gameArena.getFeature(ArenaLogicFeature.class);
        if (arenaLogicFeature == null) {
            return false;
        }
        TemplateGameArenaLogic templateGameArenaLogic = arenaLogicFeature.getArenaLogic();
        if (!(templateGameArenaLogic instanceof GameArenaLogic)) {
            return false;
        }

        nameTags.clear();
        nameTags.addAll(gameArena.getFeature(PinataFeature.class).getNameTags());

        entityType = gameArena.getFeature(PinataFeature.class).getEntityType();
        damageAsScore = gameArena.getFeature(ListenerFeature.class).isDamageAsScore();

        simpleBoundingFeatureEditor.migrate(gameArena.getFeature(SimpleBoundingFeature.class));
        simpleBoundingOffsetFeatureEditor.migrate(gameArena.getFeature(SimpleBoundingOffsetFeature.class));
        return super.migrate(sender, gameArena);
    }
}
