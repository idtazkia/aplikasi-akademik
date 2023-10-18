package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Entity
public class FileMbkm {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_krs_detail")
    private KrsDetail krsDetail;

    private String namaFile;

    private String fileMbkm;

    @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    @NotNull
    private LocalDateTime waktuInput = LocalDateTime.now();

}
