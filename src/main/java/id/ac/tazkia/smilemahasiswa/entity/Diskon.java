package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.swing.text.html.HTML;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Entity
@Data
public class Diskon {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @ManyToOne
    @JoinColumn(name = "id_nilai_jenis_tagihan")
    private NilaiJenisTagihan nilaiJenisTagihan;

    @ManyToOne
    @JoinColumn(name = "id_jenis_diskon")
    private JenisDiskon jenisDiskon;

    @Min(1000)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
