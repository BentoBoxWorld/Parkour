package world.bentobox.parkour.commands;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.parkour.Parkour;

public class KitCommand extends CompositeCommand {

    /**
     * About
     * @param islandCommand - parent command
     */
    public KitCommand(Parkour addon, CompositeCommand parent) {
        super(parent, "kit");
    }

    @Override
    public void setup() {
        setDescription("commands.parkour.kit.description");
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        ItemStack plate = new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        ItemMeta meta = plate.getItemMeta();
        meta.setDisplayName("Start/End Plate");
        plate.setItemMeta(meta);
        user.getInventory().addItem(plate);
        return true;
    }

}
