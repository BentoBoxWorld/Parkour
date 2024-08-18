package world.bentobox.parkour.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.Tab;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.Parkour;
import world.bentobox.parkour.objects.ParkourData;

/**
 * Implements a {@link Tab} that shows course rankings
 *
 * @author tastybento
 * @since 1.0.0
 */
public class CoursesTab implements Tab {

    private final Parkour addon;
    private final User user;

    /**
     * Show a tab of settings
     *
     * @param addon - addon
     * @param user  - user who is viewing the tab
     */
    public CoursesTab(Parkour addon, User user) {
        super();
        this.addon = addon;
        this.user = user;
    }

    /**
     * Get the icon for this tab
     *
     * @return panel item
     */
    @Override
    public PanelItem getIcon() {
        PanelItemBuilder pib = new PanelItemBuilder();
        // Set the icon
        pib.icon(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
        pib.name(getName());
        pib.description(user.getTranslation("parkour.courses.description"));
        return pib.build();
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.panels.Tab#getName()
     */
    @Override
    public String getName() {
        return user.getTranslation("parkour.courses.gui-title");
    }

    /**
     * Get all the flags as panel items
     *
     * @return list of all the panel items for this flag type
     */
    @Override
    @NonNull
    public List<@Nullable PanelItem> getPanelItems() {
        // Create the list
        List<PanelItem> heads = new ArrayList<>();
        // Sort the courses by runs
        addon.getParkourManager().getParkourData().stream()
                .sorted()
                .filter(hs -> Objects.nonNull(hs.getWarpSpot()))
                .forEach(hs -> {
                    addon.getIslands().getIslandById(hs.getUniqueId()).ifPresent(is -> {
                        if (is.getOwner() != null) {
                            heads.add(getHead(hs, is));
                        }
                    });
                });
        return heads;
    }

    /**
     * Get the head panel item
     *
     * @param pd         - parkour data
     * @param is.getOwner() - the UUID of the owner
     * @return PanelItem
     */
    private PanelItem getHead(ParkourData pd, Island is) {
        final String name = addon.getPlayers().getName(is.getOwner());
        List<String> description = new ArrayList<>();
        if (pd.getRunCount() > 0) {
            description.add(user.getTranslation("parkour.courses.head-description", "[name]", name, "[runs]", String.valueOf(pd.getRunCount())));
        }
        if (addon.getIslands().inTeam(addon.getOverWorld(), is.getOwner())) {
            List<String> memberList = new ArrayList<>();
            for (UUID members : is.getMemberSet()) {
                memberList.add(ChatColor.AQUA + addon.getPlayers().getName(members));
            }
            description.addAll(memberList);
        }

        PanelItemBuilder builder = new PanelItemBuilder()
                .icon(name)
                .name(name)
                .clickHandler((panel, user, clickType, slot) -> {
                    user.sendMessage("parkour.warp.warping");
                    // Teleport user
                    Util.teleportAsync(user.getPlayer(), pd.getWarpSpot(), TeleportCause.COMMAND);
                    return true;
                })
                .description(description);
        return builder.build();
    }

    @Override
    public Map<Integer, PanelItem> getTabIcons() {
        return new HashMap<>();
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.panels.Tab#getPermission()
     */
    @Override
    public String getPermission() {
        // All of these tabs can be seen by anyone
        return "";
    }

}
