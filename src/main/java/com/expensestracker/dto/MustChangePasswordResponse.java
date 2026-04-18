package com.expensestracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MustChangePasswordResponse {
    private String code;
    private String message;
    private String tempToken;
    private String email;
}
