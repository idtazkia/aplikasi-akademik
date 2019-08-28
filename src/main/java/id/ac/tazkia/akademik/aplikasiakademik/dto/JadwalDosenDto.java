package id.ac.tazkia.akademik.aplikasiakademik.dto;


import id.ac.tazkia.akademik.aplikasiakademik.entity.Dosen;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Set;

@Data @NoArgsConstructor @AllArgsConstructor
public class JadwalDosenDto {
    private Integer absen;
    private String nama;
    private String rfid;
    private String jadwal;
    private LocalTime jamMulai;
    private LocalTime jamSelesai;
}
