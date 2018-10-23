package id.ac.tazkia.akademik.aplikasiakademik.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
public class RuanganJenis {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    @Column(name = "jenis_ruangan")
    private String jenisRuangan;

    private String keterangan;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;



}
