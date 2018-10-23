package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Entity
public class Gedung {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_Kampus")
    private Kampus kampus;

    @NotNull
    private String kodeGedung;

    @NotNull
    private String namaGedung;

    @NotNull
    private String keterangan;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
