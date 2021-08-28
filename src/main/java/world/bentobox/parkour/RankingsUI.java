package world.bentobox.parkour;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Material;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.parkour.listeners.AbstractListener;

/**
 * Displays the island rankings to players
 * @author tastybento
 *
 */
public class RankingsUI {

    private static final int[] SLOTS = new int[] {4, 12, 14, 19, 20, 21, 22, 23, 24, 25};

    private final Parkour addon;
    // Background
    private final PanelItem background;

    /**
     * @param addon Parkour addon
     */
    public RankingsUI(Parkour addon) {
        this.addon = addon;
        // Background
        background = new PanelItemBuilder().icon(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
    }



    /**
     * Displays the rankings in a GUI
     * @param island - island
     * @param user - the requesting player
     */
    public void getGUI(Island island, final User user) {

        PanelBuilder panel = new PanelBuilder()
                .name(user.getTranslation("parkour.top.gui-title"))
                .size(54)
                .user(user);
        // Background
        for (int j = 0; j < 54; panel.item(j++, background));

        // Top Ten
        int i = 0;
        boolean inTopTen = false;
        for (Entry<UUID, Long> m : addon.getPm().getRankings(island, 10).entrySet()) {
            PanelItem h = getHead((i+1), m.getValue(), m.getKey(), user);
            panel.item(SLOTS[i], h);
            // If this is also the asking player
            if (m.getKey().equals(user.getUniqueId())) {
                inTopTen = true;
                addSelf(island, user, panel);
            }
            i++;
        }
        // Show remaining slots
        for (; i < SLOTS.length; i++) {
            panel.item(SLOTS[i], new PanelItemBuilder().icon(Material.GREEN_STAINED_GLASS_PANE).name(String.valueOf(i + 1)).build());
        }

        // Add yourself if you were not already in the top ten
        if (!inTopTen) {
            addSelf(island, user, panel);
        }
        panel.build();
    }

    private void addSelf(Island island, User user, PanelBuilder panel) {
        long time = addon.getPm().getTime(island, user.getUniqueId());
        if (time > 0) {
            PanelItem head = getHead(addon.getPm().getRank(island, user.getUniqueId()), time, user.getUniqueId(), user);
            panel.item(49, head);
        }

    }

    /**
     * Get the head panel item
     * @param rank - the top ten rank of this player.
     * @param time - time taken
     * @param playerUUID - the UUID of the top ten player
     * @param asker - the asker of the top ten
     * @return PanelItem head
     */
    private PanelItem getHead(int rank, long time, UUID playerUUID, User asker) {
        final String name = addon.getPlayers().getName(playerUUID);
        List<String> description = new ArrayList<>();
        if (rank > 0) {
            description.add(asker.getTranslation("parkour.top.name-rank", "[name]", name, "[rank]", String.valueOf(rank)));
        }
        description.add(asker.getTranslation("parkour.top.description","[number]", AbstractListener.getDuration(asker, time)));

        PanelItemBuilder builder = new PanelItemBuilder()
                .icon(name)
                .name(name)
                .description(description);
        return builder.build();
    }
}
