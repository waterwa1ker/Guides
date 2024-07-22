package com.example.guides.dto;

import com.example.guides.model.Referral;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Объект пользователя")
public class PersonDTO {

    @Schema(description = "Идентификатор пользователя")
    private long id;

    @Schema(description = "Имя пользователя")
    private String firstName;

    @Schema(description = "Фамилия пользователя")
    private String lastName;

    @Schema(description = "Раздел \"О себе\"")
    private String description;

    @Schema(description = "Никнейм пользователя")
    private String username;

    @Schema(description = "Рефералки пользователя")
    private List<Referral> referrals;

}
