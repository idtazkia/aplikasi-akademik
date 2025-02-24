package id.ac.tazkia.smilemahasiswa.dto.tlc.jadwal;

import java.util.List;
import lombok.Data;

@Data
public class DataMahasiswa {
    private Integer total;
    private Integer totalNotFiltered;
    List<JadwalMahasiswa> rows;
}
