package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;

@Data
@Entity
public class Ruangan {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_gedung")
    private Gedung gedung;

    @NotNull
    private String kodeRuangan;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_jenis_ruangan")
    private RuanganJenis jenisRuangan;

    @NotNull
    private String namaRuangan;

    @NotNull
    private String lantai;

    @NotNull
    private BigInteger kapasitas;

    private String keterangan;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private String ipMesin;
    private String port;

}