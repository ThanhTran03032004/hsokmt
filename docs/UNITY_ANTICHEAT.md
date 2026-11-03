# Unity-only handshake

Server command `-109` is reserved for Unity build attestation.

After command `-40` completes the stream-key exchange, Unity sends command `-109` with the one-byte payload returned by `CreateChallengeRequest()`. The server replies with command `-109` containing:

```text
byte operation = 0
int protocol
UTF nonceBase64
long serverTimestampMilliseconds
```

The Unity client calls `UnityClientAttestation.CreateResponse`, then sends the returned payload using command `-109`. This must finish before account login command `1` when `unity-only` is enabled.

## Configure a build

Generate a 32-byte key in PowerShell:

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

Create `UnityBuildManifest` in Unity from **Assets → Create → HSO → Security → Build Manifest**. Set the same build ID, base64 key, and content manifest hash used by the server.

On the VPS, set the values before starting the server:

```bat
setx HSO_UNITY_BUILD_ID "build-2026-11" /M
setx HSO_UNITY_BUILD_SECRET "b64:PUT_THE_BASE64_KEY_HERE" /M
```

Restart the terminal or Windows service after changing environment variables. First deploy with `unity-only: false`, verify that Unity sessions have a build ID in the anti-cheat log, and then change it to:

```text
unity-only: true
```

Rotate the build ID and key for every released Unity build. Keep the previous build in `unity-build-secrets` only during the update window.
