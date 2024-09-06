package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Data
public class Dokumen {

    @Id
    private String id;

    private String deskripsi;

    private String file;

    private String namaFile;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private StatusRecord status = StatusRecord.AKTIF ;

    private String userInsert;

    private String userUpdate;

    private String userDelete;

    private LocalDate tanggalInsert;

    private LocalDate tanggalUpdate;

    private LocalDate tanggalDelete;

}
