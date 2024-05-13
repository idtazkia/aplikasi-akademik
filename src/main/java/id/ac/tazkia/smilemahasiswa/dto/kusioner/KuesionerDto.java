package id.ac.tazkia.smilemahasiswa.dto.kusioner;

import id.ac.tazkia.smilemahasiswa.entity.Kuesioner;
import lombok.Data;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.ArrayList;
import java.util.List;

@Data
public class KuesionerDto {
    private String id;
    private Kuesioner kuesioner;

    private String pertanyaan;

    private String jenisPertanyaan;

    private String wajibIsi;

    private ArrayList<String> idPilihanGanda;
    private ArrayList<String> pilihanGanda;

    private Integer skalaLinearAwal;

    private Integer skalaLinearAkhir;

    private String opsiSkalaLinearAwal;

    private String opsiSkalaLinearAkhir;

    private Integer urutan;
}
