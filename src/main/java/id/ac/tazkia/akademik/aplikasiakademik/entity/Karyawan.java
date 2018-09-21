package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class Karyawan {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private String nik;

    @NotNull
    private String nama_karyawan;

    @NotNull
    private String gelar;

    @NotNull
    private String jk;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User idUser;

    private String status = "1";


}
