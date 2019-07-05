package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Diskon;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TagihanDto {
    private BigDecimal totalBayar;
    private BigDecimal totalDiskon;
    private BigDecimal totalTagihan;
    private BigDecimal total;
    private List<Diskon> diskons;
}
