package id.ac.tazkia.smilemahasiswa.entity;

public enum Akses {
    TERTUTUP(0),
    PRODI(0),
    UMUM(0),
    PILIHAN(0),
    WAJIB_UMUM(1),
    WAJIB_PRODI(1),
    WAJIB_KONSENTRASI(1);

    private final int apakahWajib;

    Akses(int apakahWajib) {
        this.apakahWajib = apakahWajib;
    }

    public int getApakahWajib() {
        return apakahWajib;
    }
}
