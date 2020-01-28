package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Data
public class Note {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @NotNull
    private String judul;

    @NotNull
    private String judulInggris;

    @ManyToOne
    @JoinColumn(name = "id_dosen1")
    private Dosen dosen;

    @ManyToOne
    @JoinColumn(name = "id_dosen2")
    private Dosen dosen2;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalInput;

    @Enumerated(EnumType.STRING)
    private StatusApprove status;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    private String fileUpload;

    private String keterangan;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalApprove;

    @ManyToOne
    @JoinColumn(name = "user_approve")
    private User userApprove;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalReject;

    @ManyToOne
    @JoinColumn(name = "user_reject")
    private User userReject;

    @Enumerated(EnumType.STRING)
    private StatusRecord jenis;



}
