package com.qz.sns.common.utils;

import java.util.regex.Pattern;

public class RegexUtil {
    // 常见正则表达式模式
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$"; // 中国手机号
    private static final String URL_REGEX = "^(https?|ftp)://[^\s/$.?#].[^\s]*$";
    private static final String IPV4_REGEX = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                             "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                             "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                             "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    private static final String ID_CARD_REGEX = "^(\\d{15}|\\d{18}|\\d{17}[Xx])$"; // 身份证号
    private static final String CHINESE_CHAR_REGEX = "^[\u4e00-\u9fa5]+$"; // 仅中文字符
    private static final String NUMBER_REGEX = "^\\d+$"; // 仅数字
    private static final String LETTER_REGEX = "^[a-zA-Z]+$"; // 仅字母
    private static final String LETTER_NUMBER_REGEX = "^[a-zA-Z0-9]+$"; // 字母和数字

    /**
     * 检查字符串是否匹配指定的正则表达式
     * 
     * @param regex 正则表达式
     * @param input 需要检查的字符串
     * @return 是否匹配
     */
    public static boolean isMatch(String regex, String input) {
        if (input == null || regex == null) {
            return false;
        }
        return Pattern.matches(regex, input);
    }

    /**
     * 预设的正则检查方法
     */
    public static boolean isEmail(String input) {
        return isMatch(EMAIL_REGEX, input);
    }

    public static boolean isPhone(String input) {
        return isMatch(PHONE_REGEX, input);
    }

    public static boolean isUrl(String input) {
        return isMatch(URL_REGEX, input);
    }

    public static boolean isIPv4(String input) {
        return isMatch(IPV4_REGEX, input);
    }

    public static boolean isIDCard(String input) {
        return isMatch(ID_CARD_REGEX, input);
    }

    public static boolean isChinese(String input) {
        return isMatch(CHINESE_CHAR_REGEX, input);
    }

    public static boolean isNumber(String input) {
        return isMatch(NUMBER_REGEX, input);
    }

    public static boolean isLetter(String input) {
        return isMatch(LETTER_REGEX, input);
    }

    public static boolean isLetterOrNumber(String input) {
        return isMatch(LETTER_NUMBER_REGEX, input);
    }

    // 测试示例

}
