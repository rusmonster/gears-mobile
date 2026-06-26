package app.constructor.csdk.problemreport.data

/**
 * Encrypts [zipPath] and produces a `.cpb` file in the same directory.
 *
 * The `.cpb` format is a self-contained binary envelope:
 * ```
 * <CONSTRUCTOR_HEADER>   — 11 bytes magic ("CONSTRUCTOR")
 * <ENCRYPTED_AES_LEN>    — INT32 (4 bytes), length of the RSA-encrypted AES key
 * <ENCRYPTED_AES_KEY>    — variable, AES-256 key encrypted with RSA-2048-OAEP/SHA-256
 * <IV>                   — 12 bytes fixed, AES-GCM initialization vector
 * <ENCRYPTED_ZIP_CONTENT>— remaining bytes, AES-256-GCM encrypted ZIP (streamed)
 * ```
 *
 * The RSA public key is supplied by the caller (see `EncryptFileUseCaseImpl`).
 * Decryption requires the corresponding RSA private key (never stored in the app).
 *
 * @return path to the produced `.cpb` file
 */
interface EncryptFileUseCase {
    suspend fun encrypt(zipPath: String): String
}
