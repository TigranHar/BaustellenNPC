package me.npc.ncps.Messages;

import me.npc.ncps.NPCS.NPCManager;
import me.npc.ncps.Ncps;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class BossBarMessages implements Listener {
    static int tick = 0;
    static double bossBarCounter = 0;
    static int messageNum = 0;
    static int headRotation = -90;
    public static BossBar bossBarM = null;
    static int headRotationTime = 100;

    public static Ncps constnpcs = Ncps.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if (bossBarM != null) bossBarM.addPlayer(p);
    }

    public static BossBar createBossBar(String message) {
        BossBar bossBar = Bukkit.createBossBar(
            message,
            BarColor.GREEN,
            BarStyle.SOLID);

        bossBarM = bossBar;
        createMessage(bossBar,message);
        return bossBar;
    }

    public static void createMessage(BossBar bossBar, String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.setTitle(message);
            bossBar.addPlayer(p);
        }
    }

    public static void stopBossBar(int taskID, BossBar bossBar) {
        constnpcs.getServer().getScheduler().cancelTask(taskID);

        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.removePlayer(player);
        }

        bossBar.setVisible(false);
    }

    public static int startBossBar(String[] messages) {
        int taskID;
        bossBarM = createBossBar(messages[0]);
        bossBarM.setProgress(0);

        if (Ncps.getData().get("headRotationTime") != null) {
            headRotationTime = Ncps.getData().getInt("headRotationTime");
        }

        taskID = constnpcs.getServer().getScheduler().scheduleSyncRepeatingTask(constnpcs, new Runnable() {
            @Override
            public void run() {
                tick++;
                ++bossBarCounter;
                bossBarM.setProgress(bossBarCounter / 100);

                if (tick % headRotationTime == 0) {
                    messageNum = 0;

                    if (messageNum >= messages.length - 1) {
                        if(headRotation != -90) headRotation = -90;
                        else headRotation = 90;

                        if (NPCManager.NPC.size() != 0) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                for (EntityPlayer npc : NPCManager.NPC) {
                                    PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
                                    connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) ((npc.yaw + headRotation) * 256 / 360)));
                                    bossBarCounter = 0;
                                }
                            }
                        }
                    }

                    else {
                        ++messageNum;
                        createMessage(bossBarM, messages[messageNum]);

                        if(headRotation != -90) headRotation = -90;
                        else headRotation = 90;

                        if (NPCManager.NPC.size() != 0) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                for (EntityPlayer npc : NPCManager.NPC) {
                                    PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
                                    connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) ((npc.yaw + headRotation) * 256 / 360)));
                                    bossBarCounter = 0;
                                }
                            }
                        }
                    }
                }
            }
        }, 0, 20L);

        return taskID;
    }
}
