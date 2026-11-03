# HSO KMT — Unity-only và Anti-cheat

Tài liệu này là đặc tả đầy đủ để agent làm **Unity client** tích hợp đúng anti-cheat của server. Đọc hết tài liệu trước khi bật `unity-only`. Nếu client gửi command đăng nhập `1` trước khi hoàn tất handshake `-109`, server sẽ coi đó là client cũ và chặn kết nối.

## 1. Kết quả cần đạt

Client Unity phải thực hiện đúng thứ tự:

```text
TCP connected
→ trao đổi stream key bằng command -40 như client hiện tại
→ Unity gửi command -109, payload [2]
→ server gửi command -109, payload challenge operation 0
→ Unity tạo HMAC và gửi command -109, payload response operation 1
→ Unity mới gửi command 1 để đăng nhập tài khoản
→ chọn nhân vật bằng command 13
→ vào game
```

Server sẽ không dựa vào tên cửa sổ, chuỗi `Unity`, User-Agent hay đuôi file. Khi `unity-only: true`, phiên chưa ký đúng challenge không được đăng nhập.

## 2. Các file liên quan

### Server Java

```text
src/main/java/Game/security/ClientAttestationService.java
src/main/java/Game/security/AntiCheatService.java
src/main/java/Game/security/PacketRateLimiter.java
src/main/java/Game/security/SessionState.java
src/main/java/Game/security/ViolationType.java
src/main/java/Game/io/Session.java
src/main/java/Game/client/MessageHandler.java
src/main/java/Game/map/MapService.java
src/main/java/Game/core/Manager.java
src/main/java/Game/core/AdminToolServer.java
```

### Unity client — mã đã viết sẵn

```text
unity-client/Assets/HSO/Security/UnityBuildManifest.cs
unity-client/Assets/HSO/Security/UnityClientAttestation.cs
unity-client/Assets/HSO/Security/UnityAttestationController.cs
```

Agent client phải copy cả ba file vào `Assets/HSO/Security` của project Unity và nối `UnityAttestationController` với network manager đang có.

## 3. Command và byte trên đường truyền

Command dành riêng cho handshake:

```text
signed byte: -109
unsigned byte: 147
hex: 0x93
```

Nếu network client dùng `sbyte`, dùng `-109`. Nếu dùng `byte`, dùng `147` hoặc `0x93`. Không tạo một command khác.

Command `-109` dùng framing bình thường của game:

```text
1 byte command đã qua stream cipher hiện tại
2 byte payload length big-endian đã qua stream cipher
N byte payload đã qua stream cipher
```

Không ghi trực tiếp payload xuống socket và không tự mã hóa thêm. Hãy gọi đúng hàm `sendMessage(command, payload)` hiện có của client sau khi command `-40` đã cài stream key.

## 4. Ba loại payload command -109

### 4.1 Unity yêu cầu challenge

Unity gửi sau khi hoàn tất key exchange:

```text
byte operation = 2
```

Payload chính xác là một byte:

```text
02
```

Mã có sẵn:

```csharp
byte[] payload = UnityClientAttestation.CreateChallengeRequest();
sendMessage(UnityClientAttestation.Command, payload);
```

### 4.2 Server trả challenge

Server trả:

```text
byte operation = 0
int32 protocol                    // big-endian
UTF nonceBase64                  // 2-byte length big-endian + UTF-8 bytes
int64 serverTimestampMillis      // big-endian, Unix milliseconds UTC
```

Hiện tại protocol mặc định là `2`. Nonce là 32 byte ngẫu nhiên được mã hóa Base64.

Java dùng `DataOutputStream.writeUTF`, còn helper C# ghi UTF-8 với độ dài 2 byte. Hai cách này cho byte giống nhau vì toàn bộ field attestation bị giới hạn ở ASCII. Không đưa Unicode hoặc dấu tiếng Việt vào Build ID, package, platform hay manifest hash.

### 4.3 Unity trả chữ ký

Unity gửi:

```text
byte operation = 1
int32 protocol                    // lấy nguyên giá trị từ challenge
UTF buildId
UTF platform
UTF packageName
UTF manifestHash
int64 clientTimestampMillis       // Unix milliseconds UTC
UTF signatureHex                  // HMAC-SHA256, 64 ký tự hex thường
```

Giới hạn server:

| Trường | Điều kiện |
|---|---|
| `protocol` | Phải bằng `unity-protocol` trên server |
| `buildId` | 1–64 ký tự và phải có key trên server |
| `platform` | 1–32 ký tự |
| `packageName` | 1–128 ký tự và phải nằm trong `unity-packages` |
| `manifestHash` | 1–128 ký tự |
| `clientTimestampMillis` | Không lệch giờ server quá 120 giây |
| `signatureHex` | HMAC hợp lệ, hex thường hoặc hoa đều parse được |

## 5. Công thức chữ ký chính xác

Chuỗi canonical dùng ký tự `|`, không thêm khoảng trắng, không thêm xuống dòng:

```text
protocol|buildId|platform|packageName|manifestHash|clientTimestampMillis|nonceBase64
```

Ví dụ hình thức:

```text
2|build-2026-11|WindowsPlayer|com.hso.kmt|a1b2c3|1793720000000|BASE64_NONCE
```

Tính chữ ký:

```text
rawKey = Base64Decode(secretBase64)
signatureBytes = HMAC-SHA256(rawKey, UTF8(canonical))
signatureHex = lowercase hex của signatureBytes
```

Phía Unity, `secretBase64` chỉ chứa Base64 thuần. Không đặt tiền tố `b64:` vào asset Unity.

Phía server, biến môi trường chứa:

```text
HSO_UNITY_BUILD_SECRET=b64:BASE64_KEY
```

Tiền tố `b64:` chỉ dùng ở server để báo rằng chuỗi phía sau phải được Base64 decode.

## 6. Cách nối vào network manager Unity

### 6.1 Thêm component

1. Copy ba file C# ở mục 2 vào project Unity.
2. Tạo GameObject tồn tại cùng network manager.
3. Add component `UnityAttestationController`.
4. Gán asset `UnityBuildManifest` vào field `Manifest` trên Inspector.
5. Giữ GameObject qua đổi scene nếu network manager cũng được giữ qua scene.

### 6.2 Khởi tạo controller

Network manager phải truyền hàm gửi packet hiện có và callback đăng nhập:

```csharp
attestation.Configure(
    (command, payload) => SendGameMessage(command, payload),
    () => SendAccountLogin()
);
```

`SendGameMessage` phải là hàm đang dùng để gửi các command game, có xử lý stream cipher và packet length. `SendAccountLogin` là logic command `1` cũ.

Nếu network manager dùng `byte` thay vì `sbyte`:

```csharp
attestation.Configure(
    (command, payload) => SendGameMessage(unchecked((byte)command), payload),
    () => SendAccountLogin()
);
```

### 6.3 Bắt đầu sau command -40

Ngay sau khi client đã nhận key, giải mã key và đặt cờ `sendKeyComplete = true`:

```csharp
attestation.OnStreamKeyExchangeCompleted();
```

Không gọi ở `Awake`, `Start` hoặc ngay lúc TCP connect. Nếu gửi `-109` trước khi stream key hoàn tất, server không xử lý được packet.

### 6.4 Chặn command 1 cho tới khi handshake xong

Xóa hoặc trì hoãn lệnh đăng nhập tự động hiện tại. Command `1` chỉ được gửi trong callback `continueLogin` của controller.

Sai:

```csharp
OnConnected();
SendAccountLogin();
```

Đúng:

```csharp
OnConnected();
// Chờ -40.

OnKeyExchangeCompleted();
attestation.OnStreamKeyExchangeCompleted();

// Controller tự gọi SendAccountLogin sau khi đã gửi response operation 1.
```

TCP bảo toàn thứ tự packet. Server sẽ xử lý response `-109` trước command đăng nhập `1` được queue ngay sau đó.

### 6.5 Chuyển packet -109 cho controller

Trong switch nhận message, thêm case trước default:

```csharp
case unchecked((sbyte)-109):
    attestation.TryHandleServerPacket(unchecked((sbyte)-109), messagePayload);
    return;
```

Nếu command phía client là `byte`:

```csharp
case 0x93:
    attestation.TryHandleServerPacket(-109, messagePayload);
    return;
```

`messagePayload` phải là payload đã giải mã và đã bỏ command/length header.

### 6.6 Kết nối lại

Khi mất kết nối hoặc bắt đầu một TCP session mới:

```csharp
attestation.ResetHandshake();
```

Mỗi kết nối phải xin nonce mới. Không cache challenge, response hoặc signature để dùng lại.

## 7. Tạo UnityBuildManifest

Trong Unity Editor:

```text
Assets
→ Create
→ HSO
→ Security
→ Build Manifest
```

Đặt asset trong thư mục chỉ dành cho build chính thức, rồi nhập:

```text
Build Id: build-2026-11
Manifest Hash: mã hash phiên bản content/data của client
Secret Base64: key Base64 thuần do VPS tạo
```

Package Name được lấy tự động bằng:

```csharp
Application.identifier
```

Giá trị này phải trùng tuyệt đối với `unity-packages` trong `hso.conf`, kể cả chữ hoa/thường.

Platform được lấy tự động bằng:

```csharp
Application.platform.ToString()
```

Ví dụ thường gặp: `WindowsPlayer`, `Android`, `IPhonePlayer`.

## 8. Tạo key và cấu hình VPS

Sau khi VPS đã pull commit anti-cheat, chạy bằng **Run as administrator**:

```bat
C:\HSOKMT\enable-unity-only.bat
```

Nhập:

```text
Build ID: build-2026-11
Package Name: package thật trong Unity Player Settings
```

Script tạo key 32 byte, đặt hai biến môi trường cấp Machine:

```text
HSO_UNITY_BUILD_ID
HSO_UNITY_BUILD_SECRET
```

Script sẽ in `Unity manifest key (base64)`. Copy chính xác phần Base64 vào `Secret Base64` của `UnityBuildManifest`, không copy tiền tố `b64:`.

Biến môi trường chỉ được Java đọc khi process server khởi động mới. Reload config trong game không đủ; phải restart server.

## 9. Quy trình bật an toàn, không khóa nhầm Unity

Làm đúng thứ tự này:

1. Agent tích hợp đủ ba file C# và các hook ở mục 6.
2. Chạy `enable-unity-only.bat` trên VPS để tạo Build ID, package và key.
3. Copy key vào `UnityBuildManifest`.
4. Build Unity bằng IL2CPP và phát hành bản client mới.
5. Trước lần test đầu, đặt tạm trong `hso.conf`:

   ```text
   anticheat-enabled: true
   unity-only: false
   ```

6. Restart server để Java process đọc build key từ biến môi trường.
7. Đăng nhập bằng Unity mới.
8. Mở Admin Tool và kiểm tra `Unity verified` tăng lên ít nhất `1`.
9. Kiểm tra `log/anticheat-events.tsv` không có `CLIENT_ATTESTATION` cho phiên Unity vừa thử.
10. Trong Admin Tool, bật **Chỉ cho Unity đã xác thực** và lưu.
11. Thử lại Unity: phải đăng nhập được.
12. Thử JAR: phải bị từ chối trước đăng nhập.

Không bật `unity-only: true` trước khi Admin Tool đã thấy ít nhất một phiên `Unity verified` từ đúng bản client sẽ phát hành.

## 10. Cấu hình server

Các khóa trong `hso.conf`:

```text
anticheat-enabled: true
unity-only: false
unity-protocol: 2
unity-packages: com.hso.kmt
unity-build-secrets:
max-inbound-packet-size: 32768
packet-rate-per-second: 120
movement-units-per-second: 600
movement-grace-units: 80
```

Có hai cách cấp build key.

### Cách khuyên dùng: biến môi trường VPS

```text
HSO_UNITY_BUILD_ID=build-2026-11
HSO_UNITY_BUILD_SECRET=b64:BASE64_KEY
```

### Cách cấu hình nhiều build trong hso.conf

```text
unity-build-secrets: build-2026-11=b64:KEY_1,build-previous=b64:KEY_2
```

Chỉ giữ build cũ trong thời gian người chơi cập nhật. Xóa build cũ rồi restart server để thu hồi quyền đăng nhập của bản đó.

## 11. Server chặn những gì

### Trước đăng nhập

- Packet lớn hơn `max-inbound-packet-size` bị ngắt.
- Pre-auth tối đa 30 packet/giây.
- Command `-109`, `1`, `13`, `14` tối đa 5 lần/giây.
- Khi Unity-only bật, command khác `-109` trước xác thực bị ghi vi phạm.
- Ba lần gửi command trước xác thực đạt 6 điểm và bị đóng kết nối.
- Build ID, package, protocol, timestamp và HMAC đều phải hợp lệ.
- Nonce chỉ dùng được trong session hiện tại và bị xóa sau lần kiểm tra response.
- Response cũ không dùng lại được trên kết nối mới.

### Tài khoản và nhân vật

- Truy vấn đăng nhập dùng `PreparedStatement`.
- ID nhân vật command `13` phải thuộc danh sách nhân vật của tài khoản.
- Chọn ID nhân vật của tài khoản khác bị log và đóng kết nối.

### Trong game

- Giới hạn packet tổng và theo command.
- Di chuyển kiểm tra khoảng cách theo thời gian, tốc độ và grace.
- Sai vị trí bị server gửi tọa độ đúng về client.
- Skill kiểm tra index, MP, stun, cooldown và số mục tiêu.
- Spam skill trước cooldown bị ghi điểm và có thể bị đóng kết nối.
- Client không tự quyết định sát thương cuối cùng.

Log vi phạm:

```text
log/anticheat-events.tsv
```

Mỗi dòng gồm:

```text
UTC time | IP | account | player | buildId | violationType | addedScore | totalScore | detail
```

## 12. Xử lý lỗi

### Java vẫn đăng nhập được

Kiểm tra Admin Tool hoặc `hso.conf`:

```text
unity-only: true
```

Nếu đang là `false`, server vẫn cho client cũ đăng nhập để test chuyển đổi.

### Unity bị báo phiên bản không hợp lệ

Kiểm tra theo thứ tự:

1. Client có gửi payload `[2]` sau command `-40` không.
2. Client có nhận command byte `0x93` không.
3. Network switch có coi `147` và `-109` là cùng command không.
4. Response operation có bằng `1` không.
5. `Build Id` có trùng `HSO_UNITY_BUILD_ID` không.
6. Client key có bỏ tiền tố `b64:` không.
7. Package có trùng `Application.identifier` và `unity-packages` không.
8. Đồng hồ Windows VPS và thiết bị có lệch quá 120 giây không.
9. Canonical có đúng thứ tự và không có newline/khoảng trắng không.
10. Client có gửi command `1` trước response `-109` không.

### Server báo không có build key

Console có dòng:

```text
UNITY-ONLY is enabled but no Unity build secret is configured.
```

Chạy lại `enable-unity-only.bat` bằng Administrator rồi restart task server. Scheduled Task chạy bằng SYSTEM nên dùng biến môi trường cấp `Machine`.

### Admin không cho bật Unity-only

API cố ý từ chối nếu Java process chưa load ít nhất một build key. Restart server rồi tải lại trang Admin Tool.

### Unity nhận challenge lặp lại

Client đang gọi `OnStreamKeyExchangeCompleted` nhiều lần hoặc reconnect nhưng không gọi `ResetHandshake`. Mỗi TCP connection chỉ yêu cầu một challenge.

### HMAC luôn sai

In tạm các giá trị sau trong Development Build rồi so với server:

```text
protocol
buildId
platform
packageName
manifestHash
clientTimestampMillis
nonceBase64
canonical
signatureHex
```

Không log `secretBase64` trong bản phát hành.

## 13. Tiêu chí nghiệm thu cho agent client

Agent chỉ được coi là hoàn tất khi đủ các mục:

- [ ] Ba file trong `Assets/HSO/Security` compile không lỗi trên Unity project thật.
- [ ] Build dùng package đúng với server.
- [ ] Command `-109` đi qua packet framing và stream cipher sẵn có.
- [ ] Challenge request chỉ gửi sau khi command `-40` hoàn tất.
- [ ] Client đọc int/long theo big-endian.
- [ ] Client dùng Unix milliseconds UTC.
- [ ] HMAC dùng raw key đã Base64 decode.
- [ ] Command đăng nhập `1` không thể chạy trước khi response operation `1` đã được gửi.
- [ ] Reconnect gọi `ResetHandshake` và xin nonce mới.
- [ ] Admin Tool hiện `Unity verified: 1` khi test một client.
- [ ] Unity đăng nhập được khi `unity-only: true`.
- [ ] JAR bị chặn khi `unity-only: true`.
- [ ] Tắt/mở scene hoặc reconnect không gửi challenge trùng.
- [ ] Development Build không log secret.

## 14. Lệnh deploy

Push thay đổi từ máy code:

```bat
push-anticheat.bat
```

Cập nhật VPS:

```bat
schtasks /Run /TN "HSOKMT Updater"
```

Restart riêng server:

```bat
schtasks /Run /TN "HSOKMT Restart"
```

## 15. Lưu ý bảo mật

Build Unity nên dùng IL2CPP. Key nằm trong client vẫn có thể bị trích xuất bởi người có khả năng reverse engineering; handshake này chặn client JAR cũ, replay và client tự khai metadata đơn giản. Các kiểm tra gameplay phía server vẫn là lớp bảo vệ chính. Mỗi lần phát hành nên đổi Build ID và key, đồng thời thu hồi build cũ sau thời gian cập nhật.
