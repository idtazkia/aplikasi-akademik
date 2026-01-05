package id.ac.tazkia.smilemahasiswa.entity.surat;

public enum JenisMagang {
    MAGANG_MANDIRI("Magang Mandiri"),
    MAGANG_MBKM("Magang MBKM"),
    MAGANG_TUGAS_AKHIR("Magang Tugas Akhir (3 SKS)");

    private final String label;

    JenisMagang(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
