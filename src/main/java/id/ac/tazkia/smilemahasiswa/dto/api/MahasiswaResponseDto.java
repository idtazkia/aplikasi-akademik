package id.ac.tazkia.smilemahasiswa.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MahasiswaResponseDto {
    private String nim;
    private String name;
    private String programStudy;
    private Boolean graduated;
    private String email;
    private String faculty;
    private String angkatan;
    private String status;
}
