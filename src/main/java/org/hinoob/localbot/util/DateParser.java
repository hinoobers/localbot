package org.hinoob.localbot.util;

import java.time.LocalDate;

public class DateParser {

    public static LocalDate parse(String str) {
        // Must support two formats: 2023-01-15 and d MMM yyyy

        if (str.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return LocalDate.parse(str);
        } else if (str.matches("\\d+ \\w+ \\d{4}")) {
            String[] parts = str.split(" ");
            int day = Integer.parseInt(parts[0]);
            int month = parseMonth(parts[1]);
            int year = Integer.parseInt(parts[2]);
            return LocalDate.of(year, month, day);
        } else {
            throw new IllegalArgumentException("Invalid date format: " + str);
        }
    }

    private static int parseMonth(String month) {
        switch (month.toLowerCase()) {
            case "jan": return 1;
            case "feb": return 2;
            case "mar": return 3;
            case "apr": return 4;
            case "may": return 5;
            case "jun": return 6;
            case "jul": return 7;
            case "aug": return 8;
            case "sep": return 9;
            case "oct": return 10;
            case "nov": return 11;
            case "dec": return 12;
            default: throw new IllegalArgumentException("Invalid month: " + month);
        }
    }
}
