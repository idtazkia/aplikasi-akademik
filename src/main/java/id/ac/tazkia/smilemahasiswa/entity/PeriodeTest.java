package id.ac.tazkia.smilemahasiswa.entity;

import id.ac.tazkia.smilemahasiswa.constant.JenisTest;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Entity
public class PeriodeTest {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tanggalUjian;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime terakhirUpload;

    private Integer kuota;

    @NotNull
    @Enumerated(EnumType.STRING)
    private JenisTest jenisTest;

    @ManyToOne
    @JoinColumn(name = "id_ruangan")
    private Ruangan ruangan;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;
}
