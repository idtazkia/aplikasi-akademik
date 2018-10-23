package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Entity
public class Konsentrasi {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_prodi")
    private Prodi idProdi;

    @NotNull
    private String kodeKonsentrasi;

    @NotNull
    private String namaKonsentrasi;

    private String keterangan;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;


}
