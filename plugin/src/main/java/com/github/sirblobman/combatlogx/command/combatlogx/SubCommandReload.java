package com.github.sirblobman.combatlogx.command.combatlogx;

import org.bukkit.command.CommandSender;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.command.CombatLogCommand;

public final class SubCommandReload extends CombatLogCommand {
    public SubCommandReload(ICombatLogX plugin) {
        super(plugin, "reload");
        setPermissionName("combatlogx.command.combatlogx.reload");
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        try {
            ICombatLogX plugin = getCombatLogX();
            plugin.onReload();

            sendMessageWithPrefix(sender, "command.combatlogx.reload-success", null);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessageWithPrefix(sender, "command.combatlogx.reload-failure", null);
            return true;
        }
    }
}
