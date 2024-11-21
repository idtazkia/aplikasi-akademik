package id.ac.tazkia.smilemahasiswa.dto.jadwaldosen;

import lombok.Data;

import java.util.List;

@Data
public class SKMengajar {
    private String dosen;
    private String email;
    private String nomor;
    private String tahun;
    private List<DetailDosen> jadwal;

}
