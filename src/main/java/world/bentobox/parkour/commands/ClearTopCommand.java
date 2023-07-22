package world.bentobox.parkour.commands;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.commands.ConfirmableCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.parkour.Parkour;
import world.bentobox.parkour.ParkourManager;

/**
 * Clears scores from the top ten
 * @author tastybento
 *
 */
public class ClearTopCommand extends ConfirmableCommand {

    /**
     * Target user to clear score
     */
    private @Nullable UUID targetUUID;
    private Parkour addon;

    /**
     * @param parent parent command
     */
    public ClearTopCommand(CompositeCommand parent) {
        super(parent, "cleartop");
    }

    @Override
    public void setup() {
        this.setPermission("parkour.cleartop");
        setOnlyPlayer(true);
        setDescription("parkour.commands.parkour.cleartop.description");
        this.setParametersHelp(".commands.parkour.cleartop.parameters");
        setConfigurableRankCommand();
        this.setDefaultCommandRank(RanksManager.OWNER_RANK);
        addon = getAddon();
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (args.size() > 1) {
            this.showHelp(this, user);
            return false;
        }
        if (!getIslands().hasIsland(getWorld(), user) && !getIslands().inTeam(getWorld(), user.getUniqueId())) {
            user.sendMessage("general.errors.no-island");
            return false;
        }
        // Check rank to use command
        Island island = getIslands().getIsland(getWorld(), user);
        int rank = Objects.requireNonNull(island).getRank(user);
        if (rank < island.getRankCommand(getUsage())) {
            user.sendMessage("general.errors.insufficient-rank", TextVariables.RANK, user.getTranslation(getPlugin().getRanksManager().getRank(rank)));
            return false;
        }
        // Check the name of the score to clear
        if (args.size() == 1) {
            // Get target
            targetUUID = Util.getUUID(args.get(0));
            if (targetUUID == null) {
                user.sendMessage("general.errors.unknown-player", TextVariables.NAME, args.get(0));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        this.askConfirmation(user, () -> confirmed(user, label, args));
        return true;
    }

    void confirmed(User user, String label, List<String> args) {
        Island island = getIslands().getIsland(getWorld(), user);
        if (island != null && targetUUID == null) {
            addon.getPm().clearScores(island);
        } else if (island != null && targetUUID != null) {
            addon.getPm().removeScore(island, User.getInstance(targetUUID));
        } else {
            user.sendMessage("general.errors.no-island");
            return;
        }
        user.sendMessage("general.success");
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String label, List<String> args) {
        Island island = getIslands().getIsland(getWorld(), user);
        return Optional.of(addon.getPm().getRankings(island, 10).keySet().stream().map(getAddon().getPlayers()::getName)
                .filter(name -> !name.isBlank()).toList());
    }

}
