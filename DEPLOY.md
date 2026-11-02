# Deploy lên VPS Windows Server 2012 R2

## Cài lần đầu

Cài Git for Windows và Java 17, đồng thời thêm `git.exe`, `java.exe` vào PATH. Mở PowerShell bằng quyền Administrator, sau đó chạy:

```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force
.\scripts\setup-vps-windows.ps1 -RepoUrl 'https://github.com/OWNER/REPOSITORY.git'
```

Script clone project vào `C:\HSOKMT`, build jar và tạo Scheduled Task `HSOKMT Server` tự chạy khi Windows khởi động.

Panel quản trị truy cập tại `http://IP_VPS:19130/admin`. Token nằm trong `hso.conf` ở dòng `admin_tool_token`.

Cấu hình database trong `hso.conf`:

```text
mysql-host: 127.0.0.1
mysql-port: 3307
mysql-user: hsokmt
mysql-password: MAT_KHAU
mysql-database: xnxx
```

Mở port game đã khai báo ở `hso.conf` (mặc định `19129`) trên firewall/security group của VPS.

## Cập nhật

```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force
.\scripts\update-vps-windows.ps1
```

Lệnh này sẽ `git pull --ff-only`, build lại jar và khởi động lại task. Log server nằm trong `log_server` và `log`.

Nếu dùng branch khác:

```powershell
.\scripts\update-vps-windows.ps1 -Branch production
```
