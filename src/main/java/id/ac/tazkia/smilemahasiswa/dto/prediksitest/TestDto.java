package id.ac.tazkia.smilemahasiswa.dto.prediksitest;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class TestDto {
    private String id;
    private String nim;
    private String no;
    private String email;
    private String jenisTest;
    private Integer ujianKe;
    private String keterangan;
    private MultipartFile file;
    private Integer nilaiListening;
    private Integer nilaiStructure;
    private Integer nilaiReading;
    private Integer nilai;
}
