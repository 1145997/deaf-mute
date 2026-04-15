@echo off
chcp 65001 >nul
title Kill Port 8080

echo 正在查找占用 8080 端口的进程...
set FOUND=0

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    set FOUND=1
    echo 发现 PID: %%a
    tasklist | findstr %%a
    echo 正在结束 PID %%a ...
    taskkill /F /PID %%a
    echo.
)

if "%FOUND%"=="0" (
    echo 没有发现占用 8080 端口的 LISTENING 进程。
)

echo.
echo 处理完成。
pause