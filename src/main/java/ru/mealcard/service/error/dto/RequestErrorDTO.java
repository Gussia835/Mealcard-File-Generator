package ru.mealcard.service.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mealcard.utils.error.ErrorType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestErrorDTO {
    private int lineCount;
    private String bankCode;
    private String branchCode;
    private String nameSystem;

    private ErrorType errorType;
}