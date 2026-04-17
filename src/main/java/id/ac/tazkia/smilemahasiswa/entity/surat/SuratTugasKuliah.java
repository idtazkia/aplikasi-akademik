package id.ac.tazkia.smilemahasiswa.entity.surat;

import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "surat_tugas_kuliah")
@Data
public class SuratTugasKuliah {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @ManyToOne
    @JoinColumn(name = "id_jadwal")
    private Jadwal jadwal;

    private String namaPerusahaan;
    private String alamatPerusahaan;
    private String hal;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApproveKaprodi = StatusApprove.WAITING;

    @ManyToOne
    @JoinColumn(name = "user_approval_kaprodi")
    private Karyawan userApprovalKaprodi;

    private String komentarKaprodi;

    @Column(columnDefinition = "DATE")
    private LocalDate tanggalApproveKaprodi;

    private String fileSurat;

    @Column(columnDefinition = "DATE")
    private LocalDate tanggalPengajuan;

    @Column(columnDefinition = "DATE")
    private LocalDate tanggalCetak;
}
