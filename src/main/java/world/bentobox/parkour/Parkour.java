package world.bentobox.parkour;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.admin.DefaultAdminCommand;
import world.bentobox.bentobox.api.commands.island.DefaultPlayerCommand;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.parkour.commands.CoursesCommand;
import world.bentobox.parkour.commands.RemoveWarpCommand;
import world.bentobox.parkour.commands.SetWarpCommand;
import world.bentobox.parkour.commands.TopCommand;
import world.bentobox.parkour.generators.ChunkGeneratorWorld;
import world.bentobox.parkour.gui.RankingsUI;
import world.bentobox.parkour.listeners.CourseRunnerListener;
import world.bentobox.parkour.listeners.MakeCourseListener;

/**
 * Main Parkour class
 * @author tastybento
 */
public class Parkour extends GameModeAddon implements Listener {

    private static final String NETHER = "_nether";
    private static final String THE_END = "_the_end";

    // Settings
    private Settings settings;
    private ChunkGenerator chunkGenerator;
    private final Config<Settings> configObject = new Config<>(this, Settings.class);

    // Manager
    private ParkourManager pm;
    private RankingsUI rankings;

    @Override
    public void onLoad() {
        // Save the default config from config.yml
        saveDefaultConfig();
        // Load settings from config.yml. This will check if there are any issues with it too.
        loadSettings();
        // Chunk generator
        chunkGenerator = settings.isUseOwnGenerator() ? null : new ChunkGeneratorWorld(this);
        // Register commands
        playerCommand = new DefaultPlayerCommand(this)

        {
            @Override
            public void setup()
            {
                super.setup();
                new TopCommand(this);
                new CoursesCommand(this);
                new SetWarpCommand(this);
                new RemoveWarpCommand(this);
            }
        };

        adminCommand = new DefaultAdminCommand(this) {};

        // Register listeners
        this.registerListener(new MakeCourseListener(this));
        this.registerListener(new CourseRunnerListener(this));
    }

    private boolean loadSettings() {
        // Load settings again to get worlds
        settings = configObject.loadConfigObject();
        if (settings == null) {
            // Disable
            logError("Parkour settings could not load! Addon disabled.");
            setState(State.DISABLED);
            return false;
        }
        return true;
    }

    @Override
    public void onEnable() {
        // Managers
        pm = new ParkourManager(this);
        // GUI
        rankings = new RankingsUI(this);
        // Register listeners
        registerListener(this);
    }

    @Override
    public void onDisable() {
        // Nothing to do here
    }

    @Override
    public void onReload() {
        if (loadSettings()) {
            log("Reloaded Parkour settings");
        }
    }

    /**
     * @return the settings
     */
    public Settings getSettings() {
        return settings;
    }

    @Override
    public void createWorlds() {
        String worldName = settings.getWorldName().toLowerCase();
        if (getServer().getWorld(worldName) == null) {
            log("Creating Parkour world ...");
        }

        // Create the world if it does not exist
        islandWorld = getWorld(worldName, World.Environment.NORMAL, chunkGenerator);
        // Make the nether if it does not exist
        if (settings.isNetherGenerate()) {
            if (getServer().getWorld(worldName + NETHER) == null) {
                log("Creating Parkour's Nether...");
            }
            netherWorld = settings.isNetherIslands() ? getWorld(worldName, World.Environment.NETHER, chunkGenerator) : getWorld(worldName, World.Environment.NETHER, null);
        }
        // Make the end if it does not exist
        if (settings.isEndGenerate()) {
            if (getServer().getWorld(worldName + THE_END) == null) {
                log("Creating Parkour's End World...");
            }
            endWorld = settings.isEndIslands() ? getWorld(worldName, World.Environment.THE_END, chunkGenerator) : getWorld(worldName, World.Environment.THE_END, null);
        }
    }

    /**
     * Gets a world or generates a new world if it does not exist
     * @param worldName2 - the overworld name
     * @param env - the environment
     * @param chunkGenerator2 - the chunk generator. If <tt>null</tt> then the generator will not be specified
     * @return world loaded or generated
     */
    private World getWorld(String worldName2, Environment env, ChunkGenerator chunkGenerator2) {
        // Set world name
        worldName2 = env.equals(World.Environment.NETHER) ? worldName2 + NETHER : worldName2;
        worldName2 = env.equals(World.Environment.THE_END) ? worldName2 + THE_END : worldName2;
        WorldCreator wc = WorldCreator.name(worldName2).type(WorldType.FLAT).environment(env);
        World w = settings.isUseOwnGenerator() ? wc.createWorld() : wc.generator(chunkGenerator2).createWorld();
        // Set spawn rates
        if (w != null) {
            setSpawnRates(w);
        }
        return w;

    }

    private void setSpawnRates(World w) {
        if (getSettings().getSpawnLimitMonsters() > 0) {
            w.setSpawnLimit(SpawnCategory.MONSTER, getSettings().getSpawnLimitMonsters());
        }
        if (getSettings().getSpawnLimitAmbient() > 0) {
            w.setSpawnLimit(SpawnCategory.AMBIENT, getSettings().getSpawnLimitAmbient());
        }
        if (getSettings().getSpawnLimitAnimals() > 0) {
            w.setSpawnLimit(SpawnCategory.ANIMAL, getSettings().getSpawnLimitAnimals());
        }
        if (getSettings().getSpawnLimitWaterAnimals() > 0) {
            w.setSpawnLimit(SpawnCategory.WATER_ANIMAL, getSettings().getSpawnLimitWaterAnimals());
        }
        if (getSettings().getTicksPerAnimalSpawns() > 0) {
            w.setTicksPerSpawns(SpawnCategory.ANIMAL, getSettings().getTicksPerAnimalSpawns());
        }
        if (getSettings().getTicksPerMonsterSpawns() > 0) {
            w.setTicksPerSpawns(SpawnCategory.MONSTER, getSettings().getTicksPerMonsterSpawns());
        }

    }

    @Override
    public WorldSettings getWorldSettings() {
        return getSettings();
    }

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return chunkGenerator;
    }

    @Override
    public void saveWorldSettings() {
        if (settings != null) {
            configObject.saveConfigObject(settings);
        }
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.addons.Addon#allLoaded()
     */
    @Override
    public void allLoaded() {
        // Reload settings and save them. This will occur after all addons have loaded
        this.loadSettings();
        this.saveWorldSettings();
    }

    /**
     * @return the pm
     */
    public ParkourManager getPm() {
        return pm;
    }

    /**
     * @return the rankings
     */
    public RankingsUI getRankings() {
        return rankings;
    }

}
