package id.ac.tazkia.smilemahasiswa.dto.api.akunting;

import lombok.Data;

@Data
public class AkuntingMahasiswaDto {
    private String id;
    private String nim;
    private String nama;
    private String prodi;
    private String program;
    private String jenjang;
    private Integer semester;
    private Integer semesterSekarang;

}
