package id.ac.tazkia.smilemahasiswa.dto.machine;

import lombok.Data;

import java.time.LocalTime;

@Data
public class ApiPresensiDosenDto {
    private Boolean sukses = true;
    private String pesanError;
    private String presensiDosen;
    private String sesiKuliah;
    private String jadwal;
    private LocalTime jamMulai;
    private LocalTime jamSelesai;
    private Integer jumlah;

}
