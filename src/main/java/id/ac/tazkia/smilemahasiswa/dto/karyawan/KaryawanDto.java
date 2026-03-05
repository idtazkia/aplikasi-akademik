package id.ac.tazkia.smilemahasiswa.dto.karyawan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KaryawanDto {
    private String id;
    private String namaKaryawan;
    private String nik;
}
