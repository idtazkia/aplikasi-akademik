package id.ac.tazkia.smilemahasiswa.dto.ujian;

import lombok.Data;

@Data
public class TahfidzRequest {
    private String id;
    private String ruangan;
    private String tanggal;
    private String penguji;
}
