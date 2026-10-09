package gc.david.dipole.review

/** Asks the platform's store to show its review popup. The store decides whether it appears. */
fun interface ReviewPrompter {

    fun request()

    companion object {

        /** For platforms without a store, previews and tests. */
        val None = ReviewPrompter {}
    }
}
