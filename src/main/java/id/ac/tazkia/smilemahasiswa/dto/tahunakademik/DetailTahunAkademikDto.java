package id.ac.tazkia.smilemahasiswa.dto.tahunakademik;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DetailTahunAkademikDto extends BaseTanggalDto {
    private String id;
    private String idTahunAkademik;
    private String idProdi;
    private String namaProdi;
    private String idProgram;
    private String namaProgram;
    private String angkatanMulai;
    private String angkatanSelesai;
    private Integer prioritas;
    private String keterangan;

}
