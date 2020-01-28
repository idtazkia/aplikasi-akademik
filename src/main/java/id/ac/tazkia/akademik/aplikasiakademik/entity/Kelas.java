package id.ac.tazkia.akademik.aplikasiakademik.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

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
//
//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(name = "kelas_mahasiswa",
//            joinColumns=@JoinColumn(name = "id_kelas"),
//            inverseJoinColumns = @JoinColumn(name = "id_mahasiswa"))
//    private Set<Mahasiswa> mahasiswas = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @ManyToOne
    @JoinColumn(name = "id_kurikulum")
    private Kurikulum kurikulum;



}
