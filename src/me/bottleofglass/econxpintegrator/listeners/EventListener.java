package me.bottleofglass.econxpintegrator.listeners;

import com.Zrips.CMI.Modules.Economy.Economy;
import com.Zrips.CMI.events.CMIAnvilItemRenameEvent;
import com.Zrips.CMI.events.CMIAnvilItemRepairEvent;
import com.Zrips.CMI.events.CMIUserBalanceChangeEvent;
import me.bottleofglass.econxpintegrator.Main;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class EventListener implements Listener {

    List<Player> l = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCMIUserBalanceChangeEvent(CMIUserBalanceChangeEvent e) { //Changes xp to match new user money whenever money changes
        Player p = e.getUser().getPlayer();
        if (!(l.isEmpty()) && l.contains(p)) {
            l.remove(p);
            Main.getLog().info(p.getName() + "'s XP wasn't changed back");
        } else {
            e.getUser().setExp((int) Economy.getBalance(e.getUser().getPlayer()));
            Main.getLog().info(p.getName() + "'s XP changed to " + Economy.getBalance(p) + "due to balance change");
        }


    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent evt) {
        evt.getPlayer().setTotalExperience((int) Economy.getBalance(evt.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void xpChange(PlayerExpChangeEvent e) { // Changes money to match new user xp whenever xp changes;
        Player p = e.getPlayer();
        l.add(p);
        setMoney(p, p.getTotalExperience());
        Main.getLog().info(p.getName() + "'s Balance changed to " + p.getTotalExperience() + "due to XP Change");


    }

    private void setMoney(Player p, double value) {
        double balance = Economy.getBalance(p);
        EconomyResponse er = null;
        if (value < balance) {
            er = Economy.withdrawPlayer(p, balance - value);
        } else if (value > balance) {
            er = Economy.depositPlayer(p, value - balance);
        }
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent evt) {
        if (!(evt.getEntity().hasPermission("cmi.keepexp"))) {
            setMoney(evt.getEntity(), 0);
            Main.getLog().info(evt.getEntity().getName() + "'s Balance changed to " + evt.getEntity().getTotalExperience() + "due to XP Change");
        }
    }
    @EventHandler
    public void onAnvilRename(CMIAnvilItemRenameEvent evt) {
        Player p = evt.getPlayer();
        setMoney(p, Economy.getBalance(p) - evt.getCost());
        Main.getLog().info(p.getName() + "'s Balance changed to " + p.getTotalExperience() + "due to XP Change");
    }
    @EventHandler
    public void onAnvilRepair(CMIAnvilItemRepairEvent evt) {
        Player p = evt.getPlayer();
        setMoney(p, Economy.getBalance(p) - evt.getRepairCost());
        Main.getLog().info(p.getName() + "'s Balance changed to " + p.getTotalExperience() + "due to XP Change");
    }

}
