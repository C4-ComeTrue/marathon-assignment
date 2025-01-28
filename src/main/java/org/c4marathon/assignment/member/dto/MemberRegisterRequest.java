package org.c4marathon.assignment.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberRegisterRequest(

        @Email
        String email,

        @NotBlank(message = "이름을 입력해주세요.")
        @Size(min = 2, max = 10, message = "10자 이하로 입력해주세요.")
        String name,

        @NotBlank(message = "공백은 입력할 수 없습니다.")
        @Size(min = 8, max = 15, message = "8자 이상 15자 이하로 입력해주세요.")
        String password

) {
}
