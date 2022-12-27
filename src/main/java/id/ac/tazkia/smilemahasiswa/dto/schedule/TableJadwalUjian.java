package id.ac.tazkia.smilemahasiswa.dto.schedule;

import lombok.Data;

import java.util.List;

@Data
public class TableJadwalUjian {
    private Integer total;
    private Integer totalNotFiltered;
    List<ListJadwalUjian> rows;
}
