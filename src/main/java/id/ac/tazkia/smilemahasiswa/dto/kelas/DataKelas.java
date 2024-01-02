package id.ac.tazkia.smilemahasiswa.dto.kelas;

import id.ac.tazkia.smilemahasiswa.dto.ploting.DataPlotingDto;
import lombok.Data;

import java.util.List;

@Data
public class DataKelas {
    private Integer total;
    private Integer totalNotFiltered;
    List<DataKelasDto> rows;
}
