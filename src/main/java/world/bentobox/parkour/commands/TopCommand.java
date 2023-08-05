package world.bentobox.parkour.commands;

import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.parkour.Parkour;

/**
 * Top ten command. Show a Top Ten GUI for the course.
 * @author tastybento
 *
 */
public class TopCommand extends CompositeCommand {

    private Island island;

    public TopCommand(CompositeCommand parent) {
        super(parent, "top");
    }

    @Override
    public void setup() {
        this.setPermission("top");
        setOnlyPlayer(true);
        setDescription("parkour.commands.parkour.top.description");
    }
    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (!getPlugin().getIWM().inWorld(getWorld())) {
            user.sendMessage("general.errors.wrong-world");
            return false;
        }
        Optional<Island> opIsland = getIslands().getIslandAt(user.getLocation());
        if (opIsland.isEmpty()) {
            user.sendMessage("parkour.errors.not-on-island");
            return false;
        }
        island = opIsland.get();
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        ((Parkour)getAddon()).getRankings().getGUI(island, user);
        return true;
    }

}
