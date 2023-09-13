package id.ac.tazkia.smilemahasiswa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.ManyToAny;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
public class PendaftaranMbkm {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "id_dosen_wali")
    private Dosen dosen;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalDaftar = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApproval = StatusApprove.WAITING;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

}
