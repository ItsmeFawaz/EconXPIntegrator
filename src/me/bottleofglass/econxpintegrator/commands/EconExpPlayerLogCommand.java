package me.bottleofglass.econxpintegrator.commands;

import me.bottleofglass.econxpintegrator.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EconExpPlayerLogCommand implements CommandExecutor {

    Main main;

    public EconExpPlayerLogCommand(Main m) {
        main = m;
        main.getCommand("econlogs").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player) { // checks if the sender is player
            Player p = (Player) commandSender;
            if (strings.length > 0 ) { // if the command has arguments
                if(strings[0].equalsIgnoreCase("enable")) { //if the argument is enable
                    if(main.p.contains(p)) { //checks if already enabled
                        return true;
                    } else {
                        main.p.add(p); //enables the if it isn't
                    }
                } else if(strings[0].equalsIgnoreCase("disable")) { //if the argument is disable
                    if(main.p.contains(p)) { //checks if enabled
                        main.p.remove(p); //disables if enabled
                    } else { // does nothing if not enabled
                        return true;
                    }
                } else {
                    return false;
                }
            } else { // if there's no arguments
                if(main.p.contains(p)){ // checks if enabled
                    main.p.remove(p); // disabled if it is enabled
                } else {
                    main.p.add(p); // enabled if it is disabled
                }
            }
            return true;
        } else {
            commandSender.sendMessage("Only Players can use this command");
            return true;
        }
    }
}
