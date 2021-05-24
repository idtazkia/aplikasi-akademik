package id.ac.tazkia.smilemahasiswa.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlotingDto {
    private String id;
    private String kodeMatakuliah;
    private String matakuliah;
    private String course;
    private String kelas;
    private Integer sks;
    private String dosen;
    private String idDosen;
    private String idKelas;
}
