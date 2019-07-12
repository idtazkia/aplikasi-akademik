package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Data
public class Prasyarat {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_matakuliah_kurikulum")
    private MatakuliahKurikulum matakuliahKurikulum;

    @ManyToOne
    @JoinColumn(name = "id_matakuliah")
    private Matakuliah matakuliah;

    @ManyToOne
    @JoinColumn(name = "id_matakuliah_kurikulum_pras")
    private MatakuliahKurikulum matakuliahKurikulumPras;

    @ManyToOne
    @JoinColumn(name = "id_matakuliah_pras")
    private Matakuliah matakuliahPras;

    private BigDecimal nilai;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
