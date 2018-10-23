package id.ac.tazkia.akademik.aplikasiakademik.entity;


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

}
