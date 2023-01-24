package id.ac.tazkia.smilemahasiswa.dto.human;

import lombok.Data;

import java.util.List;

@Data
public class TableJabatan {
    private Integer total;
    private Integer totalNotFiltered;
    private List<DataJabatan> rows;
}
