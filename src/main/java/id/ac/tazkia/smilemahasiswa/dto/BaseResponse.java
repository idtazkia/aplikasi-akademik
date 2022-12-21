package id.ac.tazkia.smilemahasiswa.dto;

import lombok.Data;

@Data
public class BaseResponse {
    private String id;
    private String nim;
    private String nama;
    private String message;
}
