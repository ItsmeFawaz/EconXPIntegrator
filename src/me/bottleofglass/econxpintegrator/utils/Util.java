package me.bottleofglass.econxpintegrator.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Util {

    private static String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
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
    //Method breaks when used with ChestShop that sends 2 consecutive balance change events
    @Deprecated
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
    public static String msg(String s) {
        String msg = ChatColor.translateAlternateColorCodes('&', "&b[&eEconXPIntegrator&b] ") + ChatColor.YELLOW + s;
        return msg;
    }
    public static int getExpToNextLevel(int level) {
        int xp;
        if (level < 16) {
            xp = ((2*level) + 7);
        } else if (level <31) {
            xp = ((5*level)-38);
        } else {
            xp = ((9*level)-158);
        }
        return xp;
    }
    //brand new and improved xp setting method :D
    public static void setTotalExp(Player p , double xp) {
        double[] array = expToLevel(xp);
        p.setLevel((int) array[0]);
        float percentageToNextLevel = (float) (array[1] / p.getExpToLevel()); //divides the remaining xp by the amount of xp required to level up to get the percentage to next level
        if (percentageToNextLevel > 1) { // slight up downs in calculations cause percentage to go a little higher than 1 and to prevent it sets it to 1
            double exp = array[1] - p.getExpToLevel();
            p.setLevel(p.getLevel()+1);
            p.setExp((float) (exp / p.getExpToLevel()));
        } else {
            p.setExp( percentageToNextLevel);
        }
    }
}
