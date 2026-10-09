package ru.mealcard.service.responseDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResponseDTO {
    private String status;
    private String filename;
    private List<String> filenames;
}
