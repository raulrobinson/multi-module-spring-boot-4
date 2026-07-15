package com.raulrobinson.exception;

/** Catálogo de códigos de error del adaptador. Prefijo EAI = External API Integration. */
public enum ErrorCatalog {

    // --- Negocio (el backend respondió, pero rechazó la operación) ---
    EAI_B001("EAI-B001", "Solicitud inválida rechazada por el backend externo"),
    EAI_B002("EAI-B002", "Recurso no encontrado en el backend externo"),
    EAI_B003("EAI-B003", "Operación no autorizada por el backend externo"),
    EAI_B004("EAI-B004", "Conflicto de negocio reportado por el backend externo"),
    EAI_B005("EAI-B005", "Regla de negocio del backend externo no satisfecha"),

    // --- Técnicos (la operación no pudo completarse) ---
    EAI_T001("EAI-T001", "Error interno del backend externo (5xx)"),
    EAI_T002("EAI-T002", "Timeout de la operación contra el backend externo"),
    EAI_T003("EAI-T003", "Circuito abierto: backend externo degradado"),
    EAI_T004("EAI-T004", "Error de conexión con el backend externo"),
    EAI_T005("EAI-T005", "Respuesta del backend externo no deserializable"),
    EAI_T006("EAI-T006", "Reintentos agotados contra el backend externo"),
    EAI_T999("EAI-T999", "Error técnico no clasificado en el adaptador");

    private final String code;
    private final String message;

    ErrorCatalog(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() { return code; }
    public String message() { return message; }
}
