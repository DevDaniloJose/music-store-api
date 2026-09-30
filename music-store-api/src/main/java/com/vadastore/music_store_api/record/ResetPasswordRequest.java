package com.vadastore.music_store_api.record;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @Size(min = 8)
    @Valid
    private String newPassword;
    private String token;

}
