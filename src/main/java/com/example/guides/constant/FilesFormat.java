package com.example.guides.constant;

import lombok.Getter;

@Getter
public enum FilesFormat {

    VIDEO("mp4"),
    IMAGE("jpeg");


    private final String format;

    FilesFormat(String format) {
        this.format = format;
    }
}
