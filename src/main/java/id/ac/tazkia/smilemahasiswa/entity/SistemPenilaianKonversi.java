package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Where(clause = "status = 'AKTIF'")
public class SistemPenilaianKonversi {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_konfigurasi_konversi")
    private KonfigurasiKonversi konfigurasiKonversi;

    @NotNull
    private String nama;

    private Integer pointBawah;
    private Integer pointAtas;
    private String sks;
    @Enumerated(EnumType.STRING)
    private OpsiNilai opsiNilai;
    private Integer nilai;
    private String grade;
    private Integer order;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    public enum OpsiNilai {
        PLUS, GRADE
    }

}
