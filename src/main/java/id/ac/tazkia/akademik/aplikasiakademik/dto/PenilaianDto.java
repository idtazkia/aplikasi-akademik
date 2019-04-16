package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.KrsDetail;
import lombok.Data;

@Data
public class PenilaianDto {
    private Integer absensiMahasiswa;
    private KrsDetail krsDetail;
    private String id;

}
