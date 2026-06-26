package app.constructor.csdk.files

interface FileWriter : AutoCloseable {
    fun write(text: String)

    fun flush()

    override fun close()
}
