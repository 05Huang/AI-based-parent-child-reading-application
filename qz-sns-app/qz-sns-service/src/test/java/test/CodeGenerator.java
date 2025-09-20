package test;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;

public class CodeGenerator {

    public static void main(String[] args) {

        FastAutoGenerator.create("jdbc:mysql://localhost:3306/qz_sns", "root", "123456")
                .globalConfig(builder -> {
                    builder.author("李彬") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .outputDir("D://mybatis-plus");// 指定输出目
                })
                .packageConfig(builder -> {
                    builder.parent("com.qz.sns") // 设置父包名
                            .moduleName("qz-sns-service") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, "D://mybatis-plus")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("paragraph_comment_count") // 设置需要生成的表名
                            .addTablePrefix(); // 设置过滤表前缀
                })
                .templateEngine(new VelocityTemplateEngine()) // 使用Velocity引擎模板
                .execute();
    }
}
