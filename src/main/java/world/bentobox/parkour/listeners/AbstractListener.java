package world.bentobox.parkour.listeners;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.bukkit.Location;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.Parkour;

public abstract class AbstractListener implements Listener {

    protected static DecimalFormat df2 = new DecimalFormat("#.##");
    protected static final String START = "Parkour_Start";
    protected static final String END = "Parkour_End";
    protected Parkour addon;
    /**
     * @param addon
     */
    public AbstractListener(Parkour addon) {
        this.addon = addon;
        df2.setRoundingMode(RoundingMode.UP);
    }

    protected boolean isLocEquals(Location l1, String l2) {
        Location l3 = Util.getLocationString(l2);
        return l1.getWorld().equals(l3.getWorld()) && l1.getBlockX() == l3.getBlockX() && l1.getBlockY() == l3.getBlockY() && l1.getBlockZ() == l3.getBlockZ();
    }


}
