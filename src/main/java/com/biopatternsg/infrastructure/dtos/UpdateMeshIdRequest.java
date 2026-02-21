package com.biopatternsg.infrastructure.dtos;

public record UpdateMeshIdRequest(
        String biologicalObjectId,
        String meshId
) {
}
