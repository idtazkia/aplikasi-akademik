package id.ac.tazkia.smilemahasiswa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "jadwal_bobot_tugas")
public class BobotTugas {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_jadwal_tugas")
    private JadwalTugas jadwalTugas;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_jadwal")
    @JsonBackReference
    private Jadwal jadwal;

    private String namaTugas;
    private BigDecimal bobot;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JsonBackReference
    private StatusRecord status = StatusRecord.AKTIF;

    private String pertemuan;

    @Enumerated(EnumType.STRING)
    private KategoriTugas kategoriTugas;
}
