package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.graduation.TahunDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
public class GraduationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraduationController.class);

    @Value("${upload.note}")
    private String uploadFolder;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private GradeDao gradeDao;

    @Value("classpath:sample/example.xlsx")
    private Resource example;

//    Attribute

    @ModelAttribute("dosen")
    public Iterable<Dosen> dosen() {
        return dosenDao.cariDosen(StatusRecord.HAPUS);
    }


    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatus(StatusRecord.AKTIF);
    }

    @ModelAttribute("tahun")
    public Iterable<TahunAkademik> tahun() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

//    Graduation Mahasiswa

    @GetMapping("/graduation/register")
    public String register(Model model, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        Note note = noteDao.findByMahasiswaAndStatus(mahasiswa, StatusApprove.WAITING);
        Note approve = noteDao.findByMahasiswaAndStatus(mahasiswa, StatusApprove.APPROVED);
        List<Note> empty = noteDao.findByMahasiswa(mahasiswa);

        if (empty == null || empty.isEmpty()) {

            return "graduation/register";

        } else {
            if (approve != null) {
                return "redirect:list";
            }

            if (approve == null && note == null) {
                return "redirect:list";
            }

            if (note != null) {
                return "redirect:list";
            }
        }
        return "graduation/register";
    }

    @GetMapping("/graduation/alert")
    public void alert(){}

    @GetMapping("/graduation/form")
    public String conceptNote(Model model, Authentication authentication, @RequestParam(required = false) String id) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        model.addAttribute("mahasiswa", mahasiswa);

        Note validasi = noteDao.cariKonsepNot(tahunAkademikDao.findByStatus(StatusRecord.AKTIF), mahasiswa, StatusApprove.REJECTED);

        List<BigDecimal> magang = krsDetailDao.nilaiMagang(StatusRecord.AKTIF, mahasiswa, "Magang");
        List<BigDecimal> metolit = krsDetailDao.nilaiMetolit(StatusRecord.AKTIF, mahasiswa, "METOLIT");

        System.out.println(metolit);

//


        if (id == null || id.isEmpty() || !StringUtils.hasText(id)) {
            if (magang != null || !magang.isEmpty()) {
                if (metolit == null || metolit.isEmpty()) {
                    LOGGER.info("nilai metolit kosong atau kurang dari 60");
                    return "redirect:alert";
                }

            }

            if (magang == null || magang.isEmpty()) {
                LOGGER.info("nilai magang kosong atau kurang dari 60");
                return "redirect:alert";
            }

            if (validasi != null) {
                return "redirect:register";
            }
            model.addAttribute("note", new Note());
        } else {
            if (magang != null || magang.isEmpty()) {
                for (BigDecimal nilaiMagang : magang) {
                    System.out.println(nilaiMagang);
                }

            }

            if (magang != null || !magang.isEmpty()) {
                if (metolit == null || metolit.isEmpty()) {
                    LOGGER.info("nilai metolit kosong atau kurang dari 60");
                    return "redirect:alert";
                }
                model.addAttribute("note", noteDao.findById(id).get());

            }

            if (magang == null || magang.isEmpty()) {
                LOGGER.info("nilai magang kosong atau kurang dari 60");
                return "redirect:alert";
            }

        }

        return "graduation/form";

    }

    @PostMapping("/graduation/form")
    public String uploadBukti(@Valid Note note,
                              BindingResult error, MultipartFile file,
                              Authentication currentUser) throws Exception {
        User user = currentUserService.currentUser(currentUser);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        if (!file.isEmpty() || file != null) {
            String namaFile = file.getName();
            String jenisFile = file.getContentType();
            String namaAsli = file.getOriginalFilename();
            Long ukuran = file.getSize();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = uploadFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            file.transferTo(tujuan);


            note.setMahasiswa(mahasiswa);
            note.setFileUpload(idFile + "." + extension);
            note.setTanggalInput(LocalDate.now());
            note.setStatus(StatusApprove.WAITING);
            note.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            noteDao.save(note);
        }else {
            note.setMahasiswa(mahasiswa);
            note.setFileUpload(note.getFileUpload());
            note.setTanggalInput(LocalDate.now());
            note.setStatus(StatusApprove.WAITING);
            note.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            noteDao.save(note);
        }

        return "redirect:list";

    }

    @GetMapping("/uploaded/{note}/bukti/")
    public ResponseEntity<byte[]> tampilkanEvidence(@PathVariable Note note, Model model) throws Exception {
        String lokasiFile = uploadFolder + File.separator + note.getMahasiswa().getNim()
                + File.separator + note.getFileUpload();
        LOGGER.debug("Lokasi file bukti : {}", lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (note.getFileUpload().toLowerCase().endsWith("jpeg") || note.getFileUpload().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (note.getFileUpload().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (note.getFileUpload().toLowerCase().endsWith("pdf")) {
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

    @GetMapping("/graduation/list")
    public String waitingPage(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        Note waiting = noteDao.findByMahasiswaAndStatus(mahasiswa,StatusApprove.WAITING);
        Note approved = noteDao.findByMahasiswaAndStatus(mahasiswa,StatusApprove.APPROVED);


        if (waiting != null){
            model.addAttribute("waiting",waiting);
        }

        if (approved != null){
            model.addAttribute("approved",approved);
        }

        List<Note> rejected = noteDao.findByMahasiswaOrderByTanggalInputDesc(mahasiswa);
        if (rejected == null || rejected.isEmpty()) {

            return "redirect:register";

        }
        model.addAttribute("mahasiswa" , mahasiswa);
        model.addAttribute("rejected" , rejected);

        return "graduation/list";

    }

//    Graduation Admin

    @GetMapping("/graduation/admin/list")
    public void list(Model model, @RequestParam(required = false) TahunAkademik tahun, Pageable pageable,
                     @RequestParam(required = false) Prodi prodi, @RequestParam(required = false) StatusApprove status){

        if (tahun != null){
            if(status != null) {
                model.addAttribute("selectedTahun",tahun);
                model.addAttribute("selectedProdi",prodi);
                model.addAttribute("status",status);
                model.addAttribute("listNote", noteDao.findByTahunAkademikAndMahasiswaIdProdiAndStatus(tahun,prodi,status,pageable));
            }else {
                model.addAttribute("selectedTahun",tahun);
                model.addAttribute("selectedProdi",prodi);
                model.addAttribute("listNote", noteDao.findByTahunAkademikAndMahasiswaIdProdiAndStatusNotIn(tahun,prodi, Arrays.asList(StatusApprove.HAPUS),pageable));
            }

        }

    }

    @GetMapping("/graduation/admin/form")
    public void formAdmin(Model model,@RequestParam(value = "id", name = "id") Note note){
        model.addAttribute("note", note);
    }

    @PostMapping("/graduation/admin/approve")
    public String Approve(Authentication authentication,
                          @RequestParam String idnote,
                          @ModelAttribute Note note,@RequestParam(required = false) String keterangan){
        User user = currentUserService.currentUser(authentication);

        Note note1 = noteDao.findById(idnote).get();
        note1.setDosen(note.getDosen());
        note1.setDosen2(note.getDosen2());
        note1.setStatus(StatusApprove.APPROVED);
        note1.setKeterangan(keterangan);
        note1.setTanggalApprove(LocalDate.now());
        note1.setUserApprove(user);
        noteDao.save(note1);

        return "redirect:list?tahun="+note1.getTahunAkademik().getId()+"&prodi="+note1.getMahasiswa().getIdProdi().getId();
    }

    @PostMapping("/graduation/admin/reject")
    public String Reject(Authentication authentication,@RequestParam String id,@RequestParam String keterangan){
        User user = currentUserService.currentUser(authentication);
        Note note = noteDao.findById(id).get();
        note.setStatus(StatusApprove.REJECTED);
        note.setKeterangan(keterangan);
        note.setTanggalReject(LocalDate.now());
        note.setUserReject(user);
        noteDao.save(note);

        return "redirect:list?tahun="+note.getTahunAkademik().getId()+"&prodi="+note.getMahasiswa().getIdProdi().getId();
    }

    @GetMapping("/graduation/admin/score")
    public void uploadScore(@RequestParam(required = false)String list){}

    @PostMapping("/graduation/admin/score")
    public String prosesFormUploadUTS(MultipartFile file, RedirectAttributes attributes) throws Exception {

        LOGGER.debug("Nama file : {}", file.getOriginalFilename());
        LOGGER.debug("Ukuran file : {} bytes", file.getSize());

        List<TahunDto> tahunDtos = new ArrayList<>();
        List<Mahasiswa> mahasiswas = new ArrayList<>();
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheetPertama = workbook.getSheetAt(0);

            int row = 5;
            int terakhir = sheetPertama.getLastRowNum() - row;
            System.out.println("jumlah row  :  " +terakhir);

            for (int i = 0; i <= terakhir; i++) {
                System.out.println(i);
                Row baris = sheetPertama.getRow(row + i);

                if (baris.getCell(1) != null) {
                    Cell nim = baris.getCell(1);
                    nim.setCellType(CellType.STRING);

                    Cell nilai = baris.getCell(6);
                    nilai.setCellType(CellType.NUMERIC);

                    String mahasiswa = mahasiswaDao.cariIdMahasiswa(nim.getStringCellValue());
                    System.out.println("nim  :  " + nim + "  nilai  " + nilai);


                    String krsDetail = krsDetailDao.idKrsDetail(mahasiswa, StatusRecord.AKTIF, "Magang", tahunAkademikDao.findByStatus(StatusRecord.AKTIF));

                    if (krsDetail == null || baris.getCell(1) != null) {
                        System.out.println("nim  :  " + nim + "  tidak memiliki krs magang");
                        Mahasiswa m = mahasiswaDao.findByNim(nim.getStringCellValue());
                        mahasiswas.add(m);
                    }

                    if (krsDetail != null) {
                        System.out.println("nim  :  " + nim + "  Krs Detail  " + krsDetail + "   nilai  :  " + nilai);
                        KrsDetail kd = krsDetailDao.findById(krsDetail).get();
                        kd.setNilaiAkhir(new BigDecimal(nilai.getNumericCellValue()));
                        krsDetailDao.save(kd);
                        TahunDto tahunDto = new TahunDto();
                        tahunDto.setId(kd.getId());
                        tahunDto.setNama(kd.getMahasiswa().getNama());
                        tahunDto.setKode(kd.getMahasiswa().getNim());
                        tahunDto.setJumlah(kd.getNilaiAkhir().intValue());
                        tahunDtos.add(tahunDto);
                    }


                }else {
                    System.out.println("kosong");
                }

            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        attributes.addFlashAttribute("list", tahunDtos);
        attributes.addFlashAttribute("mahasiswa", mahasiswas);
        return "redirect:/graduation/admin/score?list=true";

    }

    @GetMapping("/graduation/admin/inputscore")
    public void scoreInput(@RequestParam(required = false)String nim,@RequestParam(required = false)BigDecimal nilai,Model model){
        model.addAttribute("nim" , nim);
    }

    @PostMapping("/graduation/admin/inputscore")
    public String prosesScoreInput(@RequestParam(required = false)String nim,@RequestParam(required = false)BigDecimal nilai,
                                   RedirectAttributes attributes){

        Grade a = gradeDao.findById("1").get();
        Grade amin= gradeDao.findById("2").get();
        Grade bplus= gradeDao.findById("3").get();
        Grade b = gradeDao.findById("4").get();
        Grade bmin = gradeDao.findById("5").get();
        Grade cplus= gradeDao.findById("6").get();
        Grade c = gradeDao.findById("7").get();
        Grade d = gradeDao.findById("8").get();
        Grade e = gradeDao.findById("9").get();

        String krsDetail = krsDetailDao.idKrsDetail(mahasiswaDao.findByNim(nim).getId(), StatusRecord.AKTIF, "Magang", tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        if (!krsDetail.trim().isEmpty() && krsDetail != null){
            KrsDetail kd = krsDetailDao.findById(krsDetail).get();
            kd.setNilaiAkhir(nilai);

            if (kd.getNilaiAkhir().toBigInteger().intValue() >= 80 && kd.getNilaiAkhir().toBigInteger().intValue() < 85){
                System.out.println("a-");
                kd.setGrade(amin.getNama());
                kd.setBobot(a.getBobot());
            }

            if (kd.getNilaiAkhir().toBigInteger().intValue() >= 75 && kd.getNilaiAkhir().toBigInteger().intValue() < 80){
                System.out.println("b+");
                kd.setGrade(bplus.getNama());
                kd.setBobot(bplus.getBobot());

            }

            if (kd.getNilaiAkhir().toBigInteger().intValue() >= 70 && kd.getNilaiAkhir().toBigInteger().intValue() < 75){
                System.out.println("b");
                kd.setGrade(b.getNama());
                kd.setBobot(b.getBobot());
            }

            if (kd.getNilaiAkhir().toBigInteger().intValue() >= 65 && kd.getNilaiAkhir().toBigInteger().intValue() < 70){
                System.out.println("b-");
                kd.setGrade(bmin.getNama());
                kd.setBobot(bmin.getBobot());
            }

            if (kd.getNilaiAkhir().toBigInteger().intValue() >= 60 && kd.getNilaiAkhir().toBigInteger().intValue() < 65){
                System.out.println("c+");
                kd.setGrade(cplus.getNama());
                kd.setBobot(cplus.getBobot());
            }

            if (kd.getNilaiAkhir().toBigInteger().intValue() >= 55 && kd.getNilaiAkhir().toBigInteger().intValue() < 60){
                System.out.println("c");
                kd.setGrade(c.getNama());
                kd.setBobot(c.getBobot());
            }

            if (kd.getNilaiAkhir().toBigInteger().intValue() >= 50 && kd.getNilaiAkhir().toBigInteger().intValue() < 55){
                System.out.println("d");
                kd.setGrade(d.getNama());
                kd.setBobot(d.getBobot());
            }

            if (kd.getNilaiAkhir().toBigInteger().intValue() >= 0 && kd.getNilaiAkhir().toBigInteger().intValue() < 50){
                System.out.println("e");
                kd.setGrade(e.getNama());
                kd.setBobot(e.getBobot());
            }

            if (kd.getNilaiAkhir().toBigInteger().intValue() >= 85){
                System.out.println("a");
                kd.setGrade(a.getNama());
                kd.setBobot(a.getBobot());
            }
            krsDetailDao.save(kd);
            attributes.addFlashAttribute("success", "success");
            return "redirect:/graduation/admin/inputscore?nim="+nim;

        }else {
            attributes.addFlashAttribute("unsuccess", "unsuccess");
            return "redirect:/graduation/admin/inputscore?nim="+nim;
        }


    }

    @GetMapping("/sample/uploadNilai")
    public void downloadContohFileTagihan(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Example-File.xlsx");
        FileCopyUtils.copy(example.getInputStream(), response.getOutputStream());
        response.getOutputStream().flush();
    }
}
