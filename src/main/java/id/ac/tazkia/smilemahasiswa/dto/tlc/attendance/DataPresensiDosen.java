package id.ac.tazkia.smilemahasiswa.dto.tlc.attendance;

import id.ac.tazkia.smilemahasiswa.entity.PresensiDosenBahasa;
import lombok.Data;

import java.util.List;

@Data
public class DataPresensiDosen {
    private Integer total;
    private Integer totalNotFiltered;
    List<PresensiDosenBahasa> rows;
}
