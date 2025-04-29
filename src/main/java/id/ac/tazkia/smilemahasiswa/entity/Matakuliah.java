package id.ac.tazkia.smilemahasiswa.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
public class Matakuliah {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_prodi")
    @JsonBackReference
    private Prodi idProdi;

    private String kodeMatakuliah;

    private  String namaMatakuliah;

    private  String namaMatakuliahEnglish;

    private String singkatan;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
