@echo off
:loop
cd C:\xampp\mysql\bin\mysqldump -u root -pkrU7m6rCOeCOWaQL hso > backup.sql
timeout /t 1800 /nobreak > NUL
goto loop
