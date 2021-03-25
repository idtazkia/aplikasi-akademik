package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class MahasiswaBeasiswa {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @ManyToOne
    @JoinColumn(name = "id_beasiswa")
    private Beasiswa beasiswa;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private StatusRecord status = StatusRecord.AKTIF;

    @Column(nullable = false)
    private String userCreate;

    private LocalDateTime dateCreate;

    @Column(nullable = false)
    private String userUpdate;

    private LocalDateTime dateUpdate;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMulaiBerlaku;
}
