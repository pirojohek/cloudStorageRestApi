package by.pirog.cloud_storage_RestAPI.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserSignInDTO(

        @NotNull
        @Size(min = 6)
        String username,

        @NotNull
        @Size(min = 6)
        String password
){
}
