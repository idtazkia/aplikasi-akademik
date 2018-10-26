package id.ac.tazkia.akademik.aplikasiakademik.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MahasiswaAttendance {

    private String id;
    private String matakuliah;
    private Integer masuk;
    private Integer mangkir;
    private Integer izin;

}
