# Dipole

A Kotlin/Compose Multiplatform version of [Dipole](https://www.marksteeregames.com/Dipole_rules.pdf),
the stacking game by Mark Steere. It runs on Android, iOS, desktop (JVM) and the web (Wasm), all
sharing the same rules engine and UI.

Play against the computer (as White or Black) or with two players on one device. The app follows
the device's light or dark mode and offers five boards, including a high-contrast one for
colour-blind and low-vision players.

## Rules in short

- 8x8 checkerboard, only dark squares are used. Each player starts with a stack of 12 checkers.
  White moves first.
- Move a whole stack or part of it. A moved stack travels exactly as many squares as it has
  checkers, jumping over anything in between.
- Plain moves and merges go forward or diagonally forward. Straight moves need an even number
  of checkers (odd distances end on a light square).
- Captures go in any of the 8 directions and take a whole enemy stack of equal or smaller size.
- Stacks moved off the board (forward or diagonally forward) are removed from play.
- You must move if you can; with no moves you sit out. Remove all enemy checkers to win.

## Project layout

- `core` - the game without any UI. It has no Compose dependency, so the logic can't reach into
  the UI; `composeApp` depends on it.
- `core/src/commonMain/.../game` - the pieces as plain data (`Dipole.kt`), the rules
  (`DipoleRules.kt`) and the computer opponent (`ComputerPlayer.kt`, which also works out the move
  hints). `GameSession` is a game in progress (mode, moves, positions) and `GameSessions` plays,
  undoes and replays it.
- `core/src/commonMain/.../saves` - saved games (`SavedGame`), created by `SavedGameFactory`,
  turned into text by `SavedGameCodec` and stored with multiplatform-settings.
- `core/src/commonMain/.../sound` - move and capture sounds, synthesized in code (`ToneSynth`) and
  played by each platform's audio API.
- `core/src/commonMain/.../celebration` - the confetti thrown when a human wins: `ConfettiLauncher`
  throws it, `ConfettiPhysics` moves it and `ui/ConfettiOverlay.kt` draws it.
- `core/src/commonMain/.../tutorial` - the tutorial's pages and their example positions,
  shown on first launch and from the rules screen.
- `composeApp/src/commonMain/composeResources` - UI strings: English in `values`, translations in
  `values-<language>`.
- `composeApp/src/commonMain/.../ui` - the shared Compose UI. `DipoleApp` navigates between three
  screens: the game (`GameViewModel` + `GameScreen`), the rules and the tutorial
  (`TutorialViewModel` + `TutorialScreen`). `SettingsViewModel` holds the appearance and sound
  settings for the whole app. `GameUiStateMapper` works out what the game screen shows (movable
  stacks, targets, status), so the composables only read fields.
- `composeApp/src/commonMain/.../di` - dependency injection with Koin: `AppModules.kt` declares
  storage, the computer players and the ViewModels, and `DipoleApp` starts Koin on every platform.
  `AppModulesTest` builds the whole graph, so a missing binding fails the tests.
- `androidApp/src/main/res` - Android launch splash: `drawable/splash_icon.xml` (the icon with its
  arrows grouped) animated by `drawable-v31/splash_icon_animated.xml`; older Android versions show
  the icon still. `MainActivity` keeps the splash up until the animation ends.
- `composeApp/src/{jvmMain,wasmJsMain,iosMain}` - desktop, web and iOS entry points.
- `androidApp` - Android application (AGP 9 keeps the app module separate from the KMP library).
- `iosApp` - Xcode project hosting the shared UI.

## Architecture

Data classes hold values and nothing else; the logic lives in small objects and classes next to
them. A member stays on a data class only when it reads that class's own fields and encodes no rule
of the game, storage format, clock or randomness (`Move.to`, `Player.opponent`, the `Stack` size
check).

- **Plain objects** for logic with one fixed behaviour and no outside dependencies: `DipoleRules`,
  `GameSessions`, `SavedGameCodec`, `ConfettiPhysics`, `ToneSynth`, `GameUiStateMapper`. Call them
  directly; they need no injection.
- **Classes injected by Koin** for anything that needs the outside world: storage
  (`SavedGamesRepository`, `GamePreferences`), a random source or the clock (`SavedGameFactory`),
  or a thinking budget (`ComputerPlayer`).
- **ViewModels** decide when things happen (play, sound, the computer's turn, saving) and keep the
  UI state; the composables draw that state and report taps as actions.

## Credits

Dipole was designed by Mark Steere. Copyright © May 2007 by Mark Steere.

His [rules sheet](https://www.marksteeregames.com/Dipole_rules.pdf) allows programming the game for
online or offline play, as long as the name and rules are kept and the game is attributed to him.

More of his games are at [marksteeregames.com](https://www.marksteeregames.com).

## Running

| Platform | Command |
|----------|---------|
| Tests    | `./gradlew :composeApp:jvmTest` |
| Desktop  | `./gradlew :composeApp:run` |
| Android  | `./gradlew :androidApp:installDebug` (or run `androidApp` from Android Studio) |
| Web      | `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` |
| iOS      | Open `iosApp/iosApp.xcodeproj` in Xcode and run (set your team in `iosApp/Configuration/Config.xcconfig`) |
