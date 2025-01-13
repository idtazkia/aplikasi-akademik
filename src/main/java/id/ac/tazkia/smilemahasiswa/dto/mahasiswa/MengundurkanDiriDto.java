package id.ac.tazkia.smilemahasiswa.dto.mahasiswa;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import java.time.LocalDate;

@Data
public class MengundurkanDiriDto {
    private String id;
    private Mahasiswa mahasiswa;
    private TahunAkademik tahunAkademik;
    @Column(columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalMengundurkanDiri;

}
