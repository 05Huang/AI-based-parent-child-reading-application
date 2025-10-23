@echo off
chcp 65001 >nul
echo ========================================
echo 快速测试 - 生成少量测试数据
echo ========================================
echo.
echo 将自动生成：
echo   - 5篇图文文章（每篇2张图片）
echo   - 2个视频内容
echo.
echo 预计耗时：5-10分钟
echo ========================================
echo.

python quick_test.py

pause

