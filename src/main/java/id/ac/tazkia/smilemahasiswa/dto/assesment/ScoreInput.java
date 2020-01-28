package id.ac.tazkia.smilemahasiswa.dto.assesment;

import lombok.Data;

@Data
public class ScoreInput {
    private String krs;
    private String tugas;
    private String nilai;
    private String uts;
    private String uas;
    private String sds;
    private String absen;
}
