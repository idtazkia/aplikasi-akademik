package id.ac.tazkia.akademik.aplikasiakademik.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity @Data
public class MahasiswaDetailAkademik {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_prodi")
    private  Prodi idProdi;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_konsentrasi")
    private  Konsentrasi idKonsentrasi;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_dosen_wali")
    private  Dosen dosen ;

    @NotNull
    private String statusMatrikulasi;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_program")
    private  Program idProgram;


}
