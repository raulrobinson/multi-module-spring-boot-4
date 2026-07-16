package com.raulrobinson.driven.corename.logging;

/** Niveles de enmascarado aplicables a un campo sensible, configurables por YAML. */
public enum MaskingLevel {

    /** Reemplaza el valor completo: {@code ****} */
    FULL {
        @Override
        public String mask(String value) {
            return "****";
        }
    },

    /** Muestra solo los últimos 4 caracteres: {@code ************4231} */
    SHOW_LAST_4 {
        @Override
        public String mask(String value) {
            if (value == null || value.length() <= 4) return "****";
            return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
        }
    },

    /** Muestra primeros 2 y últimos 2: {@code ra********es} — útil para correos/usuarios. */
    SHOW_EDGES {
        @Override
        public String mask(String value) {
            if (value == null || value.length() <= 4) return "****";
            return value.substring(0, 2) + "*".repeat(value.length() - 4) + value.substring(value.length() - 2);
        }
    },

    /** Hash corto determinístico ({@code sha256:a1b2c3d4}): correlacionable sin exponer. */
    HASH {
        @Override
        public String mask(String value) {
            if (value == null) return "****";
            try {
                var digest = java.security.MessageDigest.getInstance("SHA-256")
                        .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                var hex = new StringBuilder();
                for (int i = 0; i < 4; i++) hex.append(String.format("%02x", digest[i]));
                return "sha256:" + hex;
            } catch (java.security.NoSuchAlgorithmException e) {
                return "****";
            }
        }
    },

    /** Elimina el valor del log por completo. */
    OMIT {
        @Override
        public String mask(String value) {
            return "<omitted>";
        }
    };

    public abstract String mask(String value);
}
