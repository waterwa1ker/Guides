package com.example.guides.constant;

import lombok.Getter;

@Getter
public enum Language {

    RUSSIAN("ru"),
    ENGLISH("eng");

    private String language;

    Language(String language) {
        this.language = language;
    }
}
