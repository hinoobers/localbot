package org.hinoob.localbot.util;

public class MessageUtil {

    public static String beautify(int likes) {
        String likesStr = String.valueOf(likes);
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (int i = likesStr.length() - 1; i >= 0; i--) {
            sb.append(likesStr.charAt(i));
            count++;

            if (count % 3 == 0 && i != 0) {
                sb.append(',');
            }
        }

        return sb.reverse().toString();
    }

    public static String beautifyCase(int likes) {
        // 2,320,000 -> 2.32M
        if (likes < 1000) {
            return String.valueOf(likes);
        } else if (likes < 1_000_000) {
            return String.format("%.1fK", likes / 1000.0);
        } else {
            return String.format("%.2fM", likes / 1_000_000.0);
        }
    }
}
