package id.ac.tazkia.smilemahasiswa.entity.surat;

import java.time.LocalDateTime;
import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;
import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.surat.HariKerja;

@Entity
@Table(name = "surat_magang")
@Data
public class SuratMagang {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @Enumerated(EnumType.STRING)
    private JenisMagang jenisMagang;

    @Enumerated(EnumType.STRING)
    private HariKerja hariKerja = HariKerja.SENIN_JUMAT;

    private Integer sksKonversi;

    private String namaPerusahaan;

    private String divisi;

    private String alamat;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulaiMagang;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalSelesaiMagang;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime tanggalDibuat;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @Enumerated(EnumType.STRING)
    private StatusRecord statusApproval = StatusRecord.WAITING;

    @ManyToOne
    @JoinColumn(name = "user_approval")
    private Dosen userApproval;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime tanggalApproval;


}