package com.example.guides.constant;

import lombok.Getter;

@Getter
public enum Language {

    RU("ru"),
    ENG("eng");

    private final String lang;

    Language(String lang) {
        this.lang = lang;
    }
}
