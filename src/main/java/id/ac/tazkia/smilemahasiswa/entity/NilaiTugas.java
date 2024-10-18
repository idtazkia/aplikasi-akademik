package id.ac.tazkia.smilemahasiswa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "krs_nilai_tugas")
public class NilaiTugas {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_krs_detail")
    private KrsDetail krsDetail;

    @ManyToOne
    @JoinColumn(name = "id_bobot_tugas")
    private BobotTugas bobotTugas;

    private BigDecimal nilai;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JsonBackReference
    private StatusRecord status = StatusRecord.AKTIF;

    private BigDecimal nilaiAkhir;

    @Enumerated(EnumType.STRING)
    private KategoriTugas kategoriTugas;

}
