package id.ac.tazkia.smilemahasiswa.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

@Entity
@Data
public class JadwalDosen {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_jadwal")
    private Jadwal jadwal;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_dosen")
    private Dosen dosen;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusJadwalDosen statusJadwalDosen;

    @NotNull
    @Min(0)
    private Integer jumlahKehadiran = 0;

    @NotNull
    @Min(0)
    private Integer jumlahTerlambat = 0;

    @NotNull
    @Min(0)
    private Integer jumlahMangkir = 0;

    @NotNull
    @Min(0)
    private Integer jumlahSakit = 0;

    @NotNull
    @Min(0)
    private Integer jumlahIzin = 0;

    /**
     * ID Aktivitas Mengajar dari Feeder API
     * Diisi setelah berhasil insert ke Feeder
     */
    private String aktivitasMengajarFeeder;

    /**
     * Status sync ke PDDIKTI Feeder untuk dosen pengajar
     * AKTIF = sudah di-sync ke Feeder
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_feeder")
    private StatusRecord statusFeeder;

    /**
     * Check apakah dosen pengajar sudah di-sync ke Feeder
     */
    public boolean isSyncedToFeeder() {
        return statusFeeder != null &&
                statusFeeder == StatusRecord.AKTIF &&
                aktivitasMengajarFeeder != null && !aktivitasMengajarFeeder.isEmpty();
    }

    /**
     * Custom setter untuk aktivitasMengajarFeeder - sekali di-set tidak bisa
     * di-null-kan
     * IMMUTABLE setelah di-set
     */
    public void setAktivitasMengajarFeeder(String aktivitasMengajarFeeder) {
        if (this.aktivitasMengajarFeeder != null && !this.aktivitasMengajarFeeder.isEmpty()) {
            if (aktivitasMengajarFeeder == null || aktivitasMengajarFeeder.isEmpty() ||
                    !this.aktivitasMengajarFeeder.equals(aktivitasMengajarFeeder)) {
                throw new IllegalStateException(
                        "aktivitasMengajarFeeder tidak dapat diubah setelah di-set. " +
                                "Data sudah ter-sync ke Feeder dengan ID: " + this.aktivitasMengajarFeeder);
            }
            return;
        }
        this.aktivitasMengajarFeeder = aktivitasMengajarFeeder;
    }

    /**
     * Custom setter untuk statusFeeder - sekali AKTIF tidak bisa diubah
     * IMMUTABLE setelah AKTIF
     */
    public void setStatusFeeder(StatusRecord statusFeeder) {
        if (this.statusFeeder == StatusRecord.AKTIF) {
            if (statusFeeder != StatusRecord.AKTIF) {
                throw new IllegalStateException(
                        "statusFeeder tidak dapat diubah setelah AKTIF. " +
                                "Data sudah ter-sync ke Feeder.");
            }
            return;
        }
        this.statusFeeder = statusFeeder;
    }

}
