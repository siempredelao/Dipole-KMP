# R8 rules for the release build. Compose, coroutines, kotlinx.serialization (navigation routes)
# and Koin ship their own rules, and the app uses no reflection of its own, so little is needed.
# If R8 reports missing classes, it suggests rules in
# androidApp/build/outputs/mapping/release/missing_rules.txt.

# multiplatform-settings creates its storage from an androidx.startup initializer, which is
# instantiated by name from the manifest.
-keep class com.russhwolf.settings.** extends androidx.startup.Initializer { <init>(); }
