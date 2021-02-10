package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "krs_nilai_tugas")
public class KrsNilaiTugasMoodle {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_krs_detail")
    private KrsDetail krsDetail;

    @ManyToOne
    @JoinColumn(name = "id_bobot_tugas")
    private BobotTugas bobotTugas;

    private BigDecimal nilai;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private BigDecimal nilaiAkhir;

}
