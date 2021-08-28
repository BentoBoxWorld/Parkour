package world.bentobox.parkour.listeners;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;

import org.bukkit.Location;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.Parkour;

public abstract class AbstractListener implements Listener {

    protected static final DecimalFormat DF2 = new DecimalFormat("#.##");
    {
        DF2.setRoundingMode(RoundingMode.UP);
    }
    protected static final String START = "Parkour_Start";
    protected static final String END = "Parkour_End";
    protected Parkour addon;
    /**
     * @param addon
     */
    public AbstractListener(Parkour addon) {
        this.addon = addon;

    }

    protected boolean isLocEquals(Location l1, String l2) {
        Location l3 = Util.getLocationString(l2);
        return l1.getWorld().equals(l3.getWorld()) && l1.getBlockX() == l3.getBlockX() && l1.getBlockY() == l3.getBlockY() && l1.getBlockZ() == l3.getBlockZ();
    }

    public static String getDuration(User user, long time) {
        double dur = time/1000D;
        String timeDescription = DF2.format(dur) + " " + user.getTranslationOrNothing("parkour.seconds");
        if (dur > 60) {
            Duration d = Duration.ofMillis(time);
            if (dur < 3600) {
                // Display in mins/seconds
                timeDescription = d.toMinutesPart() + user.getTranslationOrNothing("parkour.minutes-short") + d.toSecondsPart() + user.getTranslationOrNothing("parkour.seconds-short");
            } else {
                timeDescription = d.toHoursPart() + user.getTranslationOrNothing("parkour.hours-short") + d.toMinutesPart() + user.getTranslationOrNothing("parkour.minutes-short") + d.toSecondsPart() + user.getTranslationOrNothing("parkour.seconds-short");
            }

        }
        return timeDescription;
    }
}
