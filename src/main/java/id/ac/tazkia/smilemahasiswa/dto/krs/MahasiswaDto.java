package id.ac.tazkia.smilemahasiswa.dto.krs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MahasiswaDto {
    private String id;
    private String mahasiswa;
    private String nim;
    private String nama;
    private String prodi;
}
