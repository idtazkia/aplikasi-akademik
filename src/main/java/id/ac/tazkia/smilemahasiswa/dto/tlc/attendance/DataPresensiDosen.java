package id.ac.tazkia.smilemahasiswa.dto.tlc.attendance;

import java.util.List;
import lombok.Data;

@Data
public class DataPresensiDosen {
    private Integer total;
    private Integer totalNotFiltered;
    List<GetDataPresensi> rows;
}
