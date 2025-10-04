# PowerShell脚本 - 获取IP并自动更新配置
# 编码设置
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   IP地址检测工具 - 真机调试助手" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 获取所有网络适配器的IPv4地址
$adapters = Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
    $_.IPAddress -match '^(192\.168\.|10\.|172\.(1[6-9]|2[0-9]|3[01])\.)'
}

if ($adapters.Count -eq 0) {
    Write-Host "[错误] 未找到有效的局域网IP地址！" -ForegroundColor Red
    Write-Host "请确保你已连接到WiFi或有线网络。" -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host "[检测到的网络接口]" -ForegroundColor Green
Write-Host ""

$index = 1
$selectedAdapter = $null

foreach ($adapter in $adapters) {
    $interfaceAlias = (Get-NetAdapter -InterfaceIndex $adapter.InterfaceIndex).InterfaceAlias
    $ip = $adapter.IPAddress
    
    Write-Host "$index. $interfaceAlias" -ForegroundColor Yellow
    Write-Host "   IP地址: $ip" -ForegroundColor White
    
    # 自动选择WLAN或以太网
    if ($interfaceAlias -match "WLAN|Wi-Fi|以太网|Ethernet" -and $null -eq $selectedAdapter) {
        $selectedAdapter = $adapter
        Write-Host "   [推荐使用]" -ForegroundColor Green
    }
    
    Write-Host ""
    $index++
}

# 如果没有找到推荐的，使用第一个
if ($null -eq $selectedAdapter) {
    $selectedAdapter = $adapters[0]
}

$ip = $selectedAdapter.IPAddress
$interfaceAlias = (Get-NetAdapter -InterfaceIndex $selectedAdapter.InterfaceIndex).InterfaceAlias

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " 选定的IP地址: $ip" -ForegroundColor Green
Write-Host " 网络接口: $interfaceAlias" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 前端配置
Write-Host "[1] 前端配置" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
Write-Host "文件路径: frontend/utils/request.js"
Write-Host ""
Write-Host "  baseUrl: 'http://$($ip):8888'" -ForegroundColor White
Write-Host ""

# 后端配置
Write-Host "[2] 后端配置" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
Write-Host "文件路径: qz-sns-app/qz-sns-web/src/main/resources/application.yml"
Write-Host ""
Write-Host "  server:" -ForegroundColor White
Write-Host "    port: 8888" -ForegroundColor White
Write-Host "    address: 0.0.0.0" -ForegroundColor White
Write-Host ""

# 测试连接
Write-Host "[3] 测试连接" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
Write-Host "1. 确保SpringBoot后端已启动"
Write-Host "2. 手机连接到同一WiFi网络"
Write-Host "3. 手机浏览器访问: http://$($ip):8888"
Write-Host "4. 如果能打开页面，说明连接成功"
Write-Host ""

# 防火墙检查
Write-Host "[4] 防火墙检查" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

# 检查8888端口的防火墙规则
$firewallRule = Get-NetFirewallRule | Where-Object {
    $_.DisplayName -like "*8888*" -or $_.DisplayName -like "*SpringBoot*"
} | Select-Object -First 1

if ($null -eq $firewallRule) {
    Write-Host "⚠️  警告: 未找到8888端口的防火墙规则" -ForegroundColor Red
    Write-Host ""
    Write-Host "需要添加防火墙规则吗? (y/n): " -NoNewline -ForegroundColor Yellow
    $response = Read-Host
    
    if ($response -eq 'y' -or $response -eq 'Y') {
        try {
            # 需要管理员权限
            $command = "netsh advfirewall firewall add rule name='SpringBoot8888' dir=in action=allow protocol=TCP localport=8888"
            Start-Process powershell -ArgumentList "-Command", $command -Verb RunAs -Wait
            Write-Host "✓ 防火墙规则已添加" -ForegroundColor Green
        } catch {
            Write-Host "✗ 添加防火墙规则失败，请手动添加" -ForegroundColor Red
            Write-Host "命令: netsh advfirewall firewall add rule name='SpringBoot8888' dir=in action=allow protocol=TCP localport=8888"
        }
    }
} else {
    Write-Host "✓ 已找到防火墙规则: $($firewallRule.DisplayName)" -ForegroundColor Green
}

Write-Host ""

# 端口检查
Write-Host "[5] 端口检查" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

$portInUse = Get-NetTCPConnection -LocalPort 8888 -ErrorAction SilentlyContinue

if ($null -ne $portInUse) {
    Write-Host "✓ 8888端口正在使用中（后端可能已启动）" -ForegroundColor Green
    $process = Get-Process -Id $portInUse[0].OwningProcess -ErrorAction SilentlyContinue
    if ($null -ne $process) {
        Write-Host "  进程: $($process.ProcessName) (PID: $($process.Id))" -ForegroundColor White
    }
} else {
    Write-Host "⚠️  8888端口未使用（请启动后端）" -ForegroundColor Yellow
}

Write-Host ""

# 是否自动更新配置
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "是否自动更新前端配置文件? (y/n): " -NoNewline -ForegroundColor Yellow
$updateConfig = Read-Host

if ($updateConfig -eq 'y' -or $updateConfig -eq 'Y') {
    $requestJsPath = "frontend/utils/request.js"
    
    if (Test-Path $requestJsPath) {
        try {
            $content = Get-Content $requestJsPath -Raw -Encoding UTF8
            
            # 替换baseUrl
            $pattern = "baseUrl:\s*['""]http://[^'""]+['""]"
            $replacement = "baseUrl: 'http://$($ip):8888'"
            $newContent = $content -replace $pattern, $replacement
            
            Set-Content -Path $requestJsPath -Value $newContent -Encoding UTF8 -NoNewline
            
            Write-Host "✓ 前端配置已更新" -ForegroundColor Green
            Write-Host "  文件: $requestJsPath" -ForegroundColor Gray
            Write-Host "  新地址: http://$($ip):8888" -ForegroundColor Gray
        } catch {
            Write-Host "✗ 更新配置失败: $_" -ForegroundColor Red
        }
    } else {
        Write-Host "✗ 找不到文件: $requestJsPath" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "配置完成！按任意键退出..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

pause

