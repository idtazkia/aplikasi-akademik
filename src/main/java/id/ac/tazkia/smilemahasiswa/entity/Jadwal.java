package id.ac.tazkia.smilemahasiswa.entity;

import java.math.BigDecimal;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Entity
@Data
public class Jadwal extends Auditable {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_matakuliah_kurikulum")
    private MatakuliahKurikulum matakuliahKurikulum;

    @ManyToOne
    @JoinColumn(name = "id_hari")
    private Hari hari;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamMulai;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamSelesai;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik_prodi")
    private TahunAkademikProdi tahunAkademikProdi;

    @ManyToOne
    @JoinColumn(name = "id_prodi")
    private Prodi prodi;

    @ManyToOne
    @JoinColumn(name = "id_dosen_pengampu")
    private Dosen dosen;

    private BigDecimal bobotUts = BigDecimal.ZERO;

    private BigDecimal bobotUas = BigDecimal.ZERO;

    private BigDecimal bobotTugas = BigDecimal.ZERO;

    private BigDecimal bobotPresensi = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "id_ruangan")
    private Ruangan ruangan;

    @ManyToOne
    @JoinColumn(name = "id_kelas")
    private Kelas kelas;

    private Integer jumlahSesi;

    private String finalStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @ManyToOne
    @JoinColumn(name = "id_program")
    private Program program;

    private String sesi;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Akses akses = Akses.TERTUTUP;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusUts;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusUas;

    private Integer kapasitas;

    private String idNumberElearning;
    private String formatUts;
    private String formatUas;

    @ManyToOne
    @JoinColumn(name = "akses_uts")
    private Dosen aksesUts;

    @ManyToOne
    @JoinColumn(name = "akses_uas")
    private Dosen aksesUas;

    private String finalPloting = "WAITING";

    /**
     * ID Kelas Kuliah dari Feeder API
     * IMMUTABLE: Sekali di-set tidak bisa diubah atau di-null-kan
     */
    @Column(name = "id_kelas_kuliah_feeder")
    private String idKelasKuliahFeeder;

    /**
     * Status sync ke PDDIKTI Feeder untuk kelas
     * AKTIF = sudah di-sync ke Feeder
     * IMMUTABLE: Sekali AKTIF tidak bisa diubah kembali
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_kelas_feeder")
    private StatusRecord statusKelasFeeder;

    /**
     * Custom setter untuk idKelasKuliahFeeder - sekali di-set tidak bisa diubah
     * 
     * @param idKelasKuliahFeeder ID kelas dari Feeder
     * @throws IllegalStateException jika mencoba mengubah dari nilai yang sudah ada
     */
    public void setIdKelasKuliahFeeder(String idKelasKuliahFeeder) {
        // Jika sudah ada nilai (tidak null dan tidak kosong), tidak boleh diubah
        if (this.idKelasKuliahFeeder != null && !this.idKelasKuliahFeeder.isEmpty()) {
            if (idKelasKuliahFeeder == null || idKelasKuliahFeeder.isEmpty() ||
                    !this.idKelasKuliahFeeder.equals(idKelasKuliahFeeder)) {
                throw new IllegalStateException(
                        "idKelasKuliahFeeder tidak dapat diubah setelah di-set. " +
                                "Data sudah ter-sync ke Feeder dengan ID: " + this.idKelasKuliahFeeder);
            }
            // Jika nilai sama, tidak perlu update
            return;
        }
        this.idKelasKuliahFeeder = idKelasKuliahFeeder;
    }

    /**
     * Custom setter untuk statusKelasFeeder - sekali AKTIF tidak bisa diubah
     * 
     * @param statusKelasFeeder status sync ke Feeder
     * @throws IllegalStateException jika mencoba mengubah dari AKTIF ke nilai lain
     */
    public void setStatusKelasFeeder(StatusRecord statusKelasFeeder) {
        // Jika sudah AKTIF, tidak boleh diubah
        if (this.statusKelasFeeder == StatusRecord.AKTIF) {
            if (statusKelasFeeder != StatusRecord.AKTIF) {
                throw new IllegalStateException(
                        "statusKelasFeeder tidak dapat diubah setelah AKTIF. " +
                                "Data sudah ter-sync ke Feeder.");
            }
            // Jika nilai sama (AKTIF), tidak perlu update
            return;
        }
        this.statusKelasFeeder = statusKelasFeeder;
    }

    /**
     * Check apakah jadwal sudah di-sync ke Feeder
     * 
     * @return true jika sudah sync (status AKTIF dan ada id_kelas_kuliah_feeder)
     */
    public boolean isKelasSyncedToFeeder() {
        return statusKelasFeeder != null &&
                statusKelasFeeder == StatusRecord.AKTIF &&
                idKelasKuliahFeeder != null && !idKelasKuliahFeeder.isEmpty();
    }

    /**
     * Check apakah jadwal siap untuk sync ke Feeder
     * 
     * @return true jika status AKTIF dan lengkap (hari, jam, ruangan, kelas)
     */
    public boolean isReadyForFeederSync() {
        return status == StatusRecord.AKTIF &&
                hari != null &&
                jamMulai != null &&
                jamSelesai != null &&
                ruangan != null &&
                kelas != null &&
                matakuliahKurikulum != null &&
                tahunAkademik != null &&
                prodi != null;
    }

    /**
     * Generate nama kelas untuk Feeder (max 5 karakter)
     * Format: [kode_prodi][angkatan][kelas_suffix]
     * Satu kelas lokal = satu nama feeder (untuk semua jadwal di kelas tersebut)
     */
    public String generateNamaKelasFeeder() {
        if (prodi == null || kelas == null) {
            return "A1";
        }

        String kodeProdi = prodi.getKodeProdi() != null ? prodi.getKodeProdi() : "A";
        String angkatanRaw = kelas.getAngkatan() != null ? kelas.getAngkatan() : "1";

        // Ambil 2 digit terakhir dari angkatan (2021 → 21)
        String angkatan = angkatanRaw.length() >= 2 ? angkatanRaw.substring(angkatanRaw.length() - 2) : angkatanRaw;

        // Extract suffix dari nama kelas dengan smart parsing
        String kelasSuffix = "";
        if (kelas.getNamaKelas() != null && !kelas.getNamaKelas().isEmpty()) {
            String namaKelas = kelas.getNamaKelas();

            // Split untuk analisis
            String[] parts = namaKelas.split("-");

            if (parts.length >= 3) {
                // Format: MBS-21-KWS-A → ambil "A"
                kelasSuffix = parts[parts.length - 1].trim().toUpperCase();
            } else if (parts.length == 2) {
                // Format: HES-22 atau TAZ-25
                String part2 = parts[1].trim().toUpperCase();

                // Jika part2 adalah angka dan sama dengan angkatan, generate suffix unik
                if (part2.equals(angkatan) || part2.equals(angkatanRaw)) {
                    // Format: HES-22, TAZ-25 → generate suffix unik berdasarkan hash nama kelas
                    kelasSuffix = generateUniqueSuffix(namaKelas, kodeProdi);
                } else if (part2.matches("[A-Za-z]")) {
                    // Jika part2 adalah huruf tunggal
                    kelasSuffix = part2;
                } else {
                    // Default suffix
                    kelasSuffix = "A";
                }
            } else {
                // Format tanpa dash: ambil karakter terakhir
                kelasSuffix = namaKelas.substring(namaKelas.length() - 1).toUpperCase();
            }
        }

        // Build result: [kode_prodi][angkatan][kelas_suffix]
        String result = kodeProdi + angkatan + kelasSuffix;

        // Jika kepanjangan (>5), optimize
        if (result.length() > 5) {
            // Priority 1: 2 kode prodi + 2 angkatan + 1 kelas = 5 karakter
            String prodiCode = kodeProdi.length() >= 2 ? kodeProdi.substring(0, 2) : kodeProdi;
            String angkatanCode = angkatan.length() >= 2 ? angkatan.substring(angkatan.length() - 2) : angkatan;
            String kelasOptimized = kelasSuffix.length() >= 1 ? kelasSuffix.substring(0, 1) : "";

            result = prodiCode + angkatanCode + kelasOptimized;

            // Jika masih >5, truncate ke 5
            if (result.length() > 5) {
                result = result.substring(0, 5);
            }
        }

        // Jika masih kurang dari 3, tambahkan padding
        while (result.length() < 3) {
            result += "A";
        }

        return result;
    }

    /**
     * Generate suffix unik untuk kelas format [PRODI]-[ANGKATAN]
     * Menggunakan hash untuk memastikan setiap kelas dapat suffix yang berbeda
     */
    private String generateUniqueSuffix(String namaKelas, String kodeProdi) {
        // Hash nama kelas + kode prodi untuk generate consistent suffix
        String input = namaKelas + kodeProdi;

        // Simple hash: gunakan hashCode dan modulo 26 untuk dapat A-Z
        int hash = Math.abs(input.hashCode());
        char suffix = (char) ('A' + (hash % 26));

        return String.valueOf(suffix);
    }

}
