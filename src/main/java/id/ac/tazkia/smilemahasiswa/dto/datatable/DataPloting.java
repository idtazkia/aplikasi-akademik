package id.ac.tazkia.smilemahasiswa.dto.datatable;

import id.ac.tazkia.smilemahasiswa.dto.schedule.ListPlotingDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class DataPloting {
    String draw;
    Integer recordsTotal;
    Integer recordsFiltered;
    List<ListPlotingDto> data;
}
