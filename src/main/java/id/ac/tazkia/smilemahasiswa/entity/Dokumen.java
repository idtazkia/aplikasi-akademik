package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Dokumen {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotEmpty(message = "Deskripsi tidak boleh kosong")
    @Size(min = 8,  message = "Deskripsi minimal 8 karakter.")
    private String deskripsi;

    private String file;

    private String namaFile;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private StatusRecord status = StatusRecord.AKTIF ;

    private String userInsert;

    private String userUpdate;

    private String userDelete;

    private LocalDateTime tanggalInsert;

    private LocalDateTime tanggalUpdate;

    private LocalDateTime tanggalDelete;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "dokumen_prodi",
            joinColumns = @JoinColumn(name = "id_dokumen"),
            inverseJoinColumns = @JoinColumn(name = "id_prodi")
    )
    private Set<Prodi> prodis = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "dokumen_angkatan",
            joinColumns = @JoinColumn(name = "id_dokumen"),
            inverseJoinColumns = @JoinColumn(name = "angkatan")
    )
    private Set<Angkatan> angkatans = new HashSet<>();

}
