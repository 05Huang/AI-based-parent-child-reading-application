@echo off
chcp 65001 >nul
echo ========================================
echo    获取本机IP地址 - 用于真机调试
echo ========================================
echo.

REM 获取WLAN的IPv4地址
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4" ^| findstr "192.168"') do (
    set IP=%%a
    goto :found
)

REM 如果没找到192.168开头的，尝试查找其他私有IP段
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4" ^| findstr "10."') do (
    set IP=%%a
    goto :found
)

for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4" ^| findstr "172."') do (
    set IP=%%a
    goto :found
)

echo [错误] 未找到有效的局域网IP地址！
echo 请确保你已连接到WiFi或有线网络。
pause
exit /b 1

:found
REM 去掉IP地址前面的空格
set IP=%IP:~1%

echo [成功] 找到你的IP地址
echo.
echo ╔═══════════════════════════════════════════════════════╗
echo ║  你的局域网IP地址是: %IP%                       
echo ╚═══════════════════════════════════════════════════════╝
echo.
echo ----------------------------------------
echo 前端配置信息：
echo ----------------------------------------
echo 在 frontend/utils/request.js 中修改：
echo.
echo   baseUrl: 'http://%IP%:8888'
echo.
echo ----------------------------------------
echo 后端配置信息：
echo ----------------------------------------
echo 在 qz-sns-app/qz-sns-web/src/main/resources/application.yml 中确保有：
echo.
echo   server:
echo     port: 8888
echo     address: 0.0.0.0
echo.
echo ----------------------------------------
echo 测试连接：
echo ----------------------------------------
echo 1. 确保后端已启动（SpringBoot）
echo 2. 手机连接同一WiFi网络
echo 3. 手机浏览器访问: http://%IP%:8888
echo 4. 如果能打开页面，说明网络连接正常
echo.
echo ----------------------------------------
echo 防火墙设置：
echo ----------------------------------------
echo 如果手机连接不上，需要添加防火墙规则：
echo 以管理员身份运行命令行，执行：
echo.
echo netsh advfirewall firewall add rule name="SpringBoot8888" dir=in action=allow protocol=TCP localport=8888
echo.
echo ========================================
echo.
pause

