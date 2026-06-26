# Decrypting Problem Reports (`.cpb` files)

Problem reports submitted from the app are encrypted and saved as `.cpb` (Constructor Problem
Bundle) files, then shared by the user (e.g. as an email attachment). This guide explains how
to decrypt one using the bundled Gradle task.

Each report is encrypted to the **RSA public key** the host app supplied via
`ProblemReport.Config.publicKeyPem`. Only the holder of the matching **private key** can read
it — so keep that private key on the support/server side and never commit it.

## Prerequisites

- The **private key** file (distributed via a secure channel — never committed to the repo).

## Setting up the private key

Place the private key at the default location:

```
~/.constructor/private_key.pem
```

macOS / Linux:

```bash
mkdir -p ~/.constructor
mv /path/to/received/private_key.pem ~/.constructor/private_key.pem
chmod 600 ~/.constructor/private_key.pem
```

Windows (PowerShell):

```powershell
New-Item -ItemType Directory -Force "$env:USERPROFILE\.constructor"
Move-Item private_key.pem "$env:USERPROFILE\.constructor\private_key.pem"
```

The key must be a PEM-encoded **PKCS#8** RSA private key (`-----BEGIN PRIVATE KEY-----`).

## Usage

Run from the [`gears/`](../gears) directory (the Gradle root):

```bash
./gradlew decryptReport -Pcpb=<path/to/report.cpb>
# or point at a specific key:
./gradlew decryptReport -Pcpb=<path/to/report.cpb> -Pkey=<path/to/private_key.pem>
```

Windows:

```powershell
.\gradlew.bat decryptReport -Pcpb="C:\path\to\report.cpb"
```

### Example

```bash
./gradlew decryptReport -Pcpb=/Users/you/Downloads/report_abc123.cpb
# Output: Decrypted: /Users/you/Downloads/report_abc123.zip
```

The decrypted `.zip` is written next to the input `.cpb`.

## Inside the ZIP

The decrypted archive contains:

- `problem_report_metadata.txt` — report ID, problem type, OS/device info, the user's
  description, and steps to reproduce (if provided).
- The screenshots the user attached (original file names).
- Device log files — only if the user opted to include logs.

The **report ID** in the metadata matches the ID returned to the app at submission time, so
reports can be correlated with user feedback.

## File format reference

`.cpb` binary layout:

| Field | Size | Description |
|---|---|---|
| Magic header | 11 bytes | ASCII `CONSTRUCTOR` |
| Encrypted-key length | 4 bytes | `int32`, big-endian |
| Encrypted AES key | variable | AES-256 key, **RSA-OAEP / SHA-256**-encrypted to the public key |
| IV | 12 bytes | AES-GCM nonce |
| Ciphertext | remaining | the ZIP, **AES-256-GCM** encrypted (16-byte auth tag appended) |

The `decryptReport` task is provided by the `app.constructor.problem-report-tools` convention
plugin ([`gradle/plugins/…/app.constructor.problem-report-tools.gradle.kts`](../gradle/plugins/plugin/src/main/kotlin/app.constructor.problem-report-tools.gradle.kts)),
applied at the build root. It uses only the JDK's `java.security` / `javax.crypto`, so no extra
dependencies are required.
