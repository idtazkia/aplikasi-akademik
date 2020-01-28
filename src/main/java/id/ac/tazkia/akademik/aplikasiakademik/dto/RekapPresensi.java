package id.ac.tazkia.akademik.aplikasiakademik.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RekapPresensi {
    private String id;
    private String nim;
    private String nama;
    private String matakuliah;
    private Integer sks;
    private String kelas;
    private String dosen;
    private Integer jumlahHadir;
    private String idJadwal;

}
