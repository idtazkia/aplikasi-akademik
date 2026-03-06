package id.ac.tazkia.smilemahasiswa.entity;

/**
 * Enum Jenis Masuk Mahasiswa
 *
 * Menentukan jalur/ cara mahasiswa masuk ke perguruan tinggi
 * Sesuai dengan standar PDDIKTI
 */
public enum JenisMasuk {
    /**
     * Peserta Didik Baru - Mahasiswa baru melalui jalur reguler
     * Contoh: SNMPTN, SBMPTN, UM, jalur prestasi, dll
     */
    PESERTA_DIDIK_BARU("Peserta Didik Baru", "Mahasiswa baru melalui jalur reguler/SNMPTN/SBMPTN"),

    /**
     * Pindahan - Mahasiswa pindahan dari perguruan tinggi lain
     */
    PINDAHAN("Pindahan", "Mahasiswa pindahan dari perguruan tinggi lain"),

    /**
     * Alih Jenjang - Perpindahan jenjang (D3 ke S1, S1 ke S2, dll)
     */
    ALIH_JENJANG("Alih Jenjang", "Mahasiswa alih jenjang (D3 ke S1, atau S1 ke S2)"),

    /**
     * RPL Perolehan SKS - Recognition of Prior Learning berdasarkan pengalaman kerja
     */
    RPL_PEROLEHAN_SKS("RPL Perolehan SKS", "RPL berdasarkan pengalaman kerja/pembelajaran"),

    /**
     * RPL Transfer SKS - Recognition of Prior Learning berdasarkan transfer mata kuliah
     */
    RPL_TRANSFER_SKS("RPL Transfer SKS", "RPL berdasarkan transfer mata kuliah sebelumnya"),

    /**
     * Double Degree - Program gelar ganda dari dua institusi
     */
    DOUBLE_DEGREE("Double Degree", "Program gelar ganda"),

    /**
     * Join Degree - Program gelar bersama antar institusi
     */
    JOIN_DEGREE("Join Degree", "Program gelar bersama"),

    /**
     * Fasttrack - Program percepatan S1-S2 (3+1, 3+2, dll)
     */
    FASTTRACK("Fasttrack", "Program percepatan S1-S2"),

    /**
     * Peserta Non Gelar - Peserta course/program non-gelar
     */
    PESERTA_NON_GELAR("Peserta Non Gelar", "Peserta course non-gelar");

    private final String label;
    private final String description;

    JenisMasuk(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Ambil enum berdasarkan nama/label
     * @param label nama label yang dicari
     * @return JenisMasuk enum
     */
    public static JenisMasuk fromLabel(String label) {
        for (JenisMasuk j : values()) {
            if (j.label.equalsIgnoreCase(label) || j.name().equalsIgnoreCase(label)) {
                return j;
            }
        }
        throw new IllegalArgumentException("Jenis Masuk tidak ditemukan: " + label);
    }
}
