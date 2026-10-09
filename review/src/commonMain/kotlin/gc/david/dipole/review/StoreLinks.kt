package gc.david.dipole.review

/** How the stores know the app. */
data class StoreApp(
    /** The Android application ID, as in Google Play. */
    val androidPackage: String,
    /** The numeric App Store ID, once the app is on the App Store. */
    val appleAppId: String? = null,
)

/** The app's page in the store, for a "Rate" menu entry. */
expect object StoreLinks {

    /** The link to rate [app] on this platform, or null where there is no store (desktop, web) or no listing yet. */
    fun storePageUrl(app: StoreApp): String?
}
