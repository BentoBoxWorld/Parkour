package world.bentobox.parkour.objects;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;

/**
 * @author tastybento
 *
 */
@Table(name = "Parkour")
public class ParkourData implements DataObject, Comparable<Integer> {

    /**
     * uniqueId is the island's UUID
     */
    @Expose
    private String uniqueId;
    @Expose
    private Location start;
    @Expose
    private Location end;
    @Expose
    private Location warpSpot;

    /**
     * Player UUID key, time taken in milliseconds
     */
    @Expose
    private Map<UUID, Long> rankings;

    @Expose
    private int runCount;

    public ParkourData(String uniqueId2) {
        this.uniqueId = uniqueId2;
    }

    /**
     * @return the rankings
     */
    public Map<UUID, Long> getRankings() {
        if (rankings == null) {
            rankings = new LinkedHashMap<>();
        }
        return rankings;
    }

    /**
     * @param rankings the rankings to set
     */
    public void setRankings(Map<UUID, Long> rankings) {
        this.rankings = rankings;
    }

    /**
     * @return the runCount
     */
    public int getRunCount() {
        return runCount;
    }

    /**
     * @param runCount the runCount to set
     */
    public void setRunCount(int runCount) {
        this.runCount = runCount;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public int compareTo(Integer o) {
        return Integer.compare(runCount, o);
    }

    /**
     * @return the start
     */
    public Location getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(Location start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public Location getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(Location end) {
        this.end = end;
    }

    /**
     * @return the warpSpot
     */
    public Location getWarpSpot() {
        return warpSpot;
    }

    /**
     * @param warpSpot the warpSpot to set
     */
    public void setWarpSpot(Location warpSpot) {
        this.warpSpot = warpSpot;
    }


}
