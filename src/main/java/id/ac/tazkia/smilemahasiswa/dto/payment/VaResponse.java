package id.ac.tazkia.smilemahasiswa.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VaResponse {
    private String accountNumber;
    private String invoiceNumber;
    private String name;
    private BigDecimal amount;
    private String bankId;
}
