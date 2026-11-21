package dev.navalcode.shelf.presentation

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/** Optional deterministic motion evidence; no changes to production state or test assertions. */
internal class NativeCapture(private val rule: ComposeContentTestRule) {
    fun frame(name: String, settle: Boolean = true) {
        require(name.matches(Regex("(idle|error|empty|search|detail|(motion|return)-[0-9]{3})")))
        if (settle) rule.mainClock.advanceTimeBy(400)
        rule.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        // SurfaceFlinger/IME drawing uses a device clock, separate from Compose's test clock.
        Thread.sleep(100)
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        val source = File(instrumentation.targetContext.getExternalFilesDir(null), "$name.png")
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        for (command in listOf("mkdir -p /sdcard/Download/shelf-test-output",
            "cp ${source.absolutePath} /sdcard/Download/shelf-test-output/$name.png")) {
            ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
        }
    }

    fun transition(prefix: String, action: () -> Unit) {
        if (InstrumentationRegistry.getArguments().getString("motionCapture") != "true") {
            action(); return
        }
        rule.mainClock.autoAdvance = false
        try {
            frame("$prefix-000", settle = false)
            action()
            var previous = 0L
            for (time in listOf(16L, 80L, 160L, 240L, 400L)) {
                rule.mainClock.advanceTimeBy(time - previous)
                frame("$prefix-" + time.toString().padStart(3, '0'), settle = false)
                previous = time
            }
        } finally { rule.mainClock.autoAdvance = true }
    }
}
