package com.socialnetwork.socialnetwork.business.utils;

import java.time.LocalDate;

/**
 * Règles de carte de test (démo). Ne pas utiliser pour un vrai PSP.
 */
public final class PaymentTestCardUtils {

    public static final String TEST_CARD_NUMBER = "4242424242424242";
    public static final String TEST_CVV = "123";
    public static final String TEST_LAST4 = "4242";

    private PaymentTestCardUtils() {
    }

    public static String normalizeCardNumber(String cardNumber) {
        return cardNumber == null ? "" : cardNumber.trim().replaceAll("\\s+", "");
    }

    public static String lastFourDigits(String cardNumber) {
        String n = normalizeCardNumber(cardNumber);
        if (n.length() < 4) {
            return n;
        }
        return n.substring(n.length() - 4);
    }

    public static boolean validateFullCard(String cardholderName,
                                             String cardNumber,
                                             String expiryMonth,
                                             String expiryYear,
                                             String cvv) {
        String holder = safe(cardholderName);
        String number = normalizeCardNumber(cardNumber);
        String month = safe(expiryMonth);
        String year = safe(expiryYear);
        String cv = safe(cvv);

        if (holder.isBlank() || month.isBlank() || year.isBlank()) {
            return false;
        }
        if (!TEST_CARD_NUMBER.equals(number) || !TEST_CVV.equals(cv)) {
            return false;
        }
        return validateExpiry(month, year);
    }

    /** Carte enregistrée : on ne stocke que le last4 ; CVV saisi au moment du paiement. */
    public static boolean validateSavedCard(String last4, String expiryMonth, String expiryYear, String cvv) {
        String cv = safe(cvv);
        if (!TEST_CVV.equals(cv)) {
            return false;
        }
        if (last4 == null || !TEST_LAST4.equals(last4.trim())) {
            return false;
        }
        return validateExpiry(safe(expiryMonth), safe(expiryYear));
    }

    private static boolean validateExpiry(String expiryMonth, String expiryYear) {
        try {
            int month = Integer.parseInt(expiryMonth);
            int year = Integer.parseInt(expiryYear);
            if (month < 1 || month > 12) {
                return false;
            }
            LocalDate expiryDate = LocalDate.of(year, month, 1).withDayOfMonth(1).plusMonths(1).minusDays(1);
            return !expiryDate.isBefore(LocalDate.now());
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
