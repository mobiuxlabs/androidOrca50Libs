package com.zebra.util;

public class AppUtils extends in.mobiux.android.commonlibs.utils.AppUtils {

    private static String epcHeader = "53 4F ";
    public static String generateHexEPC(String value) {

        StringBuffer sb = new StringBuffer();
        String hexString = numberToHex(value);

        int zeroRequired = 20 - hexString.length();
        int zeroAddedCount = 0;

        while (zeroRequired > 0) {

            sb.append(0);
            zeroRequired--;
            zeroAddedCount++;

            if (zeroAddedCount % 2 == 0) {
                sb.append(" ");
            }
        }

        hexString = sb + hexString;
        hexString = epcHeader + hexString;
        return hexString.toUpperCase();
    }


    public static String numberToHex(String number) {
        if (!isNumber(number)) {
            throw new RuntimeException("value should be number only");
        }
        return Long.toHexString(Long.parseLong(number));
    }

    public static boolean isNumber(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
