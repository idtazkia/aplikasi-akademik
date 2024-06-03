package id.ac.tazkia.smilemahasiswa.constant;

public enum StatusUjian {
    LULUS ("Lulus"),
    DINILAI ("Dinilai"),
    REJECTED ("Ditolak"),
    WAITING("Menunggu di Proses"),
    TIDAK_LULUS ("Tidak Lulus");

    private final String getValueStatus;

    StatusUjian(String getValueStatus) {
        this.getValueStatus = getValueStatus;
    }

    public String getDisplayName() {
        return getValueStatus;
    }
}
