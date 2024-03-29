package id.ac.tazkia.smilemahasiswa.controller;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.graduation.SeminarDto;
import id.ac.tazkia.smilemahasiswa.dto.graduation.SemproDto;
import id.ac.tazkia.smilemahasiswa.dto.graduation.TahunDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.ScoreService;
import id.ac.tazkia.smilemahasiswa.service.SidangService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class GraduationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraduationController.class);

    @Value("${upload.note}")
    private String uploadFolder;

    @Value("${upload.seminar}")
    private String seminarFolder;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    WisudaDao wisudaDao;

    @Autowired
    PeriodeWisudaDao periodeWisudaDao;

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
    private TahunProdiDao tahunProdiDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private SeminarDao seminarDao;

    @Autowired
    private JenjangDao jenjangDao;

    @Autowired private ScoreService scoreService;

    @Autowired
    private RuanganDao ruanganDao;

    @Autowired
    private HariDao hariDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private EnableFitureDao enableFitureDao;

    @Autowired
    private SidangDao sidangDao;

    @Autowired
    private TagihanDao tagihanDao;

    @Autowired
    private SidangService sidangService;

    @Value("classpath:sample/example.xlsx")
    private Resource example;

    @Value("classpath:sample/sempro1.odt")
    private Resource formulirSempro1;

//    Attribute

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

    //    API
    @GetMapping("/api/seminar")
    @ResponseBody
    public Object[] validasiSeminar(@RequestParam Ruangan ruangan, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tanggal,
                                    @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime jamMulai,
                                    @RequestParam @DateTimeFormat(pattern = "HH:mm:ss")LocalTime jamSelesai){
        if (tanggal.getDayOfWeek().getValue() == 7){
            Hari hari = hariDao.findById("0").get();
            TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            return seminarDao.validasiJadwalSeminar(ta,hari,ruangan,jamMulai,jamSelesai,tanggal,1);
        }else {
            Hari hari = hariDao.findById(String.valueOf(tanggal.getDayOfWeek().getValue())).get();
            TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            return seminarDao.validasiJadwalSeminar(tahunAkademik,hari,ruangan,jamMulai,jamSelesai,tanggal, 1);

        }
    }

//    Graduation Mahasiswa
    @GetMapping("/graduation/finance")
    public void alertFinance() {

    }

    @GetMapping("/graduation/register")
    public String register(Model model, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        KrsDetail krsDetail = krsDetailDao.cariNilaiThesis(mahasiswa);




            List<Note> empty = noteDao.findByMahasiswaAndStatusNotIn(mahasiswa, Arrays.asList(StatusApprove.HAPUS));

            if (empty == null || empty.isEmpty()) {
                if (krsDetail != null) {
                    List<Wisuda> wisuda = wisudaDao.findByMahasiswa(mahasiswa);
                    if (wisuda.isEmpty()) {
                        return "redirect:wisuda/form";
                    }else {
                        return "redirect:sidang/mahasiswa/valid?id=" + mahasiswa.getId();
                    }
                }else {
                    return "graduation/register";
                }
            } else {
                List<Object> cekSidang = sidangDao.cekSidang(mahasiswa);
                if (cekSidang == null || cekSidang.isEmpty()) {
                    List<Object> seminar = seminarDao.cekSeminar(mahasiswa);
                    if (seminar == null || seminar.isEmpty()) {
                        return "redirect:list";
                    } else {
                        Note approve = noteDao.findByMahasiswaAndStatus(mahasiswa, StatusApprove.APPROVED);

                        return "redirect:seminar/waiting?id=" + approve.getId();

                    }
                } else {
                    List<Wisuda> wisuda = wisudaDao.findByMahasiswaAndStatusNotIn(mahasiswa, Arrays.asList(StatusApprove.HAPUS));
                    if (wisuda.isEmpty()) {
                        Seminar seminar = seminarDao.findByStatusAndPublishAndNilaiGreaterThanAndNoteMahasiswa(StatusApprove.APPROVED, "AKTIF", new BigDecimal(70), mahasiswa);
                        return "redirect:sidang/mahasiswa/list?id=" + seminar.getId();
                    } else {
                        return "redirect:sidang/mahasiswa/valid?id=" + mahasiswa.getId();

                    }
                }
            }
    }

    @GetMapping("/graduation/alert")
    public void alert(@RequestParam(required = false) String matkul,Model model){
        model.addAttribute("matkul", matkul);
    }

    @GetMapping("/graduation/form")
    public String conceptNote(Model model, Authentication authentication, @RequestParam(required = false) String id) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);

        Object[] metolit = krsDetailDao.validasiMetolit(mahasiswa);
        Object[] magang = krsDetailDao.validasiMagang(mahasiswa);
        model.addAttribute("note", new Note());

        if (mahasiswa.getIdProdi().getIdJenjang().getKodeJenjang().equals("S1")){
            if (id == null || id.isEmpty() || !StringUtils.hasText(id)) {
                System.out.println(magang.length);
                System.out.println(metolit.length);
                if (magang.length == 0){
                    return "redirect:alert?matkul=Magang";
                }else {
                    if (metolit.length == 0) {
                        return "redirect:alert?matkul=Metolit";
                    } else {
                        model.addAttribute("note", new Note());
                    return "graduation/form";
                    }
                }
            } else {

                model.addAttribute("note", noteDao.findById(id).get());

                return "graduation/form";

            }
        }

        if (mahasiswa.getIdProdi().getIdJenjang().getKodeJenjang().equals("S2")){
            if (id == null || id.isEmpty() || !StringUtils.hasText(id)) {
                if (metolit.length == 0){
                    return "redirect:alert?matkul=Metolit";
                }else {

                    model.addAttribute("note", new Note());
                    return "graduation/form";

                }
            } else {

                model.addAttribute("note", noteDao.findById(id).get());

                return "graduation/form";

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
            TahunAkademik ta = null;
            TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            if (tahunAkademik.getJenis() == StatusRecord.PENDEK){
                String kode = tahunAkademik.getKodeTahunAkademik().substring(0,4) + "2";
                ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),kode );
            }else {
                ta = tahunAkademik;
            }
            note.setTahunAkademik(ta);
            noteDao.save(note);
        }else {
            note.setMahasiswa(mahasiswa);
            note.setFileUpload(note.getFileUpload());
            note.setTanggalInput(LocalDate.now());
            note.setStatus(StatusApprove.WAITING);
            TahunAkademik ta = null;
            TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            if (tahunAkademik.getJenis() == StatusRecord.PENDEK){
                String kode = tahunAkademik.getKodeTahunAkademik().substring(0,4) + "2";
                ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),kode );
            }else {
                ta = tahunAkademik;
            }
            note.setTahunAkademik(ta);
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

    @GetMapping("/sempro/{seminar}/bukti/")
    public ResponseEntity<byte[]> tampilkanEvidence(@PathVariable Seminar seminar, Model model) throws Exception {
        String lokasiFile = seminarFolder + File.separator + seminar.getNote().getMahasiswa().getNim()
                + File.separator + seminar.getFileSkripsi();
        LOGGER.debug("Lokasi file bukti : {}", lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (seminar.getFileSkripsi().toLowerCase().endsWith("jpeg") || seminar.getFilePengesahan().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (seminar.getFileSkripsi().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (seminar.getFileSkripsi().toLowerCase().endsWith("pdf")) {
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
            return "redirect:success?note="+approved.getId();
        }

        List<Note> rejected = noteDao.findByMahasiswaOrderByTanggalInputDesc(mahasiswa);
        if (rejected == null || rejected.isEmpty()) {

            return "redirect:register";

        }
        model.addAttribute("mahasiswa" , mahasiswa);
        model.addAttribute("rejected" , rejected);

        return "graduation/list";

    }

    @GetMapping("/graduation/success")
    public String success(Model model, @RequestParam Note note){
        List<Seminar> seminar = seminarDao.findByNote(note);
        model.addAttribute("note", note);
        EnableFiture enableFiture= enableFitureDao.findByMahasiswaAndFiturAndEnable(note.getMahasiswa(),StatusRecord.SEMPRO,true);
        if (enableFiture != null) {
            model.addAttribute("sempro", enableFiture);
        }else {
            if (tagihanDao.cekTagihanLunas(note.getMahasiswa().getNim()).isEmpty()){
                EnableFiture fiture = new EnableFiture();
                fiture.setEnable(true);
                fiture.setFitur(StatusRecord.SEMPRO);
                fiture.setKeterangan("-");
                fiture.setMahasiswa(note.getMahasiswa());
                fiture.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
                enableFitureDao.save(fiture);
                model.addAttribute("sempro", fiture);
                return "graduation/success";

            }

        }

        if (!seminar.isEmpty()){
            return "redirect:seminar/waiting?id="+note.getId();
        }else {
            return "graduation/success";
        }
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

            for (int i = 0; i <= terakhir; i++) {
                Row baris = sheetPertama.getRow(row + i);

                if (baris.getCell(1) != null) {
                    Cell nim = baris.getCell(1);
                    nim.setCellType(CellType.STRING);

                    Cell nilai = baris.getCell(6);
                    nilai.setCellType(CellType.NUMERIC);

                    String mahasiswa = mahasiswaDao.cariIdMahasiswa(nim.getStringCellValue());

                    String krsDetail = krsDetailDao.idKrsDetail(mahasiswa, StatusRecord.AKTIF, "Magang");

                    if (krsDetail == null || baris.getCell(1) != null) {
                        System.out.println("nim  :  " + nim + "  tidak memiliki krs magang");
                        Mahasiswa m = mahasiswaDao.findByNim(nim.getStringCellValue());
                        mahasiswas.add(m);
                    }

                    if (krsDetail != null) {
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


        String krsDetail = krsDetailDao.idKrsDetail(mahasiswaDao.findByNim(nim).getId(), StatusRecord.AKTIF, "Magang");
        if (StringUtils.hasText(krsDetail)){
            KrsDetail kd = krsDetailDao.findById(krsDetail).get();
            kd.setNilaiAkhir(nilai);

            scoreService.hitungNilaiAkhir(kd);
            attributes.addFlashAttribute("success", "success");

        }else {
            attributes.addFlashAttribute("unsuccess", "unsuccess");
        }
        return "redirect:/graduation/admin/inputscore?nim="+nim;


    }

    @GetMapping("/sample/uploadNilai")
    public void downloadContohFileTagihan(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Example-File.xlsx");
        FileCopyUtils.copy(example.getInputStream(), response.getOutputStream());
        response.getOutputStream().flush();
    }

//    Lecturer

    @PreAuthorize("hasAnyAuthority('VIEW_DOSEN','VIEW_KPS')")
    @GetMapping("/graduation/lecture/list")
    public void listLecture(Authentication authentication, Model model){
        User user = currentUserService.currentUser(authentication);
        Dosen dosen = dosenDao.findByKaryawanIdUser(user);

        model.addAttribute("listDosen", noteDao.cariDosenPembimbing(dosen));


    }

    @RequestMapping("/graduation/{fileName}")
    public void downloadPDFResource( HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam Note note,
                                     @PathVariable("fileName") String fileName)
    {
        //If user is not authorized - he should be thrown out from here itself

        //Authorized user will download the file
        String lokasi = uploadFolder+File.separator+note.getMahasiswa().getNim();
        String dataDirectory = request.getServletContext().getRealPath(lokasi);
        Path file = Paths.get(lokasi, fileName);
        if (Files.exists(file))
        {
            response.setContentType("application/pdf");
            response.addHeader("Content-Disposition", "attachment; filename="+fileName);
            try
            {
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

//    Seminar Proposal

    @GetMapping("/graduation/sempro")
    public String sempro(Model model,@RequestParam(name = "id", value = "id", required = false) Note note){
        if (note.getStatus() == StatusApprove.APPROVED){
            model.addAttribute("note", note);
            Seminar waiting = seminarDao.findByNoteAndStatus(note,StatusApprove.WAITING);

            TahunAkademik ta = null;
            TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

            if (tahunAkademik.getJenis() == StatusRecord.PENDEK){
                String kode = tahunAkademik.getKodeTahunAkademik().substring(0,4) + "2";
                ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),kode );
            }else {
                if (LocalDate.now().compareTo(tahunAkademik.getTanggalMulai()) >= 0){
                    ta = tahunAkademik;
                }else {
                    int last = tahunAkademik.getKodeTahunAkademik().length();
                    String lastKode = String.valueOf(tahunAkademik.getKodeTahunAkademik().charAt(last - 1));

                    if (lastKode.equals("1")){
                        Integer tahun = Integer.valueOf(tahunAkademik.getTahun()) - 1;
                        String kode = String.valueOf(tahun) + "2";
                        ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),kode );
                    }else {
                        String kode = String.valueOf(tahunAkademik.getTahun()) + "1";
                        ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),kode );
                    }

                }
            }

            KrsDetail krsDetail = krsDetailDao.cariThesisSemester(note.getMahasiswa(),ta);

            if (krsDetail != null) {
                if (waiting != null) {
                    model.addAttribute("seminar", waiting);
                }else {
                    model.addAttribute("seminar", new Seminar());
                }
                return "graduation/sempro";
            }else {
                return "redirect:info";
            }

        }else {

            return "redirect:register";
        }


    }

    @GetMapping("/graduation/sempropasca")
    public String semproPasca(Model model,@RequestParam(name = "id", value = "id", required = false) Note note){
        if (note.getStatus() == StatusApprove.APPROVED){
            model.addAttribute("note", note);
            Seminar waiting = seminarDao.findByNoteAndStatus(note,StatusApprove.WAITING);

            TahunAkademik ta = null;
            TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahunAkademik, note.getMahasiswa().getIdProdi());

            if (tahunProdi.getTahunAkademik().getJenis() == StatusRecord.PENDEK){
                String kode = tahunAkademik.getKodeTahunAkademik().substring(0,4) + "2";
                ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),kode );
            }else {
                if (LocalDate.now().isAfter((tahunProdi.getMulaiKrs()))){
                    ta = tahunAkademik;
                }else {
                    int last = tahunAkademik.getKodeTahunAkademik().length();
                    String lastKode = String.valueOf(tahunAkademik.getKodeTahunAkademik().charAt(last - 1));

                    if (lastKode.equals("1")){
                        Integer tahun = Integer.valueOf(tahunAkademik.getTahun()) - 1;
                        String kode = String.valueOf(tahun) + "2";
                        ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),kode );
                    }else {
                        String kode = String.valueOf(tahunAkademik.getTahun()) + "1";
                        ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),kode );
                    }

                }
            }

            KrsDetail krsDetail = krsDetailDao.cariThesisSemester(note.getMahasiswa(),ta);
            if (krsDetail != null) {
                if (waiting != null) {
                    model.addAttribute("seminar", waiting);
                } else {
                    model.addAttribute("seminar", new Seminar());
                }
                return "graduation/sempropasca";
            }else {
                return "redirect:info";
            }
        }else {

            return "redirect:register";
        }


    }

    @GetMapping("/graduation/seminar/ulang")
    public String daftarUlang(Model model,@RequestParam(name = "id", value = "id", required = false) Note note){
        if (note.getStatus() == StatusApprove.APPROVED){
            model.addAttribute("note", note);
            Seminar waiting = seminarDao.findByNoteAndStatus(note,StatusApprove.WAITING);
            model.addAttribute("waiting", waiting);
            return "graduation/seminar/ulang";
        }else {

            return "redirect:register";
        }


    }

    @GetMapping("/graduation/seminar/waiting")
    public String waiting(Model model,@RequestParam(name = "id", value = "id", required = false) Note note, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("note", note);
        if (noteDao.findByMahasiswaAndStatus(note.getMahasiswa(),StatusApprove.APPROVED) != null) {
            Seminar seminar = seminarDao.findByNoteAndStatus(note, StatusApprove.APPROVED);
            if (seminar != null) {
                return "redirect:success?id="+seminar.getId();
            } else {
                Seminar waiting = seminarDao.findByNoteAndStatus(note,StatusApprove.WAITING);
                model.addAttribute("waiting", waiting);

                model.addAttribute("list", seminarDao.findByNote(note));
                return "graduation/seminar/waiting";
            }
        }else {
            return "redirect:../register";
        }


    }

    @GetMapping("/graduation/seminar/success")
    public String successPage(Model model,@RequestParam(name = "id", value = "id", required = false) Seminar seminar){
        model.addAttribute("seminar", seminar);

        if (seminar.getPublish() != null) {

            if (seminar.getPublish().equals("AKTIF")) {
                return "redirect:nilai?id="+seminar.getId();
            }
        }

        return "graduation/seminar/success";
    }

    @GetMapping("/graduation/seminar/nilai")
    public String nilaiPage(Model model,@RequestParam(name = "id", value = "id", required = false) Seminar seminar){
        model.addAttribute("seminar", seminar);
        List<Sidang> sidang = sidangDao.findBySeminar(seminar);
        EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(seminar.getNote().getMahasiswa(),StatusRecord.SKRIPSI,Boolean.TRUE);
        EnableFiture semprop = enableFitureDao.findByMahasiswaAndFiturAndEnable(seminar.getNote().getMahasiswa(),StatusRecord.SEMPRO,Boolean.TRUE);
        if (semprop != null) {
            model.addAttribute("sempro", semprop);
            if (enableFiture != null) {
                model.addAttribute("sidang", enableFiture);
                System.out.println(enableFiture);
            }
        }
        if (sidang.isEmpty()){
            return "graduation/seminar/nilai";
        }else {
            return "redirect:../sidang/mahasiswa/list?id="+seminar.getId();
        }


    }

    @PostMapping("/graduation/sempro")
    public String prosesSeminar(@Valid Seminar seminar,
                                BindingResult error, MultipartFile kartu,MultipartFile skripsi,MultipartFile pengesahan,
                                Authentication currentUser,MultipartFile formulir) throws Exception {
        User user = currentUserService.currentUser(currentUser);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

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
            String lokasiUpload = seminarFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            kartu.transferTo(tujuan);


            seminar.setFileBimbingan(idFile + "." + extension);

        }else {
            seminar.setFileBimbingan(seminar.getFileBimbingan());
        }

        if (!formulir.isEmpty() || formulir != null) {
            String namaAsli = formulir.getOriginalFilename();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = seminarFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            formulir.transferTo(tujuan);


            seminar.setFileFormulir(idFile + "." + extension);

        }else {
            seminar.setFileFormulir(seminar.getFileBimbingan());
        }

        if (!pengesahan.isEmpty() || pengesahan != null) {
            String namaFile = pengesahan.getName();
            String jenisFile = pengesahan.getContentType();
            String namaAsli = pengesahan.getOriginalFilename();
            Long ukuran = pengesahan.getSize();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = seminarFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            pengesahan.transferTo(tujuan);


            seminar.setFilePengesahan(idFile + "." + extension);

        }else {
            seminar.setFilePengesahan(seminar.getFilePengesahan());
        }

        if (!skripsi.isEmpty() || skripsi != null) {
            String namaFile = skripsi.getName();
            String jenisFile = skripsi.getContentType();
            String namaAsli = skripsi.getOriginalFilename();
            Long ukuran = skripsi.getSize();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = seminarFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            skripsi.transferTo(tujuan);


            seminar.setFileSkripsi(idFile + "." + extension);

        }else {
            seminar.setFileSkripsi(seminar.getFileSkripsi());
        }

        seminar.setTanggalInput(LocalDate.now());
        seminar.setStatus(StatusApprove.WAITING);
        seminar.setStatusSempro(StatusApprove.WAITING);
        seminar.setPublish(StatusRecord.NONAKTIF.toString());
        TahunAkademik ta = null;
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if (tahunAkademik.getJenis() == StatusRecord.PENDEK){
            String kode = tahunAkademik.getKodeTahunAkademik().substring(0,4) + "2";
            ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),kode );
        }else {
            ta = tahunAkademik;
        }
        seminar.setTahunAkademik(ta);
        seminarDao.save(seminar);

        return "redirect:seminar/waiting?id="+seminar.getNote().getId();

    }

    @PostMapping("/graduation/sempropasca")
    public String prosesSeminarPasca(@Valid Seminar seminar,MultipartFile turnitin,MultipartFile coverNote,
                                     BindingResult error, MultipartFile kartu,MultipartFile skripsi,MultipartFile pengesahan,
                                     Authentication currentUser,MultipartFile formulir) throws Exception {
        User user = currentUserService.currentUser(currentUser);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        if (kartu.getSize() > 0) {
            String namaAsli = kartu.getOriginalFilename();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = seminarFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            kartu.transferTo(tujuan);


            seminar.setFileBimbingan(idFile + "." + extension);

        }else {
            seminar.setFileBimbingan(seminar.getFileBimbingan());
        }

        if (formulir.getSize() > 0) {
            String namaAsli = formulir.getOriginalFilename();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = seminarFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            formulir.transferTo(tujuan);


            seminar.setFileFormulir(idFile + "." + extension);

        }else {
            seminar.setFileFormulir(seminar.getFileBimbingan());
        }

        if (pengesahan.getSize() > 0) {
            String namaAsli = pengesahan.getOriginalFilename();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = seminarFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            pengesahan.transferTo(tujuan);


            seminar.setFilePengesahan(idFile + "." + extension);

        }else {
            seminar.setFilePengesahan(seminar.getFilePengesahan());
        }

        if (skripsi.getSize() > 0) {
            String namaAsli = skripsi.getOriginalFilename();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = seminarFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            skripsi.transferTo(tujuan);


            seminar.setFileSkripsi(idFile + "." + extension);

        }else {
            seminar.setFileSkripsi(seminar.getFileSkripsi());
        }

        if (turnitin.getSize() > 0) {
            String namaAsli = turnitin.getOriginalFilename();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = seminarFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            turnitin.transferTo(tujuan);


            seminar.setFileTurnitin(idFile + "." + extension);

        }else {
            seminar.setFileTurnitin(seminar.getFileSkripsi());
        }

        if (coverNote.getSize() > 0) {
            String namaAsli = coverNote.getOriginalFilename();

//        memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = seminarFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            coverNote.transferTo(tujuan);


            seminar.setFileCoverNote(idFile + "." + extension);

        }else {
            seminar.setFileCoverNote(seminar.getFileSkripsi());
        }

        seminar.setTanggalInput(LocalDate.now());
        seminar.setStatus(StatusApprove.WAITING);
        seminar.setStatusSempro(StatusApprove.WAITING);
        seminar.setPublish(StatusRecord.NONAKTIF.toString());
        TahunAkademik ta = null;
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if (tahunAkademik.getJenis() == StatusRecord.PENDEK){
            String kode = tahunAkademik.getKodeTahunAkademik().substring(0,4) + "2";
            ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),kode );
        }else {
            ta = tahunAkademik;
        }
        seminar.setTahunAkademik(ta);
        seminarDao.save(seminar);

        return "redirect:seminar/waiting?id="+seminar.getNote().getId();

    }

    @GetMapping("/graduation/seminar/list")
    public void seminarList(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik,
                            @RequestParam(required = false) Prodi prodi, Pageable pageable){
        if (tahunAkademik != null) {
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("selectedProdi", prodi);
            model.addAttribute("listSempro", seminarDao.findByTahunAkademikAndNoteMahasiswaIdProdiAndStatusNotInOrderByStatusSemproDescPublishDesc(tahunAkademik, prodi, Arrays.asList(StatusApprove.REJECTED),pageable));
        }
    }

    @GetMapping("/graduation/seminar/view")
    public void viewSeminar(Model model, @RequestParam(name = "id", value = "id") Seminar seminar){
        model.addAttribute("seminar", seminar);



    }

    @GetMapping("/graduation/seminar/approved")
    public void approvedSeminar(@RequestParam(name = "id", value = "id") Seminar seminar,Model model){
        model.addAttribute("seminar",seminar);
        List<String> dosenList = new ArrayList<>();
        dosenList.add(seminar.getNote().getDosen().getId());
        model.addAttribute("listDosen", dosenDao.findByStatusNotInAndIdNotIn(Arrays.asList(StatusRecord.HAPUS,StatusRecord.NONAKTIF),dosenList));

    }

    @GetMapping("/graduation/seminar/approvalpasca")
    public void approvalSeminarPasca(@RequestParam(name = "id", value = "id") Seminar seminar,Model model){
        model.addAttribute("seminar",seminar);
        List<String> dosenList = new ArrayList<>();
        dosenList.add(seminar.getNote().getDosen().getId());
        model.addAttribute("listDosen", dosenDao.findByStatusNotInAndIdNotIn(Arrays.asList(StatusRecord.NONAKTIF,StatusRecord.HAPUS),dosenList));

    }

    @PostMapping("/graduation/seminar/approved")
    public String prosesApprove(@Valid Seminar seminar,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        seminar.setUserApprove(user);
        seminar.setStatus(StatusApprove.APPROVED);
        seminarDao.save(seminar);

        return "redirect:list?tahunAkademik="+seminar.getTahunAkademik().getId()+"&prodi="+seminar.getNote().getMahasiswa().getIdProdi().getId();
    }

    @GetMapping("/graduation/seminar/reject")
    public String prosesReject(@RequestParam Seminar seminar,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        seminar.setUserApprove(user);
        seminar.setStatus(StatusApprove.REJECTED);
        seminarDao.save(seminar);

        return "redirect:list?tahunAkademik="+seminar.getTahunAkademik().getId()+"&prodi="+seminar.getNote().getMahasiswa().getIdProdi().getId();
    }

    @GetMapping("/upload/{seminar}/pengesahan/")
    public ResponseEntity<byte[]> pengesahan(@PathVariable Seminar seminar, Model model) throws Exception {
        String lokasiFile = seminarFolder + File.separator + seminar.getNote().getMahasiswa().getNim()
                + File.separator + seminar.getFilePengesahan();
        LOGGER.debug("Lokasi file bukti : {}", lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (seminar.getFilePengesahan().toLowerCase().endsWith("jpeg") || seminar.getFilePengesahan().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (seminar.getFilePengesahan().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (seminar.getFilePengesahan().toLowerCase().endsWith("pdf")) {
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

    @GetMapping("/upload/{seminar}/formulir/")
    public ResponseEntity<byte[]> formulir(@PathVariable Seminar seminar, Model model) throws Exception {
        String lokasiFile = seminarFolder + File.separator + seminar.getNote().getMahasiswa().getNim()
                + File.separator + seminar.getFileFormulir();
        LOGGER.debug("Lokasi file bukti : {}", lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (seminar.getFileFormulir().toLowerCase().endsWith("jpeg") || seminar.getFileFormulir().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (seminar.getFileFormulir().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (seminar.getFileFormulir().toLowerCase().endsWith("pdf")) {
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

    @GetMapping("/upload/{seminar}/skripsi/")
    public ResponseEntity<byte[]> skripsi(@PathVariable Seminar seminar, Model model) throws Exception {
        String lokasiFile = seminarFolder + File.separator + seminar.getNote().getMahasiswa().getNim()
                + File.separator + seminar.getFileSkripsi();
        LOGGER.debug("Lokasi file bukti : {}", lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (seminar.getFileSkripsi().toLowerCase().endsWith("jpeg") || seminar.getFilePengesahan().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (seminar.getFileSkripsi().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (seminar.getFileSkripsi().toLowerCase().endsWith("pdf")) {
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

    @GetMapping("/upload/{seminar}/bimbingan/")
    public ResponseEntity<byte[]> fileBimbingan(@PathVariable Seminar seminar, Model model) throws Exception {
        String lokasiFile = seminarFolder + File.separator + seminar.getNote().getMahasiswa().getNim()
                + File.separator + seminar.getFileBimbingan();
        LOGGER.debug("Lokasi file bukti : {}", lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (seminar.getFileBimbingan().toLowerCase().endsWith("jpeg") || seminar.getFilePengesahan().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (seminar.getFileBimbingan().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (seminar.getFileBimbingan().toLowerCase().endsWith("pdf")) {
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

    @GetMapping("/upload/{seminar}/covernote/")
    public ResponseEntity<byte[]> fileCoverNote(@PathVariable Seminar seminar, Model model) throws Exception {
        String lokasiFile = seminarFolder + File.separator + seminar.getNote().getMahasiswa().getNim()
                + File.separator + seminar.getFileCoverNote();
        LOGGER.debug("Lokasi file bukti : {}", lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (seminar.getFileCoverNote().toLowerCase().endsWith("jpeg") || seminar.getFilePengesahan().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (seminar.getFileCoverNote().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (seminar.getFileCoverNote().toLowerCase().endsWith("pdf")) {
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

    @GetMapping("/upload/{seminar}/fileturnitin/")
    public ResponseEntity<byte[]> fileTurnitin(@PathVariable Seminar seminar, Model model) throws Exception {
        String lokasiFile = seminarFolder + File.separator + seminar.getNote().getMahasiswa().getNim()
                + File.separator + seminar.getFileTurnitin();
        LOGGER.debug("Lokasi file bukti : {}", lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (seminar.getFileTurnitin().toLowerCase().endsWith("jpeg") || seminar.getFilePengesahan().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (seminar.getFileTurnitin().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (seminar.getFileTurnitin().toLowerCase().endsWith("pdf")) {
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



    @GetMapping("/graduation/seminar/detail")
    public void detailSempro(Model model, @RequestParam Seminar seminar, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        model.addAttribute("seminar",seminar);
        model.addAttribute("dosen", dosen);

    }

    @GetMapping("/graduation/seminar/penilaian")
    public void penilaianSeminar(Model model,@RequestParam Seminar seminar,@RequestParam(required = false) String kosong,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        String valueHari = String.valueOf(seminar.getTanggalUjian().getDayOfWeek().getValue());
        if (seminar.getTanggalUjian().getDayOfWeek().getValue() == 7){
            Hari hari = hariDao.findById("0").get();
            model.addAttribute("hari", hari);

        }else {
            Hari hari = hariDao.findById(valueHari).get();
            model.addAttribute("hari", hari);

        }

        if (kosong != null){
            model.addAttribute("kosong", "Seminar tidak bisa di publish, karena nilai belum lengkap. Silahkan cek nilai Anda dan penguji lainnya");
        }
        model.addAttribute("dosen", dosen);
        model.addAttribute("seminar",seminar);

    }

    @GetMapping("/graduation/seminar/penilaianpasca")
    public void penilaianSeminarPasca(Model model,@RequestParam Seminar seminar,@RequestParam(required = false) String kosong,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        String valueHari = String.valueOf(seminar.getTanggalUjian().getDayOfWeek().getValue());
        if (seminar.getTanggalUjian().getDayOfWeek().getValue() == 7){
            Hari hari = hariDao.findById("0").get();
            model.addAttribute("hari", hari);

        }else {
            Hari hari = hariDao.findById(valueHari).get();
            model.addAttribute("hari", hari);

        }

        if (kosong != null){
            model.addAttribute("kosong", "Seminar tidak bisa di publish, karena nilai belum lengkap. Silahkan cek nilai Anda dan penguji lainnya");
        }
        model.addAttribute("dosen", dosen);
        model.addAttribute("seminar",seminar);

        if (seminar.getKetuaPenguji() == dosen){
            model.addAttribute("data", sidangService.getKetuaSempro(seminar));
        }

        if (seminar.getDosenPenguji() == dosen){
            model.addAttribute("data", sidangService.getPengujiSempro(seminar));
        }

        if (seminar.getNote().getDosen() == dosen){
            model.addAttribute("data", sidangService.getPembimbingSempro1(seminar));
        }

        if (seminar.getNote().getDosen2() == dosen){
            model.addAttribute("data", sidangService.getPembimbingSempro2(seminar));
        }

    }

    @PostMapping("/graduation/seminar/penilaianPasca")
    public String saveKetua(@RequestParam Seminar seminar,@RequestParam(required = false) BigDecimal nilaiA,Authentication authentication,
                            @RequestParam(required = false) BigDecimal nilaiB,@RequestParam(required = false) BigDecimal nilaiC,
                            @RequestParam(required = false) BigDecimal nilaiD,@RequestParam(required = false) BigDecimal nilaiE,@RequestParam(required = false) String beritaAcara,@RequestParam(required = false) String komentar){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        SemproDto semproDto = new SemproDto();
        semproDto.setId(seminar.getId());
        semproDto.setKomentar(komentar);
        semproDto.setBeritaAcara(beritaAcara);
        semproDto.setNilaiA(nilaiA);
        semproDto.setNilaiB(nilaiB);
        semproDto.setNilaiC(nilaiC);
        semproDto.setNilaiD(nilaiD);
        semproDto.setNilaiE(nilaiE);

        if (seminar.getKetuaPenguji() == dosen){
            sidangService.saveKetuaSeminar(semproDto);
        }

        if (seminar.getDosenPenguji() == dosen){
            sidangService.savePengujiSempro(semproDto);
        }

        if (seminar.getNote().getDosen() == dosen){
            sidangService.savePembimbingSempro(semproDto);
        }

        if (seminar.getNote().getDosen2() == dosen){
            sidangService.savePembimbing2Sempro(semproDto);
        }


        return "redirect:detail?seminar="+seminar.getId();
    }

    @GetMapping("/graduation/seminar/score")
    public void nilaiProdi(Model model,@RequestParam Seminar seminar,@RequestParam(required = false) String kosong,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        String valueHari = String.valueOf(seminar.getTanggalUjian().getDayOfWeek().getValue());
        if (seminar.getTanggalUjian().getDayOfWeek().getValue() == 7){
            Hari hari = hariDao.findById("0").get();
            model.addAttribute("hari", hari);

        }else {
            Hari hari = hariDao.findById(valueHari).get();
            model.addAttribute("hari", hari);

        }

        if (kosong != null){
            model.addAttribute("kosong", "Seminar tidak bisa di publish, karena nilai belum lengkap. Silahkan cek nilai Anda dan penguji lainnya");
        }
        model.addAttribute("dosen", dosen);
        model.addAttribute("seminar",seminar);

    }

    @PostMapping("/graduation/seminar/updatescore")
    public String updatePublish(Model model, Authentication authentication,@RequestParam Seminar seminar ,@Valid SeminarDto seminarDto){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        model.addAttribute("dosen" , dosen);

        sidangService.updatePublish(seminar,seminarDto);

        return "redirect:list?tahunAkademik=" + seminar.getTahunAkademik().getId()+"&prodi="+seminar.getNote().getMahasiswa().getIdProdi().getId();


    }

    @GetMapping("/graduation/lecture/sempro")
    public void listSeminar(@RequestParam(required = false) TahunAkademik tahunAkademik, Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        if(tahunAkademik != null){
            List<Seminar> seminarS1 = seminarDao.cariSeminar(dosen,dosen,dosen,dosen,tahunAkademik);
            List<Seminar> seminarPasca = seminarDao.cariSeminarPasca(dosen,jenjangDao.findById("02").get(),tahunAkademik);
            List<Seminar> daftarSeminar = new ArrayList<>();
            daftarSeminar.addAll(seminarS1);
            daftarSeminar.addAll(seminarPasca);

            model.addAttribute("akademik", tahunAkademik);
            model.addAttribute("list", daftarSeminar.stream()
                    .distinct()
                    .collect(Collectors.toList()));
            model.addAttribute("dosen", dosen);
        }
    }

    @PostMapping("/graduation/seminar/detailPost")
    public String saveComment(@RequestParam Seminar seminar, @RequestParam(required = false) String komentarDosen1 ,@RequestParam(required = false) String komentarDosen2,
                              @RequestParam(required = false) String komentarDosen3){

        if (komentarDosen1 != null){
            seminar.setKomentarDosen1(komentarDosen1);
//            seminarDao.save(seminar);
        }

        if (komentarDosen2 != null){
            seminar.setKomentarDosen2(komentarDosen2);
            seminarDao.save(seminar);
        }

        if (komentarDosen3 != null){
            seminar.setKomentarDosen3(komentarDosen3);
            seminarDao.save(seminar);
        }

        return "redirect:detail?seminar="+seminar.getId();

    }

    @PostMapping("/graduation/seminar/ketua")
    public String saveKetua(@RequestParam Seminar seminar,@RequestParam(required = false) BigDecimal ka,
                            @RequestParam(required = false) BigDecimal kb,@RequestParam(required = false) BigDecimal kc,
                            @RequestParam(required = false) BigDecimal kd,@RequestParam(required = false) BigDecimal ke,@RequestParam(required = false) String beritaAcara,
                            @RequestParam(required = false) BigDecimal kf,@RequestParam(required = false) String komentarKetua){
        if (seminar.getNote().getJenis() == StatusRecord.SKRIPSI){
            seminar.setKa(ka);
            seminar.setKb(kb);
            seminar.setKc(kc);
            seminar.setKd(kd);
            seminar.setKe(ke);
            seminar.setKf(kf);
            seminar.setBeritaAcara(beritaAcara);
            seminar.setKomentarKetua(komentarKetua);
            BigDecimal nilaiA = seminar.getKa().add(seminar.getUa()).add(seminar.getPa()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.25));
            BigDecimal nilaiB = seminar.getKb().add(seminar.getUb()).add(seminar.getPb()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            BigDecimal nilaiC = seminar.getKc().add(seminar.getUc()).add(seminar.getPc()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
            BigDecimal nilaiD = seminar.getKd().add(seminar.getUd()).add(seminar.getPd()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.1));
            BigDecimal nilaiE = seminar.getKe().add(seminar.getUe()).add(seminar.getPe()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            BigDecimal nilaiF = seminar.getKf().add(seminar.getUf()).add(seminar.getPf()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            seminar.setNilaiA(nilaiA);
            seminar.setNilaiB(nilaiB);
            seminar.setNilaiC(nilaiC);
            seminar.setNilaiD(nilaiD);
            seminar.setNilaiE(nilaiE);
            seminar.setNilaiF(nilaiF);
            seminar.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD).add(nilaiE).add(nilaiF));
            if (seminarDao.validasiSemproSKripsi(seminar,BigDecimal.ZERO) == null) {
                seminar.setStatusSempro(StatusApprove.APPROVED);
            }else {
                seminar.setStatusSempro(StatusApprove.WAITING);
            }
            seminarDao.save(seminar);
        }

        if(seminar.getNote().getJenis() == StatusRecord.STUDI_KELAYAKAN){
            seminar.setKa(ka);
            seminar.setKb(kb);
            seminar.setKc(kc);
            seminar.setKd(kd);
            seminar.setKe(ke);
            seminar.setBeritaAcara(beritaAcara);
            seminar.setKomentarKetua(komentarKetua);
            BigDecimal nilaiA = seminar.getKa().add(seminar.getUa()).add(seminar.getPa()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
            BigDecimal nilaiB = seminar.getKb().add(seminar.getUb()).add(seminar.getPb()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.25));
            BigDecimal nilaiC = seminar.getKc().add(seminar.getUc()).add(seminar.getPc()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
            BigDecimal nilaiD = seminar.getKd().add(seminar.getUd()).add(seminar.getPd()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            BigDecimal nilaiE = seminar.getKe().add(seminar.getUe()).add(seminar.getPe()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.1));
            seminar.setNilaiA(nilaiA);
            seminar.setNilaiB(nilaiB);
            seminar.setNilaiC(nilaiC);
            seminar.setNilaiD(nilaiD);
            seminar.setNilaiE(nilaiE);
            seminar.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD).add(nilaiE));
            if (seminarDao.validasiSemproStudy(seminar,BigDecimal.ZERO) == null) {
                seminar.setStatusSempro(StatusApprove.APPROVED);
            }else {
                seminar.setStatusSempro(StatusApprove.WAITING);
            }
            seminarDao.save(seminar);
        }

        return "redirect:detail?seminar="+seminar.getId();
    }

    @PostMapping("/graduation/seminar/dosen")
    public String saveDosen(@RequestParam Seminar seminar,@RequestParam(required = false) BigDecimal ua,
                            @RequestParam(required = false) BigDecimal ub,@RequestParam(required = false) BigDecimal uc,
                            @RequestParam(required = false) BigDecimal ud,@RequestParam(required = false) BigDecimal ue,
                            @RequestParam(required = false) BigDecimal uf,@RequestParam(required = false) String komentarPenguji){
        if (seminar.getNote().getJenis() == StatusRecord.SKRIPSI){
            seminar.setUa(ua);
            seminar.setUb(ub);
            seminar.setUc(uc);
            seminar.setUd(ud);
            seminar.setUe(ue);
            seminar.setUf(uf);
            seminar.setKomentarPenguji(komentarPenguji);
            BigDecimal nilaiA = seminar.getKa().add(seminar.getUa()).add(seminar.getPa()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.25));
            BigDecimal nilaiB = seminar.getKb().add(seminar.getUb()).add(seminar.getPb()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            BigDecimal nilaiC = seminar.getKc().add(seminar.getUc()).add(seminar.getPc()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
            BigDecimal nilaiD = seminar.getKd().add(seminar.getUd()).add(seminar.getPd()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.1));
            BigDecimal nilaiE = seminar.getKe().add(seminar.getUe()).add(seminar.getPe()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            BigDecimal nilaiF = seminar.getKf().add(seminar.getUf()).add(seminar.getPf()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            seminar.setNilaiA(nilaiA);
            seminar.setNilaiB(nilaiB);
            seminar.setNilaiC(nilaiC);
            seminar.setNilaiD(nilaiD);
            seminar.setNilaiE(nilaiE);
            seminar.setNilaiF(nilaiF);
            seminar.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD).add(nilaiE).add(nilaiF));
            if (seminarDao.validasiSemproSKripsi(seminar,BigDecimal.ZERO) == null) {
                seminar.setStatusSempro(StatusApprove.APPROVED);
            }else {
                seminar.setStatusSempro(StatusApprove.WAITING);
            }
            seminarDao.save(seminar);
        }

        if(seminar.getNote().getJenis() == StatusRecord.STUDI_KELAYAKAN){
            seminar.setUa(ua);
            seminar.setUb(ub);
            seminar.setUc(uc);
            seminar.setUd(ud);
            seminar.setUe(ue);
            seminar.setKomentarPenguji(komentarPenguji);
            BigDecimal nilaiA = seminar.getKa().add(seminar.getUa()).add(seminar.getPa()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
            BigDecimal nilaiB = seminar.getKb().add(seminar.getUb()).add(seminar.getPb()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.25));
            BigDecimal nilaiC = seminar.getKc().add(seminar.getUc()).add(seminar.getPc()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
            BigDecimal nilaiD = seminar.getKd().add(seminar.getUd()).add(seminar.getPd()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            BigDecimal nilaiE = seminar.getKe().add(seminar.getUe()).add(seminar.getPe()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.1));
            seminar.setNilaiB(nilaiB);
            seminar.setNilaiC(nilaiC);
            seminar.setNilaiD(nilaiD);
            seminar.setNilaiE(nilaiE);
            seminar.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD).add(nilaiE));
            if (seminarDao.validasiSemproStudy(seminar,BigDecimal.ZERO) == null) {
                seminar.setStatusSempro(StatusApprove.APPROVED);
            }else {
                seminar.setStatusSempro(StatusApprove.WAITING);
            }
            seminarDao.save(seminar);
        }

        return "redirect:detail?seminar="+seminar.getId();
    }

    @PostMapping("/graduation/seminar/pembimbing")
    public String savePembimbing(@RequestParam Seminar seminar,@RequestParam(required = false) BigDecimal pa,
                                 @RequestParam(required = false) BigDecimal pb,@RequestParam(required = false) BigDecimal pc,
                                 @RequestParam(required = false) BigDecimal pd,@RequestParam(required = false) BigDecimal pe,
                                 @RequestParam(required = false) BigDecimal pf,@RequestParam(required = false) String komentarPembimbing){
        if (seminar.getNote().getJenis() == StatusRecord.SKRIPSI){
            seminar.setPa(pa);
            seminar.setPb(pb);
            seminar.setPc(pc);
            seminar.setPd(pd);
            seminar.setPe(pe);
            seminar.setPf(pf);
            seminar.setKomentarPembimbing(komentarPembimbing);
            BigDecimal nilaiA = seminar.getKa().add(seminar.getUa()).add(seminar.getPa()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.25));
            BigDecimal nilaiB = seminar.getKb().add(seminar.getUb()).add(seminar.getPb()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            BigDecimal nilaiC = seminar.getKc().add(seminar.getUc()).add(seminar.getPc()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
            BigDecimal nilaiD = seminar.getKd().add(seminar.getUd()).add(seminar.getPd()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.1));
            BigDecimal nilaiE = seminar.getKe().add(seminar.getUe()).add(seminar.getPe()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            BigDecimal nilaiF = seminar.getKf().add(seminar.getUf()).add(seminar.getPf()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            seminar.setNilaiA(nilaiA);
            seminar.setNilaiB(nilaiB);
            seminar.setNilaiC(nilaiC);
            seminar.setNilaiD(nilaiD);
            seminar.setNilaiE(nilaiE);
            seminar.setNilaiF(nilaiF);
            seminar.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD).add(nilaiE).add(nilaiF));
            if (seminarDao.validasiSemproSKripsi(seminar,BigDecimal.ZERO) == null) {
                seminar.setStatusSempro(StatusApprove.APPROVED);
            }else {
                seminar.setStatusSempro(StatusApprove.WAITING);
            }
            seminarDao.save(seminar);
        }

        if(seminar.getNote().getJenis() == StatusRecord.STUDI_KELAYAKAN){
            seminar.setPa(pa);
            seminar.setPb(pb);
            seminar.setPc(pc);
            seminar.setPd(pd);
            seminar.setPe(pe);
            seminar.setKomentarPembimbing(komentarPembimbing);
            BigDecimal nilaiA = seminar.getKa().add(seminar.getUa()).add(seminar.getPa()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.3));
            BigDecimal nilaiB = seminar.getKb().add(seminar.getUb()).add(seminar.getPb()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.25));
            BigDecimal nilaiC = seminar.getKc().add(seminar.getUc()).add(seminar.getPc()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.2));
            BigDecimal nilaiD = seminar.getKd().add(seminar.getUd()).add(seminar.getPd()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.15));
            BigDecimal nilaiE = seminar.getKe().add(seminar.getUe()).add(seminar.getPe()).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(0.1));
            seminar.setNilaiB(nilaiB);
            seminar.setNilaiC(nilaiC);
            seminar.setNilaiD(nilaiD);
            seminar.setNilaiE(nilaiE);
            seminar.setNilai(nilaiA.add(nilaiB).add(nilaiC).add(nilaiD).add(nilaiE));
            if (seminarDao.validasiSemproStudy(seminar,BigDecimal.ZERO) == null) {
                seminar.setStatusSempro(StatusApprove.APPROVED);
            }else {
                seminar.setStatusSempro(StatusApprove.WAITING);
            }
            seminarDao.save(seminar);
        }

        return "redirect:detail?seminar="+seminar.getId();
    }

    @GetMapping("/graduation/seminar/mahasiswa")
    public void seminarMahasiswa(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa" , mahasiswa);

        List<Seminar> seminars = seminarDao.findByNoteMahasiswaAndStatusSemproNotInAndStatus(mahasiswa,Arrays.asList(StatusApprove.WAITING),StatusApprove.APPROVED);
        model.addAttribute("seminar", seminars);

    }

    @GetMapping("/graduation/lecture/finalisasi")
    public String finalisasiSeminar(Model model, Authentication authentication, @RequestParam Seminar seminar){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        model.addAttribute("dosen" , dosen);

        if (seminar.getNote().getJenis() == StatusRecord.SKRIPSI){
            Object skripsi = seminarDao.validasiSemproSKripsi(seminar,BigDecimal.ZERO);

            if (skripsi == null) {
                if (seminar.getKetuaPenguji() == dosen) {
                    seminar.setPublish(StatusRecord.AKTIF.toString());
                    if (seminar.getNilai().compareTo(new BigDecimal(70)) < 0) {
                        seminar.setStatusSempro(StatusApprove.FAILED);
                        seminar.setStatus(StatusApprove.FAILED);
                        EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(seminar.getNote().getMahasiswa(), StatusRecord.SEMPRO, Boolean.TRUE);
                        if (enableFiture != null) {
                            enableFiture.setEnable(Boolean.FALSE);
                            enableFitureDao.save(enableFiture);
                        }
                    }else {
                        seminar.setStatusSempro(StatusApprove.APPROVED);
                    }
                    seminarDao.save(seminar);
                }

                return "redirect:sempro?tahunAkademik=" + seminar.getTahunAkademik().getId();
            }else {
                return "redirect:../seminar/penilaian?seminar=" + seminar.getId()+"&kosong=true";

            }
        }else if (seminar.getNote().getJenis() == StatusRecord.STUDI_KELAYAKAN){
            Object study = seminarDao.validasiSemproStudy(seminar,BigDecimal.ZERO);

            if (study == null) {
                if (seminar.getKetuaPenguji() == dosen) {
                    seminar.setPublish(StatusRecord.AKTIF.toString());
                    if (seminar.getNilai().compareTo(new BigDecimal(70)) < 0) {
                        seminar.setStatusSempro(StatusApprove.FAILED);
                        seminar.setStatus(StatusApprove.FAILED);
                        EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(seminar.getNote().getMahasiswa(), StatusRecord.SEMPRO, Boolean.TRUE);
                        if (enableFiture != null) {
                            enableFiture.setEnable(Boolean.FALSE);
                            enableFitureDao.save(enableFiture);
                        }
                    }else {
                        seminar.setStatusSempro(StatusApprove.APPROVED);
                    }
                    seminarDao.save(seminar);
                }

                return "redirect:sempro?tahunAkademik=" + seminar.getTahunAkademik().getId();
            }else {
                return "redirect:../seminar/penilaian?seminar=" + seminar.getId()+"&kosong=true";

            }
        }else {
            return "redirect:sempro?tahunAkademik=" + seminar.getTahunAkademik().getId();
        }


    }

    @PostMapping("/graduation/seminar/finalisasi")
    public String finalisasiSeminar(@RequestParam Seminar seminar){
        Object jenis = null;
        if (seminar.getNote().getJenis() == StatusRecord.STUDI_KELAYAKAN){
            jenis = seminarDao.validasiSemproStudy(seminar,BigDecimal.ZERO);
        }
        if (seminar.getNote().getJenis() == StatusRecord.SKRIPSI){
            jenis = seminarDao.validasiSemproSKripsi(seminar, BigDecimal.ZERO);
        }
        if (jenis == null) {
            seminar.setPublish(StatusRecord.AKTIF.toString());

            if (seminar.getNilai().compareTo(new BigDecimal(70)) < 0) {
                seminar.setStatusSempro(StatusApprove.FAILED);
                seminar.setStatus(StatusApprove.FAILED);
                EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(seminar.getNote().getMahasiswa(), StatusRecord.SEMPRO, Boolean.TRUE);
                if (enableFiture != null) {
                    enableFiture.setEnable(Boolean.FALSE);
                    enableFitureDao.save(enableFiture);
                }
            }
            seminarDao.save(seminar);
            return "redirect:list?tahunAkademik=" + seminar.getTahunAkademik().getId()+"&prodi="+seminar.getNote().getMahasiswa().getIdProdi().getId();

        }else {
            return "lengkapi";
        }

    }

    @GetMapping("/graduation/seminar/formulir")
    public void formulirSempro(@RequestParam(name = "id") Note note,
                               HttpServletResponse response){
        try {
            // 0. Setup converter
            Options options = Options.getFrom(DocumentKind.ODT).to(ConverterTypeTo.PDF);

            // 1. Load template dari file
            InputStream in = formulirSempro1.getInputStream();

            // 2. Inisialisasi template engine, menentukan sintaks penulisan variabel
            IXDocReport report = XDocReportRegistry.getRegistry().
                    loadReport(in, TemplateEngineKind.Freemarker);

            // 3. Context object, untuk mengisi variabel
            BigDecimal totalSKS = krsDetailDao.totalSksAkhir(note.getMahasiswa().getId());
            BigDecimal totalMuti = krsDetailDao.totalMutuAkhir(note.getMahasiswa().getId());

            BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);

            IContext ctx = report.createContext();
            ctx.put("nama", note.getMahasiswa().getNama());
            ctx.put("nim", note.getMahasiswa().getNim());
            ctx.put("prodi",note.getMahasiswa().getIdProdi().getNamaProdi());
            ctx.put("ipk", ipk);
            ctx.put("sks", totalSKS);
            ctx.put("tgl", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

            response.setHeader("Content-Disposition", "attachment;filename=Formulir_Sempro-"+ note.getMahasiswa().getIdProdi().getKodeProdi() + "-" + note.getMahasiswa().getNim() +".pdf");
            OutputStream out = response.getOutputStream();
            report.convert(ctx, options, out);
            out.flush();
        } catch (Exception err){
//            logger.error(err.getMessage(), err);
        }
    }

    @GetMapping("/dowload/dataSeminar")
    public void downloadDataSeminar(@RequestParam TahunAkademik tahunAkademik, @RequestParam Prodi prodi, HttpServletResponse response) throws IOException{
        String[] colums = {"No", "Nim", "Nama", "Judul Seminar","Pembimbing", "Ketua Penguji", "Dosen Penguji", "Status"};

        List<Seminar> seminar = seminarDao.findByTahunAkademikAndNoteMahasiswaIdProdiAndStatusNotInOrderByStatusDescTanggalInputDesc(tahunAkademik, prodi, Arrays.asList(StatusApprove.REJECTED));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Seminar");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < colums.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(colums[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (Seminar data : seminar){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(data.getNote().getMahasiswa().getNim());
            row.createCell(2).setCellValue(data.getNote().getMahasiswa().getNama());
            row.createCell(3).setCellValue(data.getNote().getJudul());
            row.createCell(4).setCellValue(data.getNote().getDosen().getKaryawan().getNamaKaryawan());
            if (data.getKetuaPenguji() == null){
                row.createCell(5).setCellValue("-");
            }else {
                row.createCell(5).setCellValue(data.getKetuaPenguji().getKaryawan().getNamaKaryawan());
            }
            if (data.getDosenPenguji() == null){
                row.createCell(6).setCellValue("-");
            }else{
                row.createCell(6).setCellValue(data.getDosenPenguji().getKaryawan().getNamaKaryawan());
            }

            if (data.getStatus() == StatusApprove.APPROVED && data.getStatusSempro() == StatusApprove.WAITING){
                row.createCell(7).setCellValue("Menunggu Penilaian Lengkap");
            }else if (data.getStatus() == StatusApprove.APPROVED && data.getStatusSempro() == StatusApprove.APPROVED && data.getPublish() == "NONAKTIF"){
                row.createCell(7).setCellValue("Menunggu Publish");
            }else if (data.getStatus() == StatusApprove.APPROVED && data.getStatusSempro() == StatusApprove.APPROVED && data.getPublish() == "AKTIF"){
                row.createCell(7).setCellValue("Seminar Sudah Publish");
            }else if(data.getStatus() == StatusApprove.WAITING){
                row.createCell(7).setCellValue("Menunggu Persetujuan");
            }else if (data.getStatus() == StatusApprove.REJECTED){
                row.createCell(7).setCellValue("Ditolak");
            }
        }

        for (int i = 0; i < colums.length; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename;Data_Seminar_"+LocalDate.now()+".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }


    @PostMapping("/graduation/seminar/dosen/publish")
    @ResponseBody
    public String publishSeminar(@RequestParam Seminar seminar){
        if (seminar.getNote().getJenis() == StatusRecord.STUDI_KELAYAKAN){
            Object nilaiKosong = seminarDao.validasiPublishNilaiStudy(seminar,BigDecimal.ZERO);
            if (nilaiKosong == null) {
                seminar.setPublish("AKTIF");
                seminar.setStatusSempro(StatusApprove.APPROVED);
                seminarDao.save(seminar);
                return "berhasil";

            }else {
                return "lengkapi";
            }
        }

        if (seminar.getNote().getJenis() == StatusRecord.SKRIPSI){
            Object nilaiKosong = seminarDao.validasiPublishNilaiSkripsi(seminar,BigDecimal.ZERO);
            if (nilaiKosong == null) {
                seminar.setPublish("AKTIF");
                seminar.setStatusSempro(StatusApprove.APPROVED);
                seminarDao.save(seminar);
                return "berhasil";

            }else {
                return "lengkapi";
            }
        }

        if (seminar.getNote().getJenis() == StatusRecord.TESIS){
            Object nilaiKosong = seminarDao.validasiPublishNilaiTesis(seminar,BigDecimal.ZERO);
            if (nilaiKosong == null) {
                seminar.setPublish("AKTIF");
                seminar.setStatusSempro(StatusApprove.APPROVED);
                seminarDao.save(seminar);
                return "berhasil";

            }else {
                return "lengkapi";
            }
        }

        return "kosong";

    }


}
