package com.socialnetwork.socialnetwork.dto;

import java.util.UUID;

public class ProjectPaymentRequestDto {

    private String cardholderName;
    private String cardNumber;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    private String returnTo;

    /** Payer avec le solde portefeuille projet (pas de carte). */
    private Boolean useWalletBalance;

    /** Carte enregistrée (profil) : exiger aussi {@link #cvv}. */
    private UUID savedPaymentMethodId;

    /** Après paiement par nouvelle carte : enregistrer comme moyen de paiement. */
    private Boolean savePaymentMethod;

    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
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

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getReturnTo() {
        return returnTo;
    }

    public void setReturnTo(String returnTo) {
        this.returnTo = returnTo;
    }

    public Boolean getUseWalletBalance() {
        return useWalletBalance;
    }

    public void setUseWalletBalance(Boolean useWalletBalance) {
        this.useWalletBalance = useWalletBalance;
    }

    public UUID getSavedPaymentMethodId() {
        return savedPaymentMethodId;
    }

    public void setSavedPaymentMethodId(UUID savedPaymentMethodId) {
        this.savedPaymentMethodId = savedPaymentMethodId;
    }

    public Boolean getSavePaymentMethod() {
        return savePaymentMethod;
    }

    public void setSavePaymentMethod(Boolean savePaymentMethod) {
        this.savePaymentMethod = savePaymentMethod;
    }
}
