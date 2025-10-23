@echo off
chcp 65001 >nul
echo ========================================
echo 亲子阅读APP测试数据生成工具
echo ========================================
echo.

echo [1/4] 检查Python环境...
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到Python，请先安装Python 3.7+
    pause
    exit /b 1
)
echo ✓ Python已安装
echo.

echo [2/4] 安装依赖包...
pip install -r test_data_requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
if errorlevel 1 (
    echo ⚠️  使用清华镜像安装失败，尝试默认源...
    pip install -r test_data_requirements.txt
    if errorlevel 1 (
        echo ❌ 错误: 依赖包安装失败
        pause
        exit /b 1
    )
)
echo ✓ 依赖包安装完成
echo.

echo [3/4] 检查环境配置...
python check_environment.py
if errorlevel 1 (
    echo.
    echo ❌ 环境检查未通过，请根据上述提示修复问题
    pause
    exit /b 1
)
echo.

echo [4/4] 启动测试数据生成脚本...
echo.
python generate_test_content.py

pause

