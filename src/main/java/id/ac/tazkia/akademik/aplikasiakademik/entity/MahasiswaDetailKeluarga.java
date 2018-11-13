package id.ac.tazkia.akademik.aplikasiakademik.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity @Data
public class MahasiswaDetailKeluarga {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_mahasiswa")
    private  Mahasiswa mahasiswa;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_ayah")
    private  Ayah ayah;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_ibu")
    private  Ibu ibu;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_wali")
    private  Wali wali ;
}
