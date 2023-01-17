package id.ac.tazkia.smilemahasiswa.dto.schedule;

import id.ac.tazkia.smilemahasiswa.dto.ploting.DataPlotingDto;
import lombok.Data;

import java.util.List;

@Data
public class DataPloting {
    private Integer total;
    private Integer totalNotFiltered;
    List<DataPlotingDto> rows;
}
