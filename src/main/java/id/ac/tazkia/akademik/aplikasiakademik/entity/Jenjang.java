package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class Jenjang {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private String kodeJenjang;

    @NotNull
    private String namaJenjang;

    @NotNull
    private String keterangan;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;
}