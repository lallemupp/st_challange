/*
 *    Copyright 2019 Love Löfdahl
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.fridaymastermix;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseBody> handleMissingBodyError(HttpMessageNotReadableException error) {
        var status = HttpStatus.BAD_REQUEST;
        var errorMessage = "Missing JSON body in request";
        return new ResponseEntity<>(new ResponseBody(status.value(), errorMessage), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseBody> handleValidationErrors(MethodArgumentNotValidException error) {
        var status = HttpStatus.BAD_REQUEST;
        var errorMessage = errorMessage(error);

        return new ResponseEntity<>(new ResponseBody(status.value(), errorMessage), status);
    }

    private String errorMessage(MethodArgumentNotValidException error) {
        var fieldErrors = error.getBindingResult().getFieldErrors();
        var fields = fieldErrors.stream().map(FieldError::getField).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < fields.size(); i++) {
            sb.append(fields.get(i));

            if (i < fields.size() - 1) {
                sb.append(", ");
            }
        }

        if (fields.size() == 1) {
            sb.append(" is ");
        } else {
            sb.append(" are ");
        }
        sb.append("required");
        return sb.toString();
    }
}
