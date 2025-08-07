package id.ac.tazkia.smilemahasiswa.entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Entity
@Data
@Table(name = "detail_tahun_akademik")
public class DetailTahunAkademik {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tahun_akademik", nullable = false)
    @NotNull
    private TahunAkademik tahunAkademik;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prodi")
    private Prodi prodi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_program")
    private Program program;

    @Column(name = "angkatan_mulai", length = 4)
    private String angkatanMulai;

    @Column(name = "angkatan_selesai", length = 4)
    private String angkatanSelesai;

    @Column(name = "tanggal_mulai_krs")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulaiKrs;

    @Column(name = "tanggal_selesai_krs")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalSelesaiKrs;

    @Column(name = "tanggal_mulai_kuliah")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulaiKuliah;

    @Column(name = "tanggal_selesai_kuliah")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalSelesaiKuliah;

    @Column(name = "tanggal_mulai_uts")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulaiUts;

    @Column(name = "tanggal_selesai_uts")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalSelesaiUts;

    @Column(name = "tanggal_mulai_uas")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulaiUas;

    @Column(name = "tanggal_selesai_uas")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalSelesaiUas;

    @Column(name = "tanggal_mulai_nilai")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulaiNilai;

    @Column(name = "tanggal_selesai_nilai")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalSelesaiNilai;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer prioritas = 0;

    private String keterangan;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;
}
