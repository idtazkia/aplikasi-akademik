package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class Wisuda {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_mahasiswa")
    private Mahasiswa mahasiswa;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_sidang")
    private Sidang sidang;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "id_periode")
    private PeriodeWisuda periodeWisuda;

    private String foto;
    private String ukuran;

    @Enumerated(EnumType.STRING)
    private StatusApprove status;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalUpload = LocalDate.now();

}
