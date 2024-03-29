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
import id.ac.tazkia.smilemahasiswa.dto.graduation.SidangDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.ScoreService;
import id.ac.tazkia.smilemahasiswa.service.SidangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    ScoreService scoreService;

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
    private TahunProdiDao tahunProdiDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private SidangService sidangService;

    @Autowired
    private TagihanDao tagihanDao;

    @Autowired
    PeriodeWisudaDao periodeWisudaDao;

    @Autowired
    EnableFitureDao enableFitureDao;

    @Value("classpath:sample/filesidang.odt")
    private Resource fileNilai;

    @Value("classpath:sample/nilaiSidang.docx")
    private Resource nilaiSidang;

    @Value("classpath:sample/skripsi.odt")
    private Resource formulirSkripsi;

    @Value("classpath:sample/skripsipasca.odt")
    private Resource formulirSkripsiPasca;

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

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @ModelAttribute("tahun")
    public Iterable<TahunAkademik> tahun() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

    @GetMapping("/api/sidang")
    @ResponseBody
    public Object[] validasiSidang(@RequestParam Ruangan ruangan, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tanggal,
                                   @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime jamMulai,
                                   @RequestParam @DateTimeFormat(pattern = "HH:mm:ss")LocalTime jamSelesai){
        if (tanggal.getDayOfWeek().getValue() == 7){
            Hari hari = hariDao.findById("0").get();
            TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            return sidangDao.validasiJadwalSidang(ta,hari,ruangan,jamMulai,jamSelesai,tanggal,1);
        }else {
            Hari hari = hariDao.findById(String.valueOf(tanggal.getDayOfWeek().getValue())).get();
            TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            return sidangDao.validasiJadwalSidang(tahunAkademik,hari,ruangan,jamMulai,jamSelesai,tanggal, 1);

        }
    }

    @GetMapping("/graduation/sidang/formulir")
    public void formulirSempro(@RequestParam(name = "id") Seminar seminar,
                               HttpServletResponse response){
        try {
            // 0. Setup converter
            Options options = Options.getFrom(DocumentKind.ODT).to(ConverterTypeTo.PDF);

            // 1. Load template dari file
            InputStream in = formulirSkripsi.getInputStream();

            // 2. Inisialisasi template engine, menentukan sintaks penulisan variabel
            IXDocReport report = XDocReportRegistry.getRegistry().
                    loadReport(in, TemplateEngineKind.Freemarker);

            // 3. Context object, untuk mengisi variabel
            BigDecimal totalSKS = krsDetailDao.totalSksAkhir(seminar.getNote().getMahasiswa().getId());
            BigDecimal totalMuti = krsDetailDao.totalMutuAkhir(seminar.getNote().getMahasiswa().getId());

            BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);

            IContext ctx = report.createContext();
            ctx.put("nama", seminar.getNote().getMahasiswa().getNama());
            ctx.put("nim", seminar.getNote().getMahasiswa().getNim());
            ctx.put("prodi",seminar.getNote().getMahasiswa().getIdProdi().getNamaProdi());
            ctx.put("ipk", ipk);
            ctx.put("sks", totalSKS);
            ctx.put("tgl", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

            response.setHeader("Content-Disposition", "attachment;filename=Formulir_Skripsi-"+ seminar.getNote().getMahasiswa().getIdProdi().getKodeProdi() + "-" + seminar.getNote().getMahasiswa().getNim() +".pdf");
            OutputStream out = response.getOutputStream();
            report.convert(ctx, options, out);
            out.flush();
        } catch (Exception err){
//            logger.error(err.getMessage(), err);
        }
    }

    @GetMapping("/graduation/sidang/formulirpasca")
    public void formulirTesis(@RequestParam(name = "id") Seminar seminar,
                               HttpServletResponse response){
        try {
            // 0. Setup converter
            Options options = Options.getFrom(DocumentKind.ODT).to(ConverterTypeTo.PDF);

            // 1. Load template dari file
            InputStream in = formulirSkripsiPasca.getInputStream();

            // 2. Inisialisasi template engine, menentukan sintaks penulisan variabel
            IXDocReport report = XDocReportRegistry.getRegistry().
                    loadReport(in, TemplateEngineKind.Freemarker);

            // 3. Context object, untuk mengisi variabel
            BigDecimal totalSKS = krsDetailDao.totalSksAkhir(seminar.getNote().getMahasiswa().getId());
            BigDecimal totalMuti = krsDetailDao.totalMutuAkhir(seminar.getNote().getMahasiswa().getId());

            BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);

            IContext ctx = report.createContext();
            ctx.put("nama", seminar.getNote().getMahasiswa().getNama());
            ctx.put("nim", seminar.getNote().getMahasiswa().getNim());
            ctx.put("prodi",seminar.getNote().getMahasiswa().getIdProdi().getNamaProdi());
            ctx.put("ipk", ipk);
            ctx.put("sks", totalSKS);
            ctx.put("tgl", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

            response.setHeader("Content-Disposition", "attachment;filename=Formulir_Skripsi-"+ seminar.getNote().getMahasiswa().getIdProdi().getKodeProdi() + "-" + seminar.getNote().getMahasiswa().getNim() +".pdf");
            OutputStream out = response.getOutputStream();
            report.convert(ctx, options, out);
            out.flush();
        } catch (Exception err){
//            logger.error(err.getMessage(), err);
        }
    }

//    Mahasiswa

    @GetMapping("/graduation/sidang/mahasiswa/info")
    public void infoPenutupan(Model model){
        PeriodeWisuda periodeWisuda = periodeWisudaDao.findByStatus(StatusRecord.AKTIF);
        model.addAttribute("periode", periodeWisuda);

    }

    @GetMapping("/graduation/sidang/mahasiswa/pendaftaran")
    public String pendaftaranSidang(@RequestParam(name = "id", value = "id",required = false) Seminar seminar, Model model, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        String jenjang = mahasiswa.getIdProdi().getIdJenjang().getId();

        if (jenjang.equals("01")){
            model.addAttribute("S1", "Mahasiswa S1");
        }else if (jenjang.equals("02")){
            model.addAttribute("S2", "Mahasisw S2");
        }else{
            model.addAttribute("kosong", "null");
        }

        if (seminar.getNilai().compareTo(new BigDecimal(70)) < 0){
            return "redirect:../../seminar/nilai?id="+seminar.getId();
        }else {
            model.addAttribute("seminar",seminar);
            PeriodeWisuda periodeWisuda = periodeWisudaDao.findByStatus(StatusRecord.AKTIF);

            if (jenjang.equals("02")) {
                TahunAkademik ta = null;
                TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
                TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahunAkademik, mahasiswa.getIdProdi());

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

                KrsDetail krsDetail = krsDetailDao.cariThesisSemester(mahasiswa,ta);
                if (krsDetail != null) {
                    return "graduation/sidang/mahasiswa/pendaftaran";
                }else {
                    return "graduation/info";
                }
            }else {
                if (LocalDate.now().isAfter(periodeWisuda.getTutupSidang()) && LocalDate.now().isBefore(periodeWisuda.getBukaSidang())){
                    return "redirect:info";
                } else {
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

                    KrsDetail krsDetail = krsDetailDao.cariThesisSemester(mahasiswa,ta);
                    if (krsDetail != null) {
                        return "graduation/sidang/mahasiswa/pendaftaran";
                    }else {
                        return "graduation/info";
                    }
                }
            }
        }
    }

    @GetMapping("/graduation/sidang/mahasiswa/revisi")
    public void revisiSidang(@RequestParam(name = "id", value = "id",required = false) Sidang sidang, Model model, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        String jenjang = mahasiswa.getIdProdi().getIdJenjang().getId();

        if (jenjang.equals("01")){
            model.addAttribute("S1", "Mahasiswa S1");
        }else if (jenjang.equals("02")){
            model.addAttribute("S2", "Mahasisw S2");
        }else{
            model.addAttribute("kosong", "null");
        }

        model.addAttribute("sidang",sidang);
    }

    @PostMapping("/graduation/sidang/mahasiswa/pendaftaran")
    public String saveSidang(@ModelAttribute @Valid Sidang sidang, MultipartFile ijazah, MultipartFile ktp, Authentication authentication,
                             MultipartFile kartu,MultipartFile plagiat, MultipartFile draft,MultipartFile pendaftaran,MultipartFile berita) throws Exception{

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
            sidang.setFilePendaftaran(sidang.getFilePendaftaran());
        }

        if (!berita.isEmpty() || berita != null) {

            String namaAsli = berita.getOriginalFilename();


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
            berita.transferTo(tujuan);


            sidang.setFileBerita(idFile + "." + extension);

        }else {
            sidang.setFileBerita(sidang.getFileBerita());
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
            sidang.setFileIjazah(sidang.getFileIjazah());
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
            sidang.setFileKtp(sidang.getFileKtp());
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
            sidang.setFileTurnitin(sidang.getFileTurnitin());
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
            sidang.setFileSidang(sidang.getFileSidang());
        }
        sidang.setTanggalInput(LocalDate.now());
        sidang.setStatusSidang(StatusApprove.WAITING);
        sidang.setAkademik(StatusApprove.WAITING);
        sidang.setPublish(StatusRecord.NONAKTIF);
        TahunAkademik ta = null;
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if (tahunAkademik.getJenis() == StatusRecord.PENDEK){
            String kode = tahunAkademik.getKodeTahunAkademik().substring(0,4) + "2";
            ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),kode );
        }else {
            ta = tahunAkademik;
        }
        sidang.setTahunAkademik(ta);
        mahasiswa.setJudul(sidang.getJudulTugasAkhir());
        mahasiswa.setTitle(sidang.getJudulInggris());
        mahasiswaDao.save(mahasiswa);
        sidangDao.save(sidang);

        return "redirect:list?id="+sidang.getSeminar().getId();

    }

    @PostMapping("/graduation/sidang/mahasiswa/pendaftaranS2")
    public String saveSidangS2(@ModelAttribute @Valid Sidang sidang, Authentication authentication, MultipartFile draft, MultipartFile pendaftaran,
                               MultipartFile persetujuan, MultipartFile cv, MultipartFile kehadiran, MultipartFile plagiat) throws IOException {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        if (!draft.isEmpty() || draft != null){
            String namaFile = draft.getName();
            String jenisFile = draft.getContentType();
            String namaAsli = draft.getOriginalFilename();
            Long ukuran = draft.getSize();

            // Memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p){
                extension = namaAsli.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = sidangFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            draft.transferTo(tujuan);

            sidang.setFileSidang(idFile + "." + extension);

        }else{
            sidang.setFileSidang(sidang.getFileBimbingan());
        }

        if (!pendaftaran.isEmpty() || pendaftaran != null) {
            String namaFile = pendaftaran.getName();
            String jenisFile = pendaftaran.getContentType();
            String namaAsli = pendaftaran.getOriginalFilename();
            Long ukuran = pendaftaran.getSize();

            // memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p){
                extension = namaAsli.substring(i+1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = sidangFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            pendaftaran.transferTo(tujuan);

            sidang.setFilePendaftaran(idFile + "." + extension);
        }else{
            sidang.setFilePendaftaran(sidang.getFilePendaftaran());
        }

        if (!persetujuan.isEmpty() || persetujuan != null){
            String namaFile = persetujuan.getName();
            String jenisFile = persetujuan.getContentType();
            String namaAsli = persetujuan.getOriginalFilename();
            Long ukuran = persetujuan.getSize();

            // memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p){
                extension = namaAsli.substring(i+1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = sidangFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            persetujuan.transferTo(tujuan);

            sidang.setFilePersetujuan(idFile + "." + extension);
        }else{
            sidang.setFilePersetujuan(sidang.getFilePersetujuan());
        }

        if (!cv.isEmpty() || cv != null){
            String namaFile = cv.getName();
            String jenisFile = cv.getContentType();
            String namaAsli = cv.getOriginalFilename();
            Long ukuran = cv.getSize();

            // memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p){
                extension = namaAsli.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = sidangFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            cv.transferTo(tujuan);

            sidang.setFileKtp(idFile + "." + extension);
        }else{
            sidang.setFileKtp(sidang.getFileKtp());
        }

        if (!kehadiran.isEmpty() || kehadiran != null){
            String namaFile = kehadiran.getName();
            String jenisFile = kehadiran.getContentType();
            String namaAsli = kehadiran.getOriginalFilename();
            Long ukuran = kehadiran.getSize();

            // memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p){
                extension = namaAsli.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = sidangFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            kehadiran.transferTo(tujuan);

            sidang.setFileBimbingan(idFile + "." + extension);
        }else{
            sidang.setFileBimbingan(sidang.getFileBimbingan());
        }

        if (!plagiat.isEmpty() || plagiat != null){
            String namaFile = plagiat.getName();
            String jenisFile = plagiat.getContentType();
            String namaAsli = plagiat.getOriginalFilename();
            Long ukuran = plagiat.getSize();

            // memisahkan extension
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p){
                extension = namaAsli.substring(i+1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = sidangFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            plagiat.transferTo(tujuan);

            sidang.setFileTurnitin(idFile + "." + extension);
        }else{
            sidang.setFileTurnitin(sidang.getFileTurnitin());
        }

        sidang.setTanggalInput(LocalDate.now());
        sidang.setStatusSidang(StatusApprove.WAITING);
        sidang.setAkademik(StatusApprove.WAITING);
        sidang.setPublish(StatusRecord.NONAKTIF);
        TahunAkademik ta = null;
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if (tahunAkademik.getJenis() == StatusRecord.PENDEK){
            String kode = tahunAkademik.getKodeTahunAkademik().substring(0,4) + "2";
            ta = tahunAkademikDao.findByStatusNotInAndKodeTahunAkademik(Arrays.asList(StatusRecord.HAPUS),kode );
        }else {
            ta = tahunAkademik;
        }
        sidang.setTahunAkademik(ta);
        mahasiswa.setJudul(sidang.getJudulTugasAkhir());
        mahasiswa.setTitle(sidang.getJudulInggris());
        mahasiswaDao.save(mahasiswa);
        sidangDao.save(sidang);

        return "redirect:list?id="+sidang.getSeminar().getId();
    }

    @GetMapping("/graduation/sidang/mahasiswa/success")
    public String successPage(Model model,@RequestParam(name = "id", value = "id", required = false) Sidang sidang){
        model.addAttribute("sidang", sidang);
        /*if (LocalDate.now().plusDays(1).compareTo(sidang.getTanggalUjian()) > 0){
            System.out.println("lewat");
            model.addAttribute("waiting","waiting");
        }*/

        if (sidang.getPublish() == StatusRecord.AKTIF) {
            System.out.println("aktif");
            return "redirect:nilai?id="+sidang.getId();
        }else {
            return "graduation/sidang/mahasiswa/success";

        }

    }

    @GetMapping("/graduation/sidang/mahasiswa/nilai")
    public void nilaiPage(Model model,@RequestParam(name = "id", value = "id", required = false) Sidang sidang){
        model.addAttribute("sidang", sidang);

    }

    @GetMapping("/graduation/sidang/mahasiswa/list")
    public String waitingPageMahasiswa(Model model,@RequestParam(name = "id", value = "id", required = false) Seminar seminar, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("seminar", seminar);
        if (seminar.getStatusSempro().equals(StatusApprove.APPROVED)  && seminar.getPublish().equals("AKTIF") && seminar.getNilai().compareTo(new BigDecimal(70)) >= 0) {
            Sidang sidang = sidangDao.findBySeminarAndAkademikAndJamMulaiNotNullAndJamMulaiNotNull(seminar,StatusApprove.APPROVED);
            if (sidang != null) {
                return "redirect:success?id=" + sidang.getId();
            } else {

                List<Sidang> sidangList = sidangDao.findBySeminarOrderByAkademikDescTanggalInputDesc(seminar);
                Sidang waiting = sidangDao.findBySeminarAndAkademik(seminar,StatusApprove.WAITING);
                if (waiting != null) {
                    model.addAttribute("waiting", waiting);
                }
//
                model.addAttribute("list", sidangList);
                return "graduation/sidang/mahasiswa/list";
            }
        } else {
            return "redirect:../../register";
        }
    }

//    Akademik & Prodi

    @GetMapping("/graduation/sidang/admin/list")
    public void listApproval(@RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) Prodi prodi, Pageable page, Model model){
        model.addAttribute("selectedTahun",tahunAkademik);
        model.addAttribute("selectedProdi",prodi);

        if (tahunAkademik != null){
            model.addAttribute("listSidang", sidangDao.findByTahunAkademikAndSeminarNoteMahasiswaIdProdiAndAkademikNotInAndStatusSidangNotInOrderByAkademikDescStatusSidangDescPublishDesc(tahunAkademik,prodi,Arrays.asList(StatusApprove.REJECTED),Arrays.asList(StatusApprove.REJECTED),page));
        }
    }

    @GetMapping("/graduation/sidang/prodi/list")
    public void listJadwal (@RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) Prodi prodi, Pageable page, Model model){
        model.addAttribute("selectedTahun",tahunAkademik);
        model.addAttribute("selectedProdi",prodi);

        if (tahunAkademik != null){
            model.addAttribute("listSidang", sidangDao.findByTahunAkademikAndSeminarNoteMahasiswaIdProdiAndAkademikAndStatusSidangNotInOrderByAkademikDescStatusSidangDescPublishDesc(tahunAkademik,prodi,StatusApprove.APPROVED,Arrays.asList(StatusApprove.REJECTED),page));
        }
    }

    @GetMapping("/graduation/sidang/admin/approval")
    public void approval(){}

    @GetMapping("/graduation/sidang/admin/view")
    public void viewSidang(){}

    @GetMapping("/graduation/sidang/prodi/penjadwalan")
    public void penjadwalan(@RequestParam(name = "id", value = "id") Sidang sidang,Model model){
        model.addAttribute("sidang",sidang);
        List<String> dosenList = new ArrayList<>();
        dosenList.add(sidang.getSeminar().getNote().getDosen().getId());
        model.addAttribute("listDosen", dosenDao.findByStatusNotInAndIdNotIn(Arrays.asList(StatusRecord.HAPUS),dosenList));
    }

    @PostMapping("/graduation/sidang/prodi/penjadwalan")
    public String saveJadwal(@ModelAttribute @Valid Sidang sidang){
        sidang.setAkademik(StatusApprove.APPROVED);
        sidang.setStatusSidang(StatusApprove.APPROVED);
        sidang.setPembimbing(sidang.getSeminar().getNote().getDosen());

        if (sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getIdJenjang().getId().equals("02")){
            sidang.setPembimbing2(sidang.getSeminar().getNote().getDosen2());
        }
        sidangDao.save(sidang);

        return "redirect:list?tahunAkademik="+sidang.getTahunAkademik().getId()+"&prodi="+sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getId();
    }

    @PostMapping("/graduation/sidang/admin/tolak")
    public String tolakSidang(@RequestParam(name = "id", value = "id") Sidang sidang,@RequestParam(required = false) String komentarAkademik){
        sidang.setAkademik(StatusApprove.REJECTED);
        sidang.setKomentarAkademik(komentarAkademik);
        sidangDao.save(sidang);
        return "redirect:list?tahunAkademik="+sidang.getTahunAkademik().getId()+"&prodi="+sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getId();
    }

    @PostMapping("/graduation/sidang/prodi/tolak")
    public String tolakSidangProdi(@RequestParam(name = "id", value = "id") Sidang sidang,@RequestParam(required = false) String komentarProdi){
        sidang.setStatusSidang(StatusApprove.REJECTED);
        sidang.setKomentarProdi(komentarProdi);
        sidangDao.save(sidang);
        return "redirect:list?tahunAkademik="+sidang.getTahunAkademik().getId()+"&prodi="+sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getId();
    }

    @PostMapping("/graduation/sidang/admin/terima")
    public String terimaSidang(@RequestParam(name = "id", value = "id") Sidang sidang,@RequestParam(required = false) String komentarAkademik){
        sidang.setAkademik(StatusApprove.APPROVED);
        sidang.setKomentarAkademik(komentarAkademik);
        sidangDao.save(sidang);
        return "redirect:list?tahunAkademik="+sidang.getTahunAkademik().getId()+"&prodi="+sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getId();
    }


//    Dosen

    @GetMapping("/graduation/sidang/dosen/list")
    public void listDosen(@RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) Prodi prodi,
                          Model model,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);



        if (tahunAkademik != null) {
            List<Sidang> sidang = sidangDao.listDosenSidang(tahunAkademik,dosen);

            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("listSidang",sidang);
            model.addAttribute("dosen", dosen);
            model.addAttribute("selectedProdi", prodi);


        }
    }

    @GetMapping("/graduation/sidang/dosen/penilaian")
    public void penilaiaanSidang(Model model,@RequestParam Sidang sidang,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        String valueHari = String.valueOf(sidang.getTanggalUjian().getDayOfWeek().getValue());
        if (sidang.getTanggalUjian().getDayOfWeek().getValue() == 7){
            Hari hari = hariDao.findById("0").get();
            model.addAttribute("hari", hari);

        }else {
            Hari hari = hariDao.findById(valueHari).get();
            model.addAttribute("hari", hari);

        }
        model.addAttribute("dosen", dosen);
        model.addAttribute("sidang",sidang);

        if (sidang.getKetuaPenguji() == dosen){
            model.addAttribute("data", sidangService.getKetua(sidang));
        }

        if (sidang.getDosenPenguji() == dosen){
            model.addAttribute("data", sidangService.getPenguji(sidang));
        }

        if (sidang.getPembimbing() == dosen){
            model.addAttribute("data", sidangService.getPembimbing(sidang));
        }

        if (sidang.getPembimbing2() == dosen){
            model.addAttribute("data", sidangService.getPembimbing2(sidang));
        }

    }

    @GetMapping("/graduation/sidang/dosen/penilaianpasca")
    public void penilaiaanSidangTesis(Model model,@RequestParam Sidang sidang,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        String valueHari = String.valueOf(sidang.getTanggalUjian().getDayOfWeek().getValue());
        if (sidang.getTanggalUjian().getDayOfWeek().getValue() == 7){
            Hari hari = hariDao.findById("0").get();
            model.addAttribute("hari", hari);

        }else {
            Hari hari = hariDao.findById(valueHari).get();
            model.addAttribute("hari", hari);

        }
        model.addAttribute("dosen", dosen);
        model.addAttribute("sidang",sidang);

        if (sidang.getKetuaPenguji() == dosen){
            model.addAttribute("data", sidangService.getKetua(sidang));
        }

        if (sidang.getDosenPenguji() == dosen){
            model.addAttribute("data", sidangService.getPenguji(sidang));
        }

        if (sidang.getPembimbing() == dosen){
            model.addAttribute("data", sidangService.getPembimbing(sidang));
        }

        if (sidang.getPembimbing2() == dosen){
            model.addAttribute("data", sidangService.getPembimbing2(sidang));
        }

    }

    @PostMapping("/graduation/sidang/dosen/penilaian")
    public String saveKetua(@RequestParam Sidang sidang,@RequestParam(required = false) BigDecimal nilaiA,Authentication authentication,
                            @RequestParam(required = false) BigDecimal nilaiB,@RequestParam(required = false) BigDecimal nilaiC,
                            @RequestParam(required = false) BigDecimal nilaiD,@RequestParam(required = false) String beritaAcara,@RequestParam(required = false) String komentar){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        SidangDto sidangDto = new SidangDto();
        sidangDto.setId(sidang.getId());
        sidangDto.setKomentar(komentar);
        sidangDto.setBeritaAcara(beritaAcara);
        sidangDto.setNilaiA(nilaiA);
        sidangDto.setNilaiB(nilaiB);
        sidangDto.setNilaiC(nilaiC);
        sidangDto.setNilaiD(nilaiD);

        if (sidang.getKetuaPenguji() == dosen){
            sidangService.saveKetua(sidangDto);
        }

        if (sidang.getDosenPenguji() == dosen){
            sidangService.savePenguji(sidangDto);
        }

        if (sidang.getPembimbing() == dosen){
            sidangService.savePembimbing(sidangDto);
        }

        if (sidang.getPembimbing2() == dosen){
            sidangService.savePembimbing2(sidangDto);
        }


        return "redirect:list?tahunAkademik="+sidang.getTahunAkademik().getId()+"&prodi="+sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getId();
    }

    @PostMapping("/graduation/sidang/dosen/penilaianpasca")
    public String savePasca(@RequestParam Sidang sidang,@RequestParam(required = false) BigDecimal nilaiA,Authentication authentication,
                            @RequestParam(required = false) BigDecimal nilaiB,@RequestParam(required = false) BigDecimal nilaiC,
                            @RequestParam(required = false) BigDecimal nilaiD,@RequestParam(required = false) String beritaAcara,@RequestParam(required = false) String komentar){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        SidangDto sidangDto = new SidangDto();
        sidangDto.setId(sidang.getId());
        sidangDto.setKomentar(komentar);
        sidangDto.setBeritaAcara(beritaAcara);
        sidangDto.setNilaiA(nilaiA);
        sidangDto.setNilaiB(nilaiB);
        sidangDto.setNilaiC(nilaiC);
        sidangDto.setNilaiD(nilaiD);

        if (sidang.getKetuaPenguji() == dosen){
            sidangService.saveKetuaPasca(sidangDto);
        }

        if (sidang.getDosenPenguji() == dosen){
            sidangService.savePengujiPasca(sidangDto);
        }

        if (sidang.getPembimbing() == dosen){
            sidangService.savePembimbingPasca(sidangDto);
        }

        if (sidang.getPembimbing2() == dosen){
            sidangService.savePembimbing2(sidangDto);
        }


        return "redirect:list?tahunAkademik="+sidang.getTahunAkademik().getId()+"&prodi="+sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getId();
    }

    @PostMapping("/graduation/sidang/dosen/publish")
    @ResponseBody
    public String publishSidang(@RequestParam Sidang sidang){
        Object nilaiKosong = null;
        if (sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getIdJenjang().getId().equals("01")){
            nilaiKosong = sidangDao.validasiPublishNilai(sidang,BigDecimal.ZERO);
        }
        if (sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getIdJenjang().getId().equals("02")){
            nilaiKosong = sidangDao.validasiPublishNilaiPasca(sidang,BigDecimal.ZERO);
        }
        if (nilaiKosong == null) {

            if (sidang.getNilai().compareTo(new BigDecimal(70))>= 0){
                Mahasiswa mahasiswa = mahasiswaDao.findById(sidang.getSeminar().getNote().getMahasiswa().getId()).get();
                mahasiswa.setTanggalLulus(LocalDate.now());
                mahasiswa.setStatusAktif("LULUS");
                mahasiswaDao.save(mahasiswa);

                sidang.setPublish(StatusRecord.AKTIF);
                sidangDao.save(sidang);

                KrsDetail cariThesis = krsDetailDao.cariThesis(mahasiswa);
                if (cariThesis != null){
                    if (sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getIdJenjang().getId().equals("01")){
                        BigDecimal nilaiSidang = sidang.getNilai().multiply(new BigDecimal(60)).divide(new BigDecimal(100));
                        BigDecimal nilaiSempro = sidang.getSeminar().getNilai().multiply(new BigDecimal(40)).divide(new BigDecimal(100));
                        BigDecimal nilaiAKhir = nilaiSidang.add(nilaiSempro).setScale(2, RoundingMode.HALF_UP);
                        cariThesis.setNilaiAkhir(nilaiAKhir);
                        scoreService.hitungNilaiAkhir(cariThesis);

                    }
                    if (sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getIdJenjang().getId().equals("02")){
                        BigDecimal nilaiSidang = sidang.getNilai().multiply(new BigDecimal(50)).divide(new BigDecimal(100));
                        BigDecimal nilaiSempro = sidang.getSeminar().getNilai().multiply(new BigDecimal(50)).divide(new BigDecimal(100));
                        BigDecimal nilaiAKhir = nilaiSidang.add(nilaiSempro).setScale(2, RoundingMode.HALF_UP);
                        cariThesis.setNilaiAkhir(nilaiAKhir);
                        scoreService.hitungNilaiAkhir(cariThesis);
                    }
                }



            }else {
                return "nilai tidak ada";
            }
            return "berhasil";

        }else {
            return "lengkapi";
        }

    }

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

    @GetMapping("/upload/{sidang}/berita/")
    public ResponseEntity<byte[]> berita(@PathVariable Sidang sidang, Model model) throws Exception {
        String lokasiFile = sidangFolder + File.separator + sidang.getSeminar().getNote().getMahasiswa().getNim()
                + File.separator + sidang.getFileBerita();

        try {
            HttpHeaders headers = new HttpHeaders();
            if (sidang.getFileBerita().toLowerCase().endsWith("jpeg") || sidang.getFileBerita().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (sidang.getFileBerita().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (sidang.getFileBerita().toLowerCase().endsWith("pdf")) {
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

    @GetMapping("/upload/{sidang}/persetujuan/")
    public ResponseEntity<byte[]> persetujuan(@PathVariable Sidang sidang, Model model) throws Exception {
        String lokasiFile = sidangFolder + File.separator + sidang.getSeminar().getNote().getMahasiswa().getNim()
                + File.separator + sidang.getFilePersetujuan();

        try {
            HttpHeaders headers = new HttpHeaders();
            if (sidang.getFilePersetujuan().toLowerCase().endsWith("jpeg") || sidang.getFilePersetujuan().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (sidang.getFilePersetujuan().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (sidang.getFilePersetujuan().toLowerCase().endsWith("pdf")) {
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

    @GetMapping("/upload/{sidang}/bimbinganSidang/")
    public ResponseEntity<byte[]> bimbinganSidang(@PathVariable Sidang sidang, Model model) throws Exception {
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

    @GetMapping("/graduation/sidang/download")
    public void nilaiSidang(@RequestParam(name = "id")String nim, HttpServletResponse response){
        try {
            Options options = Options.getFrom(DocumentKind.ODT).to(ConverterTypeTo.PDF);
            InputStream in = fileNilai.getInputStream();

            IXDocReport report = XDocReportRegistry.getRegistry().
                    loadReport(in, TemplateEngineKind.Freemarker);

            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            IContext ctx = report.createContext();
            ctx.put("nama", mahasiswa.getNama());

            response.setHeader("Content-Disposition", "attachment;filename=Surat-Keterangan.pdf");
            OutputStream out = response.getOutputStream();
            report.convert(ctx, options, out);
            out.flush();

        }catch (Exception err){

        }
    }

    @GetMapping("/graduation/sidang/admin/score")
    public void nilaiProdi(Model model,@RequestParam Sidang sidang,@RequestParam(required = false) String kosong,
                           @RequestParam(required = false) String akses,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        String valueHari = String.valueOf(sidang.getTanggalUjian().getDayOfWeek().getValue());
        if (sidang.getTanggalUjian().getDayOfWeek().getValue() == 7){
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
        model.addAttribute("akses", akses);
        model.addAttribute("sidang",sidang);

    }

    @PostMapping("/graduation/sidang/updatescore")
    public String updatePublish(@RequestParam(required = false) String akses , Model model, Authentication authentication,
                                @RequestParam Sidang sidang ,@Valid SeminarDto seminarDto,@RequestParam(required = false) String jenjang ){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        model.addAttribute("dosen" , dosen);

        if (jenjang.equals("sarjana")){
            sidangService.updateSidang(sidang,seminarDto);
        }
        if (jenjang.equals("pascasarjana")){
            sidangService.updateSidangPasca(sidang,seminarDto);
        }


        if (akses.equals("prodi")){
            return "redirect:../sidang/prodi/list?tahunAkademik=" + sidang.getTahunAkademik().getId()+"&prodi="+sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getId();

        }

        if (akses.equals("admin")){
            return "redirect:../sidang/admin/list?tahunAkademik=" + sidang.getTahunAkademik().getId()+"&prodi="+sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getId();

        }

        return "redirect:list?tahunAkademik=" + sidang.getTahunAkademik().getId()+"&prodi="+sidang.getSeminar().getNote().getMahasiswa().getIdProdi().getId();


    }


}
