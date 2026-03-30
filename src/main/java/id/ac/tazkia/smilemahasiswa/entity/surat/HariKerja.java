package id.ac.tazkia.smilemahasiswa.entity.surat;

public enum HariKerja {
    SENIN_JUMAT("Senin - Jumat"),
    SENIN_SABTU("Senin - Sabtu");

    private final String label;

    HariKerja(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
