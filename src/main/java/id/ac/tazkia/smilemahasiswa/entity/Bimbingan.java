package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class Bimbingan {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    private String hari;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggal;

    @Column(columnDefinition = "DATETIME")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime tanggalInput = LocalDateTime.now();

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String keterangan;

    @Enumerated(EnumType.STRING)
    private StatusApprove statusApproval;

    @Enumerated(EnumType.STRING)
    private StatusRecord status;

    @ManyToOne
    @JoinColumn(name = "id_dosen")
    private Dosen dosen;

}
