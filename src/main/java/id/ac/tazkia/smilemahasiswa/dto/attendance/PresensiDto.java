package id.ac.tazkia.smilemahasiswa.dto.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.Column;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class PresensiDto {
    private Dosen dosen;
    private String id;
    private String beritaAcara;
    private String rps;
    private Jadwal jadwal;
    private String jenisPertemuan;
    private DetailPertemuan detailPertemuan;
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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime jamMasuk;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime jamKeluar;
    private String linkVideo;
    private String linkOnline;
    private StatusRecord ceramah;
    private StatusRecord diskusi;
    private StatusRecord presentasi;
    private StatusRecord tanyajawab;
    private StatusRecord kuis;
    private StatusRecord praktikum;
    private Boolean isEdit;
}
