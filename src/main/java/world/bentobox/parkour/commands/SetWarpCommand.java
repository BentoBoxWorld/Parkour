package world.bentobox.parkour.commands;

import java.util.List;
import java.util.Objects;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.parkour.Parkour;
import world.bentobox.parkour.ParkourManager;

public class SetWarpCommand extends CompositeCommand {



    public SetWarpCommand(CompositeCommand parent) {
        super(parent, "setwarp");
    }

    @Override
    public void setup() {
        this.setPermission("parkour.setwarp");
        setOnlyPlayer(true);
        setDescription("parkour.commands.parkour.setwarp.description");
        setConfigurableRankCommand();
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (!args.isEmpty()) {
            this.showHelp(this, user);
            return false;
        }
        if (!getIslands().userIsOnIsland(getWorld(), user)) {
            user.sendMessage("parkour.errors.not-on-island");
            return false;
        }
        // Check rank to use command
        Island island = getIslands().getIsland(getWorld(), user);
        int rank = Objects.requireNonNull(island).getRank(user);
        if (rank < island.getRankCommand(getUsage())) {
            user.sendMessage("general.errors.insufficient-rank", TextVariables.RANK, user.getTranslation(getPlugin().getRanksManager().getRank(rank)));
            return false;
        }
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        ParkourManager pm = ((Parkour)getAddon()).getPm();
        Island island = getIslands().getIsland(getWorld(), user);
        if (pm.getWarpSpot(island).isEmpty()) {
            user.sendMessage("parkour.warp.set");
        } else {
            user.sendMessage("parkour.warp.replaced");
        }
        pm.setWarpSpot(island, user.getLocation());

        return true;
    }

}
