package app.constructor.csdk.files

import platform.Foundation.NSFileHandle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.closeFile
import platform.Foundation.dataUsingEncoding
import platform.Foundation.writeData

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
internal class IosFileWriter(
    private val handle: NSFileHandle,
) : FileWriter {
    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun write(text: String) {
        val data = (text as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        handle.writeData(data)
    }

    override fun flush() {
        // NSFileHandle doesn't have explicit flush, writes are immediate
    }

    override fun close() {
        handle.closeFile()
    }
}
