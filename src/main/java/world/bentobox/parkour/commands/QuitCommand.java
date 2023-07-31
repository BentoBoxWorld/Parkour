package world.bentobox.parkour.commands;

import java.util.List;
import java.util.Optional;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.parkour.Parkour;

/**
 * Quits the course
 *
 */
public class QuitCommand extends CompositeCommand {

    public QuitCommand(CompositeCommand parent) {
        super(parent, "quit");
    }

    @Override
    public void setup() {
        this.setPermission("parkour.quit");
        setOnlyPlayer(true);
        setDescription("parkour.commands.parkour.quit.description");
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

        if (!((Parkour) getAddon()).getParkourRunManager().getTimers().containsKey(user.getUniqueId())) {
            user.sendMessage("parkour.errors.not-in-run");
            return false;
        }

        return true;
    }


    @Override
    public boolean execute(User user, String label, List<String> args) {

        ((Parkour)getAddon()).getParkourRunManager().clear(user.getUniqueId());
        user.sendMessage("parkour.quit.success");
        return true;
    }


}
