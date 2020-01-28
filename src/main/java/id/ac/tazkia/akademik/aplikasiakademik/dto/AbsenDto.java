package id.ac.tazkia.akademik.aplikasiakademik.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenDto {
    private String nim;
    private String nama;
    private String id;
    private Integer totalHadir;
    private Integer totalHadirDosen;

    public void tambahAbsen(Integer absen) {
        totalHadir = totalHadir + absen;
    }
}
