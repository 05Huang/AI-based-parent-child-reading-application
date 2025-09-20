package com.qz.sns.common.utils;

import java.util.Random;

public class VerificationCodeGenerator {

    /**
     * 生成一个四位的数字验证码。
     *
     * @return 四位数字验证码字符串
     */
    public static String generateNumericVerificationCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000); // 生成一个1000到9999之间的随机数
        return String.valueOf(code);
    }

    /**
     * 生成一个四位的包含数字和字母的验证码。
     *
     * @return 四位包含数字和字母的验证码字符串
     */
    public static String generateAlphanumericVerificationCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // 可选字符集
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 4; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }


}