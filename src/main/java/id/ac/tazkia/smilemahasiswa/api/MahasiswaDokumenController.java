package id.ac.tazkia.smilemahasiswa.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDokumenDao;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.MahasiswaDokumen;
import id.ac.tazkia.smilemahasiswa.service.MahasiswaDokumenService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;


@RestController
@RequestMapping("/api/mahasiswa-dokumen")
public class MahasiswaDokumenController {
    @Autowired
    private MahasiswaDokumenDao mahasiswaDokumenDao;

    @Autowired
    private MahasiswaDokumenService mahasiswaDokumenService;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Value("${upload.dokumen}")
    private String uploadDir;

    @PostMapping("/upload/{idMahasiswa}")
    public ResponseEntity<List<MahasiswaDokumen>> uploadDokumen(
            @PathVariable String idMahasiswa,
            @RequestParam("keterangan[]") List<String> keterangans,
            @RequestParam("file[]") List<MultipartFile> files) throws IOException {

        Mahasiswa mahasiswa = mahasiswaDao.findById(idMahasiswa)
                .orElseThrow(() -> new RuntimeException("Mahasiswa tidak ditemukan"));

        List<MahasiswaDokumen> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            result.add(mahasiswaDokumenService.save(mahasiswa, keterangans.get(i), files.get(i)));
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{idMahasiswa}")
    public ResponseEntity<List<MahasiswaDokumen>> getDokumenMahasiswa(@PathVariable String idMahasiswa) {
        Mahasiswa mahasiswa = mahasiswaDao.findById(idMahasiswa)
                .orElseThrow(() -> new RuntimeException("Mahasiswa tidak ditemukan"));
        List<MahasiswaDokumen> mahasiswaDokumen = mahasiswaDokumenDao.findByMahasiswa(mahasiswa);
        return ResponseEntity.ok(mahasiswaDokumen); // asumsi ada relasi OneToMany di Mahasiswa
    }

    

    @GetMapping("/show/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // otomatis deteksi tipe file
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteDokumen(@PathVariable String id) {
        MahasiswaDokumen dok = mahasiswaDokumenDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Dokumen tidak ditemukan"));

        try {
            mahasiswaDokumenService.delete(dok);
            return ResponseEntity.ok("Dokumen berhasil dihapus");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Gagal menghapus dokumen: " + e.getMessage());
        }
    }
}
