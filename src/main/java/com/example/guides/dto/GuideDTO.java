package com.example.guides.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Объект гайда")
public class GuideDTO {

    @Schema(name = "Идентификатор гайда")
    private long id;

    @Schema(name = "Ссылка на изображение гайда")
    private String mainImg;

    @Schema(name = "Описание гайда")
    private String description;

    @Schema(name = "Главы гайда")
    private List<ChapterDTO> chapters;

    @Schema(name = "Цена гайда")
    private int price;

    @Schema(name = "Количество покупок гайда")
    private int count;

    @Schema(name = "Количество заработанной валюты за этот гайд")
    private int earnings;

    @Schema(name = "Время создания гайда")
    private LocalDateTime createdAt;

}
