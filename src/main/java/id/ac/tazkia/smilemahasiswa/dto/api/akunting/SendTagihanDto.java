package id.ac.tazkia.smilemahasiswa.dto.api.akunting;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SendTagihanDto {
    private String codeTemplate;
    private String dateTransaction;
    private String description;
    private String institut = "bed6a06c-afe9-4345-8b3f-ebe2cbb49a51";
    private String type;
    private BigDecimal amounts;
}
