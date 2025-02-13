package id.ac.tazkia.smilemahasiswa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class FileService {
    public String uploadFile(MultipartFile file, String uploadFolder) throws Exception {
        String fileName = null;
        String namaAsli = file.getOriginalFilename();

        if (file != null || file.isEmpty()) {
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = uploadFolder;
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            file.transferTo(tujuan);
            fileName = idFile + "." + extension;
        }

        return fileName;
    }
    
    public ResponseEntity<byte[]> tampilkanFile(String fileName, String uploadFolder) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (fileName.toLowerCase().endsWith("jpeg") || fileName.toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (fileName.toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (fileName.toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else if (fileName.toLowerCase().endsWith("xlsx") || fileName.toLowerCase().endsWith("xls") || fileName.toLowerCase().endsWith("csv")) {
                headers.setContentType(new MediaType("application", "force-download"));
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(uploadFolder));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception err) {
            log.warn(err.getMessage(), err);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
