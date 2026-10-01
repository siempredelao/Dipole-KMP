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

- `composeApp/src/commonMain/.../game` - rules engine (`Dipole.kt`) and computer opponent
  (`ComputerPlayer.kt`, which also works out the move hints), plain Kotlin with no UI dependencies.
- `composeApp/src/commonMain/.../game` also has `GameSession`, the state of a game in progress
  (mode, moves, undo).
- `composeApp/src/commonMain/.../saves` - saved games, stored with multiplatform-settings.
- `composeApp/src/commonMain/.../sound` - move and capture sounds, synthesized in code and played
  by each platform's audio API.
- `composeApp/src/commonMain/.../celebration` - the confetti thrown when a human wins, as plain
  Kotlin physics; `ui/ConfettiOverlay.kt` draws it.
- `composeApp/src/commonMain/.../tutorial` - the tutorial's pages and their example positions,
  shown on first launch and from the rules screen.
- `composeApp/src/commonMain/composeResources` - UI strings: English in `values`, translations in
  `values-<language>`.
- `composeApp/src/commonMain/.../ui` - the shared Compose UI: `DipoleViewModel` holds the state and
  `DipoleScreen` draws it.
- `androidApp/src/main/res` - Android launch splash: `drawable/splash_icon.xml` (the icon with its
  arrows grouped) animated by `drawable-v31/splash_icon_animated.xml`; older Android versions show
  the icon still. `MainActivity` keeps the splash up until the animation ends.
- `composeApp/src/{jvmMain,wasmJsMain,iosMain}` - desktop, web and iOS entry points.
- `androidApp` - Android application (AGP 9 keeps the app module separate from the KMP library).
- `iosApp` - Xcode project hosting the shared UI.

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
