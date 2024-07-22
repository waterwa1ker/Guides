package com.example.guides.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Объект рефералки")
public class ReferralDTO {

    @Schema(description = "Обладатель реферальной ссылки")
    private PersonDTO referralOwner;

    @Schema(description = "Приглашенное лицо")
    private PersonDTO referral;
}
