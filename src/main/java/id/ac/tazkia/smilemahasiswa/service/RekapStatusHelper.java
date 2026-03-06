package id.ac.tazkia.smilemahasiswa.service;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper service untuk rekap status mahasiswa
 * Menyediakan method-method yang dipanggil dari Thymeleaf template
 */
@Component
public class RekapStatusHelper {

    // ========================================
    // Helper Methods untuk Rekap Masuk
    // ========================================

    /**
     * Get semua angkatan unik dari rekap
     * Data structure: [jenis_masuk, angkatan, jumlah]
     */
    public List<String> getAllAngkatan(List<Object[]> rekap) {
        if (rekap == null) return new ArrayList<>();
        return rekap.stream()
                .map(row -> (String) row[1])
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get semua jenis masuk unik dari rekap
     * Data structure: [jenis_masuk, angkatan, jumlah]
     */
    public List<String> getAllJenisMasuk(List<Object[]> rekap) {
        if (rekap == null) return new ArrayList<>();
        return rekap.stream()
                .map(row -> (String) row[0])
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Helper untuk convert Number ke Long
     */
    private Long toLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof BigInteger) return ((BigInteger) obj).longValue();
        if (obj instanceof Number) return ((Number) obj).longValue();
        return 0L;
    }

    /**
     * Get jumlah berdasarkan jenis masuk dan angkatan
     * Data structure: [jenis_masuk, angkatan, jumlah]
     */
    public Long getJumlahByAngkatan(List<Object[]> rekap, String jenisMasuk, String angkatan) {
        if (rekap == null) return 0L;
        return rekap.stream()
                .filter(row -> row[0].equals(jenisMasuk) && row[1].equals(angkatan))
                .map(row -> toLong(row[2]))
                .findFirst()
                .orElse(0L);
    }

    /**
     * Get total berdasarkan jenis masuk
     * Data structure: [jenis_masuk, angkatan, jumlah]
     */
    public Long getTotalByJenisMasuk(List<Object[]> rekap, String jenisMasuk) {
        if (rekap == null) return 0L;
        return rekap.stream()
                .filter(row -> row[0].equals(jenisMasuk))
                .map(row -> toLong(row[2]))
                .reduce(0L, Long::sum);
    }

    /**
     * Get total berdasarkan angkatan
     * Data structure: [jenis_masuk, angkatan, jumlah]
     */
    public Long getTotalByAngkatan(List<Object[]> rekap, String angkatan) {
        if (rekap == null) return 0L;
        return rekap.stream()
                .filter(row -> row[1].equals(angkatan))
                .map(row -> toLong(row[2]))
                .reduce(0L, Long::sum);
    }

    /**
     * Get grand total
     */
    public Long getGrandTotal(List<Object[]> rekap) {
        if (rekap == null) return 0L;
        return rekap.stream()
                .map(row -> toLong(row[2]))
                .reduce(0L, Long::sum);
    }

    // ========================================
    // Helper Methods untuk Rekap Keluar
    // ========================================

    /**
     * Get semua tahun akademik unik dari rekap keluar
     * Data structure: [status_keluar, tahun_akademik, jumlah]
     */
    public List<String> getAllTahunAkademik(List<Object[]> rekap) {
        if (rekap == null) return new ArrayList<>();
        return rekap.stream()
                .map(row -> (String) row[1])
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get semua status keluar unik dari rekap
     * Data structure: [status_keluar, tahun_akademik, jumlah]
     */
    public List<String> getAllStatusKeluar(List<Object[]> rekap) {
        if (rekap == null) return new ArrayList<>();
        return rekap.stream()
                .map(row -> (String) row[0])
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get count berdasarkan status keluar
     * Data structure: [status_keluar, tahun_akademik, jumlah]
     */
    public Long getCountByStatus(List<Object[]> rekap, String status) {
        if (rekap == null) return 0L;
        return rekap.stream()
                .filter(row -> row[0].equals(status))
                .map(row -> toLong(row[2]))
                .reduce(0L, Long::sum);
    }

    /**
     * Get jumlah berdasarkan status keluar dan tahun akademik
     * Data structure: [status_keluar, tahun_akademik, jumlah]
     */
    public Long getJumlahByTahun(List<Object[]> rekap, String statusKeluar, String tahun) {
        if (rekap == null) return 0L;
        return rekap.stream()
                .filter(row -> row[0].equals(statusKeluar) && row[1].equals(tahun))
                .map(row -> toLong(row[2]))
                .findFirst()
                .orElse(0L);
    }

    /**
     * Get total berdasarkan status keluar
     * Data structure: [status_keluar, tahun_akademik, jumlah]
     */
    public Long getTotalByStatusKeluar(List<Object[]> rekap, String statusKeluar) {
        if (rekap == null) return 0L;
        return rekap.stream()
                .filter(row -> row[0].equals(statusKeluar))
                .map(row -> toLong(row[2]))
                .reduce(0L, Long::sum);
    }

    /**
     * Get total berdasarkan tahun akademik
     * Data structure: [status_keluar, tahun_akademik, jumlah]
     */
    public Long getTotalByTahun(List<Object[]> rekap, String tahun) {
        if (rekap == null) return 0L;
        return rekap.stream()
                .filter(row -> row[1].equals(tahun))
                .map(row -> toLong(row[2]))
                .reduce(0L, Long::sum);
    }
}
