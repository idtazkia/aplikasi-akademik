package id.ac.tazkia.smilemahasiswa.entity;

/**
 * Enum Status Keluar Mahasiswa
 *
 * Menentukan status/cara mahasiswa keluar dari perguruan tinggi
 * Sesuai dengan standar PDDIKTI
 */
public enum StatusKeluar {
    /**
     * Lulus - Mahasiswa menyelesaikan studi dan dinyatakan lulus
     */
    LULUS("Lulus", "Mahasiswa lulus kuliah"),

    /**
     * Mengundurkan Diri - Mahasiswa keluar atas permintaan sendiri
     */
    MENGUNDURKAN_DIRI("Mengundurkan Diri", "Mahasiswa mengundurkan diri atas kemauan sendiri"),

    /**
     * Meninggal Dunia - Mahasiswa meninggal dunia
     */
    MENINGGAL_DUNIA("Meninggal Dunia", "Mahasiswa meninggal dunia"),

    /**
     * Putus Studi - Mahasiswa dinyatakan putus studi karena tidak aktif melebihi batas waktu
     */
    PUTUS_STUDI("Putus Studi", "Mahasiswa putus studi (tidak aktif melebihi batas waktu)"),

    /**
     * Dikeluarkan - Mahasiswa dikeluarkan karena pelanggaran akademik/non-akademik
     */
    DIKELUARKAN("Dikeluarkan", "Mahasiswa dikeluarkan karena pelanggaran"),

    /**
     * Mutasi - Mahasiswa pindah ke perguruan tinggi lain
     */
    MUTASI("Mutasi", "Mahasiswa pindah ke perguruan tinggi lain");

    private final String label;
    private final String description;

    StatusKeluar(String label, String description) {
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
     * Cek apakah status keluar adalah final (tidak bisa kuliah lagi)
     */
    public boolean isFinalStatus() {
        return this != null;
    }

    /**
     * Cek apakah status keluar adalah positif (lulus)
     */
    public boolean isPositiveExit() {
        return this == LULUS;
    }

    /**
     * Cek apakah status keluar karena alasan akademik
     */
    public boolean isAcademicReason() {
        return this == LULUS || this == PUTUS_STUDI || this == DIKELUARKAN;
    }

    /**
     * Cek apakah status keluar karena alasan non-akademik/personal
     */
    public boolean isPersonalReason() {
        return this == MENGUNDURKAN_DIRI || this == MENINGGAL_DUNIA || this == MUTASI;
    }

    /**
     * Ambil enum berdasarkan nama/label
     * @param label nama label yang dicari
     * @return StatusKeluar enum
     */
    public static StatusKeluar fromLabel(String label) {
        for (StatusKeluar s : values()) {
            if (s.label.equalsIgnoreCase(label) || s.name().equalsIgnoreCase(label)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Status Keluar tidak ditemukan: " + label);
    }
}
