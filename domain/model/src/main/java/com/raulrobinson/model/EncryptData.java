package com.raulrobinson.model;

public record EncryptData(Data data) {

    public record Data(
            String encryptionAlgorithm,
            String cryptographicKeyAlias,
            String plainTextData
    ) {}
}
