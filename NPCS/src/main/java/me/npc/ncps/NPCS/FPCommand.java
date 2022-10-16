package me.npc.ncps.NPCS;

import me.npc.ncps.Ncps;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.npc.ncps.NPCS.NPCManager.createNPC;

public class FPCommand implements CommandExecutor {

    private Ncps plugin = Ncps.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            Location loc = player.getLocation();

            if (command.getName().equalsIgnoreCase("baustellennpc")) {
                if (args.length == 4) {
                    if (args[0].equalsIgnoreCase("create")) {
                        String npcName = args[1];
                        String skinName = args[2];
                        String message = args[3];

                        String token[] = message.split("_");
                        String resultString = "";

                        for(int a=0; a < token.length; a++){
                            resultString += token[a] + " ";
                        }

                        createNPC(player, npcName, skinName, resultString);
                    }
                }
            }
        }
        return true;
    }
}
