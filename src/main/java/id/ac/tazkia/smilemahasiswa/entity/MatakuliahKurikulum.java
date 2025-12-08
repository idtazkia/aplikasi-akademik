package id.ac.tazkia.smilemahasiswa.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MatakuliahKurikulum {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_kurikulum")
    @JsonBackReference
    private Kurikulum kurikulum;

    @ManyToOne
    @JoinColumn(name = "id_matakuliah")
    private Matakuliah matakuliah;

    private Integer nomorUrut;

    private String wajib;

    private String responsi;

    @NotNull
    private Integer semester;

    private String matakuliahKurikulumSemester;

    @NotNull
    private Integer jumlahSks;

    private String syaratTugasAkhir;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private Integer sksMinimal;
    private BigDecimal ipkMinimal;

    @ManyToOne
    @JoinColumn(name = "id_konsentrasi")
    @JsonBackReference
    private Konsentrasi konsentrasi;

    private String silabus;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "matakuliah_kurikulum_program", joinColumns = @JoinColumn(name = "id_matakuliah_kurikulum"), inverseJoinColumns = @JoinColumn(name = "id_program"))
    private Set<Program> programs = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Akses akses;

    @Enumerated(EnumType.STRING)
    private StatusRecord statusSkripsi = StatusRecord.NONAKTIF;

    private Integer sds;

    @Enumerated(EnumType.STRING)
    private StatusRecord konsepNote = StatusRecord.NONAKTIF;

    @Enumerated(EnumType.STRING)
    private StatusRecord sempro = StatusRecord.NONAKTIF;

    /**
     * Status sync ke PDDIKTI Feeder
     * AKTIF = sudah di-sync ke Feeder
     * NONAKTIF = belum di-sync (default)
     * IMMUTABLE: Sekali AKTIF tidak bisa diubah kembali
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_feeder")
    @Getter
    private StatusRecord statusFeeder = StatusRecord.NONAKTIF;

    /**
     * Custom setter untuk statusFeeder - sekali AKTIF tidak bisa diubah
     * 
     * @param statusFeeder status sync ke Feeder
     * @throws IllegalStateException jika mencoba mengubah dari AKTIF ke nilai lain
     */
    public void setStatusFeeder(StatusRecord statusFeeder) {
        // Jika sudah AKTIF, tidak boleh diubah
        if (this.statusFeeder == StatusRecord.AKTIF) {
            if (statusFeeder != StatusRecord.AKTIF) {
                throw new IllegalStateException(
                        "statusFeeder tidak dapat diubah setelah AKTIF. " +
                                "Data sudah ter-sync ke Feeder.");
            }
            // Jika nilai sama (AKTIF), tidak perlu update
            return;
        }
        this.statusFeeder = statusFeeder;
    }

    /**
     * Cek apakah sudah di-sync ke Feeder
     */
    public boolean isSyncedToFeeder() {
        return this.statusFeeder == StatusRecord.AKTIF;
    }

}
