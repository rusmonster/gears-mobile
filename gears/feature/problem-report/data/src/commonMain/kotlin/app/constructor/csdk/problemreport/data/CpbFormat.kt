package app.constructor.csdk.problemreport.data

internal object CpbFormat {
    const val FILE_EXTENSION = "cpb"
    val HEADER: ByteArray = "CONSTRUCTOR".encodeToByteArray()
    const val HEADER_SIZE = 11 // "CONSTRUCTOR".length
    const val LENGTH_FIELD_SIZE = 4 // INT32
    const val IV_SIZE = 12 // AES-GCM standard IV
}
