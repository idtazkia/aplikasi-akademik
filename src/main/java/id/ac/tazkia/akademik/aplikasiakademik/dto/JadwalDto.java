package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Lob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class JadwalDto {
    private Dosen dosen;
    private String id;
    private Set<Dosen> dosens = new HashSet<>();
    @Lob @Column(columnDefinition = "LONGTEXT")
    private String beritaAcara;
    private Jadwal jadwal;
    private PresensiDosen presensiDosen;
    private String waktuMulai;
    private String waktuSelesai;
    private String pertemuan;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(columnDefinition = "DATE")
    private LocalDate tanggal;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamMulai;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamSelesai;
    private TahunAkademik tahunAkademik;
    private KrsDetail krsDetail;
    private SesiKuliah sesiKuliah;
    private Mahasiswa mahasiswa;
    private StatusPresensi statusPresensi;
    private String catatan;
    private Integer rating;
    private LocalDateTime jamMasuk;
    private LocalDateTime jamKeluar;

}
