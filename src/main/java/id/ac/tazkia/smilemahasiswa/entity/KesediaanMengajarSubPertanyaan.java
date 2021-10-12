package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
public class KesediaanMengajarSubPertanyaan {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_kesediaan_mengajar_pertanyaan")
    KesediaanMengajarPertanyaan kesediaanMengajarPertanyaan;

    private String pertanyaan;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;
}
