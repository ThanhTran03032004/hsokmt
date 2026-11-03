using System;
using UnityEngine;

namespace HSO.Security
{
    public sealed class UnityAttestationController : MonoBehaviour
    {
        [SerializeField] private UnityBuildManifest manifest;

        private Action<sbyte, byte[]> sendPacket;
        private Action continueLogin;
        private bool challengeRequested;
        private bool responseSent;

        public bool IsComplete => responseSent;

        public void Configure(Action<sbyte, byte[]> packetSender, Action loginContinuation)
        {
            sendPacket = packetSender ?? throw new ArgumentNullException(nameof(packetSender));
            continueLogin = loginContinuation ?? throw new ArgumentNullException(nameof(loginContinuation));
            if (manifest == null)
                throw new InvalidOperationException("UnityBuildManifest is not assigned.");
            manifest.Validate();
        }

        public void OnStreamKeyExchangeCompleted()
        {
            if (sendPacket == null)
                throw new InvalidOperationException("Configure must be called before starting attestation.");
            if (challengeRequested)
                return;

            challengeRequested = true;
            sendPacket(UnityClientAttestation.Command, UnityClientAttestation.CreateChallengeRequest());
        }

        public bool TryHandleServerPacket(sbyte command, byte[] payload)
        {
            if (command != UnityClientAttestation.Command)
                return false;
            if (!challengeRequested)
                throw new InvalidOperationException("Received an attestation challenge before requesting it.");
            if (responseSent)
                throw new InvalidOperationException("Received a duplicate attestation challenge.");
            if (payload == null || payload.Length == 0 || payload[0] != 0)
                throw new InvalidOperationException("Server returned an invalid attestation packet.");

            byte[] response = UnityClientAttestation.CreateResponse(payload, manifest);
            sendPacket(UnityClientAttestation.Command, response);
            responseSent = true;

            // TCP preserves packet order, so account login can be queued after the response.
            continueLogin();
            return true;
        }

        public void ResetHandshake()
        {
            challengeRequested = false;
            responseSent = false;
        }
    }
}
