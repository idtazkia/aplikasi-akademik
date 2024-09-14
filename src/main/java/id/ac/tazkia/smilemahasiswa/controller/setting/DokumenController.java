package id.ac.tazkia.smilemahasiswa.controller.setting;

import id.ac.tazkia.smilemahasiswa.dao.AngkatanDao;
import id.ac.tazkia.smilemahasiswa.dao.DokumenDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Controller
public class DokumenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dokumen.class);


    @Autowired
    private DokumenDao dokumenDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private AngkatanDao angkatanDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;


    @Autowired
    @Value("${upload.dokumen}")
    private String uploadFolder;

    @GetMapping("/dokumen")
    public String listDokumen(Model model,
                              @RequestParam(required = false) String search,
                              @PageableDefault(size = 10) Pageable pageable) {

        if(StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listDokumen", dokumenDao.findByStatusAndDeskripsiContainingIgnoreCaseOrStatusAndNamaFileContainingIgnoreCaseOrderByStartDateAsc(StatusRecord.AKTIF,search, StatusRecord.AKTIF, search, pageable));
        }else {
            model.addAttribute("listDokumen", dokumenDao.findByStatusOrderByStartDateAsc(StatusRecord.AKTIF, pageable));
        }
        model.addAttribute("statusmenu","active");
        return "dokumen/list";

    }


    @GetMapping("/dokumen/baru")
    public String newDokumen(Model model) {


        model.addAttribute("dokumen", new Dokumen());
        model.addAttribute("listProdi", prodiDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("listAngkatan", angkatanDao.findAll());
        model.addAttribute("statusmenu","active");
        return "dokumen/form";

    }

    @GetMapping("/dokumen/edit")
    public String editDokumen(Model model,@RequestParam(required = false) Dokumen dokumen) {


        model.addAttribute("dokumen", dokumen);
        model.addAttribute("listProdi", prodiDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("listAngkatan", angkatanDao.findAll());
        model.addAttribute("statusmenu","active");
        return "dokumen/form";

    }

    @GetMapping("/dokumen/mahasiswa")
    public String dokumenMahasiswa(Model model,
                                   Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        model.addAttribute("listDokumen",dokumenDao.findByStatusAndStartDateLessThanEqualAndAngkatansAndProdisAndEndDateGreaterThanEqualOrderByTanggalInsertDesc(StatusRecord.AKTIF,LocalDate.now(),angkatanDao.findByAngkatan(mahasiswa.getAngkatan()),mahasiswa.getIdProdi(),LocalDate.now()));

        return "dokumen/mahasiswa";
    }



    @PostMapping("/dokumen/save")
    public String saveDokumen(@ModelAttribute @Valid Dokumen dokumen,
                              BindingResult error,
                              Model model,
                              @RequestParam("fileUpload") MultipartFile file,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) throws IOException {

        if (error.hasErrors()) {
            model.addAttribute("dokumen", dokumen);
            model.addAttribute("listProdi", prodiDao.findByStatus(StatusRecord.AKTIF));
            model.addAttribute("listAngkatan", angkatanDao.findAll());
            model.addAttribute("statusmenu", "active");
            return "dokumen/form"; // Mengembalikan ke halaman form jika ada error
        }

        User user = currentUserService.currentUser(authentication);


        if(file.isEmpty()){
            model.addAttribute("dokumen", dokumen);
            model.addAttribute("listProdi", prodiDao.findByStatus(StatusRecord.AKTIF));
            model.addAttribute("listAngkatan", angkatanDao.findAll());
            model.addAttribute("statusmenu","active");
            model.addAttribute("erorfile", "File tidak boleh kosong");
            return "dokumen/form";
        }

        String namaFile =  file.getName();
        String jenisFile = file.getContentType();
        String namaAsli = file.getOriginalFilename();
        Long ukuran = file.getSize();
        String extension = "";
        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));
        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }
        String idFile = UUID.randomUUID().toString();
        if (ukuran <= 0){

            System.out.println("file :"+ namaAsli);
            System.out.println("Ukuran :"+ ukuran);
            if(dokumen.getFile() == null){
                dokumen.setFile("default.jpg");
            }
        }else{
            dokumen.setFile(idFile + "." + extension);
            dokumen.setNamaFile(file.getOriginalFilename());
            System.out.println("file :"+ namaAsli);
            System.out.println("Ukuran :"+ ukuran);
            LOGGER.debug("Lokasi upload : {}", uploadFolder);
            new File(uploadFolder).mkdirs();
            File tujuan = new File(uploadFolder + File.separator + idFile + "." + extension);
            file.transferTo(tujuan);
        }
        dokumen.setStatus(StatusRecord.AKTIF);
        dokumen.setUserUpdate(user.getUsername());
        dokumen.setTanggalUpdate(LocalDateTime.now());
        dokumenDao.save(dokumen);


        redirectAttributes.addFlashAttribute("success","Dokumen berhasil disimpan......");
        return "redirect:../dokumen";
    }


    @GetMapping("/dokumen/akses")
    public String aksesDokumen(Model model,@RequestParam Dokumen dokumen) {

        model.addAttribute("dokumen", dokumen);
        model.addAttribute("listProdi", prodiDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("listAngkatan", angkatanDao.findAll());

        return "dokumen/akses";
    }



    @GetMapping("/dokumen/{dokumen}/download")
    public ResponseEntity<byte[]> tampilkanDokumen(@PathVariable Dokumen dokumen) throws Exception {
        String lokasiFile = uploadFolder + File.separator + dokumen.getFile();
        LOGGER.debug("Lokasi file bukti : {}", lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (dokumen.getNamaFile().toLowerCase().endsWith("jpeg") || dokumen.getNamaFile().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (dokumen.getNamaFile().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (dokumen.getNamaFile().toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception err) {
            LOGGER.warn(err.getMessage(), err);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
