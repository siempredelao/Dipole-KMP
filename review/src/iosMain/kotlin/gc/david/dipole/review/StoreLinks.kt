package gc.david.dipole.review

actual object StoreLinks {

    actual fun storePageUrl(app: StoreApp): String? =
        app.appleAppId?.let { "itms-apps://apps.apple.com/app/id$it?action=write-review" }
}
