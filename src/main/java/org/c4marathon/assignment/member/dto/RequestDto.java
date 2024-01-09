package org.c4marathon.assignment.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RequestDto {

    public record JoinDto(

        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "이메일 입력값이 형식에 맞지 않습니다.")
        String email,
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,16}$", message = "비밀번호 입력값이 형식에 맞지 않습니다.")
        String password,
        @NotBlank
        String name
    ) {}

    public record LoginDto(
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "이메일 입력값이 형식에 맞지 않습니다.")
        String email,
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,16}$", message = "비밀번호 입력값이 형식에 맞지 않습니다.")
        String password
    ) {}
}
