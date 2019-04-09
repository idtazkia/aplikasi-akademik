package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Kurikulum;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import lombok.Data;

@Data
public class MatkulDto {
    private String idMat;
    private String namaMatakuliah;
    private String kodeMatakuliah;
    private String namaMatakuliahEnglish;
    private String singkatan;
    private String responsi;
    private Integer nourut;
    private String wajib;
    private Kurikulum kurikulum;
    private String matakuliahKurikulumSemester;
    private Integer semester;
    private String syaratTugas;
    private Integer sks;
    private Prodi prodi;
}
