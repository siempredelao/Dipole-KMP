package gc.david.dipole.review

actual object StoreLinks {

    // The Play Store app opens these links itself; without it they open in the browser.
    actual fun storePageUrl(app: StoreApp): String? =
        "https://play.google.com/store/apps/details?id=${app.androidPackage}"
}
