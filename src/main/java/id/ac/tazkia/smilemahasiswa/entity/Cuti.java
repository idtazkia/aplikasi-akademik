package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
public class Cuti {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    private String keterangan;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private String semester;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulaiCuti;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalBerakhirCuti;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalPengajuaan;

    private String statusPengajuaan;

    private String userApproved;
}
