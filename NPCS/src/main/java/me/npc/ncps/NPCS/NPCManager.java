package me.npc.ncps.NPCS;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.npc.ncps.Messages.BossBarMessages;
import me.npc.ncps.Ncps;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NPCManager {

    public Ncps main = Ncps.getInstance();
    public static List<EntityPlayer> NPC = new ArrayList<EntityPlayer>();
    public static String CHAT_NAME = ChatColor.AQUA + "[NPC SYSTEM] >> ";
    public static String[] messages = new String[] {};
    public static int bossBarTaskID = 0;
    public static boolean shouldStart = false;

    public static String[] push(String[] array, String pushItem) {
        String[] longer = new String[array.length + 1];

        for (int i = 0; i < array.length; i++)
            longer[i] = array[i];

        longer[array.length] = pushItem;

        return longer;
    }

    public String[] getFromPlayer(Player playerBukkit) {
        EntityPlayer playerNMS = ((CraftPlayer) playerBukkit).getHandle();
        GameProfile profile = playerNMS.getProfile();
        Property property = profile.getProperties().get("textures").iterator().next();
        String texture = property.getValue();
        String signature = property.getSignature();
        return new String[] {texture, signature};
    }
    private static String[] getFromName(Player player, String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            String uuid = new JsonParser().parse(reader).getAsJsonObject().get("id") .getAsString();
            URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid
                    + "?unsigned=false");
            InputStreamReader reader2 = new InputStreamReader(url2.openStream());
            JsonObject property = new JsonParser().parse(reader2).getAsJsonObject().get("properties")
                    .getAsJsonArray().get(0).getAsJsonObject();
            String texture = property.get("value").getAsString();
            String signature = property.get("signature").getAsString();
            Bukkit.broadcastMessage(CHAT_NAME + ChatColor.GREEN + "Created an NPC with the name \"" + name + "\"");
            return new String[] {texture, signature};
        } catch (Exception e) {
            Bukkit.broadcastMessage(CHAT_NAME + ChatColor.RED + "Could not find the account with the following name \"" + name + "\"");
            return null;
        }
    }

    public static void createNPC(Player p, String name, String username, String message) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        EntityPlayer npc = new EntityPlayer(server, server.getWorldServer(0), profile,
                new PlayerInteractManager(server.getWorldServer(0)));

        npc.setLocation(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(),
                p.getLocation().getYaw(), p.getLocation().getPitch());

        String[] a = getFromName(p, username);
        if (a != null)
            profile.getProperties().put("textures", new Property("textures", a[0], a[1]));

        addNPCPacket(npc);

        int var = 1;
        if(Ncps.getData().contains("data"))
            var = Ncps.getData().getConfigurationSection("data").getKeys(false).size() + 1;

        Ncps.getData().set("data." + var + ".x", (int) p.getLocation().getX());
        Ncps.getData().set("data." + var + ".y", (int) p.getLocation().getY());
        Ncps.getData().set("data." + var + ".z", (int) p.getLocation().getZ());
        Ncps.getData().set("data." + var + ".p", p.getLocation().getPitch());
        Ncps.getData().set("data." + var + ".yaw", p.getLocation().getYaw());
        Ncps.getData().set("data." + var + ".world", p.getLocation().getWorld().getName());
        Ncps.getData().set("data." + var + ".name", username);
        Ncps.getData().set("data." + var + ".username", name);
        if (a != null) {
            Ncps.getData().set("data." + var + ".text", a[0]);
            Ncps.getData().set("data." + var + ".signature", a[1]);
        }
        Ncps.getData().set("data." + var + ".message", message);
        Ncps.saveData();

        NPC.add(npc);

        if (messages.length < 2) {
            messages = NPCManager.push(messages, message);
        }

        if (messages.length >= 2 && !shouldStart) {
            BossBarMessages.startBossBar(messages);
            shouldStart = true;
        }
    }

    public static void loadNpcs(Location location, GameProfile gameProfile) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        GameProfile profile = gameProfile;

        EntityPlayer npc = new EntityPlayer(server, server.getWorldServer(0), profile,
                new PlayerInteractManager(server.getWorldServer(0)));

        npc.setLocation(location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());

        addNPCPacket(npc);

        NPC.add(npc);
    }

    public static void addNPCPacket(EntityPlayer npc) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
            connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
            connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (npc.yaw * 256 / 360)));
        }
    }
    public static void addJoinPacket(Player player) {
        for (EntityPlayer npc : NPC) {
            PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
            connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
            connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (npc.yaw * 256 / 360)));
        }
    }

    public static void removeNPC(Player player, EntityPlayer npc) {
        PlayerConnection connection = ((CraftPlayer)player).getHandle() .playerConnection;
        connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
    }
}
