# Dipole

A Kotlin/Compose Multiplatform version of [Dipole](https://www.marksteeregames.com/Dipole_rules.pdf),
the stacking game by Mark Steere. It runs on Android, iOS, desktop (JVM) and the web (Wasm), all
sharing the same rules engine and UI.

Play against the computer or with two players on one device.

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
- `composeApp/src/commonMain/.../ui` - the shared Compose UI: `DipoleViewModel` holds the state and
  `DipoleScreen` draws it.
- `composeApp/src/{jvmMain,wasmJsMain,iosMain}` - desktop, web and iOS entry points.
- `androidApp` - Android application (AGP 9 keeps the app module separate from the KMP library).
- `iosApp` - Xcode project hosting the shared UI.

## Running

| Platform | Command |
|----------|---------|
| Tests    | `./gradlew :composeApp:jvmTest` |
| Desktop  | `./gradlew :composeApp:run` |
| Android  | `./gradlew :androidApp:installDebug` (or run `androidApp` from Android Studio) |
| Web      | `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` |
| iOS      | Open `iosApp/iosApp.xcodeproj` in Xcode and run (set your team in `iosApp/Configuration/Config.xcconfig`) |
