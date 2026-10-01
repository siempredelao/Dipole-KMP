package gc.david.dipole.ui

import kotlinx.serialization.Serializable

/** The app's screens, as type-safe navigation destinations. */
@Serializable
data object GameRoute

@Serializable
data object RulesRoute

@Serializable
data object TutorialRoute
