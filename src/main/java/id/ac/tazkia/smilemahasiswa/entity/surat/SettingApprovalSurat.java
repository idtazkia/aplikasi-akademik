package id.ac.tazkia.smilemahasiswa.entity.surat;

import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import lombok.Data;

@Entity
@Table(name = "setting_approval_surat")
@Data
public class SettingApprovalSurat {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String namaSetting;

    @ManyToOne
    @JoinColumn(name = "id_karyawan_approver")
    private Karyawan karyawanApprover;

    private String jenisSurat;

    private StatusRecord status;

    private String userDibuat;

    private LocalDate tanggalDibuat;

    private String userUpdate;

    private LocalDate tanggalUpdate;
}
