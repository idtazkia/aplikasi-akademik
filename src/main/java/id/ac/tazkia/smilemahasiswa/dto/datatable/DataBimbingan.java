package id.ac.tazkia.smilemahasiswa.dto.datatable;

import id.ac.tazkia.smilemahasiswa.dto.graduation.BimbinganDto;
import lombok.Data;

import java.util.List;

@Data
public class DataBimbingan {
    private Integer total;
    private Integer totalNotFiltered;
    private List<BimbinganDto> rows;
}
