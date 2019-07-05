package id.ac.tazkia.akademik.aplikasiakademik.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Entity
@Table(name = "histori_rencana_detail")
public class HistoriRencana {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne
    @JoinColumn(name = "id_rencana_detail")
    private RencanaDetail rencanaDetail;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    @Min(0)
    private BigDecimal amount;

    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate waktuDiubah;
}
