package com.elias.gestobar.model.dto;

import com.elias.gestobar.model.enums.Role;

public record UserSessionDto(
        Long    id,
        String  nombre,
        String  apellido,
        Role role,
        boolean active
) {}
