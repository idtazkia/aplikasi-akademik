package id.ac.tazkia.smilemahasiswa.constant;

public enum StatusTugasAkhir {
    WAITING ("Menunggu", 1),
    PENDING_SCHEDULED ("Belum Dijadwalkan", 2),
    SCHEDULED ("Dijadwalkan", 3),
    ASSESSMENT ("Menunggu Penilaian Lengkap & Publish", 4),
    APPROVED_AKADEMIK ("Approved Akademik", 5),
    APPROVED ("Menunggu Publish", 6),
    PUBLISHED ("Sudah Publish", 8),
    FAILED ("Tidak Lulus", 9),
    REJECTED ("DItolak", 10),
    HAPUS("Dihapus", 9);

    private final String displayName;
    private final int order;

    StatusTugasAkhir(String displayName, int order) {
        this.displayName = displayName;
        this.order = order;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getOrder() {
        return order;
    }
}
