package com.raulrobinson.usecase.mapper;

import com.raulrobinson.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface MultiUseCaseMapper {

    @Mapping(target = "data", source = ".")
    DecryptData toDecryptModel(MultiModel model);

    @Mapping(target = "data", source = ".")
    EncryptData toEncryptModel(MultiModel model);

    /**
     * Maps the MultiModel to DecryptData.Data object.
     * @param model The MultiModel object to be mapped.
     * @return A DecryptData.Data object containing the mapped data, or null if the input model or its data is null.
     */
    default DecryptData.Data mapDecryptData(MultiModel model) {
        if (model == null || model.data() == null) return null;
        return new DecryptData.Data(
                model.data().encryptionAlgorithm(),
                model.data().cryptographicKeyAlias(),
                model.data().encryptedData()
        );
    }

    /**
     * Maps the MultiModel to EncryptData.Data object.
     * @param model The MultiModel object to be mapped.
     * @return A EncryptData.Data object containing the mapped data, or null if the input model or its data is null.
     */
    default EncryptData.Data mapEncryptData(MultiModel model) {
        if (model == null || model.data() == null) return null;
        return new EncryptData.Data(
                model.data().encryptionAlgorithm(),
                model.data().cryptographicKeyAlias(),
                model.data().plainTextData()
        );
    }

    /**
     * Converts a Boolean value to "Y" or "N" string representation.
     * @param value The Boolean value to be converted.
     * @return "Y" if the value is true, "N" if the value is false, or null if the value is null.
     */
    @Named("booleanToYN")
    default String booleanToYN(Boolean value) {
        if (value == null) return null;
        return value ? "Y" : "N";
    }
}
