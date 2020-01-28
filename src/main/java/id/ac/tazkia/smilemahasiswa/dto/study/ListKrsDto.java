package id.ac.tazkia.smilemahasiswa.dto.study;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListKrsDto {
    private String id;
    private String hari;
    private LocalTime jamMulai;
    private LocalTime jamSelesai;
    private String hariEnglish;
    private String matkul;
}
