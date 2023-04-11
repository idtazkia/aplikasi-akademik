package id.ac.tazkia.smilemahasiswa.dto.ujian;

import lombok.Data;

import java.util.List;

@Data
public class TableUjian {
    private Integer total;
    private Integer totalNotFiltered;
    private List<ListTahfidz> rows;

}
