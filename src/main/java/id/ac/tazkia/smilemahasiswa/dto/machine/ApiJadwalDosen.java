package id.ac.tazkia.smilemahasiswa.dto.machine;

import lombok.Data;

import java.time.LocalTime;

@Data
public class ApiJadwalDosen {
    private String jadwal;
    private String dosen;
    private String namaDosen;
    private String namaMatakuliah;
    private String namaMatakuliahEng;
    private LocalTime jamMulai;
    private LocalTime jamSelesai;
    private Integer jumlah;
    private Boolean sukses = true;
    private String pesanError;
}
