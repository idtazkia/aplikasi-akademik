package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
public class KesediaanMengajarJawaban {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_kesediaan_mengajar_sub_pertanyaan")
    KesediaanMengajarSubPertanyaan kesediaanMengajarSubPertanyaan;

    private String jawaban;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @ManyToOne
    @JoinColumn(name = "id_user")
    User user;
}
