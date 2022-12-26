package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
public class JadwalUjian {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_jadwal")
    private Jadwal jadwal;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggal;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamMulai;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamSelesai;

    @ManyToOne
    @JoinColumn(name = "id_ruangan")
    private Ruangan ruangan;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;
}
