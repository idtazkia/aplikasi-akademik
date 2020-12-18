package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class Fakultas {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_lembaga")
    private Lembaga idLembaga;

    @NotNull
    private String kodeFakultas;

    @NotNull
    private String namaFakultas;

    @NotNull
    private String namaFakultasEnglish;

    private String keterangan;

    @ManyToOne
    @JoinColumn(name = "id_dosen")
    private Dosen dosen;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;
}
