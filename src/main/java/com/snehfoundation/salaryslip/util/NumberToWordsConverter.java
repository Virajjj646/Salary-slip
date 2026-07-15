package com.snehfoundation.salaryslip.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Converts a rupee amount to words using the Indian numbering system
 * (Thousand / Lakh / Crore), e.g. 19000 -> "Nineteen Thousand Rupees Only",
 * matching the "In Words" line on the SNEH Foundation salary slip.
 * <p>
 * Salaries here are whole rupees, so the amount is rounded to the nearest
 * rupee before conversion -- there's no paise handling.
 */
@Component
public class NumberToWordsConverter {

    private static final String[] ONES = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public String toWords(BigDecimal amount) {
        if (amount == null) {
            return "Zero Rupees Only";
        }
        long value = amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
        if (value < 0) {
            return "Minus " + convert(Math.abs(value)) + " Rupees Only";
        }
        if (value == 0) {
            return "Zero Rupees Only";
        }
        return convert(value) + " Rupees Only";
    }

    private String convert(long number) {
        StringBuilder result = new StringBuilder();

        long crore = number / 10_000_000;
        number %= 10_000_000;
        long lakh = number / 100_000;
        number %= 100_000;
        long thousand = number / 1_000;
        number %= 1_000;
        long hundred = number / 100;
        long remainder = number % 100;

        if (crore > 0) {
            result.append(convertBelowThousand(crore)).append(" Crore ");
        }
        if (lakh > 0) {
            result.append(convertBelowThousand(lakh)).append(" Lakh ");
        }
        if (thousand > 0) {
            result.append(convertBelowThousand(thousand)).append(" Thousand ");
        }
        if (hundred > 0) {
            result.append(ONES[(int) hundred]).append(" Hundred ");
        }
        if (remainder > 0) {
            if (!result.isEmpty()) {
                result.append("and ");
            }
            result.append(convertBelowHundred(remainder));
        }

        return result.toString().trim();
    }

    /** Handles 0-999, used for the crore/lakh/thousand group multipliers as well as leftovers. */
    private String convertBelowThousand(long number) {
        if (number < 100) {
            return convertBelowHundred(number);
        }
        long hundred = number / 100;
        long remainder = number % 100;
        String result = ONES[(int) hundred] + " Hundred";
        if (remainder > 0) {
            result += " and " + convertBelowHundred(remainder);
        }
        return result;
    }

    /** Handles 0-99. */
    private String convertBelowHundred(long number) {
        if (number < 20) {
            return ONES[(int) number];
        }
        long tens = number / 10;
        long remainder = number % 10;
        String result = TENS[(int) tens];
        if (remainder > 0) {
            result += "-" + ONES[(int) remainder];
        }
        return result;
    }
}