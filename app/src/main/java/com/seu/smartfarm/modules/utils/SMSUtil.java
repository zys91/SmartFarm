package com.seu.smartfarm.modules.utils;

import java.util.regex.Pattern;

public class SMSUtil {
    /**
     * 中国移动号码正则
     * 139、138、137、136、135、134、147、150、151、152、157、158、159、178、182、183、184、187、188、198、195、172、148
     * 虚拟运营商号段: 1703、1705、1706、165
     **/
    private static final String MOBILE_PATTERN = "(^1(3[4-9]|47|5[0-27-9]|65|78|8[2-478]|98|95|72|48)\\d{8}$)|(^170[356]\\d{7}$)";

    /**
     * 中国电信号码正则
     * 133、149、153、173、177、180、181、189、199、191、193、197
     * 虚拟运营商号段: 162、1700、1701、1702
     **/
    private static final String TELECOM_PATTERN = "(^1(33|49|53|62|7[37]|8[019]|9[1379])\\d{8}$)|(^170[012]\\d{7}$)";

    /**
     * 中国联通号码正则
     * 130、131、132、155、156、185、186、145、175、176、166、140、170、196
     * 虚拟运营商号段: 171、1707、1708、1709、167
     **/
    private static final String UNICOM_PATTERN = "(^1(3[0-2]|4[05]|5[56]|6[67]|7[0156]|8[56]|96)\\d{8}$)|(^170[7-9]\\d{7}$)";


    public static boolean getOperator(String phone) {

        if (Pattern.matches(MOBILE_PATTERN, phone)) {
            return true;
        } else if (Pattern.matches(TELECOM_PATTERN, phone)) {
            return true;
        } else if (Pattern.matches(UNICOM_PATTERN, phone)) {
            return true;
        } else {
            return false;
        }
    }
}
