package id.ac.tazkia.smilemahasiswa.dto.api.akunting;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;

@Data
public class SendPembayaranDto {
    private String codeTemplate;
    private String dateTransaction;
    private String description;
    private String institut;
    public String attachment;
    public String tags = "SMILE";
    public ArrayList<BigDecimal> amounts;
}
