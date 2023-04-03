package id.ac.tazkia.smilemahasiswa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseResponse {
    private String id;
    private String nim;
    private String nama;
    private String message;
}
