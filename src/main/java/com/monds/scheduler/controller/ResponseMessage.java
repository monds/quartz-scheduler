package com.monds.scheduler.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMessage {

    private String status;
    private String message;

    public static ResponseMessage buildErrorMessage(Exception e) {
        return new ResponseMessage("failed", e.getMessage());
    }
}
