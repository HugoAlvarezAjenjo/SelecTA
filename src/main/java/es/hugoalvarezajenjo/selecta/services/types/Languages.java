package es.hugoalvarezajenjo.selecta.services.types;

public enum Languages {
    SPANISH("Español"),
    ENGLISH("Inglés");

    private final String value;

    Languages(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
