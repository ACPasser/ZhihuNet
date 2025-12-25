package org.acpasser.zhihunet.crawler.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum TargetType {
    QUESTION("question"),
    ANSWER("answer"),
    ARTICLE("article"),
    PIN("pin"),
    COLLECTION("collection");

    private final String type;

    @JsonValue
    public String getType() {
        return this.type;
    }

    @JsonCreator
    public static TargetType fromType(String type) {
        return Arrays.stream(values())
                .filter(tt -> tt.type.equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知类型: " + type));
    }
}