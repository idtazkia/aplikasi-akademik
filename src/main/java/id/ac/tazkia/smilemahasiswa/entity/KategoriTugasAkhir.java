package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Data
public class KategoriTugasAkhir {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String nama;

    @ManyToOne
    @JoinColumn(name = "id_prodi")
    private Prodi prodi;

    @Enumerated(EnumType.STRING)
    private Jenis jenis;

    @Enumerated(EnumType.STRING)
    private JenisValidasi jenisValidasi;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
