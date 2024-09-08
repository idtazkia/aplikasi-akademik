package id.ac.tazkia.smilemahasiswa.controller.setting;

import id.ac.tazkia.smilemahasiswa.dao.EventAbsensiDao;
import id.ac.tazkia.smilemahasiswa.dao.EventDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class EventController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Event.class);


    @Autowired
    private EventDao eventDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private EventAbsensiDao eventAbsensiDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    @Value("${upload.event}")
    private String uploadFolder;

    @Autowired
    @Value("${upload.event.absensi}")
    private String uploadFolderAbsensi;

    @GetMapping("/academic/event")
    public String akademikEvent(Model model,
                                @PageableDefault(size = 10)Pageable pageable){

        model.addAttribute("listEvent", eventDao.findByStatusOrderByScheduleStartDesc(StatusRecord.AKTIF,pageable));

        return "event/list";
    }

    @GetMapping("/academic/event/detail")
    public String akademikEvent(Model model,
                                @RequestParam(required = true)Event event,
                                @PageableDefault(size = 10)Pageable pageable){

        model.addAttribute("event",event);
        model.addAttribute("listEventAbsensi",eventAbsensiDao.findByStatusAndEventOrderByWaktuAbsenAsc(StatusRecord.AKTIF, event, pageable));

        return "event/detail";
    }


    @GetMapping("/academic/event/baru")
    public String akademikEventBaru(Model model){

        model.addAttribute("event", new Event());
        model.addAttribute("statusEvent", StatusEvent.values());
        model.addAttribute("listTahunAkademik", tahunAkademikDao.findAllByOrderByStatusAscKodeTahunAkademikDesc());

        return "event/form";
    }

    @GetMapping("/academic/event/edit")
    public String akademikEventEdit(@RequestParam(required = true)Event event,
                                    Model model){

        model.addAttribute("event", event);
        model.addAttribute("statusEvent", StatusEvent.values());
        model.addAttribute("listTahunAkademik", tahunAkademikDao.findAllByOrderByStatusAscKodeTahunAkademikDesc());

        return "event/form";
    }

    @PostMapping("/academic/event/save")
    public String akademikEventSave(@ModelAttribute @Valid Event event,
                                    BindingResult error,
                                    RedirectAttributes redirectAttributes,
                                    Authentication authentication,
                                    @RequestParam("fileUpload") MultipartFile file,
                                    Model model) throws IOException {

        User user = currentUserService.currentUser(authentication);


        System.out.println("start : "+ event.getScheduleStart());
        if(error.hasErrors()){
            model.addAttribute("event", event);
            model.addAttribute("statusEvent", StatusEvent.values());
            model.addAttribute("listTahunAkademik", tahunAkademikDao.findAllByOrderByStatusAscKodeTahunAkademikDesc());
            return "event/form";
        }

        // Validasi untuk file upload
        if (file.isEmpty()) {
            model.addAttribute("error", "File upload tidak boleh kosong");
            model.addAttribute("event", event);
            model.addAttribute("statusEvent", StatusEvent.values());
            model.addAttribute("listTahunAkademik", tahunAkademikDao.findAllByOrderByStatusAscKodeTahunAkademikDesc());
            return "event/form";
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
            if(event.getFile() == null){
                event.setFile("default.jpg");
            }
        }else{
            event.setFile(idFile + "." + extension);
            System.out.println("file :"+ namaAsli);
            System.out.println("Ukuran :"+ ukuran);
            LOGGER.debug("Lokasi upload : {}", uploadFolder);
            new File(uploadFolder).mkdirs();
            File tujuan = new File(uploadFolder + File.separator + idFile + "." + extension);
            file.transferTo(tujuan);
        }
        event.setStatus(StatusRecord.AKTIF);
        event.setUserInsert(user.getUsername());
        event.setUserUpdate(user.getUsername());
        event.setDateInsert(LocalDateTime.now());
        event.setDateUpdate(LocalDateTime.now());
        eventDao.save(event);


        redirectAttributes.addFlashAttribute("success","Setting event berhasil.....");
        return "redirect:../event";
    }

    @PostMapping("/academic/event/hapus")
    public String akademikEventHapus(@RequestParam (required = true) Event event,
                                    RedirectAttributes redirectAttributes,
                                    Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        System.out.println("event : "+ event);
        event.setStatus(StatusRecord.HAPUS);
        event.setUserDelete(user.getUsername());
        event.setDateDelete(LocalDateTime.now());
        eventDao.save(event);

        redirectAttributes.addFlashAttribute("success","Hapus event berhasil.....");
        return "redirect:../event";
    }

    @GetMapping("/event/{event}/download")
    public ResponseEntity<byte[]> tampilkanEvent(@PathVariable Event event) throws Exception {
        String lokasiFile = uploadFolder + File.separator + event.getFile();
        LOGGER.debug("Lokasi file bukti : {}", lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (event.getFile().toLowerCase().endsWith("jpeg") || event.getFile().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (event.getFile().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
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


    @GetMapping("/academic/event/presensi")
    public String absensiEvent(Model model,
                               @RequestParam(required = true) Event event,
                               Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        model.addAttribute("event", event);
        EventAbsensi eventAbsensi = eventAbsensiDao.findByStatusAndEventAndMahasiswa(StatusRecord.AKTIF,event,mahasiswa);
        System.out.println("eventAbsensi : "+ eventAbsensi);
        if(eventAbsensi == null){
            model.addAttribute("mode", "save");
            model.addAttribute("eventAbsensi", new EventAbsensi());
            return "event/absen";
        }else{
            model.addAttribute("mode", "update");
            model.addAttribute("eventAbsensi", eventAbsensi);
            return "event/absen";
        }


    }

    @PostMapping("/academic/event/presensi/save")
    public String eventAbsensiSave(@ModelAttribute @Valid EventAbsensi eventAbsensi,
                                   BindingResult error, // Pindahkan BindingResult langsung setelah @Valid
                                   @RequestParam Event event,
                                   @RequestParam String mode,
                                   @RequestParam("fileUpload") MultipartFile file,
                                   RedirectAttributes redirectAttributes,
                                   Authentication authentication,
                                   Model model) throws IOException {

        // Mendapatkan user yang sedang login
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        // Cek apakah ada error validasi
        if (error.hasErrors()) {
            model.addAttribute("event", event);
            model.addAttribute("eventAbsensi", eventAbsensi);
            return "event/absen"; // Kembali ke halaman form jika ada error
        }

        // Validasi untuk file upload
        if (file.isEmpty()) {
            model.addAttribute("error", "File upload tidak boleh kosong");
            model.addAttribute("event", event);
            model.addAttribute("eventAbsensi", eventAbsensi);
            return "event/absen";
        }

        String namaFile = file.getOriginalFilename();
        String jenisFile = file.getContentType();
        Long ukuran = file.getSize();
        String extension = "";
        String idFile = UUID.randomUUID().toString();

        int i = namaFile.lastIndexOf('.');
        if (i > 0) {
            extension = namaFile.substring(i + 1);
        }

        // Cek apakah file kosong
        if (ukuran <= 0) {
            if (event.getFile() == null) {
                event.setFile("default.jpg");
            }
            return "redirect:/dashboard";
        }

        // Cek ukuran file
        if (ukuran > 500 * 1024) { // Lebih dari 500KB
            // Kompres file gambar
            File compressedFile = new File(uploadFolderAbsensi + File.separator + idFile + "." + extension);
            Thumbnails.of(file.getInputStream())
                    .size(1024, 1024) // Atur ukuran maksimum (sesuaikan sesuai kebutuhan)
                    .outputQuality(0.8) // Sesuaikan kualitas output (0.0 sampai 1.0)
                    .toFile(compressedFile);
            eventAbsensi.setFile(idFile + "." + extension);

            System.out.println("File dikompres: " + namaFile);
            System.out.println("Ukuran asli: " + ukuran + " bytes");
            System.out.println("Ukuran setelah kompresi: " + compressedFile.length() + " bytes");
        } else {
            // Simpan file tanpa kompresi jika ukurannya <= 500KB
            File tujuan = new File(uploadFolderAbsensi + File.separator + idFile + "." + extension);
            file.transferTo(tujuan);
            eventAbsensi.setFile(idFile + "." + extension);
        }
        eventAbsensi.setMahasiswa(mahasiswa);
        eventAbsensi.setEvent(event);
        eventAbsensi.setStatus(StatusRecord.AKTIF);
        eventAbsensi.setWaktuAbsen(LocalDateTime.now());
        if(mode.equals("save")){
            eventAbsensi.setDateInsert(LocalDateTime.now());
        }
        if(mode.equals("update")){
            eventAbsensi.setDateUpdate(LocalDateTime.now());
        }
        eventAbsensiDao.save(eventAbsensi);

        redirectAttributes.addFlashAttribute("sukses","Absensi event sukses..");
        return "redirect:/dashboard";
    }






    @GetMapping("/attendance/{eventAbsensi}/download")
    public ResponseEntity<byte[]> tampilkanEventAbsensi(@PathVariable EventAbsensi eventAbsensi) throws Exception {
        String lokasiFile = uploadFolderAbsensi + File.separator + eventAbsensi.getFile();
        LOGGER.debug("Lokasi file bukti : {}", lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (eventAbsensi.getFile().toLowerCase().endsWith("jpeg") || eventAbsensi.getFile().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (eventAbsensi.getFile().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
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
