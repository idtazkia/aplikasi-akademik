package id.ac.tazkia.smilemahasiswa.entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import id.ac.tazkia.smilemahasiswa.constant.MagangStatus;
import lombok.Data;

@Entity
@Data
public class FileMagang {
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

    private String fileMagang;

    private String filePenilaian;

    @Column(columnDefinition = "TEXT")
    private String komentar;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalUpload = LocalDate.now();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition =  "varchar")
    private MagangStatus jenis;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusApprove status = StatusApprove.WAITING;
}
