package world.bentobox.parkour;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.FlagsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class, BentoBox.class, User.class, Config.class, DatabaseSetup.class, Util.class,
        RanksManager.class })
public abstract class AbstractParkourTest {

	@Mock
	protected User user;
	@Mock
	protected IslandsManager im;
	@Mock
	protected Island island;
	@Mock
	protected BentoBox plugin;
	@Mock
	protected FlagsManager fm;
	@Mock
	protected Settings settings;
	@Mock
	protected PlaceholdersManager phm;
	@Mock
	protected IslandWorldManager iwm;
	@Mock
	protected Parkour addon;
    @Mock
    private RanksManager rm;
	protected static AbstractDatabaseHandler<Object> h;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void beforeClass() throws IllegalAccessException, InvocationTargetException, IntrospectionException {
		// This has to be done beforeClass otherwise the tests will interfere with each
		// other
		h = mock(AbstractDatabaseHandler.class);
		// Database
		PowerMockito.mockStatic(DatabaseSetup.class);
		DatabaseSetup dbSetup = mock(DatabaseSetup.class);
		when(DatabaseSetup.getDatabase()).thenReturn(dbSetup);
		when(dbSetup.getHandler(any())).thenReturn(h);
		when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));
	}

	@After
	public void tearDown() throws IOException {
		User.clearUsers();
		Mockito.framework().clearInlineMocks();
		deleteAll(new File("database"));
		deleteAll(new File("database_backup"));
		deleteAll(new File("addon.jar"));
		deleteAll(new File("config.yml"));
		deleteAll(new File("addons"));
	}

	protected void deleteAll(File file) throws IOException {
		if (file.exists()) {
			Files.walk(file.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}

	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Set up plugin
		Whitebox.setInternalState(BentoBox.class, "instance", plugin);
		when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());

		// The database type has to be created one line before the thenReturn() to work!
		DatabaseType value = DatabaseType.JSON;
		when(plugin.getSettings()).thenReturn(settings);
		when(settings.getDatabaseType()).thenReturn(value);
		when(plugin.getPlaceholdersManager()).thenReturn(phm);
		// Placeholders
		when(phm.replacePlaceholders(any(), anyString())).thenAnswer(a -> (String) a.getArgument(1, String.class));

		// Command manager
		CommandsManager cm = mock(CommandsManager.class);
		when(plugin.getCommandsManager()).thenReturn(cm);

		// Player
		Player p = mock(Player.class);
		// Sometimes use Mockito.withSettings().verboseLogging()
		when(user.isOp()).thenReturn(false);
		UUID uuid = UUID.randomUUID();
		when(user.getUniqueId()).thenReturn(uuid);
		when(user.getPlayer()).thenReturn(p);
		when(user.getName()).thenReturn("tastybento");
		User.setPlugin(plugin);

		// Island World Manager
		when(plugin.getIWM()).thenReturn(iwm);

		// Player has island to begin with
		island = mock(Island.class);
		when(im.getIsland(Mockito.any(), Mockito.any(UUID.class))).thenReturn(island);
		when(plugin.getIslands()).thenReturn(im);

		// Locales
		// Return the reference (USE THIS IN THE FUTURE)
		when(user.getTranslation(Mockito.anyString()))
				.thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));

		// Server
		PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
		Server server = mock(Server.class);
		when(Bukkit.getServer()).thenReturn(server);
		when(Bukkit.getLogger()).thenReturn(Logger.getAnonymousLogger());
		when(Bukkit.getPluginManager()).thenReturn(mock(PluginManager.class));

		// Addons manager
		AddonsManager am = mock(AddonsManager.class);
		when(plugin.getAddonsManager()).thenReturn(am);

		// Flags manager
		when(plugin.getFlagsManager()).thenReturn(fm);
		when(fm.getFlags()).thenReturn(Collections.emptyList());

		// RanksManager
        Whitebox.setInternalState(RanksManager.class, "instance", rm);
        when(rm.getRank(any())).thenReturn("ranks.member");
	}

}
