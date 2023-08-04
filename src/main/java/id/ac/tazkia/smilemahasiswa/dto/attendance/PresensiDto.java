package id.ac.tazkia.smilemahasiswa.dto.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.Column;

import org.springframework.format.annotation.DateTimeFormat;

import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.KrsDetail;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.PresensiDosen;
import id.ac.tazkia.smilemahasiswa.entity.SesiKuliah;
import id.ac.tazkia.smilemahasiswa.entity.StatusPresensi;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import lombok.Data;

@Data
public class PresensiDto {
    private Dosen dosen;
    private String id;
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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime jamMasuk;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime jamKeluar;
}
