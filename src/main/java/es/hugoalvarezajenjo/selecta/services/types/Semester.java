package es.hugoalvarezajenjo.selecta.services.types;

public enum Semester {
    FIRST(1),
    SECOND(2),
    THIRD(3),
    FOURTH(4),
    FIFTH(5),
    SIXTH(6),
    SEVENTH(7),
    EIGHTH(8);

    private final int semesterNumber;

    Semester(final int semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    @Override
    public String toString() {
        return Integer.toString(this.semesterNumber);
    }
}
