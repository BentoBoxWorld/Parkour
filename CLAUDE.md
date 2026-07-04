# CLAUDE.md

Guidance for working in this repository.

## What this is

**Parkour** is a BentoBox *game-mode addon*: each player gets their own plot ("course") in a
dedicated world and builds/runs parkour courses. It is a Maven project that produces a jar loaded
by BentoBox at runtime.

- Requires **BentoBox 3.14.0+**, **Java 21**, **Paper/Spigot 1.21.11** API.
- Main addon class: `world.bentobox.parkour.Parkour` (extends `GameModeAddon`).
- Plugin entry point: `ParkourPladdon` (+ `src/main/resources/plugin.yml`); addon metadata in
  `src/main/resources/addon.yml`.

## Build & test

```bash
mvn clean package        # compile, run all tests, build the jar (defaultGoal)
mvn test                 # run the full test suite
mvn test -Dtest=FooTest  # run a single test class
```

- The jar name comes from `<build.version>` in `pom.xml` (e.g. `Parkour-1.5.0-SNAPSHOT-LOCAL.jar`).
  Bump the release version by editing `<build.version>`.
- Surefire already passes the `--add-opens` flags MockBukkit/Mockito need — don't remove them.
- If a local JDK newer than the CI's Java 21 makes JaCoCo choke, run with `-Djacoco.skip=true`
  (CI runs on Java 21 via `.github/workflows/build.yml`, so this is a local-only workaround).

## Architecture

- `ParkourManager` — persistence and course data: start/end plate locations, warp spots, per-player
  scores/rankings. Backed by BentoBox's database (`objects/ParkourData`).
- `ParkourRunRecord` — a `record` holding the *live* run state: `checkpoints`, `timers`,
  `currentlyTeleporting` (all keyed by player UUID). Obtained via `addon.getParkourRunRecord()`.
- `Settings` — the `config.yml` model (`@ConfigEntry`/`@ConfigComment` annotations, implements
  `WorldSettings`). Add a config option by adding a field + getter/setter here.
- `listeners/`
  - `AbstractListener` — shared base (`isLocEquals`, etc.).
  - `MakeCourseListener` — course *building*: block place/break of the marker plates.
  - `CourseRunnerListener` — course *running*: start/end/checkpoint plates, void falls, teleport
    and gamemode handling.
- `commands/` — player command `/parkour` (aliases `pk`) subcommands (Warp/SetWarp/RemoveWarp/Top/
  ClearTop/Quit/Courses) plus the admin command; all extend BentoBox `CompositeCommand`.
- `gui/` — `CoursesTab`, `RankingsUI` panels. `generators/ChunkGeneratorWorld` — the void world gen.

**Marker blocks** (pressure plates):
- Start/End: `LIGHT_WEIGHTED_PRESSURE_PLATE`
- Checkpoint: `POLISHED_BLACKSTONE_PRESSURE_PLATE`
- Warp spot: `WARPED_PRESSURE_PLATE`

## Conventions

- **Locales** (`src/main/resources/locales/*.yml`) use **MiniMessage** tags (`<red>`, `<bold>`, …),
  not legacy `&` codes — BentoBox 3.14 parses MiniMessage natively. Keep the 23 language files
  key-identical to `en-US.yml`; runtime placeholders like `[number]`/`[direction]` must stay literal.
- User-facing text goes through `User#getTranslation`/`sendMessage`/`notify` with a locale key; never
  hard-code colours or messages.

## Testing

Tests use **JUnit 5 + MockBukkit + Mockito 5** (no PowerMock).

- Extend `world.bentobox.parkour.CommonTestSetup` — it stands up a MockBukkit server, the BentoBox
  singleton, `User` plumbing, and common manager mocks (`plugin`, `im`, `island`, `iwm`, `addon`,
  `world`, `location`, `mockPlayer`, `notifier`, …). Override `setUp()` with `@Override @BeforeEach`
  and call `super.setUp()` first.
- Do **not** re-`mockStatic(Bukkit.class)` or `mockStatic(Util.class)` — `CommonTestSetup` already
  holds those as `mockedBukkit`/`mockedUtil`; stub through those fields.
- The addon-load test (`ParkourTest`) builds a jar with `config.yml` and mocks `DatabaseSetup`
  statically — copy that pattern for anything that persists data.
- Keep test classes and `@Test` methods package-private (Sonar S5786).

**Environment gotchas** (BentoBox 3.14 / Paper 1.21.11 differ from older mocks):
- `User#sendMessage` routes through **Adventure** (`CommandSender.sendMessage(Component)`), not
  `player.spigot()`. Verify the Adventure `Component`, not a Bungee `TextComponent`.
- Paper folded `TeleportCause.CHORUS_FRUIT` into `CONSUMABLE_EFFECT` and demoted `CHORUS_FRUIT` to a
  non-constant alias — use `CONSUMABLE_EFFECT` (it can't appear in a `switch` label).
- `RanksManager` is a singleton via `getInstance()`; inject a mock with
  `WhiteBox.setInternalState(RanksManager.class, "instance", mock)` and reset it in tearDown.
- `showHelp` resolves the top label to the world friendly name (`"Parkour"`), not `null`.

## Release process

1. Work on feature branches → PR into `develop` (SNAPSHOT versions live here).
2. To release: bump `<build.version>`, open a `develop` → `master` PR titled `Release x.y.z`, and
   create a **draft** GitHub release tagged `x.y.z` targeting `master`.
3. Merge the PR; Jenkins builds the non-SNAPSHOT jar; upload it to the release and publish — the
   `publish.yml` / `modrinth-publish.yml` workflows push to CurseForge, Hangar and Modrinth.
