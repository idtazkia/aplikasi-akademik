package id.ac.tazkia.smilemahasiswa.entity;

import com.sun.tracing.dtrace.ModuleName;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Data
public class KategoriDetail {

    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_kategori")
    private KategoriTugasAkhir kategori;

    @ManyToOne
    @JoinColumn(name = "id_dosen")
    private Dosen dosen;

    private Integer nomorUrut;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
