package world.bentobox.parkour.commands;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.panels.builders.TabbedPanelBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.parkour.Parkour;
import world.bentobox.parkour.gui.CoursesTab;

public class CoursesCommand extends CompositeCommand {

    public CoursesCommand(Parkour addon, CompositeCommand parent) {
        super(parent, "courses", "tracks");
    }

    @Override
    public void setup() {
        this.setPermission("parkour.courses");
        setOnlyPlayer(true);
        setDescription("parkour.commands.parkour.courses.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        new TabbedPanelBuilder()
        .user(user)
        .world(getWorld())
        .tab(1, new CoursesTab(getAddon(), user))
        .startingSlot(1)
        .size(54)
        .build().openPanel();
        return true;
    }

}
