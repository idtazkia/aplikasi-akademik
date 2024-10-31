package id.ac.tazkia.smilemahasiswa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;

@Entity
@Data
public class DetailJadwalBahasa {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_jadwal_bahasa")
    @JsonBackReference
    private JadwalBahasa jadwalBahasa;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamMulai;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime jamSelesai;

    @ManyToOne
    @JoinColumn(name = "id_ruangan")
    private Ruangan ruangan;

    @ManyToOne
    @JoinColumn(name = "id_hari")
    private Hari hari;

    private String sesi;

}
