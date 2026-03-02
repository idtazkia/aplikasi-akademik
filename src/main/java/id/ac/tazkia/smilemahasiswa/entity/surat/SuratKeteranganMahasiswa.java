package id.ac.tazkia.smilemahasiswa.entity.surat;

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
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import lombok.Data;

@Entity
@Table(name = "surat_keterangan_mahasiswa")
@Data
public class SuratKeteranganMahasiswa {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @Enumerated(EnumType.STRING)
    private StatusRecord status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalDibuat;

    @Enumerated(EnumType.STRING)
    private StatusRecord statusApproval;

    @ManyToOne
    @JoinColumn(name = "user_approval")
    private Karyawan userApproval;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalApproval;

    private String keperluan;
}
