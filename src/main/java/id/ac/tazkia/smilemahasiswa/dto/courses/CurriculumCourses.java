package id.ac.tazkia.smilemahasiswa.dto.courses;

import id.ac.tazkia.smilemahasiswa.entity.Akses;
import lombok.Data;

@Data
public class CurriculumCourses {
    private String id;
    private String matakuliah;
    private String namaMatakuliah;
    private String namaMatakuliahInggris;
    private String kodeMatakuliah;
    private String responsi;
    private String skripsi;
    private String konsepNote;
    private String sempro;
    private Akses akses;
    private Integer jumlahSks;
}
