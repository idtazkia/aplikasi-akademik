package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
public class SidangController {
    @Autowired
    private RuanganDao ruanganDao;

    @Value("${upload.sidang}")
    private String sidangFolder;

    @Autowired
    private HariDao hariDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private MahasiswaDao mahasiswaDao;

   @Autowired
   private SidangDao sidangDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private SeminarDao seminarDao;

    @ModelAttribute("dosen")
    public Iterable<Dosen> dosen() {
        return dosenDao.cariDosen(StatusRecord.HAPUS);
    }

    @ModelAttribute("ruangan")
    public Iterable<Ruangan> ruangan() {
        return ruanganDao.findByStatus(StatusRecord.AKTIF);
    }

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatus(StatusRecord.AKTIF);
    }

    @ModelAttribute("tahun")
    public Iterable<TahunAkademik> tahun() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }


//    Mahasiswa

    @GetMapping("/graduation/sidang/mahasiswa/pendaftaran")
    public String pendaftaranSidang(@RequestParam(name = "id", value = "id",required = false) Seminar seminar, Model model){

        if (seminar.getNilai().compareTo(new BigDecimal(70)) < 0){
            return "redirect:../../seminar/nilai?id="+seminar.getId();
        }else {
            model.addAttribute("seminar",seminar);
            return "graduation/sidang/mahasiswa/pendaftaran";
        }
    }

    @GetMapping("/graduation/sidang/mahasiswa/revisi")
    public void revisiSidang(@RequestParam(name = "id", value = "id",required = false) Sidang sidang, Model model){

        model.addAttribute("sidang",sidang);
    }

    @PostMapping("/graduation/sidang/mahasiswa/pendaftaran")
    public String saveSidang(@ModelAttribute @Valid Sidang sidang, MultipartFile ijazah, MultipartFile ktp, Authentication authentication,
                             MultipartFile kartu,MultipartFile plagiat, MultipartFile draft,MultipartFile pendaftaran) throws Exception{

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        if (!pendaftaran.isEmpty() || pendaftaran != null) {
            String namaFile = pendaftaran.getName();
            String jenisFile = pendaftaran.getContentType();
            String namaAsli = pendaftaran.getOriginalFilename();
            Long ukuran = pendaftaran.getSize();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = sidangFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            pendaftaran.transferTo(tujuan);


            sidang.setFilePendaftaran(idFile + "." + extension);

        }else {
            sidang.setFilePendaftaran(sidang.getFileBimbingan());
        }

        if (!kartu.isEmpty() || kartu != null) {
            String namaFile = kartu.getName();
            String jenisFile = kartu.getContentType();
            String namaAsli = kartu.getOriginalFilename();
            Long ukuran = kartu.getSize();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = sidangFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            kartu.transferTo(tujuan);


            sidang.setFileBimbingan(idFile + "." + extension);

        }else {
            sidang.setFileBimbingan(sidang.getFileBimbingan());
        }

        if (!ijazah.isEmpty() || ijazah != null) {
            String namaFile = ijazah.getName();
            String jenisFile = ijazah.getContentType();
            String namaAsli = ijazah.getOriginalFilename();
            Long ukuran = ijazah.getSize();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = sidangFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            ijazah.transferTo(tujuan);


            sidang.setFileIjazah(idFile + "." + extension);

        }else {
            sidang.setFileIjazah(sidang.getFileBimbingan());
        }

        if (!ktp.isEmpty() || ktp != null) {
            String namaFile = ktp.getName();
            String jenisFile = ktp.getContentType();
            String namaAsli = ktp.getOriginalFilename();
            Long ukuran = ktp.getSize();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = sidangFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            ktp.transferTo(tujuan);


            sidang.setFileKtp(idFile + "." + extension);

        }else {
            sidang.setFileKtp(sidang.getFileBimbingan());
        }

        if (!plagiat.isEmpty() || plagiat != null) {
            String namaFile = plagiat.getName();
            String jenisFile = plagiat.getContentType();
            String namaAsli = plagiat.getOriginalFilename();
            Long ukuran = plagiat.getSize();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = sidangFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            plagiat.transferTo(tujuan);


            sidang.setFileTurnitin(idFile + "." + extension);

        }else {
            sidang.setFileTurnitin(sidang.getFileBimbingan());
        }

        if (!draft.isEmpty() || draft != null) {
            String namaFile = draft.getName();
            String jenisFile = draft.getContentType();
            String namaAsli = draft.getOriginalFilename();
            Long ukuran = draft.getSize();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = sidangFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            draft.transferTo(tujuan);


            sidang.setFileSidang(idFile + "." + extension);

        }else {
            sidang.setFileSidang(sidang.getFileBimbingan());
        }
        sidang.setTanggalInput(LocalDate.now());
        sidang.setStatus(StatusRecord.AKTIF);
        sidang.setStatusSidang(StatusApprove.WAITING);
        sidang.setAkademik(StatusApprove.WAITING);
        sidang.setPublish(StatusRecord.NONAKTIF);
        sidang.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        sidangDao.save(sidang);

        return "redirect:list?id="+sidang.getSeminar().getId();

    }

    @GetMapping("/graduation/sidang/mahasiswa/list")
    public String waitingPageMahasiswa(Model model,@RequestParam(name = "id", value = "id", required = false) Seminar seminar, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("seminar", seminar);
        if (seminar.getStatusSempro().equals(StatusApprove.APPROVED)  && seminar.getPublish().equals("AKTIF") && seminar.getNilai().compareTo(new BigDecimal(70)) >= 0) {
            Sidang sidang = sidangDao.findBySeminarAndStatusAndStatusSidangAndAkademik(seminar,StatusRecord.AKTIF,StatusApprove.APPROVED,StatusApprove.APPROVED);
            if (sidang != null) {
                return "redirect:success?id=" + seminar.getId();
            } else {

                List<Sidang> sidangList = sidangDao.findBySeminarAndStatusNotInOrderByTanggalInputDesc(seminar,Arrays.asList(StatusRecord.HAPUS));
                Sidang waiting = sidangDao.findBySeminarAndStatus(seminar,StatusRecord.AKTIF);
//
                model.addAttribute("list", sidangList);
                model.addAttribute("waiting", waiting);
                return "graduation/sidang/mahasiswa/list";
            }
        } else {
            return "redirect:../../register";
        }
    }

//    Admin

    @GetMapping("/graduation/sidang/admin/list")
    public void listApproval(@RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) Prodi prodi, Pageable page, Model model){
        model.addAttribute("selectedTahun",tahunAkademik);
        model.addAttribute("selectedProdi",prodi);

        if (tahunAkademik != null){
            model.addAttribute("listSidang", sidangDao.findByTahunAkademikAndSeminarNoteMahasiswaIdProdiAndAkademikNotInAndStatus(tahunAkademik,prodi,Arrays.asList(StatusApprove.REJECTED),StatusRecord.AKTIF,page));
        }
    }

    @GetMapping("/graduation/sidang/admin/approval")
    public void approval(){}

    @GetMapping("/graduation/sidang/admin/view")
    public void viewSidang(){}

    @GetMapping("/graduation/sidang/admin/penjadwalan")
    public void penjadwalan(){}

//    Dosen

    @GetMapping("/graduation/sidang/dosen/list")
    public void listDosen(){}

//    file
    @GetMapping("/upload/{sidang}/sidang/")
    public ResponseEntity<byte[]> sidang(@PathVariable Sidang sidang, Model model) throws Exception {
        String lokasiFile = sidangFolder + File.separator + sidang.getSeminar().getNote().getMahasiswa().getNim()
                + File.separator + sidang.getFileSidang();

        try {
            HttpHeaders headers = new HttpHeaders();
            if (sidang.getFileSidang().toLowerCase().endsWith("jpeg") || sidang.getFileSidang().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (sidang.getFileSidang().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (sidang.getFileSidang().toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception err) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


        }
    }

        @GetMapping("/upload/{sidang}/pendaftaran/")
        public ResponseEntity<byte[]> pendaftaran(@PathVariable Sidang sidang, Model model) throws Exception {
            String lokasiFile = sidangFolder + File.separator + sidang.getSeminar().getNote().getMahasiswa().getNim()
                    + File.separator + sidang.getFilePendaftaran();

            try {
                HttpHeaders headers = new HttpHeaders();
                if (sidang.getFilePendaftaran().toLowerCase().endsWith("jpeg") || sidang.getFilePendaftaran().toLowerCase().endsWith("jpg")) {
                    headers.setContentType(MediaType.IMAGE_JPEG);
                } else if (sidang.getFilePendaftaran().toLowerCase().endsWith("png")) {
                    headers.setContentType(MediaType.IMAGE_PNG);
                } else if (sidang.getFilePendaftaran().toLowerCase().endsWith("pdf")) {
                    headers.setContentType(MediaType.APPLICATION_PDF);
                } else {
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                }
                byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
                return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
            } catch (Exception err) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


            }

        }

    @GetMapping("/upload/{sidang}/ijazah/")
    public ResponseEntity<byte[]> ijazah(@PathVariable Sidang sidang, Model model) throws Exception {
        String lokasiFile = sidangFolder + File.separator + sidang.getSeminar().getNote().getMahasiswa().getNim()
                + File.separator + sidang.getFileIjazah();

        try {
            HttpHeaders headers = new HttpHeaders();
            if (sidang.getFileIjazah().toLowerCase().endsWith("jpeg") || sidang.getFileIjazah().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (sidang.getFileIjazah().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (sidang.getFileIjazah().toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception err) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


        }

    }

    @GetMapping("/upload/{sidang}/ktp/")
    public ResponseEntity<byte[]> ktp(@PathVariable Sidang sidang, Model model) throws Exception {
        String lokasiFile = sidangFolder + File.separator + sidang.getSeminar().getNote().getMahasiswa().getNim()
                + File.separator + sidang.getFileKtp();

        try {
            HttpHeaders headers = new HttpHeaders();
            if (sidang.getFileKtp().toLowerCase().endsWith("jpeg") || sidang.getFileKtp().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (sidang.getFileKtp().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (sidang.getFileKtp().toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception err) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


        }

    }

    @GetMapping("/upload/{sidang}/bimbingan/")
    public ResponseEntity<byte[]> bimbingan(@PathVariable Sidang sidang, Model model) throws Exception {
        String lokasiFile = sidangFolder + File.separator + sidang.getSeminar().getNote().getMahasiswa().getNim()
                + File.separator + sidang.getFileBimbingan();

        try {
            HttpHeaders headers = new HttpHeaders();
            if (sidang.getFileBimbingan().toLowerCase().endsWith("jpeg") || sidang.getFileBimbingan().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (sidang.getFileBimbingan().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (sidang.getFileBimbingan().toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception err) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


        }

    }


    @GetMapping("/upload/{sidang}/turnitin/")
    public ResponseEntity<byte[]> turnitin(@PathVariable Sidang sidang, Model model) throws Exception {
        String lokasiFile = sidangFolder + File.separator + sidang.getSeminar().getNote().getMahasiswa().getNim()
                + File.separator + sidang.getFileTurnitin();

        try {
            HttpHeaders headers = new HttpHeaders();
            if (sidang.getFileTurnitin().toLowerCase().endsWith("jpeg") || sidang.getFileTurnitin().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (sidang.getFileTurnitin().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (sidang.getFileTurnitin().toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception err) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


        }

    }


}
