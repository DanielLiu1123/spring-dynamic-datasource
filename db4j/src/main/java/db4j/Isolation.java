package db4j;

public enum Isolation {
    UNSPECIFIED,
    READ_UNCOMMITTED,
    READ_COMMITTED,
    REPEATABLE_READ,
    SERIALIZABLE
}
