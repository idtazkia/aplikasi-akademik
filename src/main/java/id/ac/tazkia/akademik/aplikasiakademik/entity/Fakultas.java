package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Table(name = "tb_fakultas")
@Entity
@Data
public class Fakultas {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idFakultas;


    @NotNull
    private String kodeFakultas;

    @NotNull
    private String namaFakultas;

    @NotNull
    private String pejabat;

    @NotNull
    private String na = "1";

    @NotNull
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
