package id.ac.tazkia.smilemahasiswa.api;

import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dto.api.MahasiswaResponseDto;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mahasiswa")
public class MahasiswaRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MahasiswaRestController.class);

    @Autowired
    private MahasiswaDao mahasiswaDao;

    /**
     * Get mahasiswa data by NIM
     * 
     * @param nim NIM mahasiswa
     * @return MahasiswaResponseDto containing student information
     */
    @GetMapping(value = "/{nim}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MahasiswaResponseDto> getMahasiswaByNim(@PathVariable String nim) {
        
        LOGGER.info("API request: Get mahasiswa by NIM: {}", nim);

        // Find mahasiswa by NIM
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

        if (mahasiswa == null) {
            LOGGER.warn("Mahasiswa with NIM {} not found", nim);
            return ResponseEntity.notFound().build();
        }

        // Check if mahasiswa record is active
        if (mahasiswa.getStatus() != StatusRecord.AKTIF) {
            LOGGER.warn("Mahasiswa with NIM {} has inactive status", nim);
            return ResponseEntity.notFound().build();
        }

        // Build response DTO
        MahasiswaResponseDto response = new MahasiswaResponseDto();
        response.setNim(mahasiswa.getNim());
        response.setName(mahasiswa.getNama());
        response.setId(mahasiswa.getId()); // Add ID to response

        // Program Study
        if (mahasiswa.getIdProdi() != null) {
            response.setProgramStudy(mahasiswa.getIdProdi().getNamaProdi());

            // Faculty
            if (mahasiswa.getIdProdi().getFakultas() != null) {
                response.setFaculty(mahasiswa.getIdProdi().getFakultas().getNamaFakultas());
            } else {
                response.setFaculty("-");
            }
        } else {
            response.setProgramStudy("-");
            response.setFaculty("-");
        }

        // Check if graduated (has graduation date)
        boolean isGraduated = mahasiswa.getTanggalLulus() != null;
        response.setGraduated(isGraduated);

        // Email
        String email = mahasiswa.getEmailTazkia();
        if (email == null || email.isEmpty()) {
            email = mahasiswa.getEmailPribadi();
        }
        response.setEmail(email != null ? email : "-");

        // Angkatan
        response.setAngkatan(mahasiswa.getAngkatan());

        // Status - determine based on various fields
        String status = determineStatus(mahasiswa);
        response.setStatus(status);

        LOGGER.info("Successfully retrieved mahasiswa data for NIM: {}", nim);

        return ResponseEntity.ok(response);
    }

    /**
     * Determine student status based on multiple factors
     * 
     * @param mahasiswa Mahasiswa entity
     * @return Status string
     */
    private String determineStatus(Mahasiswa mahasiswa) {
        // If graduated
        if (mahasiswa.getTanggalLulus() != null) {
            return "LULUS";
        }

        // Check statusAktif field
        String statusAktif = mahasiswa.getStatusAktif();
        if (statusAktif != null) {
            switch (statusAktif.toUpperCase()) {
                case "AKTIF":
                    return "AKTIF";
                case "NONAKTIF":
                case "NON AKTIF":
                    return "NONAKTIF";
                case "CUTI":
                    return "CUTI";
                case "KELUAR":
                case "MENGUNDURKAN DIRI":
                    return "KELUAR";
                case "DO":
                case "DROP OUT":
                    return "DROP OUT";
                default:
                    return statusAktif.toUpperCase();
            }
        }

        // Default to AKTIF if no specific status
        return "AKTIF";
    }
}
