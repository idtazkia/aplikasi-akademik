package id.ac.tazkia.smilemahasiswa.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for Tahun Akademik related operations
 */
@Slf4j
public final class TahunAkademikUtils {

    private TahunAkademikUtils() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Parse angkatan dengan memperhitungkan format berbeda untuk pascasarjana
     * Pascasarjana menggunakan 2 digit (17), sarjana menggunakan 4 digit (2017)
     * 
     * @param angkatan String angkatan yang akan di-parse
     * @return Integer tahun dalam format 4 digit, atau null jika input kosong
     * @throws NumberFormatException jika format angkatan tidak valid
     */
    public static Integer parseAngkatan(String angkatan) throws NumberFormatException {
        if (angkatan == null || angkatan.trim().isEmpty()) {
            return null;
        }

        int value = Integer.parseInt(angkatan.trim());

        // Jika 2 digit (format pascasarjana), konversi ke 4 digit
        // Asumsi: 17 -> 2017, 18 -> 2018, dst.
        // Untuk tahun 00-30 dianggap 2000-2030, untuk 31-99 dianggap 1931-1999
        if (value < 100) {
            if (value <= 30) {
                return 2000 + value; // 00-30 -> 2000-2030
            } else {
                return 1900 + value; // 31-99 -> 1931-1999
            }
        }

        // Jika sudah 4 digit, langsung return
        return value;
    }

    /**
     * Cek apakah dua range angkatan saling bertumpang tindih (overlap)
     * Mempertimbangkan format angkatan yang berbeda untuk pascasarjana (2 digit) vs
     * sarjana (4 digit)
     * 
     * @param newMulai        angkatan mulai yang baru
     * @param newSelesai      angkatan selesai yang baru
     * @param existingMulai   angkatan mulai yang sudah ada
     * @param existingSelesai angkatan selesai yang sudah ada
     * @return true jika ada overlap, false jika tidak
     */
    public static boolean isOverlapping(String newMulai, String newSelesai,
            String existingMulai, String existingSelesai) {
        try {
            // Handle null values - jika null berarti unlimited
            Integer newStart = parseAngkatan(newMulai);
            Integer newEnd = parseAngkatan(newSelesai);
            Integer existingStart = parseAngkatan(existingMulai);
            Integer existingEnd = parseAngkatan(existingSelesai);

            // Jika salah satu range unlimited (null), selalu overlap
            if (newStart == null || newEnd == null || existingStart == null || existingEnd == null) {
                return true;
            }

            // Cek overlap: dua range overlap jika tidak terpisah sepenuhnya
            // Range A (newStart-newEnd) dan Range B (existingStart-existingEnd)
            // Tidak overlap jika: newEnd < existingStart || newStart > existingEnd
            // Overlap jika: !(newEnd < existingStart || newStart > existingEnd)
            return !(newEnd < existingStart || newStart > existingEnd);

        } catch (NumberFormatException e) {
            log.warn(
                    "Invalid angkatan format detected: newMulai={}, newSelesai={}, existingMulai={}, existingSelesai={}",
                    newMulai, newSelesai, existingMulai, existingSelesai);
            return false; // Jika format salah, anggap tidak overlap
        }
    }

    /**
     * Generate kode tahun akademik selanjutnya berdasarkan tahun akademik
     * sebelumnya
     * 
     * @param kodeTahunSebelum  kode tahun akademik sebelumnya
     * @param jenisTahunSebelum jenis tahun akademik sebelumnya
     * @param tahunSebelum      tahun dari tahun akademik sebelumnya
     * @return String kode tahun akademik baru
     */
    public static String generateNextKode(String kodeTahunSebelum, String jenisTahunSebelum, String tahunSebelum) {
        if ("PENDEK".equals(jenisTahunSebelum)) {
            int tahun = Integer.parseInt(tahunSebelum) + 1;
            return tahun + "1";
        } else {
            int kodeSebelum = Integer.parseInt(kodeTahunSebelum);
            return String.valueOf(kodeSebelum + 1);
        }
    }

    /**
     * Generate nama tahun akademik berdasarkan kode
     * 
     * @param kode kode tahun akademik (contoh: 20241)
     * @return String nama tahun akademik (contoh: 2024/2025 Semester Ganjil)
     */
    public static String generateNamaTahunAkademik(String kode) {
        if (kode == null || kode.length() < 5) {
            throw new IllegalArgumentException("Kode tahun akademik tidak valid: " + kode);
        }

        int tahun = Integer.parseInt(kode.substring(0, 4));
        int tahunBerikutnya = tahun + 1;
        String kodeJenis = kode.substring(4, 5);
        String jenis = getJenisSemester(kodeJenis);

        return String.format("%d/%d Semester %s", tahun, tahunBerikutnya, jenis);
    }

    /**
     * Get jenis semester berdasarkan kode
     * 
     * @param kodeJenis kode jenis semester (1, 2, atau 3)
     * @return String nama jenis semester
     */
    public static String getJenisSemester(String kodeJenis) {
        switch (kodeJenis) {
            case "1":
                return "Ganjil";
            case "2":
                return "Genap";
            case "3":
                return "Pendek";
            default:
                return "Unknown";
        }
    }

    /**
     * Validate format kode tahun akademik
     * 
     * @param kode kode yang akan divalidasi
     * @return true jika format valid, false jika tidak
     */
    public static boolean isValidKodeTahunAkademik(String kode) {
        if (kode == null || kode.length() != 5) {
            return false;
        }

        try {
            // Cek apakah 4 digit pertama adalah tahun yang valid
            int tahun = Integer.parseInt(kode.substring(0, 4));
            if (tahun < 1900 || tahun > 2100) {
                return false;
            }

            // Cek apakah digit terakhir adalah jenis semester yang valid (1, 2, atau 3)
            String jenis = kode.substring(4, 5);
            return "1".equals(jenis) || "2".equals(jenis) || "3".equals(jenis);

        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Extract tahun dari kode tahun akademik
     * 
     * @param kode kode tahun akademik
     * @return Integer tahun, atau null jika format tidak valid
     */
    public static Integer extractTahunFromKode(String kode) {
        if (!isValidKodeTahunAkademik(kode)) {
            return null;
        }

        try {
            return Integer.parseInt(kode.substring(0, 4));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Extract jenis semester dari kode tahun akademik
     * 
     * @param kode kode tahun akademik
     * @return String jenis semester (1, 2, atau 3), atau null jika format tidak
     *         valid
     */
    public static String extractJenisFromKode(String kode) {
        if (!isValidKodeTahunAkademik(kode)) {
            return null;
        }

        return kode.substring(4, 5);
    }
}
