package me.bottleofglass.econxpintegrator.listeners;

import com.Zrips.CMI.events.CMIAnvilItemRenameEvent;
import com.Zrips.CMI.events.CMIAnvilItemRepairEvent;
import com.Zrips.CMI.events.CMIUserBalanceChangeEvent;
import me.bottleofglass.econxpintegrator.Main;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.*;
import java.util.logging.Logger;

public class EventListener implements Listener {

    Logger log = Main.getLog();

    private static String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    Set<Player> set = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBalanceChange(CMIUserBalanceChangeEvent evt) { //Gets balance change and sets XP according to that
        Player p = evt.getUser().getPlayer();
        if(set.contains(p)) {//List would contain the player if the Balance change was done by setMoney and hence cancel this event
            set.remove(p);
            log.info("BalanceChangeEvent Ignored");
        } else {
            //sets XP to balance
            setPlayerExperience(p, evt.getTo());
        }
    }
    //updates balance whenever players get xp naturally
    @EventHandler
    public void onExpChange(PlayerExpChangeEvent evt) {

        Player p = evt.getPlayer();

        setMoney(p, (getPlayerExperience(p) + evt.getAmount()));
        //logs
        log.info(p.getName() + "'s balance changed to " + (getPlayerExperience(p) + evt.getAmount()) + "from " + getPlayerExperience(p) + "due to xp change(NATURAL)");
        p.sendMessage(p.getName() + "'s balance changed to " + (getPlayerExperience(p) + evt.getAmount()) + "from " + getPlayerExperience(p) + "due to xp change(NATURAL)");
    }
    //updates balance upon item enchant
    @EventHandler
    public void onEnchant(EnchantItemEvent evt) {

        Player p = evt.getEnchanter();

        int newlevel = (p.getLevel() - (evt.whichButton()+1));
        int newExperience = (levelToExp(newlevel) + (int) (deltaLevelToExp(newlevel) * p.getExp()));

        setMoney(p, newExperience);

        log.info(p.getName() + "'s balance changed to " +  newExperience +" from "+ getPlayerExperience(p) + "due to xp change(ENCHANT) by pressing" + evt.whichButton());

        p.sendMessage(p.getName() + "'s balance changed to " +  newExperience +" from "+ getPlayerExperience(p) + "due to xp change(ENCHANT)");
    }

    //updates balance upon anvil repair
    @EventHandler
    public void onAnvilRepair(CMIAnvilItemRepairEvent evt) {
        Player p = evt.getPlayer();
        int newlevel = p.getLevel() - evt.getRepairCost();
        int newExperience = (levelToExp(newlevel) + (int) (deltaLevelToExp(newlevel) * p.getExp()));
        setMoney(p, newExperience);

        log.info(p.getName() + "'s balance changed to " +  newExperience + " from " + getPlayerExperience(p)+  "due to xp change(REPAIR)");
        p.sendMessage(p.getName() + "'s balance changed to " +  newExperience+ " from "+ getPlayerExperience(p) + "due to xp change(REPAIR)");
    }
    //updates balance upon anvil rename
    @EventHandler
    public void onAnvilRename(CMIAnvilItemRenameEvent evt) {
        Player p = evt.getPlayer();
        int newlevel = p.getLevel() - evt.getCost();
        int newExperience = (levelToExp(newlevel) + (int) (deltaLevelToExp(newlevel) * p.getExp()));
        setMoney(p, newExperience);

        log.info(p.getName() + "'s balance changed to " +  newExperience + " from " + getPlayerExperience(p)+  "due to xp change(REPAIR)");
        p.sendMessage(p.getName() + "'s balance changed to " +  newExperience+ " from "+ getPlayerExperience(p) + "due to xp change(REPAIR)");
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
            newmoney = getPlayerExperience(p) * percentage / 100;
            double[] array = expToLevel(newmoney);
            evt.setNewLevel((int) array[0]);
            evt.setNewExp((int) array[1]);
        } else {
            setMoney(p, 0);
        }
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
    // total xp calculation based by lvl
    public static int levelToExp(int level) {

        if (level <= 15) {
            return (int) (level * level + 6 * level);
        } else if (level <= 30) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }
    // xp calculation for one current lvl
    public static int deltaLevelToExp(int level) {
        if (version.contains("1_7")) {
            if (level <= 15) {
                return 17;
            } else if (level <= 30) {
                return 3 * level - 31;
            } else {
                return 7 * level - 155;
            }
        } else {
            if (level <= 15) {
                return 2 * level + 7;
            } else if (level <= 30) {
                return 5 * level - 38;
            } else {
                return 9 * level - 158;
            }
        }
    }
    // xp calculation for one current lvl
    public static int currentlevelxpdelta(Player player) {
        int levelxp = deltaLevelToExp(player.getLevel()) - ((levelToExp(player.getLevel()) + (int) (deltaLevelToExp(player.getLevel()) * player.getExp())) - levelToExp(player.getLevel()));
        return levelxp;
    }
    public static int getPlayerExperience(Player player) {
        int bukkitExp = (levelToExp(player.getLevel()) + (int) (deltaLevelToExp(player.getLevel()) * player.getExp()));
        return bukkitExp;
    }

    public static void setPlayerExperience(Player p , double xp) {

        p.setLevel(0);
        p.setExp(0);
        p.setTotalExperience(0);
        p.giveExp((int) xp);
    }
    //turns players total xp to level and remaining xp
    public static double[] expToLevel(double xp) {
        int level = 0;
        while (xp >= 0) {
            if(level < 16) {
                if((xp-(7+(level*2))) <= 0) {
                    break;
                }
                xp = xp-(7+(level*2));
                level++;
            } else if (level < 32) {
                if ((xp-((7+ (level*2) + (level-15)*5))) <= 0) {
                    break;
                }
                xp = xp-((7 + (level*2)) + (level-15)*5);
                level++;
            } else {
                if((xp-(7+((15*2) + (16 * 5) + ((level-31)*9)))) <= 0) {
                    break;
                }
                xp = xp-(7+((15*2) + (16 * 5) + ((level-31)*9)));
                level++;
            }


        }
        double[] array = new double[2];
        array[0] = level;
        array[1] = xp;
        return array;
    }





}