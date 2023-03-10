package id.ac.tazkia.smilemahasiswa.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

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
    private LocalDate tanggalPengajuan;

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
}
