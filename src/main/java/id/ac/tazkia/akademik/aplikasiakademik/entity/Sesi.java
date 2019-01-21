package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Data @Table(name = "sesi_jadwal")
public class Sesi {
    @Id
    @GeneratedValue(generator = "uuid" )
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String nama;

    @Column(columnDefinition = "TIME")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamMasuk;

    @Column(columnDefinition = "TIME")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime jamKeluar;

}
