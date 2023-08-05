package world.bentobox.parkour.listeners;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;

import org.bukkit.Location;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.parkour.Parkour;

public abstract class AbstractListener implements Listener {

    protected static final DecimalFormat DF2 = new DecimalFormat("#.##");
    static {
        DF2.setRoundingMode(RoundingMode.UP);
    }
    protected Parkour addon;
    /**
     * @param addon Parkour addon
     */
    protected AbstractListener(Parkour addon) {
        this.addon = addon;

    }

    /**
     * Compares two Locations
     * @param l1 location 1
     * @param l2 location 2
     * @return returns true if the worlds and block values are the same
     */
    protected boolean isLocEquals(Location l1, Location l2) {
        return l1.getWorld().equals(l2.getWorld()) && l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ();
    }

    /**
     * Returns a string describing the time taken by this user
     * @param user user
     * @param time time take in milliseconds
     * @return description
     */
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
