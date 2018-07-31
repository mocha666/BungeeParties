package net.simplyrin.bungeeparties.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.simplyrin.bungeeparties.Main;
import net.simplyrin.bungeeparties.exceptions.NotJoinedException;
import net.simplyrin.bungeeparties.messages.Messages;
import net.simplyrin.bungeeparties.utils.PartyManager.PartyUtils;

/**
 * Created by SimplyRin on 2018/07/31.
 *
 * Copyright (C) 2018 SimplyRin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class EventListener implements Listener {

	private Main plugin;

	public EventListener(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();

		this.plugin.getPartyManager().getPlayer(player);

		this.plugin.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Name", player.getName());
		this.plugin.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Currently-Joined-Party", "NONE");
		this.plugin.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Party-List", new ArrayList<>());
		this.plugin.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Requests", new ArrayList<>());

		this.plugin.getPlayerManager().getConfig().set("Name." + player.getName().toLowerCase(), player.getUniqueId().toString());
		this.plugin.getPlayerManager().getConfig().set("UUID." + player.getUniqueId().toString(), player.getName().toLowerCase());
	}

	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		PartyUtils myParties = this.plugin.getPartyManager().getPlayer(player);

		if(myParties.getParties().size() == 0) {
			return;
		}

		try {
			if(!myParties.isPartyOwner()) {
				return;
			}
		} catch (NotJoinedException e) {
			return;
		}

		for(String partyPlayerUniqueId : myParties.getParties()) {
			PartyUtils targetPlayer = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);

			this.plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);
			this.plugin.info(targetPlayer.getPlayer(), myParties.getDisplayName() + "&e has disbanded the party!");
			this.plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);

			this.plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Currently-Joined-Party", "NONE");
			this.plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Party-List", new ArrayList<>());
			this.plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Requests", new ArrayList<>());
		}

		this.plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Currently-Joined-Party", "NONE");
		this.plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Party-List", new ArrayList<>());
		this.plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Requests", new ArrayList<>());
	}

	@EventHandler
	public void onSwitch(ServerSwitchEvent event) {
		ProxiedPlayer player = event.getPlayer();
		PartyUtils partyUtils = this.plugin.getPartyManager().getPlayer(player);

		try {
			if(!partyUtils.isPartyOwner()) {
				return;
			}
		} catch (NotJoinedException e) {
			return;
		}

		String serverName = player.getServer().getInfo().getName().toLowerCase();
		if(serverName.contains(this.plugin.getConfigManager().getConfig().getString("Plugin.Bypass-Lobby-Name-Contains").toLowerCase())) {
			return;
		}

		List<String> parties = partyUtils.getParties();
		for(String partyPlayerUniqueId : parties) {
			ProxiedPlayer targetPlayer = this.plugin.getProxy().getPlayer(UUID.fromString(partyPlayerUniqueId));
			if(targetPlayer != null) {
				this.plugin.info(targetPlayer, "&aSending you to " + player.getServer().getInfo().getName() + ".");
				targetPlayer.connect(player.getServer().getInfo());
			}
		}
	}

}
