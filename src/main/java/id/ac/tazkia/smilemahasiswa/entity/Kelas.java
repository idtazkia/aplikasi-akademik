package id.ac.tazkia.smilemahasiswa.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class Kelas {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String kodeKelas;

    private String namaKelas;

    private String keterangan;

    @ManyToOne
    @JoinColumn(name = "id_prodi")
    private Prodi idProdi;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @ManyToOne
    @JoinColumn(name = "id_kurikulum")
    private Kurikulum kurikulum;

    private String bahasa;

    @Enumerated(EnumType.STRING)
    private StatusRecord konsentrasi;

    private String angkatan;



}
