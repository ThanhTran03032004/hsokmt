using System;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using UnityEngine;

namespace HSO.Security
{
    public static class UnityClientAttestation
    {
        public const sbyte Command = -109;

        public static byte[] CreateChallengeRequest()
        {
            return new byte[] { 2 };
        }

        public static byte[] CreateResponse(byte[] challengePayload, UnityBuildManifest manifest)
        {
            if (challengePayload == null) throw new ArgumentNullException(nameof(challengePayload));
            if (manifest == null) throw new ArgumentNullException(nameof(manifest));
            manifest.Validate();

            int protocol;
            string nonce;
            using (var challenge = new MemoryStream(challengePayload, false))
            {
                int operation = challenge.ReadByte();
                if (operation != 0) throw new InvalidDataException("Invalid server attestation operation.");
                protocol = ReadInt32BigEndian(challenge);
                nonce = ReadUtf(challenge, 128);
                ReadInt64BigEndian(challenge);
            }

            string platform = Application.platform.ToString();
            string packageName = Application.identifier;
            long timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            string canonical = string.Join("|",
                protocol.ToString(), manifest.BuildId, platform, packageName,
                manifest.ManifestHash, timestamp.ToString(), nonce);

            byte[] key = Convert.FromBase64String(manifest.SecretBase64);
            byte[] signature;
            using (var hmac = new HMACSHA256(key))
                signature = hmac.ComputeHash(Encoding.UTF8.GetBytes(canonical));

            using (var response = new MemoryStream(512))
            {
                response.WriteByte(1);
                WriteInt32BigEndian(response, protocol);
                WriteUtf(response, manifest.BuildId);
                WriteUtf(response, platform);
                WriteUtf(response, packageName);
                WriteUtf(response, manifest.ManifestHash);
                WriteInt64BigEndian(response, timestamp);
                WriteUtf(response, ToLowerHex(signature));
                return response.ToArray();
            }
        }

        private static int ReadInt32BigEndian(Stream stream)
        {
            byte[] bytes = new byte[4];
            ReadExactly(stream, bytes);
            return (bytes[0] << 24) | (bytes[1] << 16) | (bytes[2] << 8) | bytes[3];
        }

        private static long ReadInt64BigEndian(Stream stream)
        {
            byte[] bytes = new byte[8];
            ReadExactly(stream, bytes);
            ulong value = ((ulong)bytes[0] << 56) | ((ulong)bytes[1] << 48)
                | ((ulong)bytes[2] << 40) | ((ulong)bytes[3] << 32)
                | ((ulong)bytes[4] << 24) | ((ulong)bytes[5] << 16)
                | ((ulong)bytes[6] << 8) | bytes[7];
            return unchecked((long)value);
        }

        private static string ReadUtf(Stream stream, int maximumLength)
        {
            int high = stream.ReadByte();
            int low = stream.ReadByte();
            if (high < 0 || low < 0) throw new EndOfStreamException();
            int length = (high << 8) | low;
            if (length <= 0 || length > maximumLength) throw new InvalidDataException("Invalid UTF field length.");
            byte[] bytes = new byte[length];
            ReadExactly(stream, bytes);
            return Encoding.UTF8.GetString(bytes);
        }

        private static void WriteUtf(Stream stream, string value)
        {
            foreach (char character in value ?? string.Empty)
                if (character < 0x21 || character > 0x7E)
                    throw new InvalidDataException("Attestation text fields must contain ASCII characters only.");
            byte[] bytes = Encoding.UTF8.GetBytes(value ?? string.Empty);
            if (bytes.Length > ushort.MaxValue) throw new InvalidDataException("UTF field is too large.");
            stream.WriteByte((byte)(bytes.Length >> 8));
            stream.WriteByte((byte)bytes.Length);
            stream.Write(bytes, 0, bytes.Length);
        }

        private static void WriteInt32BigEndian(Stream stream, int value)
        {
            stream.WriteByte((byte)(value >> 24));
            stream.WriteByte((byte)(value >> 16));
            stream.WriteByte((byte)(value >> 8));
            stream.WriteByte((byte)value);
        }

        private static void WriteInt64BigEndian(Stream stream, long value)
        {
            ulong unsigned = unchecked((ulong)value);
            for (int shift = 56; shift >= 0; shift -= 8)
                stream.WriteByte((byte)(unsigned >> shift));
        }

        private static void ReadExactly(Stream stream, byte[] target)
        {
            for (int offset = 0; offset < target.Length;)
            {
                int read = stream.Read(target, offset, target.Length - offset);
                if (read <= 0) throw new EndOfStreamException();
                offset += read;
            }
        }

        private static string ToLowerHex(byte[] bytes)
        {
            var result = new StringBuilder(bytes.Length * 2);
            foreach (byte value in bytes) result.Append(value.ToString("x2"));
            return result.ToString();
        }
    }
}
