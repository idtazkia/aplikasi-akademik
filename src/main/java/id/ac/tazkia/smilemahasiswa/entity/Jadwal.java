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
     * 
     * Format nama kelas lokal yang didukung:
     * 1. MES-25-REG-IBM → M25IB (prodi=M, angkatan=25, suffix=IB dari IBM)
     * 2. MES-25-REG-IWM → M25IW (prodi=M, angkatan=25, suffix=IW dari IWM)
     * 3. MBS-A-22-A → M22AA (prodi=M, angkatan=22, kelas=A, suffix=A)
     * 4. HES-22 → H22AA (prodi=H, angkatan=22, default)
     * 
     * Strategy: Gunakan 2 karakter pertama dari part terakhir untuk membedakan
     * 
     * Satu kelas lokal = satu nama feeder (untuk semua jadwal di kelas tersebut)
     */
    public String generateNamaKelasFeeder() {
        if (prodi == null || kelas == null) {
            return "A1";
        }

        String kodeProdi = prodi.getKodeProdi() != null ? prodi.getKodeProdi() : "A";
        String angkatanRaw = kelas.getAngkatan() != null ? kelas.getAngkatan() : "1";

        // Ambil 1 karakter pertama dari kode prodi
        String prodiCode = kodeProdi.length() >= 1 ? kodeProdi.substring(0, 1).toUpperCase() : "A";

        // Ambil 2 digit terakhir dari angkatan (2021 → 21, 2025 → 25)
        String angkatan = angkatanRaw.length() >= 2 ? angkatanRaw.substring(angkatanRaw.length() - 2) : angkatanRaw;

        // Extract suffix dari nama kelas - gunakan 2 karakter dari part terakhir
        String kelasSuffix = "AA"; // Default

        if (kelas.getNamaKelas() != null && !kelas.getNamaKelas().isEmpty()) {
            String namaKelas = kelas.getNamaKelas().toUpperCase().trim();
            String[] parts = namaKelas.split("-");

            if (parts.length >= 4) {
                // Format: MES-25-REG-IBM atau MBS-A-22-KWS
                // Ambil 2 karakter pertama dari part terakhir untuk suffix
                String lastPart = parts[parts.length - 1].trim();
                if (lastPart.length() >= 2) {
                    kelasSuffix = lastPart.substring(0, 2);
                } else if (lastPart.length() == 1) {
                    // Jika part terakhir hanya 1 karakter, cek part sebelumnya
                    String prevPart = parts[parts.length - 2].trim();
                    if (prevPart.matches("\\d+")) {
                        // part sebelumnya adalah angka (angkatan), gunakan lastPart + hash
                        kelasSuffix = lastPart + generateUniqueSuffix(namaKelas, kodeProdi);
                    } else {
                        // Gabungkan karakter pertama dari 2 part terakhir
                        kelasSuffix = prevPart.substring(0, 1) + lastPart;
                    }
                } else {
                    kelasSuffix = "AA";
                }
            } else if (parts.length == 3) {
                // Format: MBS-A-22 atau MBS-22-A atau MES-25-REG
                String part1 = parts[1].trim();
                String part2 = parts[2].trim();

                if (part1.matches("\\d+")) {
                    // Format: MBS-22-A → angkatan di tengah, kelas di akhir
                    kelasSuffix = part2.length() >= 2 ? part2.substring(0, 2) : part2 + "A";
                } else if (part2.matches("\\d+")) {
                    // Format: MBS-A-22 → kelas di tengah, angkatan di akhir
                    kelasSuffix = part1.length() >= 2 ? part1.substring(0, 2) : part1 + "A";
                } else {
                    // Keduanya bukan angka, gunakan part terakhir
                    kelasSuffix = part2.length() >= 2 ? part2.substring(0, 2) : part2 + "A";
                }
            } else if (parts.length == 2) {
                // Format: HES-22 atau TAZ-A
                String part1 = parts[1].trim();

                if (part1.matches("\\d+")) {
                    // Format: HES-22 → tidak ada info kelas, generate dari hash
                    kelasSuffix = generateUniqueSuffix(namaKelas, kodeProdi) + "A";
                } else {
                    // Format: TAZ-A → parts[1] adalah kelas
                    kelasSuffix = part1.length() >= 2 ? part1.substring(0, 2) : part1 + "A";
                }
            } else {
                // Format tanpa dash: ambil 2 karakter terakhir
                if (namaKelas.length() >= 2) {
                    kelasSuffix = namaKelas.substring(namaKelas.length() - 2);
                } else {
                    kelasSuffix = namaKelas + "A";
                }
            }
        }

        // Build result: [prodiCode_1][angkatan_2][kelasSuffix_2] = 5 karakter
        String result = prodiCode + angkatan + kelasSuffix;

        // Jika kepanjangan (>5), truncate ke 5 karakter
        if (result.length() > 5) {
            result = result.substring(0, 5);
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
