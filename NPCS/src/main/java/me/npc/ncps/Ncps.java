package me.npc.ncps;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.npc.ncps.Messages.BossBarMessages;
import me.npc.ncps.NPCS.CustomConfigFile;
import me.npc.ncps.NPCS.FPCommand;
import me.npc.ncps.NPCS.NPCManager;
import me.npc.ncps.NPCS.OnPlayerJoinAddPackets;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

import static me.npc.ncps.Messages.BossBarMessages.bossBarM;
import static me.npc.ncps.NPCS.NPCManager.messages;
import static me.npc.ncps.NPCS.NPCManager.shouldStart;

public final class Ncps extends JavaPlugin {

    private static Ncps instance;
    public static CustomConfigFile data;

    public static Ncps getInstance() {
        return instance;
    }

    private void setInstance(Ncps instance) {
        this.instance = instance;
    }

    @Override
    public void onEnable() {
        setInstance(this);
        data = new CustomConfigFile(this);

        if (data.getConfig().contains("data"))
            loadNpc();

        this.getCommand("baustellennpc").setExecutor(new FPCommand());
        this.getServer().getPluginManager().registerEvents(new OnPlayerJoinAddPackets(), this);
        this.getServer().getPluginManager().registerEvents(new BossBarMessages(), this);
    }

    @Override
    public void onDisable() {
        if (NPCManager.NPC.size() != 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (EntityPlayer npc : NPCManager.NPC)
                    NPCManager.removeNPC(player, npc);
            }
        }
        messages = new String[]{};
        shouldStart = false;

        if (bossBarM != null)
            bossBarM.setVisible(false);
    }

    public static FileConfiguration getData() {
        return data.getConfig();
    }

    public static void saveData() {
        data.saveConfig();
    }

    public static void loadNpc() {
        FileConfiguration file = data.getConfig();
        if (file.getConfigurationSection("data") != null) {
            file.getConfigurationSection("data").getKeys(false).forEach(npc -> {
                Location location = new Location(Bukkit.getWorld(file.getString("data." + npc + ".world")),
                        file.getInt("data." + npc + ".x"), file.getInt("data." + npc + ".y"), file.getInt("data." + npc + ".z"));
                location.setPitch((float) file.getDouble("data." + npc + ".p"));
                location.setYaw((float) file.getDouble("data." + npc + ".yaw"));
                String name = file.getString("data." + npc + ".username");

                String message = file.getString("data." + npc + ".message");

                GameProfile profile = new GameProfile(UUID.randomUUID(), name);
                if (file.getString("data." + npc + ".text") != null) {
                    profile.getProperties().put("textures", new Property("textures", file.getString("data." + npc + ".text"),
                            file.getString("data." + npc + ".signature")));
                }

                NPCManager.loadNpcs(location, profile);

                MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();

                EntityPlayer npcAdd = new EntityPlayer(server, server.getWorldServer(0), profile,
                        new PlayerInteractManager(server.getWorldServer(0)));

                NPCManager.NPC.add(npcAdd);

                if (messages.length < 2) {
                    messages = NPCManager.push(messages, message);
                }

                if (messages.length >= 2 && !shouldStart) {
                    BossBarMessages.startBossBar(messages);
                    shouldStart = true;
                }
            });
        }
    }
}
