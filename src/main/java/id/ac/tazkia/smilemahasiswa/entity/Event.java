package id.ac.tazkia.smilemahasiswa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Data
public class Event {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_tahun_akademik")
    private TahunAkademik tahunAkademik;

    @NotEmpty(message = "Nama acara tidak boleh kosong.")
    @Size(min = 8, max = 255, message = "Nama acara minimal 8 karakter dan maksimak 255 karakter.")
    private String eventName;

    @NotEmpty(message = "Deskripsi acara tidak boleh kosong.")
    @Size(min = 20, message = "Deskripsi acara harus minimal 20 karakter.")
    @Column(columnDefinition = "LONGTEXT")
    private String eventDescription;

    @Column(columnDefinition = "LONGTEXT")
    private String file;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusEvent eventType;

    @URL(message = "Tautan acara harus berupa URL yang valid.")
    private String eventLink;

    @NotNull @Enumerated(EnumType.STRING)
    private StatusRecord status = StatusRecord.AKTIF;

    private String userInsert;
    private String userUpdate;
    private String userDelete;

    private LocalDateTime dateInsert;
    private LocalDateTime dateUpdate;
    private LocalDateTime dateDelete;

    @Column(columnDefinition = "DATETIME")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime scheduleStart;

    @Column(columnDefinition = "DATETIME")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime scheduleEnd;

    @Column(columnDefinition = "DATETIME")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime schedulePublish;


}
