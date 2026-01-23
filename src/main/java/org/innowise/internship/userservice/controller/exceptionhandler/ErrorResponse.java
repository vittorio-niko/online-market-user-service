package org.innowise.internship.userservice.controller.exceptionhandler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private ErrorCode code;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> fieldErrors;
}
