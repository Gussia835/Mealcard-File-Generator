package ru.mealcard.service.generate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mealcard.service.format.dto.EnrollDTO;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateRequestDTO {
    private String bankCode;
    private String branchCode;
    private String nameSystem;
    private String procType;
    private String processAt;
    private List<EnrollDTO> cards;
}