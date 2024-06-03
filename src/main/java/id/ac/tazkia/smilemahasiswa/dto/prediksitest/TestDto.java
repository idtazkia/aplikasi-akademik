package id.ac.tazkia.smilemahasiswa.dto.prediksitest;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class TestDto {
    private String id;
    private String nim;
    private String no;
    private String email;
    private Integer ujianKe;
    private String keterangan;
    private MultipartFile file;
    private BigDecimal nilaiListening;
    private BigDecimal nilaiStructure;
    private BigDecimal nilaiReading;
    private BigDecimal nilai;
}
