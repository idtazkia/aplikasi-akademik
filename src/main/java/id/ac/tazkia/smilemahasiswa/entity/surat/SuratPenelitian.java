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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;
import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import lombok.Data;

@Entity
@Table(name = "surat_penelitian")
@Data
public class SuratPenelitian {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    private String namaPerusahaan;

    private String divisi;

    private String alamat;

    private String judulSkripsi;

    @NotBlank(message = "Nomor telepon tidak boleh kosong")
    @Pattern(regexp = "[0-9]{10,15}", message = "Nomor telepon hanya boleh berisi angka (10-15 digit)")
    private String nomorTelepon;

    @Enumerated(EnumType.STRING)
    private StatusRecord status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalDibuat;

    @Enumerated(EnumType.STRING)
    private StatusRecord statusApproval;

    @ManyToOne
    @JoinColumn(name = "user_approval")
    private Dosen userApproval;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalApproval;

}
