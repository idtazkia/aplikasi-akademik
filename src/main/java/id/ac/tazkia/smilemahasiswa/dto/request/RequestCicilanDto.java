package id.ac.tazkia.smilemahasiswa.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RequestCicilanDto {
    private Integer urutan;
    private String nomorTagihan;
    private String keterangan;
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalKirim;
    private Boolean status;
    private BigDecimal nominal;
}
