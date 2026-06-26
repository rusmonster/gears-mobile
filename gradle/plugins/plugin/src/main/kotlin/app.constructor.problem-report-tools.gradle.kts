// Tools for working with problem reports, registered on the build root.
// Applied from the root build via `id("app.constructor.problem-report-tools")`.
if (project == rootProject) {

    tasks.register("decryptReport") {
        group = "tools"
        description = "Decrypt a .cpb problem report. Usage: ./gradlew decryptReport -Pcpb=<path> [-Pkey=<path>]"
        // Ad-hoc tool: reads -P properties and arbitrary file paths at execution time.
        notCompatibleWithConfigurationCache("Reads -P properties and file paths at execution time")

        // Captured at configuration time to keep the task closure serializable.
        val cpbProp = providers.gradleProperty("cpb")
        val keyProp = providers.gradleProperty("key")
        val baseDir = layout.projectDirectory.asFile

        doLast {
            val cpbPath = cpbProp.orNull
                ?: error("Missing required parameter: -Pcpb=<path/to/report.cpb>")
            val keyPath = keyProp.orNull
                ?: "${System.getProperty("user.home")}/.constructor/private_key.pem"

            fun resolve(p: String) = java.io.File(p).let { if (it.isAbsolute) it else baseDir.resolve(p) }
            val cpbFile = resolve(cpbPath)
            require(cpbFile.exists()) { "File not found: $cpbPath" }
            val keyFile = resolve(keyPath)
            require(keyFile.exists()) { "Private key not found: $keyPath" }

            val pem = keyFile.readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("-----") && !it.startsWith("===") }
                .joinToString("")
            val privateKey = java.security.KeyFactory.getInstance("RSA")
                .generatePrivate(java.security.spec.PKCS8EncodedKeySpec(java.util.Base64.getDecoder().decode(pem)))

            val bytes = cpbFile.readBytes()
            val header = "CONSTRUCTOR".toByteArray()
            require(bytes.take(header.size).toByteArray().contentEquals(header)) { "Not a valid .cpb file" }
            var offset = header.size

            fun readInt32(): Int {
                val v = ((bytes[offset].toInt() and 0xFF) shl 24) or
                    ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
                    ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
                    (bytes[offset + 3].toInt() and 0xFF)
                offset += 4
                return v
            }

            val encAesKeyLen = readInt32()
            val encAesKey = bytes.copyOfRange(offset, offset + encAesKeyLen).also { offset += encAesKeyLen }
            val iv = bytes.copyOfRange(offset, offset + 12).also { offset += 12 }
            val encZip = bytes.copyOfRange(offset, bytes.size)

            val oaepParams = javax.crypto.spec.OAEPParameterSpec(
                "SHA-256", "MGF1", java.security.spec.MGF1ParameterSpec.SHA256, javax.crypto.spec.PSource.PSpecified.DEFAULT,
            )
            val rsaCipher = javax.crypto.Cipher.getInstance("RSA/ECB/OAEPPadding")
            rsaCipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey, oaepParams)
            val aesKeyBytes = rsaCipher.doFinal(encAesKey)

            val aesCipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
            aesCipher.init(
                javax.crypto.Cipher.DECRYPT_MODE,
                javax.crypto.spec.SecretKeySpec(aesKeyBytes, "AES"),
                javax.crypto.spec.GCMParameterSpec(128, iv),
            )
            val decryptedZip = aesCipher.doFinal(encZip)

            val outputFile = java.io.File(cpbFile.parentFile, "${cpbFile.nameWithoutExtension}.zip")
            outputFile.writeBytes(decryptedZip)
            println("Decrypted: ${outputFile.absolutePath}")
        }
    }
}
