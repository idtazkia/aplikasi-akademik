package id.ac.tazkia.smilemahasiswa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity Riwayat Status Mahasiswa per Semester
 *
 * Menyimpan riwayat status berjalan mahasiswa untuk setiap tahun akademik/semester
 * Satu mahasiswa hanya memiliki satu status per tahun akademik
 */
@Entity
@Data
public class RiwayatStatusMahasiswa {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    /**
     * Mahasiswa yang memiliki riwayat status ini
     */
    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    @JsonBackReference
    private Mahasiswa mahasiswa;

    /**
     * Tahun akademik untuk riwayat status ini
     */
    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    /**
     * Status berjalan mahasiswa untuk periode ini
     * Menggunakan enum StatusRecord yang sudah ada
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord statusBerjalan;

    /**
     * Tanggal status mulai berlaku
     */
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulai;

    /**
     * Tanggal status berakhir (opsional, untuk status sementara seperti CUTI)
     */
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalSelesai;

    /**
     * Keterangan tambahan terkait status
     * Contoh: Alasan cuti, alasan nonaktif, dll
     */
    @Column(columnDefinition = "TEXT")
    private String keterangan;

    /**
     * Waktu record dibuat
     */
    private LocalDateTime createdAt;

    /**
     * User yang membuat record
     */
    private String createdBy;

    /**
     * Waktu record terakhir diupdate
     */
    private LocalDateTime updatedAt;

    /**
     * User yang mengupdate record
     */
    private String updatedBy;

    /**
     * User insert (legacy, untuk compatibility)
     */
    private String userInsert;

    /**
     * User update (legacy, untuk compatibility)
     */
    private String userUpdate;

    /**
     * Date insert (legacy, untuk compatibility)
     */
    private LocalDateTime dateInsert;

    /**
     * Date update (legacy, untuk compatibility)
     */
    private LocalDateTime dateUpdate;

    /**
     * Cek apakah status adalah aktif
     */
    public boolean isAktif() {
        return statusBerjalan == StatusRecord.AKTIF;
    }

    /**
     * Cek apakah status adalah cuti
     */
    public boolean isCuti() {
        return statusBerjalan == StatusRecord.CUTI;
    }

    /**
     * Cek apakah status adalah nonaktif
     */
    public boolean isNonAktif() {
        return statusBerjalan == StatusRecord.NONAKTIF;
    }

    /**
     * Cek apakah status adalah lulus
     */
    public boolean isLulus() {
        return statusBerjalan == StatusRecord.LULUS;
    }

    /**
     * Cek apakah mahasiswa dalam status yang menghambat pembelajaran
     * (cuti, nonaktif, mengundurkan diri, dll)
     */
    public boolean isTidakAktifBelajar() {
        return statusBerjalan == StatusRecord.CUTI
                || statusBerjalan == StatusRecord.NONAKTIF
                || statusBerjalan == StatusRecord.MENGUNDURKAN_DIRI
                || statusBerjalan == StatusRecord.KELUAR;
    }

    /**
     * Cek apakah status memperbolehkan mahasiswa mengambil KRS
     */
    public boolean isBolehAmbilKrs() {
        return statusBerjalan == StatusRecord.AKTIF
                || statusBerjalan == StatusRecord.MBKM
                || statusBerjalan == StatusRecord.PRAAKTIF;
    }

    /**
     * Helper method untuk set createdAt sebelum insert
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (dateInsert == null) {
            dateInsert = LocalDateTime.now();
        }
    }

    /**
     * Helper method untuk set updatedAt sebelum update
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        dateUpdate = LocalDateTime.now();
    }

    /**
     * Tampilkan informasi singkat
     */
    public String getDisplayInfo() {
        return String.format("%s - %s: %s",
                tahunAkademik != null ? tahunAkademik.getNamaTahunAkademik() : "-",
                statusBerjalan,
                keterangan != null ? keterangan : "");
    }
}
