package id.ac.tazkia.akademik.aplikasiakademik.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NilaiDto {
    private String id;
    private BigDecimal nilai;
}
