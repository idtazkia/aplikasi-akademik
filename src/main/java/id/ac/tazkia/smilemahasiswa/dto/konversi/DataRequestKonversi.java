package id.ac.tazkia.smilemahasiswa.dto.konversi;

import id.ac.tazkia.smilemahasiswa.dto.kelas.DataKelasDto;
import lombok.Data;

import java.util.List;

@Data
public class DataRequestKonversi {
    private Integer total;
    private Integer totalNotFiltered;
    List<GetRequestKonversi> rows;
}
