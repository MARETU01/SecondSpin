package com.secondspin.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlFormUtils {

    /**
     * 从 HTML 表单字符串中提取 action 属性值
     */
    public static String extractActionUrl(String htmlForm) {
        Pattern pattern = Pattern.compile("action\\s*=\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(htmlForm);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("无法从 HTML 表单中提取 action URL");
    }
}