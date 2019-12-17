package me.bottleofglass.econxpintegrator.listeners;

import com.Zrips.CMI.Modules.Economy.Economy;
import com.Zrips.CMI.events.CMIUserBalanceChangeEvent;
import me.bottleofglass.econxpintegrator.Main;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;

public class EventListener implements Listener {

    HashMap<Player, Double> hm = new HashMap<>();

    @EventHandler
    public void onCMIUserBalanceChangeEvent(CMIUserBalanceChangeEvent e) { //Changes xp to match new user money whenever money changes
        Player p = e.getUser().getPlayer();
        if (hm.containsKey(p) && hm.get(p) == e.getFrom()) {
            return;
        } else {
            e.getUser().setExp((int) Economy.getBalance(e.getUser().getPlayer()));
        }


    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent evt) {
        evt.getPlayer().setTotalExperience((int) Economy.getBalance(evt.getPlayer()));
    }
    @EventHandler
    public void xpChange(PlayerExpChangeEvent e){ // Changes money to match new user xp whenever xp changes;
        Player p = e.getPlayer();
        Main.getEconomy().depositPlayer(p, e.getAmount());
        if(hm.containsKey(p)) {
            hm.replace(p, (double) p.getTotalExperience());
        } else {
            hm.put(p, (double) p.getTotalExperience());
        }

    }

}
