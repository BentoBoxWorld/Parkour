package world.bentobox.parkour;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

public class ParkourPladdon extends Pladdon {

    @Override
    public Addon getAddon() {
        return new Parkour();
    }
}
