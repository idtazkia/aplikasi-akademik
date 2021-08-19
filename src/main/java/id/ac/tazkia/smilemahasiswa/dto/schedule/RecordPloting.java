package id.ac.tazkia.smilemahasiswa.dto.schedule;

import lombok.Data;

import java.util.List;

@Data
public class RecordPloting {
    private Integer total;
    private Integer totalNotFiltered;
    List<ListPlotingDto> rows;
}
