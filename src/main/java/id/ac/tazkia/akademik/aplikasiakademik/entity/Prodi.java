package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

    @Entity
    @Data
    public class Prodi {

        @Id
        @GeneratedValue(generator = "uuid" )
        @GenericGenerator(name = "uuid", strategy = "uuid2")
        private String id;

        @ManyToOne
        @JoinColumn(name = "id_jurusan")
        private Jurusan idJurusan;

        private String kodeProdi;

        private String namaProdi;

        private String keterangan;


        @Enumerated(EnumType.STRING)
        private StatusRecord status = StatusRecord.AKTIF;

}
