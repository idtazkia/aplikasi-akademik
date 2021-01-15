package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.zip.CheckedOutputStream;

@Entity
@Data
@Table(name = "tagihan")
public class Tagihan {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String nomor;

    @ManyToOne @JoinColumn(name = "id_nilai_jenis_tagihan")
    private NilaiJenisTagihan nilaiJenisTagihan;

    @ManyToOne @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    private String keterangan;

    @Min(1000)
    private BigDecimal nilaiTagihan;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalPembuatan;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalJatuhTempo;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalPenangguhan;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    @ManyToOne
    @JoinColumn(name = "id_karyawan")
    private Karyawan karyawan;

    @NotNull
    @Column(columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private Boolean lunas = false;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private String idTagihanSebelumnya;

}
