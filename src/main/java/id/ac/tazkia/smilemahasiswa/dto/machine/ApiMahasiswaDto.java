package id.ac.tazkia.smilemahasiswa.dto.machine;

import lombok.Data;

@Data
public class ApiMahasiswaDto {
    private Boolean sukses = true;
    private String pesanError;
    private String krsDetail;
    private String jadwal;
    private String mahasiswa;
    private String rfid;
    private String nim;
    private Integer absen;
    private Integer jumlah;
}
