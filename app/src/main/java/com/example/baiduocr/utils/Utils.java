package com.example.baiduocr.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Utils {

    // 判断一个字符串是否含有数字
    public static boolean hasDigit(String content) {
        boolean flag = false;
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        if (m.matches()) {
            flag = true;
        }
        return flag;
    }

    /**
     * 判断是否为汉字
     *
     * @param str
     * @return
     */
    public static boolean isChinese(String str) {
        Pattern p_str = Pattern.compile("[\\u4e00-\\u9fa5]+");
        Matcher m = p_str.matcher(str);
        if (m.find() && m.group(0).equals(str)) {
            return true;
        }
        return false;
    }

    /**
     * 正则表达式
     * 匹配手机号的规则：[3456789]是手机号第二位可能出现的数字
     * 目前手机号开头如下：
     * 130，131，132，133，134，135，136，137，138，139
     * 145，147
     * 150，151，152，153，155，156，157，158，159
     * 166
     * 170，171，172 ，173，174，175，176，177，178
     * 180，181，182，183，184，185，186，187，188，189
     * 198，199
     * @param mobile
     * @return 校验通过返回true，否则返回false
     */

    public static boolean isChinaPhoneLegal(String mobile)
            throws PatternSyntaxException {
        String regExp = "^((13[0-9])|(147,145)|(15[^4])|(166)|(17[0-8])|(18[0-9])|(19[8-9]))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(mobile);
        return m.matches();
    }

}
