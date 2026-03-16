const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const tabbarDir = path.join(__dirname, '../static/images/tabbar');

// 获取所有SVG文件
const svgFiles = fs.readdirSync(tabbarDir).filter(file => file.endsWith('.svg'));

// 转换每个SVG文件为PNG
svgFiles.forEach(async (svgFile) => {
  const svgPath = path.join(tabbarDir, svgFile);
  const pngPath = path.join(tabbarDir, svgFile.replace('.svg', '.png'));
  
  try {
    await sharp(svgPath)
      .resize(81, 81)
      .png()
      .toFile(pngPath);
    
    console.log(`Converted ${svgFile} to PNG`);
  } catch (error) {
    console.error(`Error converting ${svgFile}:`, error);
  }
}); 