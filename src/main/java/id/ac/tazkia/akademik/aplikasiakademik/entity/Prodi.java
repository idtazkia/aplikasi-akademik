package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

    @Entity
    @Data
    @Table(name = "tb_prodi")
    public class Prodi {

        @Id
        @GeneratedValue(generator = "uuid" )
        @GenericGenerator(name = "uuid", strategy = "uuid2")
        private String idProdi;

        @ManyToOne
        @JoinColumn(name = "id_jurusan")
        private Jurusan idJurusan;

        @ManyToOne
        @JoinColumn(name = "id_jenjang")
        private Jenjang idJenjang;

        private String kodeProdi;

        private String namaProdi;

        private String pejabat;

        @NotNull
        private String na = "1";

        @NotNull
        private String status = "1";

        @Column(columnDefinition = "DATE")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDateTime tglInsert;

        @Column(columnDefinition = "DATE")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDateTime tglEdit;

        @ManyToOne
        @JoinColumn(name = "user_insert")
        private User userInsert;

        @ManyToOne
        @JoinColumn(name = "user_edit ")
        private User userEdit;

}
