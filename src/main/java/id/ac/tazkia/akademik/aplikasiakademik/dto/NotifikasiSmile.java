package id.ac.tazkia.akademik.aplikasiakademik.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotifikasiSmile {
    private String konfigurasi;
    private String email;
    private String mobile;
    private Object data;
}
