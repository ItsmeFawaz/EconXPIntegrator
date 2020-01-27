package me.bottleofglass.econxpintegrator.listeners;

import com.Zrips.CMI.events.CMIAnvilItemRenameEvent;
import com.Zrips.CMI.events.CMIAnvilItemRepairEvent;
import com.Zrips.CMI.events.CMIUserBalanceChangeEvent;
import me.bottleofglass.econxpintegrator.Main;
import me.bottleofglass.econxpintegrator.utils.Util;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class EventListener implements Listener {

    Logger log = Main.getLog();

    Set<Player> set = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBalanceChange(CMIUserBalanceChangeEvent evt) { //Gets balance change and sets XP according to that
        Player p = Bukkit.getPlayerExact(evt.getUser().getDisplayName());
        if(set.contains(p)) {//List would contain the player if the Balance change was done by setMoney and hence cancel this event
            set.remove(p);
            if (Main.isLogsEnabled) {
                log.info(p.getName() + "'s BalanceChangeEvent Ignored");
            }

        } else {
            //sets XP to balance
            Util.setTotalExp(p, evt.getTo());
            /*p.sendMessage("Balance: " + Main.getEconomy().getBalance(p));
            p.sendMessage("Experience: " + Util.getPlayerExperience(p)); */
            if (Main.isLogsEnabled) {
                log.info("");
            }
        }
    }
    //updates balance whenever players get xp naturally
    @EventHandler
    public void onExpChange(PlayerExpChangeEvent evt) {

        Player p = evt.getPlayer();

        setMoney(p, (Util.getPlayerExperience(p) + evt.getAmount()));
        //logs
        if (Main.isLogsEnabled) {
            log.info(p.getName() + "'s balance changed to " + (Util.getPlayerExperience(p) + evt.getAmount()) + " from " + Util.getPlayerExperience(p) + "to match natural exp change");
        }
        if (Main.p.contains(p)) {
            p.sendMessage(Util.msg(p.getName() + "'s balance changed to " + (Util.getPlayerExperience(p) + evt.getAmount()) + " from " + Util.getPlayerExperience(p) + "to match natural exp change"));
        }
    }
    //updates balance upon item enchant
    @EventHandler
    public void onEnchant(EnchantItemEvent evt) {

        Player p = evt.getEnchanter();

        int newlevel = (p.getLevel() - (evt.whichButton()+1));
        int newExperience = (Util.levelToExp(newlevel) + (int) (Util.deltaLevelToExp(newlevel) * p.getExp()));

        setMoney(p, newExperience);

        if(Main.isLogsEnabled) {
            log.info(p.getName() + "'s balance changed to " +  newExperience +" from "+ Util.getPlayerExperience(p) + " to match exp change from enchanting");
        }
        if(Main.p.contains(p)) {
            p.sendMessage(Util.msg(p.getName() + "'s balance changed to " +  newExperience +" from "+ Util.getPlayerExperience(p) + " to match exp change from enchanting"));
        }
    }

    //updates balance upon anvil repair
    @EventHandler
    public void onAnvilRepair(CMIAnvilItemRepairEvent evt) {
        Player p = evt.getPlayer();
        int newlevel = p.getLevel() - evt.getRepairCost();
        int newExperience = (Util.levelToExp(newlevel) + (int) (Util.deltaLevelToExp(newlevel) * p.getExp()));
        setMoney(p, newExperience);

        if(Main.isLogsEnabled) {
            log.info(p.getName() + "'s balance changed to " +  newExperience + " from " + Util.getPlayerExperience(p)+  "to match exp change from item repair");
        }
        if(Main.p.contains(p)) {
            p.sendMessage(Util.msg(p.getName() + "'s balance changed to " +  newExperience + " from " + Util.getPlayerExperience(p)+  "to match exp change from item repair"));
        }
    }
    //updates balance upon anvil rename
    @EventHandler
    public void onAnvilRename(CMIAnvilItemRenameEvent evt) {
        Player p = evt.getPlayer();
        int newlevel = p.getLevel() - evt.getCost();
        int newExperience = (Util.levelToExp(newlevel) + (int) (Util.deltaLevelToExp(newlevel) * p.getExp()));
        setMoney(p, newExperience);

        if(Main.isLogsEnabled) {
            log.info(p.getName() + "'s balance changed to " +  newExperience + " from " + Util.getPlayerExperience(p)+  "to match exp change from item rename");
        }
        if (Main.p.contains(p)) {
            p.sendMessage(Util.msg(p.getName() + "'s balance changed to " +  newExperience + " from " + Util.getPlayerExperience(p)+  "to match exp change from item rename"));
        }
    }
    //catches player death and sets xp/balance to a percentage depending on permission
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent evt) {
        Player p = evt.getEntity();
        p.sendMessage("Player Death Event caught");
        if(p.hasPermission("cmi.keepexp")) {
            p.sendMessage("You have keepexp enabled so balance not changed");
            return;
        }
        int percentage = -1;
        for(int i = 100;i > -1 ; i--) {
            if(p.hasPermission("expecon.xppercentage." + i)) {
                percentage = i;
                p.sendMessage("Percentage found " + i);
                break;
            }
        }
        int newmoney;
        if(percentage != -1) {
            newmoney = Util.getPlayerExperience(p) * percentage / 100;
            double[] array = Util.expToLevel(newmoney);
            evt.setNewLevel((int) array[0]);
            evt.setNewExp((int) array[1]);
            setMoney(p, newmoney);
            if (Main.p.contains(p)) {
                p.sendMessage(Util.msg(p.getName() + "'s balance changed to " + percentage + "% of your previous balance as a result of death"));
            }
            if (Main.isLogsEnabled) {
                log.info(p.getName() + "'s balance changed to " + percentage + "% of your previous balance as a result of death");
            }

        } else {
            setMoney(p, 0);
        }
    }
    @EventHandler
    public void onCMIExpCommand(PlayerCommandPreprocessEvent evt) {
        if(evt.isCancelled()) {
            return;
        }
        Player p = evt.getPlayer();
        String[] command = evt.getMessage().split(" ");
        if (command[0].equalsIgnoreCase("cmi")) {
            if(command[1].equalsIgnoreCase("exp")) {
                if(command.length > 3) {
                    if (Bukkit.getServer().getPlayer(command[2]) != null) {
                        String subcmd = command[3];
                        int amount = Integer.parseInt(command[4]);
                        switch (subcmd){
                            case "set":
                                setMoney(p, amount);
                                break;
                            case "give":
                                setMoney(p, Main.getEconomy().getBalance(p) + amount);
                                break;
                            case "take":
                                setMoney(p, Main.getEconomy().getBalance(p) - amount);
                                break;
                            case "clear":
                                setMoney(p, 0);
                                break;
                            default:
                                break;
                        }
                    } else {
                        String subcmd = command[2];
                        int amount = Integer.parseInt(command[3]);
                        switch (subcmd){
                            case "set":
                                setMoney(p, amount);
                                break;
                            case "give":
                                setMoney(p, Main.getEconomy().getBalance(p) + amount);
                                break;
                            case "take":
                                setMoney(p, Main.getEconomy().getBalance(p) - amount);
                                break;
                            case "clear":
                                setMoney(p, 0);
                                break;
                            default:
                                break;
                        }
                    }

                }
            }
        }
    }
    @EventHandler
    public void onServerCommand(ServerCommandEvent evt) {
        if(evt.isCancelled()) {
            return;
        }
        String[] command = evt.getCommand().split(" ");
        if (command[0].equalsIgnoreCase("cmi")) {
            if(command[1].equalsIgnoreCase("exp")) {
                if(command.length > 3) {
                    if (Bukkit.getPlayer(command[2]) == null) return;
                    Player p = Bukkit.getPlayer(command[2]);
                    String subcmd = command[3];
                    int amount = Integer.parseInt(command[4]);
                    switch (subcmd){
                        case "set":
                            setMoney(p, amount);
                            break;
                        case "give":
                            setMoney(p, Main.getEconomy().getBalance(p) + amount);
                            break;
                        case "take":
                            setMoney(p, Main.getEconomy().getBalance(p) - amount);
                            break;
                        case "clear":
                            setMoney(p, 0);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player p = evt.getPlayer();
        Util.setTotalExp(p, Main.getEconomy().getBalance(p));
    }

    //sets player money through single economy method
    public boolean setMoney(Player p , double value) {
        double balance = Main.getEconomy().getBalance(p);
        EconomyResponse er;
        if (value > balance) {
            er = Main.getEconomy().depositPlayer(p, (value - balance));
            set.add(p);
        } else if (value < balance) {
            er = Main.getEconomy().withdrawPlayer(p, (balance - value));
            set.add(p);
        } else {
            return true;
        }
        return er.transactionSuccess();
    }


}