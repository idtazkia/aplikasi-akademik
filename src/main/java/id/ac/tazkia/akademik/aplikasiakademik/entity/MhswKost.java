package id.ac.tazkia.akademik.aplikasiakademik.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Table(name = "tb_mhsw_kost")
@Entity
@Data
public class MhswKost {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idMhswKost;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_mhsw")
    private Mahasiswa idMhsw;

    @NotNull
    private String alamat;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_propinsi")
    private Provinsi idPropinsi;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_kokab")
    private KabupatenKota idKokab;

    @NotNull
    private String kodepos;

    @NotNull
    private String negara;

    @NotNull
    private String status ="1";

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
