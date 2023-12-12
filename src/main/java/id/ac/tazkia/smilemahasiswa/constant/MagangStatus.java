package id.ac.tazkia.smilemahasiswa.constant;

public enum MagangStatus {
    MAGANG("Laporan Magang Tugas Akhir"),
    MBKM("Laporan Magang MBKM");

    private final String getValueStatus;

    MagangStatus(String getValueStatus) {
        this.getValueStatus = getValueStatus;
    }

    public String getDisplayName() {
        return getValueStatus;
    }
}
