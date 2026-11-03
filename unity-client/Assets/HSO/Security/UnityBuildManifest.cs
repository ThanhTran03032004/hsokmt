using UnityEngine;

namespace HSO.Security
{
    [CreateAssetMenu(fileName = "UnityBuildManifest", menuName = "HSO/Security/Build Manifest")]
    public sealed class UnityBuildManifest : ScriptableObject
    {
        [SerializeField] private string buildId = "build-2026-11";
        [SerializeField] private string manifestHash = "development";
        [SerializeField] private string secretBase64 = "";

        public string BuildId => buildId.Trim();
        public string ManifestHash => manifestHash.Trim();
        public string SecretBase64 => secretBase64.Trim();

        public void Validate()
        {
            if (string.IsNullOrWhiteSpace(BuildId) || BuildId.Length > 64)
                throw new System.InvalidOperationException("Unity build ID is invalid.");
            if (string.IsNullOrWhiteSpace(ManifestHash) || ManifestHash.Length > 128)
                throw new System.InvalidOperationException("Content manifest hash is invalid.");
            if (!IsAscii(BuildId) || !IsAscii(ManifestHash))
                throw new System.InvalidOperationException("Build ID and manifest hash must contain ASCII characters only.");
            byte[] key = System.Convert.FromBase64String(SecretBase64);
            if (key.Length < 32)
                throw new System.InvalidOperationException("Unity build key must contain at least 32 bytes.");
        }

        private static bool IsAscii(string value)
        {
            foreach (char character in value)
                if (character < 0x21 || character > 0x7E)
                    return false;
            return true;
        }
    }
}
