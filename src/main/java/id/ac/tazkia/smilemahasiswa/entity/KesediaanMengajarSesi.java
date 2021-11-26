package id.ac.tazkia.smilemahasiswa.entity;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Data
public class KesediaanMengajarSesi {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_kesediaan_mengajar_dosen")
    private KesediaanMengajarDosen kesediaanDosen;

    @ManyToOne
    @JoinColumn(name = "id_hari")
    private Hari hari;

    private Boolean sesi1 = Boolean.FALSE;

    private Boolean sesi2 = Boolean.FALSE;

    private Boolean sesi3 = Boolean.FALSE;

    private Boolean sesi4 = Boolean.FALSE;

    private Boolean sesi5 = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}