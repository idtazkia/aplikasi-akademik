package id.ac.tazkia.smilemahasiswa.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDokumenDao;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.MahasiswaDokumen;

@Service
public class MahasiswaDokumenService {
    @Autowired
    private MahasiswaDokumenDao mahasiswaDokumenDao;

    @Value("${upload.dokumen}")
    private String uploadDir;


    public MahasiswaDokumen save(Mahasiswa mahasiswa, String keterangan, MultipartFile file) throws IOException {
        // buat folder kalau belum ada
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // sanitize keterangan
        String safeKeterangan = keterangan.replaceAll("[^a-zA-Z0-9-_]", "_");

        // ambil ekstensi asli
        String ext = "";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        // nama file unik pakai UUID
        String fileName = safeKeterangan + "_" + UUID.randomUUID() + ext;

        // simpan file ke folder
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // simpan entity (hanya simpan nama file / path)
        MahasiswaDokumen dok = new MahasiswaDokumen();
        dok.setMahasiswa(mahasiswa);
        dok.setKeterangan(keterangan);
        dok.setFile(fileName); // hanya nama file UUID
        return mahasiswaDokumenDao.save(dok);
    }

    public void delete(MahasiswaDokumen dok) throws IOException {
        // hapus file fisik
        Path filePath = Paths.get(uploadDir).resolve(dok.getFile()).normalize();
        Files.deleteIfExists(filePath);

        mahasiswaDokumenDao.delete(dok);
    }


}
