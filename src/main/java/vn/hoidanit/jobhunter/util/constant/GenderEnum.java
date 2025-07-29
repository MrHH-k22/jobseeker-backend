package vn.hoidanit.jobhunter.util.constant;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum GenderEnum {
    MALE,
    FEMALE,
    OTHER;

    @JsonCreator
    public static GenderEnum fromString(String value) {
        return GenderEnum.valueOf(value.toUpperCase());
    }
}
