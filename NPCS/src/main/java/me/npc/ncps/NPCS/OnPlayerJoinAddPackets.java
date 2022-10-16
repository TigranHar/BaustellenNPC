package me.npc.ncps.NPCS;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static me.npc.ncps.NPCS.NPCManager.NPC;

public class OnPlayerJoinAddPackets implements Listener {

    @EventHandler
    public void AddPackets(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (NPC == null)
            return;
        if (NPC.isEmpty())
            return;
        NPCManager.addJoinPacket(player);
    }
}
