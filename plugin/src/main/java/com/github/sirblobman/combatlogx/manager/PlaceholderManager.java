package com.github.sirblobman.combatlogx.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.placeholder.IPlaceholderExpansion;
import com.github.sirblobman.combatlogx.api.utility.CommandHelper;
import com.github.sirblobman.combatlogx.api.utility.PlaceholderHelper;

import org.jetbrains.annotations.Nullable;

public final class PlaceholderManager extends Manager implements IPlaceholderManager {
    private static final Pattern BRACKET_PLACEHOLDER_PATTERN;

    static {
        BRACKET_PLACEHOLDER_PATTERN = Pattern.compile("\\{(.+)}");
    }

    private final Map<String, IPlaceholderExpansion> expansionMap;

    public PlaceholderManager(ICombatLogX plugin) {
        super(plugin);
        this.expansionMap = new LinkedHashMap<>();
    }

    @Override
    public IPlaceholderExpansion getPlaceholderExpansion(String id) {
        Validate.notEmpty(id, "id must not be empty!");
        return this.expansionMap.get(id);
    }

    @Override
    public List<IPlaceholderExpansion> getPlaceholderExpansions() {
        Collection<IPlaceholderExpansion> valueCollection = this.expansionMap.values();
        List<IPlaceholderExpansion> valueList = new ArrayList<>(valueCollection);
        return Collections.unmodifiableList(valueList);
    }

    @Override
    public void registerPlaceholderExpansion(IPlaceholderExpansion expansion) {
        Validate.notNull(expansion, "expansion must not be null!");

        String expansionId = expansion.getId();
        IPlaceholderExpansion oldRegistry = this.expansionMap.putIfAbsent(expansionId, expansion);
        if (oldRegistry != null) {
            String errorMessage = "A placeholder expansion with id '" + expansionId + "' is already registered.";
            throw new IllegalArgumentException(errorMessage);
        }
    }

    @Nullable
    @Override
    public String getPlaceholderReplacement(Player player, List<Entity> enemyList, String placeholder) {
        Validate.notNull(player, "player must not be null!");
        Validate.notNull(placeholder, "placeholder must not be null!");

        int underscoreIndex = placeholder.indexOf('_');
        if (underscoreIndex == -1) {
            return null;
        }

        String expansionId = placeholder.substring(0, underscoreIndex);
        IPlaceholderExpansion expansion = getPlaceholderExpansion(expansionId);
        if (expansion == null) {
            return null;
        }

        String subPlaceholder = placeholder.substring(underscoreIndex + 1);
        return expansion.getReplacement(player, enemyList, subPlaceholder);
    }

    @Override
    public String replaceAll(Player player, List<Entity> enemyList, String string) {
        Validate.notNull(player, "player must not be null!");
        Validate.notNull(string, "string must not be null!");

        StringBuffer buffer = new StringBuffer();
        Matcher matcher = BRACKET_PLACEHOLDER_PATTERN.matcher(string);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = getPlaceholderReplacement(player, enemyList, placeholder);
            if (replacement != null) {
                matcher.appendReplacement(buffer, replacement);
            }
        }

        matcher.appendTail(buffer);
        String replaced = buffer.toString();
        return PlaceholderHelper.replacePlaceholderAPI(player, replaced);
    }

    @Override
    public void runReplacedCommands(Player player, List<Entity> enemyList, Iterable<String> commands) {
        Validate.notNull(player, "player must not be null!");

        ICombatLogX plugin = getCombatLogX();
        for (String originalCommand : commands) {
            String replacedCommand = replaceAll(player, enemyList, originalCommand);
            if (replacedCommand.startsWith("[PLAYER]")) {
                String playerCommand = replacedCommand.substring(8);
                CommandHelper.runAsPlayer(plugin, player, playerCommand);
            } else if (replacedCommand.startsWith("[OP]")) {
                String opCommand = replacedCommand.substring(4);
                CommandHelper.runAsOperator(plugin, player, opCommand);
            } else {
                CommandHelper.runAsConsole(plugin, replacedCommand);
            }
        }
    }
}