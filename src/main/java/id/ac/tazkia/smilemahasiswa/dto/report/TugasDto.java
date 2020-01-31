package id.ac.tazkia.smilemahasiswa.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TugasDto {
    private String id;
    private String nama;
    private BigDecimal nilai;
}
