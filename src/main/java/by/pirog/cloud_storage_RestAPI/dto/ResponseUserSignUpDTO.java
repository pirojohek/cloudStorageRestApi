package by.pirog.cloud_storage_RestAPI.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ResponseUserSignUpDTO {
    String username;
}