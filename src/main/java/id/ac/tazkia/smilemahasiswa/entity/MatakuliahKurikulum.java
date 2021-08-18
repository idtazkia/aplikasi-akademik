package id.ac.tazkia.smilemahasiswa.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class MatakuliahKurikulum {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_kurikulum")
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
    private Konsentrasi konsentrasi;

    private String silabus;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "matakuliah_kurikulum_program",
            joinColumns=@JoinColumn(name = "id_matakuliah_kurikulum"),
            inverseJoinColumns = @JoinColumn(name = "id_program"))
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


}
