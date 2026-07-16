package com.raulrobinson.api.dto;

import lombok.Builder;

@Builder
public record MultiRequestDto(
	DataRequest data
) {
	@Builder
	public record DataRequest(
			// encrypt / decrypt
			String plainTextData,
			String encryptedData,
			String cryptographicKeyAlias,
			String encryptionAlgorithm
	) {
	}
}