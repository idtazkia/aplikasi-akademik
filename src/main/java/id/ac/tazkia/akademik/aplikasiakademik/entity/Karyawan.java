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

    @Column(name = "nama_karyawan")
    @NotNull
    private String namaKaryawan;

    @NotNull
    private String gelar;

    @Column(name = "jenis_kelamin")
    @NotNull
    private String jk;

    @OneToOne
    @JoinColumn(name = "id_user")
    private User idUser;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;


}
