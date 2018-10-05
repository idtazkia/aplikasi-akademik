package id.ac.tazkia.akademik.aplikasiakademik.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class MatakuliahKurikulum {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_kurikulum")
    private Kurikulum idkurikulum;

    @ManyToOne
    @JoinColumn(name = "id_matakuliah")
    private MataKuliah idMatakuliah;

    private Integer noUrut;

    private String wajib;

    private String responsi;

    private Integer semester;

    private String matakuliahKurikulumSemester;

    private Integer jumlahSks;

    private String syaratTugasAkhir;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
