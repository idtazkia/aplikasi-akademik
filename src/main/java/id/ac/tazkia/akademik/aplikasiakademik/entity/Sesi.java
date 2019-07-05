package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;

@Data @Entity
public class Sesi {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NotNull
    private String sesi;

    @NotNull
    private String namaSesi;

    @ManyToOne
    @JoinColumn(name = "id_jenjang")
    private Jenjang jenjang;

    private Integer sks;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamMulai;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamSelesai;
}
