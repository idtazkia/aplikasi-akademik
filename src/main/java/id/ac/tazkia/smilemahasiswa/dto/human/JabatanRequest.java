package id.ac.tazkia.smilemahasiswa.dto.human;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JabatanRequest {
    private String id;
    private String nama;
    private String status;
    private Integer minimal;
    private Integer maksimal;
}
