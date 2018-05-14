package com.a9klab.nanopi3serialport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 字符串操作工具包
 *
 * @author HEX
 */
public class StringUtils {


    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input)) {
            return true;
        }
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        }
        return isEmpty(o.toString());
    }

    public static boolean isNotEmpty(String input) {
        if (input == null || "".equals(input)) {
            return false;
        }
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotEmpty(Object o) {
        if (o == null) {
            return false;
        }
        return isNotEmpty(o.toString());
    }


    /**
     * 字符串转整数
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        if (isEmpty(str)) {
            return 0;
        }
        boolean isFlag = true;
        for (int i = 0; i < str.length(); i++) {
            if (data10.contains(str.substring(i, i + 1))) {
                continue;
            } else {
                isFlag = false;
                break;
            }
        }
        if (!isFlag) {
            return -1;
        }
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return defValue;
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static int toInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        return toInt(obj.toString(), 0);
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 字符串转布尔值
     *
     * @param b
     * @return 转换异常返回 false
     */
    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
        }
        return false;
    }

    public static String getString(String s) {
        return s == null ? "" : s;
    }


    /**
     * 截取字符串
     *
     * @param start 从那里开始，0算起
     * @param num   截取多少个
     * @param str   截取的字符串
     * @return
     */
    public static String getSubString(int start, int num, String str) {
        if (str == null) {
            return "";
        }
        int leng = str.length();
        if (start < 0) {
            start = 0;
        }
        if (start > leng) {
            start = leng;
        }
        if (num < 0) {
            num = 1;
        }
        int end = start + num;
        if (end > leng) {
            end = leng;
        }
        return str.substring(start, end);
    }

    /**
     * 默认去掉.0
     *
     * @param f
     * @param value_step
     * @return
     */
//    public static String floatToStr(float f, float value_step) {
//        String str = String.valueOf(f);
//        if (str.endsWith(".0")) {
//            str = str.replace(".0", "");
//        } else {
//            String s = String.valueOf(value_step);
//            int pos = s.lastIndexOf('.');
//            if (pos > 0) {
//                int length = s.length() - pos;
//                int p = str.lastIndexOf('.');
//                if (str.length() > length + p) {
//                    str = str.substring(0, length + p);
//                }
//            }
//        }
//        return str;
//    }

    /**
     * @param f
     * @return
     */
    public static String floatToStr(float f) {
//        String str = String.valueOf(f);
//        int pos = str.lastIndexOf('.');
//        if (pos > 0 && str.length() >= pos + 2) {
//            str = str.substring(0, pos + 2);
//        }
        return toString(f, 1);
    }

    private static String data10 = "0123456789";

    private static String data16 = "0123456789ABCDEF";

    /**
     * 0x12变换为“12”
     *
     * @param bt
     * @return
     */
    public static String print(byte bt) {
        return data16.charAt(bt >> 4 & 0x0f) + "" + data16.charAt(bt & 0x0f);
    }

    /**
     * “12”变换为0x12
     *
     * @param ss
     * @return
     */
    public static byte strToByte(String ss) {
        if (ss == null) {
            return 0x0;
        }
        if (ss.indexOf('.') >= 0) {
            ss = ss.substring(0, ss.indexOf('.'));
            //TLog.error(ss);
        }
        if (ss.length() == 0) {
            return 0x0;
        } else if (ss.length() == 1) {
            return (byte) (data16.indexOf(ss.substring(0, 1)));
        }
        return (byte) (data16.indexOf(ss.substring(0, 1)) << 4 | data16.indexOf(ss.substring(1, 2)));
    }

    public static String toString(float f, int n) {

        if (n == 0) {
            return String.format("%1.0f", f);
        } else if (n == 1) {
            return String.format("%1.1f", f);
        } else if (n == 2) {
            return String.format("%1.2f", f);
        } else if (n == 3) {
            return String.format("%1.3f", f);
        } else {
            return String.valueOf(n);
        }
    }

    public static String longToTimeString6(long l) {
        return String.format("%02d:%02d:%02d", l / 60 / 60, l / 60 % 60, l % 60 % 60);
    }

    public static String longToTimeString4(long l) {
        return String.format("%02d:%02d", l / 60, l % 60);
    }

    public static String longToTimeString2(long l) {
        return String.format("%02d", l);
    }
}
