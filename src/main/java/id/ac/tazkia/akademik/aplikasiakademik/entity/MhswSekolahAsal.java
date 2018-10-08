package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Table(name = "tb_mhsw_sekolah_asal")
@Entity
@Data
public class MhswSekolahAsal {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String idMhswSekolahAsal;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_mhsw")
    private Mahasiswa idMhsw;

    @NotNull
    private String kodepos;



    @NotNull
    private String negara;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_sekolah")
    private Sekolah idSekolah;

    @NotNull
    private String alamat;



    @NotNull
    private String jurusanSekolah;

    @NotNull
    private String nilai;

    @NotNull
    private String tahunLulus;

    private String ijazah;

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
