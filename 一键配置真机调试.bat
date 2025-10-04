@echo off
chcp 65001 >nul
color 0A
title 真机调试一键配置工具

echo.
echo ╔════════════════════════════════════════════════════╗
echo ║                                                    ║
echo ║       真机调试一键配置工具 v1.0                     ║
echo ║                                                    ║
echo ╚════════════════════════════════════════════════════╝
echo.

REM 检查是否以管理员身份运行
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [警告] 未以管理员身份运行，防火墙规则可能无法自动添加
    echo [提示] 右键点击此文件，选择"以管理员身份运行"可获得完整功能
    echo.
    timeout /t 3 >nul
)

echo [步骤 1/5] 检测网络环境...
echo ----------------------------------------

REM 获取WLAN的IPv4地址
set IP=
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4" ^| findstr "192.168"') do (
    set IP=%%a
    goto :found_ip
)

echo [错误] 未找到有效的局域网IP地址！
echo [提示] 请确保已连接到WiFi网络
pause
exit /b 1

:found_ip
set IP=%IP:~1%
echo ✓ 检测到IP地址: %IP%
echo.

echo [步骤 2/5] 配置前端...
echo ----------------------------------------

REM 检查前端配置文件是否存在
if not exist "frontend\utils\request.js" (
    echo [错误] 找不到 frontend\utils\request.js
    echo [提示] 请确保在项目根目录运行此脚本
    pause
    exit /b 1
)

REM 备份原配置
if not exist "frontend\utils\request.js.backup" (
    copy "frontend\utils\request.js" "frontend\utils\request.js.backup" >nul
    echo ✓ 已备份原配置文件
)

REM 使用PowerShell替换baseUrl
powershell -Command "(Get-Content 'frontend\utils\request.js' -Raw) -replace \"baseUrl:\s*'http://[^']+'\", \"baseUrl: 'http://%IP%:8888'\" | Set-Content 'frontend\utils\request.js' -NoNewline"

if %errorlevel% equ 0 (
    echo ✓ 前端配置已更新
    echo   baseUrl: 'http://%IP%:8888'
) else (
    echo [错误] 前端配置更新失败
)
echo.

echo [步骤 3/5] 配置后端...
echo ----------------------------------------

REM 检查后端配置文件
if not exist "qz-sns-app\qz-sns-web\src\main\resources\application.yml" (
    echo [警告] 找不到后端配置文件
    echo [提示] 请手动确认后端配置
) else (
    REM 检查是否已有 address: 0.0.0.0 配置
    findstr /C:"address: 0.0.0.0" "qz-sns-app\qz-sns-web\src\main\resources\application.yml" >nul
    if %errorlevel% equ 0 (
        echo ✓ 后端配置已正确
    ) else (
        echo [提示] 后端配置可能需要手动添加:
        echo   server:
        echo     address: 0.0.0.0
    )
)
echo.

echo [步骤 4/5] 配置防火墙...
echo ----------------------------------------

REM 检查防火墙规则是否存在
netsh advfirewall firewall show rule name="SpringBoot8888" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ 防火墙规则已存在
) else (
    echo [尝试] 添加防火墙规则...
    netsh advfirewall firewall add rule name="SpringBoot8888" dir=in action=allow protocol=TCP localport=8888 >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✓ 防火墙规则已添加
    ) else (
        echo [失败] 防火墙规则添加失败（需要管理员权限）
        echo [手动] 请以管理员身份运行此脚本，或手动添加规则
    )
)
echo.

echo [步骤 5/5] 检查后端状态...
echo ----------------------------------------

REM 检查8888端口是否在使用
netstat -ano | findstr :8888 | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ 8888端口正在监听（后端可能已启动）
) else (
    echo [警告] 8888端口未监听
    echo [提示] 请启动SpringBoot后端应用
)
echo.

echo ╔════════════════════════════════════════════════════╗
echo ║                 配置完成！                          ║
echo ╚════════════════════════════════════════════════════╝
echo.
echo [配置信息]
echo ----------------------------------------
echo IP地址: %IP%
echo 后端地址: http://%IP%:8888
echo.
echo [下一步操作]
echo ----------------------------------------
echo 1. 启动SpringBoot后端（如果未启动）
echo 2. 手机连接同一WiFi网络
echo 3. 手机浏览器测试: http://%IP%:8888
echo 4. HBuilderX中运行到手机
echo.
echo [测试连接]
echo ----------------------------------------
echo 正在尝试打开浏览器测试...
timeout /t 2 >nul
start http://%IP%:8888
echo.
echo ========================================
echo 按任意键退出...
pause >nul

