package id.ac.tazkia.smilemahasiswa.dto.tlc.jadwal;

import lombok.Data;

import java.util.List;

@Data
public class DataJadwalBahasa {
    private Integer total;
    private Integer totalNotFiltered;
    List<JadwalRequest> rows;
}
