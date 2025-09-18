package id.ac.tazkia.smilemahasiswa.dto.api.akunting;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SendTagihanDto {
    private String codeTemplate;
    private String dateTransaction;
    private String description;
    private String institut;
    private String type;
    private String tags;
    private BigDecimal amounts;
}
