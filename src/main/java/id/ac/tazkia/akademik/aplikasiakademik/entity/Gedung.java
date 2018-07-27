package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_gedung")
public class Gedung {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idGedung;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_Kampus")
    private Kampus idKampus;

    @Column(nullable = false)
    private String namaGedung;

    @Column(nullable = false)
    private String keterangan;

    @NotNull
    private String status;


    @NotNull @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglInsert;

    @NotNull @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tglEdit;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "user_insert")
    private User userInsert;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "user_edit ")
    private User userEdit;

    @NotNull
    private String na = "1";

}
