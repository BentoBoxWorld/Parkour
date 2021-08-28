package world.bentobox.parkour.generators;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import world.bentobox.parkour.Parkour;

/**
 * @author tastybento
 *         Creates the world
 */
public class ChunkGeneratorWorld extends ChunkGenerator {

    /**
     * @param addon - addon
     */
    public ChunkGeneratorWorld(Parkour addon) {
        super();
    }


    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid) {
        return createChunkData(world);
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(final World world) {
        return Collections.emptyList();
    }

}