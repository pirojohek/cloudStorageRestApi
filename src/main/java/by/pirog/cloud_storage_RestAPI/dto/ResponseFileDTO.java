package by.pirog.cloud_storage_RestAPI.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseFileDTO {
    private String path;

    private String name;

    private Long size;

    private String type;
}
