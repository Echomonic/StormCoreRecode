package net.nethersmp.storm.punishment.api;

public enum PunishmentType {

    PERMANENT_BAN,
    PERMANENT_MUTE,
    TEMPORARY_BAN,
    TEMPORARY_MUTE,

    ;

    public boolean isPermanent() {

        return switch (this) {
            case PERMANENT_BAN, PERMANENT_MUTE -> true;
            case TEMPORARY_BAN, TEMPORARY_MUTE -> false;
        };
    }

    public boolean isMute() {
        return switch (this) {
            case TEMPORARY_MUTE, PERMANENT_MUTE -> true;
            case TEMPORARY_BAN, PERMANENT_BAN -> false;
        };
    }

    public boolean isBan() {
        return switch (this) {
            case TEMPORARY_BAN, PERMANENT_BAN -> true;
            case TEMPORARY_MUTE, PERMANENT_MUTE -> false;
        };
    }
}
