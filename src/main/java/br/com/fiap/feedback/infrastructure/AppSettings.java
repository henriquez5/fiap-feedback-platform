package br.com.fiap.feedback.infrastructure;

public final class AppSettings {
    private AppSettings() {
    }

    public static String get(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    public static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("A configuracao obrigatoria '" + name + "' nao foi definida.");
        }
        return value.trim();
    }
}
