package world.bentobox.parkour;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author tastybento
 */
class SettingsTest extends CommonTestSetup {

    private Settings s;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        s = new Settings();
    }

    /**
     * Test method for {@link Settings#getFriendlyName()}.
     */
    @Test
    void testGetFriendlyName() {
        assertEquals("Parkour", s.getFriendlyName());
    }

    /**
     * Test method for {@link Settings#getWorldName()}.
     */
    @Test
    void testGetWorldName() {
        assertEquals("parkour_world", s.getWorldName());
    }

    /**
     * Test method for {@link Settings#getDifficulty()}.
     */
    @Test
    void testGetDifficulty() {
        assertEquals(Difficulty.PEACEFUL, s.getDifficulty());
    }

    /**
     * Test method for {@link Settings#getIslandDistance()}.
     */
    @Test
    void testGetIslandDistance() {
        assertEquals(400, s.getIslandDistance());
    }

    /**
     * Test method for {@link Settings#getIslandProtectionRange()}.
     */
    @Test
    void testGetIslandProtectionRange() {
        assertEquals(100, s.getIslandProtectionRange());
    }

    /**
     * Test method for {@link Settings#getIslandStartX()}.
     */
    @Test
    void testGetIslandStartX() {
        assertEquals(0, s.getIslandStartX());
    }

    /**
     * Test method for {@link Settings#getIslandStartZ()}.
     */
    @Test
    void testGetIslandStartZ() {
        assertEquals(0, s.getIslandStartZ());
    }

    /**
     * Test method for {@link Settings#getIslandXOffset()}.
     */
    @Test
    void testGetIslandXOffset() {
        assertEquals(0, s.getIslandXOffset());
    }

    /**
     * Test method for {@link Settings#getIslandZOffset()}.
     */
    @Test
    void testGetIslandZOffset() {
        assertEquals(0, s.getIslandZOffset());
    }

    /**
     * Test method for {@link Settings#getIslandHeight()}.
     */
    @Test
    void testGetIslandHeight() {
        assertEquals(100, s.getIslandHeight());
    }

    /**
     * Test method for {@link Settings#isUseOwnGenerator()}.
     */
    @Test
    void testIsUseOwnGenerator() {
        assertFalse(s.isUseOwnGenerator());
    }

    /**
     * Test method for {@link Settings#getSeaHeight()}.
     */
    @Test
    void testGetSeaHeight() {
        assertEquals(0, s.getSeaHeight());
    }

    /**
     * Test method for {@link Settings#getMaxIslands()}.
     */
    @Test
    void testGetMaxIslands() {
        assertEquals(-1, s.getMaxIslands());
    }

    /**
     * Test method for {@link Settings#getDefaultGameMode()}.
     */
    @Test
    void testGetDefaultGameMode() {
        assertEquals(GameMode.CREATIVE, s.getDefaultGameMode());
    }

    /**
     * Test method for {@link Settings#isNetherGenerate()}.
     */
    @Test
    void testIsNetherGenerate() {
        assertTrue(s.isNetherGenerate());
    }

    /**
     * Test method for {@link Settings#isNetherIslands()}.
     */
    @Test
    void testIsNetherIslands() {
        assertTrue(s.isNetherIslands());
    }

    /**
     * Test method for {@link Settings#isNetherRoof()}.
     */
    @Test
    void testIsNetherRoof() {
        assertTrue(s.isNetherRoof());
    }

    /**
     * Test method for {@link Settings#getNetherSpawnRadius()}.
     */
    @Test
    void testGetNetherSpawnRadius() {
        assertEquals(32, s.getNetherSpawnRadius());
    }

    /**
     * Test method for {@link Settings#isEndGenerate()}.
     */
    @Test
    void testIsEndGenerate() {
        assertTrue(s.isEndGenerate());
    }

    /**
     * Test method for {@link Settings#isEndIslands()}.
     */
    @Test
    void testIsEndIslands() {
        assertTrue(s.isEndIslands());
    }

    /**
     * Test method for {@link Settings#isDragonSpawn()}.
     */
    @Test
    void testIsDragonSpawn() {
        assertFalse(s.isDragonSpawn());
    }

    /**
     * Test method for {@link Settings#getRemoveMobsWhitelist()}.
     */
    @Test
    void testGetRemoveMobsWhitelist() {
        assertTrue(s.getRemoveMobsWhitelist().isEmpty());

    }

    /**
     * Test method for {@link Settings#getWorldFlags()}.
     */
    @Test
    void testGetWorldFlags() {
        assertTrue(s.getWorldFlags().isEmpty());
    }

    /**
     * Test method for {@link Settings#getDefaultIslandFlagNames()}.
     */
    @Test
    void testGetDefaultIslandFlagNames() {
        assertTrue(s.getDefaultIslandFlagNames().isEmpty());
    }

    /**
     * Test method for {@link Settings#getDefaultIslandSettingNames()}.
     */
    @Test
    void testGetDefaultIslandSettingNames() {
        assertTrue(s.getDefaultIslandSettingNames().isEmpty());
    }

    /**
     * Test method for {@link Settings#getDefaultIslandFlags()}.
     */
    @Test
    void testGetDefaultIslandFlags() {
        assertTrue(s.getDefaultIslandFlags().isEmpty());
    }

    /**
     * Test method for {@link Settings#getDefaultIslandSettings()}.
     */
    @Test
    void testGetDefaultIslandSettings() {
        assertTrue(s.getDefaultIslandSettings().isEmpty());
    }

    /**
     * Test method for {@link Settings#getHiddenFlags()}.
     */
    @Test
    void testGetHiddenFlags() {
        assertTrue(s.getHiddenFlags().isEmpty());
    }

    /**
     * Test method for {@link Settings#getVisitorBannedCommands()}.
     */
    @Test
    void testGetVisitorBannedCommands() {
        assertTrue(s.getVisitorBannedCommands().isEmpty());
    }

    /**
     * Test method for {@link Settings#getFallingBannedCommands()}.
     */
    @Test
    void testGetFallingBannedCommands() {
        assertTrue(s.getFallingBannedCommands().isEmpty());
    }

    /**
     * Test method for {@link Settings#getMaxTeamSize()}.
     */
    @Test
    void testGetMaxTeamSize() {
        assertEquals(4, s.getMaxTeamSize());
    }

    /**
     * Test method for {@link Settings#getMaxHomes()}.
     */
    @Test
    void testGetMaxHomes() {
        assertEquals(5, s.getMaxHomes());
    }

    /**
     * Test method for {@link Settings#getResetLimit()}.
     */
    @Test
    void testGetResetLimit() {
        assertEquals(-1, s.getResetLimit());
    }

    /**
     * Test method for {@link Settings#isLeaversLoseReset()}.
     */
    @Test
    void testIsLeaversLoseReset() {
        assertFalse(s.isLeaversLoseReset());
    }

    /**
     * Test method for {@link Settings#isKickedKeepInventory()}.
     */
    @Test
    void testIsKickedKeepInventory() {
        assertFalse(s.isKickedKeepInventory());
    }

    /**
     * Test method for {@link Settings#isCreateIslandOnFirstLoginEnabled()}.
     */
    @Test
    void testIsCreateIslandOnFirstLoginEnabled() {
        assertFalse(s.isCreateIslandOnFirstLoginEnabled());
    }

    /**
     * Test method for {@link Settings#getCreateIslandOnFirstLoginDelay()}.
     */
    @Test
    void testGetCreateIslandOnFirstLoginDelay() {
        assertEquals(5, s.getCreateIslandOnFirstLoginDelay());
    }

    /**
     * Test method for {@link Settings#isCreateIslandOnFirstLoginAbortOnLogout()}.
     */
    @Test
    void testIsCreateIslandOnFirstLoginAbortOnLogout() {
        assertTrue(s.isCreateIslandOnFirstLoginAbortOnLogout());
    }

    /**
     * Test method for {@link Settings#isOnJoinResetMoney()}.
     */
    @Test
    void testIsOnJoinResetMoney() {
        assertFalse(s.isOnJoinResetMoney());
    }

    /**
     * Test method for {@link Settings#isOnJoinResetInventory()}.
     */
    @Test
    void testIsOnJoinResetInventory() {
        assertFalse(s.isOnJoinResetInventory());
    }

    /**
     * Test method for {@link Settings#isOnJoinResetEnderChest()}.
     */
    @Test
    void testIsOnJoinResetEnderChest() {
        assertFalse(s.isOnJoinResetEnderChest());
    }

    /**
     * Test method for {@link Settings#isOnLeaveResetMoney()}.
     */
    @Test
    void testIsOnLeaveResetMoney() {
        assertFalse(s.isOnLeaveResetMoney());
    }

    /**
     * Test method for {@link Settings#isOnLeaveResetInventory()}.
     */
    @Test
    void testIsOnLeaveResetInventory() {
        assertFalse(s.isOnLeaveResetInventory());
    }

    /**
     * Test method for {@link Settings#isOnLeaveResetEnderChest()}.
     */
    @Test
    void testIsOnLeaveResetEnderChest() {
        assertFalse(s.isOnLeaveResetEnderChest());
    }

    /**
     * Test method for {@link Settings#isDeathsCounted()}.
     */
    @Test
    void testIsDeathsCounted() {
        assertTrue(s.isDeathsCounted());
    }

    /**
     * Test method for {@link Settings#isAllowSetHomeInNether()}.
     */
    @Test
    void testIsAllowSetHomeInNether() {
        assertTrue(s.isAllowSetHomeInNether());
    }

    /**
     * Test method for {@link Settings#isAllowSetHomeInTheEnd()}.
     */
    @Test
    void testIsAllowSetHomeInTheEnd() {
        assertTrue(s.isAllowSetHomeInTheEnd());
    }

    /**
     * Test method for {@link Settings#isRequireConfirmationToSetHomeInNether()}.
     */
    @Test
    void testIsRequireConfirmationToSetHomeInNether() {
        assertTrue(s.isRequireConfirmationToSetHomeInNether());
    }

    /**
     * Test method for {@link Settings#isRequireConfirmationToSetHomeInTheEnd()}.
     */
    @Test
    void testIsRequireConfirmationToSetHomeInTheEnd() {
        assertTrue(s.isRequireConfirmationToSetHomeInTheEnd());
    }

    /**
     * Test method for {@link Settings#getDeathsMax()}.
     */
    @Test
    void testGetDeathsMax() {
        assertEquals(10, s.getDeathsMax());
    }

    /**
     * Test method for {@link Settings#isTeamJoinDeathReset()}.
     */
    @Test
    void testIsTeamJoinDeathReset() {
        assertTrue(s.isTeamJoinDeathReset());
    }

    /**
     * Test method for {@link Settings#getGeoLimitSettings()}.
     */
    @Test
    void testGetGeoLimitSettings() {
        assertTrue(s.getGeoLimitSettings().isEmpty());
    }

    /**
     * Test method for {@link Settings#getIvSettings()}.
     */
    @Test
    void testGetIvSettings() {
        assertTrue(s.getIvSettings().isEmpty());
    }

    /**
     * Test method for {@link Settings#getResetEpoch()}.
     */
    @Test
    void testGetResetEpoch() {
        assertEquals(0L, s.getResetEpoch());
    }

    /**
     * Test method for {@link Settings#setFriendlyName(java.lang.String)}.
     */
    @Test
    void testSetFriendlyName() {
        s.setFriendlyName("test");
        assertEquals("test", s.getFriendlyName());
    }

    /**
     * Test method for {@link Settings#setWorldName(java.lang.String)}.
     */
    @Test
    void testSetWorldName() {
        s.setWorldName("test");
        assertEquals("test", s.getWorldName());
    }

    /**
     * Test method for {@link Settings#setDifficulty(org.bukkit.Difficulty)}.
     */
    @Test
    void testSetDifficulty() {
        s.setDifficulty(Difficulty.HARD);
        assertEquals(Difficulty.HARD, s.getDifficulty());
    }

    /**
     * Test method for {@link Settings#setIslandDistance(int)}.
     */
    @Test
    void testSetIslandDistance() {
        s.setIslandDistance(12345);
        assertEquals(12345, s.getIslandDistance());
    }

    /**
     * Test method for {@link Settings#setIslandProtectionRange(int)}.
     */
    @Test
    void testSetIslandProtectionRange() {
        s.setIslandProtectionRange(12345);
        assertEquals(12345, s.getIslandProtectionRange());
    }

    /**
     * Test method for {@link Settings#setIslandStartX(int)}.
     */
    @Test
    void testSetIslandStartX() {
        s.setIslandStartX(12345);
        assertEquals(12345, s.getIslandStartX());
    }

    /**
     * Test method for {@link Settings#setIslandStartZ(int)}.
     */
    @Test
    void testSetIslandStartZ() {
        s.setIslandStartZ(12345);
        assertEquals(12345, s.getIslandStartZ());
    }

    /**
     * Test method for {@link Settings#setIslandXOffset(int)}.
     */
    @Test
    void testSetIslandXOffset() {
        s.setIslandXOffset(12345);
        assertEquals(12345, s.getIslandXOffset());
    }

    /**
     * Test method for {@link Settings#setIslandZOffset(int)}.
     */
    @Test
    void testSetIslandZOffset() {
        s.setIslandZOffset(12345);
        assertEquals(12345, s.getIslandZOffset());
    }

    /**
     * Test method for {@link Settings#setIslandHeight(int)}.
     */
    @Test
    void testSetIslandHeight() {
        s.setIslandHeight(12345);
        assertEquals(12345, s.getIslandHeight());
    }

    /**
     * Test method for {@link Settings#setUseOwnGenerator(boolean)}.
     */
    @Test
    void testSetUseOwnGenerator() {
        s.setUseOwnGenerator(true);
        assertTrue(s.isUseOwnGenerator());
    }

    /**
     * Test method for {@link Settings#setSeaHeight(int)}.
     */
    @Test
    void testSetSeaHeight() {
        s.setSeaHeight(12345);
        assertEquals(12345, s.getSeaHeight());
    }

    /**
     * Test method for {@link Settings#setMaxIslands(int)}.
     */
    @Test
    void testSetMaxIslands() {
        s.setMaxIslands(12345);
        assertEquals(12345, s.getMaxIslands());
    }

    /**
     * Test method for {@link Settings#setDefaultGameMode(org.bukkit.GameMode)}.
     */
    @Test
    void testSetDefaultGameMode() {
        s.setDefaultGameMode(GameMode.SPECTATOR);
        assertEquals(GameMode.SPECTATOR, s.getDefaultGameMode());
    }

    /**
     * Test method for {@link Settings#setNetherGenerate(boolean)}.
     */
    @Test
    void testSetNetherGenerate() {
        s.setNetherGenerate(false);
        assertFalse(s.isNetherGenerate());
        s.setNetherGenerate(true);
        assertTrue(s.isNetherGenerate());
    }

    /**
     * Test method for {@link Settings#setNetherIslands(boolean)}.
     */
    @Test
    void testSetNetherIslands() {
        s.setNetherIslands(false);
        assertFalse(s.isNetherIslands());
        s.setNetherIslands(true);
        assertTrue(s.isNetherIslands());
    }

    /**
     * Test method for {@link Settings#setNetherRoof(boolean)}.
     */
    @Test
    void testSetNetherRoof() {
        s.setNetherRoof(false);
        assertFalse(s.isNetherRoof());
        s.setNetherRoof(true);
        assertTrue(s.isNetherRoof());
    }

    /**
     * Test method for {@link Settings#setNetherSpawnRadius(int)}.
     */
    @Test
    void testSetNetherSpawnRadius() {
        s.setNetherSpawnRadius(12345);
        assertEquals(12345, s.getNetherSpawnRadius());
    }

    /**
     * Test method for {@link Settings#setEndGenerate(boolean)}.
     */
    @Test
    void testSetEndGenerate() {
        s.setEndGenerate(false);
        assertFalse(s.isEndGenerate());
        s.setEndGenerate(true);
        assertTrue(s.isEndGenerate());
    }

    /**
     * Test method for {@link Settings#setEndIslands(boolean)}.
     */
    @Test
    void testSetEndIslands() {
        s.setEndIslands(false);
        assertFalse(s.isEndIslands());
        s.setEndIslands(true);
        assertTrue(s.isEndIslands());
    }

    /**
     * Test method for {@link Settings#setRemoveMobsWhitelist(java.util.Set)}.
     */
    @Test
    void testSetRemoveMobsWhitelist() {
        s.setRemoveMobsWhitelist(Collections.singleton(EntityType.AXOLOTL));
        assertTrue(s.getRemoveMobsWhitelist().contains(EntityType.AXOLOTL));
    }

    /**
     * Test method for {@link Settings#setWorldFlags(java.util.Map)}.
     */
    @Test
    void testSetWorldFlags() {
        s.setWorldFlags(Map.of("trueFlag", true, "falseFlag", false));
        assertTrue(s.getWorldFlags().get("trueFlag"));
        assertFalse(s.getWorldFlags().get("falseFlag"));
    }

    /**
     * Test method for {@link Settings#setHiddenFlags(java.util.List)}.
     */
    @Test
    void testSetHiddenFlags() {
        s.setHiddenFlags(List.of("FLAG1", "FLAG2"));
        assertTrue(s.getHiddenFlags().contains("FLAG2"));
        assertFalse(s.getHiddenFlags().contains("FLAG3"));
    }

    /**
     * Test method for {@link Settings#setParkourAllowedCommands(java.util.List)}.
     */
    @Test
    void testSetParkourAllowedCommands() {
        s.setParkourAllowedCommands(List.of("allowed"));
        assertTrue(s.getParkourAllowedCommands().contains("allowed"));
        assertFalse(s.getParkourAllowedCommands().contains("not-allowed"));
    }


    /**
     * Test method for {@link Settings#setVisitorBannedCommands(java.util.List)}.
     */
    @Test
    void testSetVisitorBannedCommands() {
        s.setVisitorBannedCommands(List.of("banned"));
        assertTrue(s.getVisitorBannedCommands().contains("banned"));
        assertFalse(s.getVisitorBannedCommands().contains("not-banned"));
    }

    /**
     * Test method for {@link Settings#setFallingBannedCommands(java.util.List)}.
     */
    @Test
    void testSetFallingBannedCommands() {
        s.setFallingBannedCommands(List.of("banned"));
        assertTrue(s.getFallingBannedCommands().contains("banned"));
        assertFalse(s.getFallingBannedCommands().contains("not-banned"));
    }

    /**
     * Test method for {@link Settings#setMaxTeamSize(int)}.
     */
    @Test
    void testSetMaxTeamSize() {
        s.setMaxTeamSize(12345);
        assertEquals(12345, s.getMaxTeamSize());
    }

    /**
     * Test method for {@link Settings#setMaxHomes(int)}.
     */
    @Test
    void testSetMaxHomes() {
        s.setMaxHomes(12345);
        assertEquals(12345, s.getMaxHomes());
    }

    /**
     * Test method for {@link Settings#setResetLimit(int)}.
     */
    @Test
    void testSetResetLimit() {
        s.setResetLimit(12345);
        assertEquals(12345, s.getResetLimit());
    }

    /**
     * Test method for {@link Settings#setLeaversLoseReset(boolean)}.
     */
    @Test
    void testSetLeaversLoseReset() {
        s.setLeaversLoseReset(false);
        assertFalse(s.isLeaversLoseReset());
        s.setLeaversLoseReset(true);
        assertTrue(s.isLeaversLoseReset());
    }

    /**
     * Test method for {@link Settings#setKickedKeepInventory(boolean)}.
     */
    @Test
    void testSetKickedKeepInventory() {
        s.setKickedKeepInventory(false);
        assertFalse(s.isKickedKeepInventory());
        s.setKickedKeepInventory(true);
        assertTrue(s.isKickedKeepInventory());
    }

    /**
     * Test method for {@link Settings#setOnJoinResetMoney(boolean)}.
     */
    @Test
    void testSetOnJoinResetMoney() {
        s.setOnJoinResetMoney(false);
        assertFalse(s.isOnJoinResetMoney());
        s.setOnJoinResetMoney(true);
        assertTrue(s.isOnJoinResetMoney());
    }

    /**
     * Test method for {@link Settings#setOnJoinResetInventory(boolean)}.
     */
    @Test
    void testSetOnJoinResetInventory() {
        s.setOnJoinResetInventory(false);
        assertFalse(s.isOnJoinResetInventory());
        s.setOnJoinResetInventory(true);
        assertTrue(s.isOnJoinResetInventory());
    }

    /**
     * Test method for {@link Settings#setOnJoinResetEnderChest(boolean)}.
     */
    @Test
    void testSetOnJoinResetEnderChest() {
        s.setOnJoinResetEnderChest(false);
        assertFalse(s.isOnJoinResetEnderChest());
        s.setOnJoinResetEnderChest(true);
        assertTrue(s.isOnJoinResetEnderChest());
    }

    /**
     * Test method for {@link Settings#setOnLeaveResetMoney(boolean)}.
     */
    @Test
    void testSetOnLeaveResetMoney() {
        s.setOnLeaveResetMoney(false);
        assertFalse(s.isOnLeaveResetMoney());
        s.setOnLeaveResetMoney(true);
        assertTrue(s.isOnLeaveResetMoney());
    }

    /**
     * Test method for {@link Settings#setOnLeaveResetInventory(boolean)}.
     */
    @Test
    void testSetOnLeaveResetInventory() {
        s.setOnLeaveResetInventory(false);
        assertFalse(s.isOnLeaveResetInventory());
        s.setOnLeaveResetInventory(true);
        assertTrue(s.isOnLeaveResetInventory());
    }

    /**
     * Test method for {@link Settings#setOnLeaveResetEnderChest(boolean)}.
     */
    @Test
    void testSetOnLeaveResetEnderChest() {
        s.setOnLeaveResetEnderChest(false);
        assertFalse(s.isOnLeaveResetEnderChest());
        s.setOnLeaveResetEnderChest(true);
        assertTrue(s.isOnLeaveResetEnderChest());
    }

    /**
     * Test method for {@link Settings#setCreateIslandOnFirstLoginEnabled(boolean)}.
     */
    @Test
    void testSetCreateIslandOnFirstLoginEnabled() {
        s.setCreateIslandOnFirstLoginEnabled(false);
        assertFalse(s.isCreateIslandOnFirstLoginEnabled());
        s.setCreateIslandOnFirstLoginEnabled(true);
        assertTrue(s.isCreateIslandOnFirstLoginEnabled());
    }

    /**
     * Test method for {@link Settings#setCreateIslandOnFirstLoginDelay(int)}.
     */
    @Test
    void testSetCreateIslandOnFirstLoginDelay() {
        s.setCreateIslandOnFirstLoginDelay(12345);
        assertEquals(12345, s.getCreateIslandOnFirstLoginDelay());
    }

    /**
     * Test method for {@link Settings#setCreateIslandOnFirstLoginAbortOnLogout(boolean)}.
     */
    @Test
    void testSetCreateIslandOnFirstLoginAbortOnLogout() {
        s.setCreateIslandOnFirstLoginAbortOnLogout(false);
        assertFalse(s.isCreateIslandOnFirstLoginAbortOnLogout());
        s.setCreateIslandOnFirstLoginAbortOnLogout(true);
        assertTrue(s.isCreateIslandOnFirstLoginAbortOnLogout());
    }

    /**
     * Test method for {@link Settings#setDeathsCounted(boolean)}.
     */
    @Test
    void testSetDeathsCounted() {
        s.setDeathsCounted(false);
        assertFalse(s.isDeathsCounted());
        s.setDeathsCounted(true);
        assertTrue(s.isDeathsCounted());
    }

    /**
     * Test method for {@link Settings#setDeathsMax(int)}.
     */
    @Test
    void testSetDeathsMax() {
        s.setDeathsMax(12345);
        assertEquals(12345, s.getDeathsMax());
    }

    /**
     * Test method for {@link Settings#setTeamJoinDeathReset(boolean)}.
     */
    @Test
    void testSetTeamJoinDeathReset() {
        s.setTeamJoinDeathReset(false);
        assertFalse(s.isTeamJoinDeathReset());
        s.setTeamJoinDeathReset(true);
        assertTrue(s.isTeamJoinDeathReset());
    }

    /**
     * Test method for {@link Settings#setGeoLimitSettings(java.util.List)}.
     */
    @Test
    void testSetGeoLimitSettings() {
        s.setGeoLimitSettings(List.of("test"));
        assertTrue(s.getGeoLimitSettings().contains("test"));
    }

    /**
     * Test method for {@link Settings#setIvSettings(java.util.List)}.
     */
    @Test
    void testSetIvSettings() {
        s.setIvSettings(List.of("test"));
        assertTrue(s.getIvSettings().contains("test"));
    }

    /**
     * Test method for {@link Settings#setAllowSetHomeInNether(boolean)}.
     */
    @Test
    void testSetAllowSetHomeInNether() {
        s.setAllowSetHomeInNether(false);
        assertFalse(s.isAllowSetHomeInNether());
        s.setAllowSetHomeInNether(true);
        assertTrue(s.isAllowSetHomeInNether());
    }

    /**
     * Test method for {@link Settings#setAllowSetHomeInTheEnd(boolean)}.
     */
    @Test
    void testSetAllowSetHomeInTheEnd() {
        s.setAllowSetHomeInTheEnd(false);
        assertFalse(s.isAllowSetHomeInTheEnd());
        s.setAllowSetHomeInTheEnd(true);
        assertTrue(s.isAllowSetHomeInTheEnd());
    }

    /**
     * Test method for {@link Settings#setRequireConfirmationToSetHomeInNether(boolean)}.
     */
    @Test
    void testSetRequireConfirmationToSetHomeInNether() {
        s.setRequireConfirmationToSetHomeInNether(false);
        assertFalse(s.isRequireConfirmationToSetHomeInNether());
        s.setRequireConfirmationToSetHomeInNether(true);
        assertTrue(s.isRequireConfirmationToSetHomeInNether());
    }

    /**
     * Test method for {@link Settings#setRequireConfirmationToSetHomeInTheEnd(boolean)}.
     */
    @Test
    void testSetRequireConfirmationToSetHomeInTheEnd() {
        s.setRequireConfirmationToSetHomeInTheEnd(false);
        assertFalse(s.isRequireConfirmationToSetHomeInTheEnd());
        s.setRequireConfirmationToSetHomeInTheEnd(true);
        assertTrue(s.isRequireConfirmationToSetHomeInTheEnd());
    }

    /**
     * Test method for {@link Settings#setResetEpoch(long)}.
     */
    @Test
    void testSetResetEpoch() {
        s.setResetEpoch(12345);
        assertEquals(12345, s.getResetEpoch());
    }

    /**
     * Test method for {@link Settings#getPermissionPrefix()}.
     */
    @Test
    void testGetPermissionPrefix() {
        assertEquals("parkour", s.getPermissionPrefix());
    }

    /**
     * Test method for {@link Settings#isWaterUnsafe()}.
     */
    @Test
    void testIsWaterUnsafe() {
        assertFalse(s.isWaterUnsafe());
    }

    /**
     * Test method for {@link Settings#getDefaultBiome()}.
     */
    @Test
    void testGetDefaultBiome() {
        assertEquals(Biome.PLAINS, s.getDefaultBiome());
    }

    /**
     * Test method for {@link Settings#setDefaultBiome(org.bukkit.block.Biome)}.
     */
    @Test
    void testSetDefaultBiome() {
        assertEquals(Biome.PLAINS, s.getDefaultBiome());
        s.setDefaultBiome(Biome.BAMBOO_JUNGLE);
        assertEquals(Biome.BAMBOO_JUNGLE, s.getDefaultBiome());
    }

    /**
     * Test method for {@link Settings#getBanLimit()}.
     */
    @Test
    void testGetBanLimit() {
        assertEquals(-1, s.getBanLimit());
    }

    /**
     * Test method for {@link Settings#setBanLimit(int)}.
     */
    @Test
    void testSetBanLimit() {
        assertEquals(-1, s.getBanLimit());
        s.setBanLimit(12345);
        assertEquals(12345, s.getBanLimit());
    }

    /**
     * Test method for {@link Settings#getPlayerCommandAliases()}.
     */
    @Test
    void testGetPlayerCommandAliases() {
        assertEquals("parkour pk",s.getPlayerCommandAliases());
    }

    /**
     * Test method for {@link Settings#setPlayerCommandAliases(java.lang.String)}.
     */
    @Test
    void testSetPlayerCommandAliases() {
        assertEquals("parkour pk",s.getPlayerCommandAliases());
        s.setPlayerCommandAliases("aliases");
        assertEquals("aliases",s.getPlayerCommandAliases());
    }

    /**
     * Test method for {@link Settings#getAdminCommandAliases()}.
     */
    @Test
    void testGetAdminCommandAliases() {
        assertEquals("parkouradmin pkadmin",s.getAdminCommandAliases());
    }

    /**
     * Test method for {@link Settings#setAdminCommandAliases(java.lang.String)}.
     */
    @Test
    void testSetAdminCommandAliases() {
        assertEquals("parkouradmin pkadmin",s.getAdminCommandAliases());
        s.setAdminCommandAliases("aliases");
        assertEquals("aliases",s.getAdminCommandAliases());
    }

    /**
     * Test method for {@link Settings#isDeathsResetOnNewIsland()}.
     */
    @Test
    void testIsDeathsResetOnNewIsland() {
        assertTrue(s.isDeathsResetOnNewIsland());
    }

    /**
     * Test method for {@link Settings#setDeathsResetOnNewIsland(boolean)}.
     */
    @Test
    void testSetDeathsResetOnNewIsland() {
        s.setDeathsResetOnNewIsland(false);
        assertFalse(s.isDeathsResetOnNewIsland());
        s.setDeathsResetOnNewIsland(true);
        assertTrue(s.isDeathsResetOnNewIsland());
    }

    /**
     * Test method for {@link Settings#getOnJoinCommands()}.
     */
    @Test
    void testGetOnJoinCommands() {
        assertTrue(s.getOnJoinCommands().isEmpty());
    }

    /**
     * Test method for {@link Settings#setOnJoinCommands(java.util.List)}.
     */
    @Test
    void testSetOnJoinCommands() {
        s.setOnJoinCommands(List.of("command", "do this"));
        assertEquals("do this", s.getOnJoinCommands().get(1));
    }

    /**
     * Test method for {@link Settings#getOnLeaveCommands()}.
     */
    @Test
    void testGetOnLeaveCommands() {
        assertTrue(s.getOnLeaveCommands().isEmpty());
    }

    /**
     * Test method for {@link Settings#setOnLeaveCommands(java.util.List)}.
     */
    @Test
    void testSetOnLeaveCommands() {
        s.setOnLeaveCommands(List.of("command", "do this"));
        assertEquals("do this", s.getOnLeaveCommands().get(1));
    }

    /**
     * Test method for {@link Settings#getOnRespawnCommands()}.
     */
    @Test
    void testGetOnRespawnCommands() {
        assertTrue(s.getOnRespawnCommands().isEmpty());
    }

    /**
     * Test method for {@link Settings#isOnJoinResetHealth()}.
     */
    @Test
    void testIsOnJoinResetHealth() {
        assertTrue(s.isOnJoinResetHealth());
    }

    /**
     * Test method for {@link Settings#setOnJoinResetHealth(boolean)}.
     */
    @Test
    void testSetOnJoinResetHealth() {
        s.setOnJoinResetHealth(false);
        assertFalse(s.isOnJoinResetHealth());
        s.setOnJoinResetHealth(true);
        assertTrue(s.isOnJoinResetHealth());
    }

    /**
     * Test method for {@link Settings#isOnJoinResetHunger()}.
     */
    @Test
    void testIsOnJoinResetHunger() {
        assertTrue(s.isOnJoinResetHunger());
    }

    /**
     * Test method for {@link Settings#setOnJoinResetHunger(boolean)}.
     */
    @Test
    void testSetOnJoinResetHunger() {
        s.setOnJoinResetHunger(false);
        assertFalse(s.isOnJoinResetHunger());
        s.setOnJoinResetHunger(true);
        assertTrue(s.isOnJoinResetHunger());
    }

    /**
     * Test method for {@link Settings#isOnJoinResetXP()}.
     */
    @Test
    void testIsOnJoinResetXP() {
        assertFalse(s.isOnJoinResetXP());
    }

    /**
     * Test method for {@link Settings#setOnJoinResetXP(boolean)}.
     */
    @Test
    void testSetOnJoinResetXP() {
        s.setOnJoinResetXP(false);
        assertFalse(s.isOnJoinResetXP());
        s.setOnJoinResetXP(true);
        assertTrue(s.isOnJoinResetXP());
    }

    /**
     * Test method for {@link Settings#isOnLeaveResetHealth()}.
     */
    @Test
    void testIsOnLeaveResetHealth() {
        assertFalse(s.isOnLeaveResetHealth());
    }

    /**
     * Test method for {@link Settings#setOnLeaveResetHealth(boolean)}.
     */
    @Test
    void testSetOnLeaveResetHealth() {
        s.setOnLeaveResetHealth(false);
        assertFalse(s.isOnLeaveResetHealth());
        s.setOnLeaveResetHealth(true);
        assertTrue(s.isOnLeaveResetHealth());
    }

    /**
     * Test method for {@link Settings#isOnLeaveResetHunger()}.
     */
    @Test
    void testIsOnLeaveResetHunger() {
        assertFalse(s.isOnLeaveResetHunger());
    }

    /**
     * Test method for {@link Settings#setOnLeaveResetHunger(boolean)}.
     */
    @Test
    void testSetOnLeaveResetHunger() {
        s.setOnLeaveResetHunger(false);
        assertFalse(s.isOnLeaveResetHunger());
        s.setOnLeaveResetHunger(true);
        assertTrue(s.isOnLeaveResetHunger());
    }

    /**
     * Test method for {@link Settings#isOnLeaveResetXP()}.
     */
    @Test
    void testIsOnLeaveResetXP() {
        assertFalse(s.isOnLeaveResetXP());
    }

    /**
     * Test method for {@link Settings#setOnLeaveResetXP(boolean)}.
     */
    @Test
    void testSetOnLeaveResetXP() {
        assertFalse(s.isOnLeaveResetXP());
        s.setOnLeaveResetXP(true);
        assertTrue(s.isOnLeaveResetXP());
    }

    /**
     * Test method for {@link Settings#isPasteMissingIslands()}.
     */
    @Test
    void testIsPasteMissingIslands() {
        assertFalse(s.isPasteMissingIslands());
    }

    /**
     * Test method for {@link Settings#setPasteMissingIslands(boolean)}.
     */
    @Test
    void testSetPasteMissingIslands() {
        assertFalse(s.isPasteMissingIslands());
        s.setPasteMissingIslands(true);
        assertTrue(s.isPasteMissingIslands());


    }

    /**
     * Test method for {@link Settings#isTeleportPlayerToIslandUponIslandCreation()}.
     */
    @Test
    void testIsTeleportPlayerToIslandUponIslandCreation() {
        assertTrue(s.isTeleportPlayerToIslandUponIslandCreation());
    }

    /**
     * Test method for {@link Settings#setTeleportPlayerToIslandUponIslandCreation(boolean)}.
     */
    @Test
    void testSetTeleportPlayerToIslandUponIslandCreation() {
        assertTrue(s.isTeleportPlayerToIslandUponIslandCreation());
        s.setTeleportPlayerToIslandUponIslandCreation(false);
        assertFalse(s.isTeleportPlayerToIslandUponIslandCreation());
    }

    /**
     * Test method for {@link Settings#getSpawnLimitMonsters()}.
     */
    @Test
    void testGetSpawnLimitMonsters() {
        assertEquals(-1, s.getSpawnLimitMonsters());
    }

    /**
     * Test method for {@link Settings#setSpawnLimitMonsters(int)}.
     */
    @Test
    void testSetSpawnLimitMonsters() {
        assertEquals(-1, s.getSpawnLimitMonsters());
        s.setSpawnLimitMonsters(12345);
        assertEquals(12345, s.getSpawnLimitMonsters());
    }

    /**
     * Test method for {@link Settings#getSpawnLimitAnimals()}.
     */
    @Test
    void testGetSpawnLimitAnimals() {
        assertEquals(-1, s.getSpawnLimitAnimals());
    }

    /**
     * Test method for {@link Settings#setSpawnLimitAnimals(int)}.
     */
    @Test
    void testSetSpawnLimitAnimals() {
        assertEquals(-1, s.getSpawnLimitAnimals());
        s.setSpawnLimitAnimals(12345);
        assertEquals(12345, s.getSpawnLimitAnimals());
    }

    /**
     * Test method for {@link Settings#getSpawnLimitWaterAnimals()}.
     */
    @Test
    void testGetSpawnLimitWaterAnimals() {
        assertEquals(-1, s.getSpawnLimitWaterAnimals());
    }

    /**
     * Test method for {@link Settings#setSpawnLimitWaterAnimals(int)}.
     */
    @Test
    void testSetSpawnLimitWaterAnimals() {
        assertEquals(-1, s.getSpawnLimitWaterAnimals());
        s.setSpawnLimitWaterAnimals(12345);
        assertEquals(12345, s.getSpawnLimitWaterAnimals());
    }

    /**
     * Test method for {@link Settings#getSpawnLimitAmbient()}.
     */
    @Test
    void testGetSpawnLimitAmbient() {
        assertEquals(-1, s.getSpawnLimitAmbient());
    }

    /**
     * Test method for {@link Settings#setSpawnLimitAmbient(int)}.
     */
    @Test
    void testSetSpawnLimitAmbient() {
        assertEquals(-1, s.getSpawnLimitAmbient());
        s.setSpawnLimitAmbient(12345);
        assertEquals(12345, s.getSpawnLimitAmbient());
    }

    /**
     * Test method for {@link Settings#getTicksPerAnimalSpawns()}.
     */
    @Test
    void testGetTicksPerAnimalSpawns() {
        assertEquals(-1, s.getTicksPerAnimalSpawns());
    }

    /**
     * Test method for {@link Settings#setTicksPerAnimalSpawns(int)}.
     */
    @Test
    void testSetTicksPerAnimalSpawns() {
        assertEquals(-1, s.getTicksPerAnimalSpawns());
        s.setTicksPerAnimalSpawns(12345);
        assertEquals(12345, s.getTicksPerAnimalSpawns());
    }

    /**
     * Test method for {@link Settings#getTicksPerMonsterSpawns()}.
     */
    @Test
    void testGetTicksPerMonsterSpawns() {
        assertEquals(-1, s.getTicksPerMonsterSpawns());
    }

    /**
     * Test method for {@link Settings#setTicksPerMonsterSpawns(int)}.
     */
    @Test
    void testSetTicksPerMonsterSpawns() {
        assertEquals(-1, s.getTicksPerMonsterSpawns());
        s.setTicksPerMonsterSpawns(12345);
        assertEquals(12345, s.getTicksPerMonsterSpawns());
    }

    /**
     * Test method for {@link Settings#getMaxCoopSize()}.
     */
    @Test
    void testGetMaxCoopSize() {
        assertEquals(4, s.getMaxCoopSize());
    }

    /**
     * Test method for {@link Settings#setMaxCoopSize(int)}.
     */
    @Test
    void testSetMaxCoopSize() {
        s.setMaxCoopSize(12345);
        assertEquals(12345, s.getMaxCoopSize());
    }

    /**
     * Test method for {@link Settings#getMaxTrustSize()}.
     */
    @Test
    void testGetMaxTrustSize() {
        assertEquals(4, s.getMaxTrustSize());
    }

    /**
     * Test method for {@link Settings#setMaxTrustSize(int)}.
     */
    @Test
    void testSetMaxTrustSize() {
        s.setMaxTrustSize(12345);
        assertEquals(12345, s.getMaxTrustSize());
    }

    /**
     * Test method for {@link Settings#getDefaultNewPlayerAction()}.
     */
    @Test
    void testGetDefaultNewPlayerAction() {
        assertEquals("create", s.getDefaultNewPlayerAction());
    }

    /**
     * Test method for {@link Settings#setDefaultNewPlayerAction(java.lang.String)}.
     */
    @Test
    void testSetDefaultNewPlayerAction() {
        s.setDefaultNewPlayerAction("test");
        assertEquals("test", s.getDefaultNewPlayerAction());
    }

    /**
     * Test method for {@link Settings#getDefaultPlayerAction()}.
     */
    @Test
    void testGetDefaultPlayerAction() {
        assertEquals("go", s.getDefaultPlayerAction());
    }

    /**
     * Test method for {@link Settings#setDefaultPlayerAction(java.lang.String)}.
     */
    @Test
    void testSetDefaultPlayerAction() {
        s.setDefaultPlayerAction("test");
        assertEquals("test", s.getDefaultPlayerAction());
    }

    /**
     * Test method for {@link Settings#getMobLimitSettings()}.
     */
    @Test
    void testGetMobLimitSettings() {
        assertTrue(s.getMobLimitSettings().isEmpty());
    }

    /**
     * Test method for {@link Settings#setMobLimitSettings(java.util.List)}.
     */
    @Test
    void testSetMobLimitSettings() {
        s.setMobLimitSettings(List.of("test"));
        assertEquals("test", s.getMobLimitSettings().get(0));
    }

    /**
     * Test method for {@link Settings#getDefaultNetherBiome()}.
     */
    @Test
    void testGetDefaultNetherBiome() {
        assertEquals(Biome.NETHER_WASTES, s.getDefaultNetherBiome());
    }

    /**
     * Test method for {@link Settings#setDefaultNetherBiome(org.bukkit.block.Biome)}.
     */
    @Test
    void testSetDefaultNetherBiome() {
        assertEquals(Biome.NETHER_WASTES, s.getDefaultNetherBiome());
        s.setDefaultNetherBiome(Biome.BADLANDS);
        assertEquals(Biome.BADLANDS, s.getDefaultNetherBiome());
    }

    /**
     * Test method for {@link Settings#getDefaultEndBiome()}.
     */
    @Test
    void testGetDefaultEndBiome() {
        assertEquals(Biome.THE_END, s.getDefaultEndBiome());
    }

    /**
     * Test method for {@link Settings#setDefaultEndBiome(org.bukkit.block.Biome)}.
     */
    @Test
    void testSetDefaultEndBiome() {
        assertEquals(Biome.THE_END, s.getDefaultEndBiome());
        s.setDefaultEndBiome(Biome.BADLANDS);
        assertEquals(Biome.BADLANDS, s.getDefaultEndBiome());
    }

    /**
     * Test method for {@link Settings#isMakeNetherPortals()}.
     */
    @Test
    void testIsMakeNetherPortals() {
        assertFalse(s.isMakeNetherPortals());
    }

    /**
     * Test method for {@link Settings#isMakeEndPortals()}.
     */
    @Test
    void testIsMakeEndPortals() {
        assertFalse(s.isMakeEndPortals());
    }

    /**
     * Test method for {@link Settings#isPreventVoidDeath()}.
     */
    @Test
    void testIsPreventVoidDeath() {
        assertTrue(s.isPreventVoidDeath());
    }

    /**
     * Test method for {@link Settings#setPreventVoidDeath(boolean)}.
     */
    @Test
    void testSetPreventVoidDeath() {
        s.setPreventVoidDeath(false);
        assertFalse(s.isPreventVoidDeath());
    }

}
