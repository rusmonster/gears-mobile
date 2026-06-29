@file:Suppress("ktlint:constructor:test-method-naming")

package app.constructor.csdk.problemreport.data

import app.constructor.csdk.files.FileSystem
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.algorithms.SHA256
import gearsmobile.feature.problem_report.data.generated.resources.Res
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
class EncryptFileUseCaseImplTest {

    private val fileSystem = FileSystem()
    private lateinit var tempDir: String
    private lateinit var zipPath: String

    @BeforeTest
    fun setUp() {
        tempDir = fileSystem.createTempDir()
        zipPath = "$tempDir/test.zip"
    }

    @AfterTest
    fun tearDown() {
        fileSystem.delete(tempDir)
    }

    private suspend fun createSut(): EncryptFileUseCaseImpl {
        val publicKeyPem = Res.readBytes("files/test_public_key.pem")
        return EncryptFileUseCaseImpl(publicKeyPem)
    }

    @Test
    fun producesFileWithCpbExtension() = runTest {
        val sut = createSut()
        fileSystem.writeText(zipPath, "hello")
        val cpbPath = sut.encrypt(zipPath)
        assertTrue(cpbPath.endsWith(".${CpbFormat.FILE_EXTENSION}"), "Output must have .cpb extension")
    }

    @Test
    fun cpbFileStartsWithConstructorHeader() = runTest {
        val sut = createSut()
        fileSystem.writeText(zipPath, "hello")
        val cpbPath = sut.encrypt(zipPath)
        val bytes = SystemFileSystem.source(Path(cpbPath)).buffered().use { it.readByteArray() }
        val header = bytes.take(CpbFormat.HEADER_SIZE).toByteArray()
        assertTrue(header.contentEquals(CpbFormat.HEADER), "CPB file must start with CONSTRUCTOR header")
    }

    @Test
    fun eachCallProducesDifferentCiphertext() = runTest {
        val sut = createSut()
        fileSystem.writeText(zipPath, "same content")
        val cpb1 = SystemFileSystem.source(Path(sut.encrypt(zipPath))).buffered().use { it.readByteArray() }

        fileSystem.writeText(zipPath, "same content")
        val cpb2 = SystemFileSystem.source(Path(sut.encrypt(zipPath))).buffered().use { it.readByteArray() }

        assertTrue(!cpb1.contentEquals(cpb2), "Each encryption must produce unique output due to random AES key and IV")
    }

    @Test
    fun encryptedContentIsDecryptableWithPrivateKey() = runTest {
        val sut = createSut()
        val originalContent = "zip file content for roundtrip test"
        fileSystem.writeText(zipPath, originalContent)
        val cpbPath = sut.encrypt(zipPath)

        val decrypted = decryptCpb(cpbPath)
        assertEquals(originalContent, decrypted.decodeToString())
    }

    @OptIn(DelicateCryptographyApi::class)
    private suspend fun decryptCpb(cpbPath: String): ByteArray {
        val cpbBytes = SystemFileSystem.source(Path(cpbPath)).buffered().use { it.readByteArray() }
        var offset = CpbFormat.HEADER_SIZE

        val encAesKeyLen = cpbBytes.readInt32At(offset)
        offset += CpbFormat.LENGTH_FIELD_SIZE
        val encAesKey = cpbBytes.copyOfRange(offset, offset + encAesKeyLen)
        offset += encAesKeyLen

        val iv = cpbBytes.copyOfRange(offset, offset + CpbFormat.IV_SIZE)
        offset += CpbFormat.IV_SIZE

        val encZip = cpbBytes.copyOfRange(offset, cpbBytes.size)

        val privateKeyPem = Res.readBytes("files/test_private_key.pem")
        val rsaPrivateKey = CryptographyProvider.Default
            .get(RSA.OAEP)
            .privateKeyDecoder(SHA256)
            .decodeFromByteArray(
                RSA.PrivateKey.Format.PEM,
                privateKeyPem.decodeToString().replace("\r", "").encodeToByteArray(),
            )

        val aesKeyBytes = rsaPrivateKey.decryptor().decrypt(encAesKey)
        val aesKey = CryptographyProvider.Default
            .get(AES.GCM)
            .keyDecoder()
            .decodeFromByteArray(AES.Key.Format.RAW, aesKeyBytes)

        return aesKey.cipher().decryptWithIv(iv, encZip)
    }

    private fun ByteArray.readInt32At(offset: Int): Int = ((this[offset].toInt() and 0xFF) shl 24) or
        ((this[offset + 1].toInt() and 0xFF) shl 16) or
        ((this[offset + 2].toInt() and 0xFF) shl 8) or
        (this[offset + 3].toInt() and 0xFF)
}
