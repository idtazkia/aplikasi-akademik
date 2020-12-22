package id.ac.tazkia.smilemahasiswa.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
    @Entity
    @Getter
    @Setter
    public class Prodi {

        @Id
        @GeneratedValue(generator = "uuid" )
        @GenericGenerator(name = "uuid", strategy = "uuid2")
        private String id;

        @ManyToOne
        @JoinColumn(name = "id_jurusan")
        private Jurusan idJurusan;

        @ManyToOne
        @JoinColumn(name = "id_jenjang")
        private Jenjang idJenjang;

        private String kodeProdi;

        private String namaProdi;

        private String namaProdiEnglish;

        private String keterangan;

        private String jabatan;

        @ManyToOne
        @JoinColumn(name = "id_dosen")
        private Dosen dosen;

        @Enumerated(EnumType.STRING)
        private StatusRecord status = StatusRecord.AKTIF;

        private String noSk;

        @Column(columnDefinition = "DATE")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate tanggalSk;

//        @ManyToMany(fetch = FetchType.EAGER)
//        @JoinTable(name = "prodi_program",
//                joinColumns=@JoinColumn(name = "id_prodi"),
//                inverseJoinColumns = @JoinColumn(name = "id_program"))
//        private Set<Program> programs= new HashSet<>();

}
