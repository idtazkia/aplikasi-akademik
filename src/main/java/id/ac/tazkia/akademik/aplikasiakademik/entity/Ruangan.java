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
@Table(name = "tb_ruangan")
public class Ruangan {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idRuangan;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_gedung")
    private Gedung idGedung;

    @NotNull
    private String namaRuangan;

    @NotNull
    private BigInteger kapasitas;

    @NotNull
    private String keterangan;

    @NotNull
    private String na = "1";

    @NotNull
    private String status = "1";

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglInsert;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglEdit;

    @ManyToOne
    @JoinColumn(name = "user_insert")
    private User userInsert;

    @ManyToOne
    @JoinColumn(name = "user_edit ")
    private User userEdit;

}