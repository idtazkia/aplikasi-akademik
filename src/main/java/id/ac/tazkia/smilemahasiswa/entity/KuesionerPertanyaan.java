package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class KuesionerPertanyaan {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_kuesioner")
    private Kuesioner kuesioner;

    private String pertanyaan;

    private String jenisPertanyaan;

    private String wajibIsi;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private LocalDateTime createdDate;

    private Integer skalaLinearAwal;

    private Integer skalaLinearAkhir;

    private String opsiSkalaLinearAwal;

    private String opsiSkalaLinearAkhir;

    private Integer urutan;
}
