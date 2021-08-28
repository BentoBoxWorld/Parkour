package world.bentobox.parkour.objects;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;

/**
 * @author tastybento
 *
 */
@Table(name = "HighScores")
public class HighScores implements DataObject {

    /**
     * uniqueId is the island's UUID
     */
    @Expose
    private String uniqueId = "";

    /**
     * Player UUID key, time taken in milliseconds
     */
    @Expose
    private Map<UUID, Long> rankings;

    @Expose
    private int runCount;

    public HighScores(String uniqueId2) {
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


}
