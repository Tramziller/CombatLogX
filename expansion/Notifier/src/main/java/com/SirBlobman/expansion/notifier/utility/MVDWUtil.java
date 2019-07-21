package com.SirBlobman.expansion.notifier.utility;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.SirBlobman.combatlogx.utility.Util;
import com.SirBlobman.expansion.notifier.config.ConfigNotifier;

import be.maximvdw.placeholderapi.EventAPI;

public class MVDWUtil extends Util {
	private static List<UUID> HAS_SCOREBOARD = newList();
	public static void enableScoreboard(Player player) {
		UUID uuid = player.getUniqueId();
		if(HAS_SCOREBOARD.contains(uuid)) return;
		
		Plugin plugin = Bukkit.getPluginManager().getPlugin("FeatherBoard");
		String scoreboardName = ConfigNotifier.SCORE_BOARD_FEATHERBOARD_NAME;
		EventAPI.triggerEvent(plugin, player, scoreboardName, true);
		
		HAS_SCOREBOARD.add(uuid);
	}
	
	public static void disableScoreboard(Player player) {
		UUID uuid = player.getUniqueId();
		HAS_SCOREBOARD.remove(uuid);
		

		Plugin plugin = Bukkit.getPluginManager().getPlugin("FeatherBoard");
		String scoreboardName = ConfigNotifier.SCORE_BOARD_FEATHERBOARD_NAME;
		EventAPI.triggerEvent(plugin, player, scoreboardName, false);
	}
}