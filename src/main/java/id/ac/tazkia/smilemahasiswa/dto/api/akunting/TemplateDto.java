package id.ac.tazkia.smilemahasiswa.dto.api.akunting;

import lombok.Data;

import java.util.List;

@Data
public class TemplateDto {
    private String responseCode;
    private String responseMessage;
    private List<TemplateJurnalDto> data;
}
