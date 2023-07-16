package world.bentobox.parkour.commands;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.parkour.Parkour;
import world.bentobox.parkour.ParkourManager;

public class SetWarpCommand extends CompositeCommand {



    public SetWarpCommand(Parkour addon, CompositeCommand parent) {
        super(parent, "setwarp");
    }

    @Override
    public void setup() {
        this.setPermission("parkour.setwarp");
        setOnlyPlayer(true);
        setDescription("parkour.commands.parkour.setwarp.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (!getIslands().userIsOnIsland(getWorld(), user)) {
            user.sendMessage("parkour.errors.not-on-island");
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
