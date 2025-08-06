package swyp.hobbi.swyphobbiback.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest {
    private String email;
    private String code;
    private String newPassword;
}