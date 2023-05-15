package id.ac.tazkia.smilemahasiswa.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class UjianTahfidzKompre {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    private String jenisUjian;

    private Integer ujianKe;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private String statusApprovalAdmin;
    private String statusApprovalKps;

    private String fileSurat;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime tanggalPengajuan;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalUjian;

    @ManyToOne
    @JoinColumn(name = "id_ruangan")
    private Ruangan ruangan;

    private String penguji;

    @ManyToOne
    @JoinColumn(name = "approval_kps")
    private Dosen dosenKps;


    private String emailAdmin;

    private Integer urutan;

    private String komentarAdmin;

    private String komentarKps;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    @ManyToOne
    @JoinColumn(name = "id_prodi")
    private Prodi prodi;


    @Enumerated(EnumType.STRING)
    private StatusRecord statusLulus;

    private BigDecimal nilai;

    private String grade;

}
