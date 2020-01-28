package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
public class MatkulDto {
    private String idMat;
    private String id;
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
    private Konsentrasi konsentrasi;
    private Integer sksMinimal;
    private BigDecimal ipkMinimal;
    private MultipartFile silabus;
    private Set<Program> programs = new HashSet<>();
    private String namaFile;
    private Akses akses;
    private StatusRecord statusSkripsi;
    private Integer sds;
    private StatusRecord konsepNote;
    private StatusRecord sempro;
}
