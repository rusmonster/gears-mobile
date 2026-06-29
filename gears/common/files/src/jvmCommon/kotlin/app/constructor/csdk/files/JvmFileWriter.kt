package app.constructor.csdk.files

import java.io.OutputStreamWriter

internal class JvmFileWriter(
    private val writer: OutputStreamWriter,
) : FileWriter {
    override fun write(text: String) {
        writer.write(text)
    }

    override fun flush() {
        writer.flush()
    }

    override fun close() {
        writer.close()
    }
}
