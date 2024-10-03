package id.ac.tazkia.smilemahasiswa.constant;

public enum StatusSempro {
    WAITING ("Menunggu", 1),
    PENDING_SCHEDULED ("Belum Dijadwalkan", 2),
    SCHEDULED ("Dijadwalkan", 3),
    ASSESSMENT ("Menunggu Penilaian Lengkap & Publish", 4),
    APPROVED ("Seminar Sudah Publish", 5),
    FAILED ("Seminar Tidak Lulus", 6),
    HAPUS("Dihapus", 7);

    private final String displayName;
    private final int order;

    StatusSempro(String displayName, int order) {
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
