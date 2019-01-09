package id.ac.tazkia.akademik.aplikasiakademik.entity;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class SesiKuliah {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jadwal")
    private Jadwal jadwal;

    @Column(columnDefinition = "LONGTEXT") @Lob
    private String beritaAcara;

    @NotNull private LocalDateTime waktuMulai = LocalDateTime.now();
    @NotNull private LocalDateTime waktuSelesai = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_presensi_dosen")
    @NotNull
    private PresensiDosen presensiDosen;
}
