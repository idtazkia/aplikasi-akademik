package id.ac.tazkia.akademik.aplikasiakademik.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KelasMahasiswaDto {
    private String id;
    private String nama;
    private String nim;
    private String kelas;
    private String kurikulum;
}
