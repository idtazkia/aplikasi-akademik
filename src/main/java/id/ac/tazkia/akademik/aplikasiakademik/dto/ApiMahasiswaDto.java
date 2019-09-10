package id.ac.tazkia.akademik.aplikasiakademik.dto;

import lombok.Data;

@Data
public class ApiMahasiswaDto {
    private String krsDetail;
    private String jadwal;
    private String mahasiswa;
    private String rfid;
    private String nim;
    private Integer absen;
    private Integer jumlah;
}
