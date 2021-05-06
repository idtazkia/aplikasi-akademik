package id.ac.tazkia.smilemahasiswa.dto.payment;

import id.ac.tazkia.smilemahasiswa.entity.JenisDocument;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

@Data
public class UploadBerkasDto {

    @NotNull
    private String tagihan;

    @Enumerated(EnumType.STRING)
    private JenisDocument jenisDocument1;

    @NotNull
    private String fileBerkas1;

    @Enumerated(EnumType.STRING)
    private JenisDocument jenisDocument2;

    @NotNull
    private String fileBerkas2;

    @Enumerated(EnumType.STRING)
    private JenisDocument jenisDocument3;

    @NotNull
    private String fileBerkas3;

    @Enumerated(EnumType.STRING)
    private JenisDocument jenisDocument4;

    @NotNull
    private String fileBerkas4;

    @Enumerated(EnumType.STRING)
    private JenisDocument jenisDocument5;

    private String fileBerkas5;

}
