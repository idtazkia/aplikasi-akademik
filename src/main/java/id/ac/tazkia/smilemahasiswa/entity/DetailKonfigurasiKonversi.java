package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Where(clause = "status = 'AKTIF'")
public class DetailKonfigurasiKonversi {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_konfigurasi_konversi")
    private KonfigurasiKonversi konfigurasiKonversi;

    @NotNull
    private String nama;

    private String level;

    private Integer nilai;

    @Enumerated(EnumType.STRING)
    private KonfigurasiKonversi.sistem kategori;

    private Integer nilaiPlus;

    private String nilaiGrade;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private Integer number;
    private Integer levelNumber;
}
