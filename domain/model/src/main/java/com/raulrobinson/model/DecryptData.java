package com.raulrobinson.model;

public record DecryptData(Data data) {

    public record Data(
            String encryptionAlgorithm,
            String cryptographicKeyAlias,
            String encryptedData
    ) {}
}
