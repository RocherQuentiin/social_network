package com.socialnetwork.socialnetwork.dto;

import java.util.UUID;

public class UserPaymentMethodResponseDto {

    private UUID id;
    private String cardholderName;
    private String last4;
    private String expiryMonth;
    private String expiryYear;
    private String label;
    private String displayLabel;

    public UserPaymentMethodResponseDto() {
    }

    public UserPaymentMethodResponseDto(UUID id, String cardholderName, String last4, String expiryMonth,
                                        String expiryYear, String label, String displayLabel) {
        this.id = id;
        this.cardholderName = cardholderName;
        this.last4 = last4;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.label = label;
        this.displayLabel = displayLabel;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public String getLast4() {
        return last4;
    }

    public void setLast4(String last4) {
        this.last4 = last4;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public void setDisplayLabel(String displayLabel) {
        this.displayLabel = displayLabel;
    }
}
