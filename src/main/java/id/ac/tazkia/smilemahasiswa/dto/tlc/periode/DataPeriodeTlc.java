package id.ac.tazkia.smilemahasiswa.dto.tlc.periode;

import lombok.Data;

import java.util.List;

@Data
public class DataPeriodeTlc {
    private Integer total;
    private Integer totalNotFiltered;
    List<PeriodeDto> rows;
}
