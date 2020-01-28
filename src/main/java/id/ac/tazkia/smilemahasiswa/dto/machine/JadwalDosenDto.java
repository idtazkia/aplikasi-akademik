package id.ac.tazkia.smilemahasiswa.dto.machine;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JadwalDosenDto {
    private String idDosen;
    private Integer absen;
    private String nama;
    private String rfid;
    private String jadwal;
    private LocalTime jamMulai;
    private LocalTime jamSelesai;
    private Integer jumlah;
}
