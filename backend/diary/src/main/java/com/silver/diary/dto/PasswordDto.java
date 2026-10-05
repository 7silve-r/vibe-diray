package com.silver.diary.dto;

import lombok.Data;

@Data
public class PasswordDto {
    private String oldPwd;
    private String newPwd;
    private String reNewPwd;
}
