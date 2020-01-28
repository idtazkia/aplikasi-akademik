package id.ac.tazkia.smilemahasiswa.dto.graduation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TahunDto {
    private String id;
    private String kode;
    private String nama;
    private Integer jumlah;
}
