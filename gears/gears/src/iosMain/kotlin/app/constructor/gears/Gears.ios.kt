package app.constructor.gears

/**
 * Initialises Gears Mobile on iOS.
 *
 * Must be called once before creating any ViewModel (typically from your app's start-up code,
 * e.g. the `App` initializer or `AppDelegate`).
 *
 * It enables file-based logging so that SDK logs can be bundled into a report when the user
 * leaves "include logs" enabled. Unlike Android, iOS resolves its storage directory without a
 * host-supplied context, so no argument is required.
 */
fun Gears.initialize() {
    GearsInitializer.init()
}
