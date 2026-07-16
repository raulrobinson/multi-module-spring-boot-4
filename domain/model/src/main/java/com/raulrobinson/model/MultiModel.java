package com.raulrobinson.model;

public record MultiModel(
	DataRequest data
) {
	public record DataRequest(
		// encrypt / decrypt
		String plainTextData,
		String encryptedData,
		String cryptographicKeyAlias,
		String encryptionAlgorithm
	) {
	}
}