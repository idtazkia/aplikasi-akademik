package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "tb_lembaga")
@Entity
@Data
public class Lembaga {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idLembaga;


    @NotNull
    private String namaLembaga;

    @NotNull
    private String alamat;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "propinsi")
    private Provinsi provinsi;

    @ManyToOne
    @JoinColumn(name = "kokab")
    private KabupatenKota kokab;

    private String logo;

    private String status;

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
