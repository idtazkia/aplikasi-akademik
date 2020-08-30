package id.ac.tazkia.smilemahasiswa.dto.attendance;

import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Lob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class JadwalDto {
    private Dosen dosen;
    private String id;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String beritaAcara;
    private Jadwal jadwal;
    private PresensiDosen presensiDosen;
    private String waktuMulai;
    private String waktuSelesai;
    private String pertemuan;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(columnDefinition = "DATE")
    private LocalDate tanggal;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamMulai;
    @DateTimeFormat(pattern = "HH:mm:ss")
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
