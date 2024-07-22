package com.example.guides.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Объект гайда")
public class GuideDTO {

    @Schema(name = "Ссылка на изображение гайда")
    private String mainImg;

    @Schema(name = "Описание гайда")
    private String description;

    @Schema(name = "Главы гайда")
    private List<ChapterDTO> chapters;

}
