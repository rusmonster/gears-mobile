package app.constructor.csdk.problemreport.data

import dev.whyoleg.cryptography.BinarySize.Companion.bits
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlinx.io.Buffer
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class EncryptFileUseCaseImpl(
    private val publicKeyPem: ByteArray,
) : EncryptFileUseCase {

    private val aesKeyGenerator by lazy {
        CryptographyProvider.Default
            .get(AES.GCM)
            .keyGenerator(KEY_SIZE_BITS.bits)
    }

    private val rsaPublicKeyDecoder by lazy {
        CryptographyProvider.Default
            .get(RSA.OAEP)
            .publicKeyDecoder(SHA256)
    }

    // @cpt-dod:cpt-cyberfabricmobile-dod-problem-report-encrypt:p1
    @OptIn(DelicateCryptographyApi::class)
    override suspend fun encrypt(zipPath: String): String {
        val keyPem = publicKeyPem

        // @cpt-begin:cpt-cyberfabricmobile-dod-problem-report-encrypt:p1:inst-keys
        val aesKey = aesKeyGenerator.generateKey()
        val iv = CryptographyRandom.nextBytes(CpbFormat.IV_SIZE)

        val rawAesKey = aesKey.encodeToByteArray(AES.Key.Format.RAW)
        val rsaPublicKey = rsaPublicKeyDecoder.decodeFromByteArray(
            RSA.PublicKey.Format.PEM,
            keyPem.decodeToString().replace("\r", "").encodeToByteArray(),
        )
        val encryptedAesKey = rsaPublicKey.encryptor().encrypt(rawAesKey)
        // @cpt-end:cpt-cyberfabricmobile-dod-problem-report-encrypt:p1:inst-keys

        // @cpt-begin:cpt-cyberfabricmobile-dod-problem-report-encrypt:p1:inst-envelope
        val cpbPath = zipPath.replaceAfterLast('.', CpbFormat.FILE_EXTENSION)
        SystemFileSystem.sink(Path(cpbPath)).buffered().use { sink ->
            val header = Buffer()
            header.write(CpbFormat.HEADER)
            header.writeInt(encryptedAesKey.size)
            header.write(encryptedAesKey)
            header.write(iv)
            sink.write(header, header.size)

            val encryptingSource = aesKey.cipher()
                .encryptingSourceWithIv(iv, SystemFileSystem.source(Path(zipPath)))
            encryptingSource.use { source ->
                val chunk = Buffer()
                while (source.readAtMostTo(chunk, BUFFER_SIZE) != -1L) {
                    sink.write(chunk, chunk.size)
                }
            }
        }
        // @cpt-end:cpt-cyberfabricmobile-dod-problem-report-encrypt:p1:inst-envelope

        // @cpt-begin:cpt-cyberfabricmobile-dod-problem-report-encrypt:p1:inst-return
        return cpbPath
        // @cpt-end:cpt-cyberfabricmobile-dod-problem-report-encrypt:p1:inst-return
    }

    companion object {
        private const val KEY_SIZE_BITS = 256
        private const val BUFFER_SIZE = 100 * 1_024L
    }
}
