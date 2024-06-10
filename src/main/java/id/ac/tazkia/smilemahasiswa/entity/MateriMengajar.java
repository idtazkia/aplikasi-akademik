package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
public class MateriMengajar {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_sesi_kuliah")
    private SesiKuliah sesiKuliah;

    private String namaFile;

    private String file;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
