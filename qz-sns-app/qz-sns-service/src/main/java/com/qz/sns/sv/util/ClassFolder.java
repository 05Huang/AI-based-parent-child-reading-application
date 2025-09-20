package com.qz.sns.sv.util;

/**
 * @Author: 李彬
 * @Date: 2025/4/29 下午11:33
 * @Version: v1.0.0
 * @Description: TODO
 **/
public class ClassFolder {

    public static void main(String[] args) {
        // 测试类加载器
        System.out.println("ClassLoader: " + ClassFolder.class.getClassLoader());
        System.out.println("System ClassLoader: " + ClassLoader.getSystemClassLoader());
        System.out.println("Bootstrap ClassLoader: " + String.class.getClassLoader());

        // 测试 Jsoup 类是否存在
        try {
            Class.forName("org.jsoup.Jsoup");
            System.out.println("Jsoup class found!");
        } catch (ClassNotFoundException e) {
            System.out.println("Jsoup class not found: " + e.getMessage());
            // 打印类加载器层次结构
            ClassLoader cl = ClassFolder.class.getClassLoader();
            while (cl != null) {
                System.out.println("ClassLoader: " + cl);
                cl = cl.getParent();
            }
        }
    }
}
