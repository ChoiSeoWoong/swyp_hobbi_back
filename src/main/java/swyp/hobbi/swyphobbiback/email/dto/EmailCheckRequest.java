package swyp.hobbi.swyphobbiback.email.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailCheckRequest {
    private String email;
    private String code;
}
