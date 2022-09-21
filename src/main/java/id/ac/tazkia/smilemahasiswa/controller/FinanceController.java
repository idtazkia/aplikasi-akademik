package id.ac.tazkia.smilemahasiswa.controller;

import com.lowagie.text.pdf.codec.wmf.MetaDo;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.payment.SisaTagihanDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.authenticator.SpnegoAuthenticator;
import org.apache.commons.math3.geometry.enclosing.EnclosingBall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller @Slf4j
public class FinanceController {

    @Autowired
    private KrsDao krsDao;
    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private MahasiswaDao mahasiswaDao;
    @Autowired
    private ProgramDao programDao;
    @Autowired
    private ProdiDao prodiDao;
    @Autowired
    private TahunProdiDao tahunAkademikProdiDao;
    @Autowired
    private EnableFitureDao enableFitureDao;
    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired
    private TagihanDao tagihanDao;
    @Autowired
    private MemoKeuanganDao memoKeuanganDao;
    @Autowired
    private CurrentUserService currentUserService;
    @Autowired
    private KaryawanDao karyawanDao;
    @Autowired
    private JadwalDao jadwalDao;
    @Autowired
    private SesiDao sesiDao;


    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }

    @ModelAttribute("program")
    public Iterable<Program> program() {
        return programDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @ModelAttribute("tahun")
    public Iterable<TahunAkademik> tahun() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

    @Value("${upload.memoKeuangan}")
    private String uploadMemo;

    @GetMapping("/activation/sesi")
    public void aktifasiKrs(Model model,@RequestParam(required = false) TahunAkademik tahun){
        if (tahun != null){
            List<Jadwal> jadwal = jadwalDao.cariSesiNull(tahun);
            for (Jadwal j : jadwal){
                Sesi sesi = sesiDao.findByJamMulaiAndJamSelesaiAndSks(j.getJamMulai(),j.getJamSelesai(),j.getMatakuliahKurikulum().getJumlahSks());
                j.setSesi(sesi.getSesi());
                jadwalDao.save(j);
            }
        }

    }


    @GetMapping("/activation/krs")
    public void aktifasiKrs(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik,
                            @RequestParam(required = false) String nim){


        Mahasiswa mhsw = mahasiswaDao.findByNim(nim);
        if (mhsw != null){
            Krs krs = krsDao.findByTahunAkademikAndMahasiswaAndStatus(tahunAkademik,mhsw,StatusRecord.AKTIF);
            model.addAttribute("krsMahasiswa", krs);
        }else {
            model.addAttribute("message", "nim tidak ada");
        }
        model.addAttribute("selectedTahun", tahunAkademik);
        model.addAttribute("selectedNim", nim);


    }

    @PostMapping("/activation/process")
    public String prosesKrs(@RequestParam TahunAkademik tahunAkademik,
                            @RequestParam(required = false) String nim){
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

        if (mahasiswa != null){
            Krs cariKrs = krsDao.findByTahunAkademikAndMahasiswaAndStatus(tahunAkademik, mahasiswa,StatusRecord.AKTIF);
            TahunAkademikProdi tahunAkademikProdi = tahunAkademikProdiDao.findByTahunAkademikAndProdi(tahunAkademik, mahasiswa.getIdProdi());
            if (cariKrs == null) {
                Krs krs = new Krs();
                krs.setTanggalTransaksi(LocalDateTime.now());
                krs.setStatus(StatusRecord.AKTIF);
                krs.setTahunAkademik(tahunAkademik);
                krs.setNim(mahasiswa.getNim());
                krs.setProdi(mahasiswa.getIdProdi());
                krs.setMahasiswa(mahasiswa);
                krs.setTahunAkademikProdi(tahunAkademikProdi);
                krsDao.save(krs);

                List<KrsDetail> cek = krsDetailDao.findByMahasiswaAndTahunAkademikAndStatusAndKrsNull(mahasiswa, tahunAkademik, StatusRecord.AKTIF);
                if (!cek.isEmpty() || cek != null){
                    for (KrsDetail krsDetail : cek){
                        krsDetail.setKrs(krs);
                        krsDetailDao.save(krsDetail);
                    }
                }

            }else {
                cariKrs.setStatus(StatusRecord.AKTIF);
                krsDao.save(cariKrs);

                List<KrsDetail> cek = krsDetailDao.findByMahasiswaAndTahunAkademikAndStatusAndKrsNull(mahasiswa, tahunAkademik, StatusRecord.AKTIF);
                if (!cek.isEmpty() || cek != null){
                    for (KrsDetail krsDetail : cek){
                        krsDetail.setKrs(cariKrs);
                        krsDetailDao.save(krsDetail);
                    }
                }
            }
        }
        return "redirect:krs?mahasiswa=AKTIF" + "&tahunAkademik=" + tahunAkademik.getId()+"&nim="+nim;

    }

    @GetMapping("/activation/kartu")
    public void aktifasiKrs(Model model,
                            @RequestParam(required = false) TahunAkademik tahunAkademik,@RequestParam(required = false) String status,
                            @RequestParam(required = false) String nim,@RequestParam(required = false) String uas){

        model.addAttribute("selectedTahun", tahunAkademik);
        model.addAttribute("selectedNim", nim);
        Mahasiswa mhsw = mahasiswaDao.findByNim(nim);
        model.addAttribute("mahasiswa", mhsw);
        model.addAttribute("status", status);

    }

    @PostMapping("/activation/kartu")
    public String prosesKrs(@RequestParam TahunAkademik tahunAkademik,
                            @RequestParam(required = false) String nim,@RequestParam(required = false) String status){

        if (status == "UTS") {
            Mahasiswa mhsw = mahasiswaDao.findByNim(nim);
            EnableFiture validasiFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhsw,
                    StatusRecord.UTS,true,tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            if (validasiFiture == null) {
                EnableFiture enableFiture = new EnableFiture();
                enableFiture.setEnable(true);
                enableFiture.setFitur(StatusRecord.UTS);
                enableFiture.setKeterangan("-");
                enableFiture.setMahasiswa(mhsw);
                enableFiture.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
                enableFitureDao.save(enableFiture);
            }

        }else {
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            EnableFiture validasiFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mahasiswa,
                    StatusRecord.UAS, true, tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            if (validasiFiture == null) {
                EnableFiture enableFiture = new EnableFiture();
                enableFiture.setEnable(true);
                enableFiture.setFitur(StatusRecord.UAS);
                enableFiture.setMahasiswa(mahasiswa);
                enableFiture.setKeterangan("-");
                enableFiture.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
                enableFitureDao.save(enableFiture);
            }
        }

        return "redirect:kartu?tahunAkademik=" + tahunAkademik.getId()+"&nim="+nim+"&status="+status;

    }

    @GetMapping("/activation/rfid")
    public void rfid(@RequestParam(required = false) String nim, @RequestParam(required = false) String rfid, Model model){
        model.addAttribute("nim", nim);
        model.addAttribute("rfid", rfid);

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

        if (mahasiswa != null){
            model.addAttribute("mahasiswa", mahasiswa);
        }
    }

    @PostMapping("/activation/rfid")
    public String prosesRfid(@RequestParam String nim,@RequestParam String rfid){

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        mahasiswa.setRfid(rfid);
        mahasiswaDao.save(mahasiswa);

        return "redirect:rfid?nim="+nim+"&rfid="+rfid;
    }

    @GetMapping("/activation/cicilan")
    public void cicilan(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) String nim, @PageableDefault(size = 10) Pageable page){

        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        if (mhs != null){
            model.addAttribute("tagihanMahasiswa", tagihanDao.findByStatusNotInAndMahasiswaAndTahunAkademik(Arrays.asList(StatusRecord.HAPUS), mhs, tahunAkademik, page));
        }else {
            model.addAttribute("message", "nim tidak ada");
        }
        model.addAttribute("selectTahun", tahunAkademik);
        model.addAttribute("selectNim", nim);
    }

    @PostMapping("/activation/cicilan")
    public String prosesCicilan(@RequestParam TahunAkademik tahunAkademik, @RequestParam(required = false) String nim, RedirectAttributes attributes){
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        if (mhs != null) {
            EnableFiture cekEnableFitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.CICILAN, true, tahunAkademik);
            if (cekEnableFitur == null) {
                EnableFiture enableFiture = new EnableFiture();
                enableFiture.setFitur(StatusRecord.CICILAN);
                enableFiture.setEnable(true);
                enableFiture.setKeterangan("-");
                enableFiture.setMahasiswa(mhs);
                enableFiture.setTahunAkademik(tahunAkademik);
                enableFitureDao.save(enableFiture);
            }else{
                attributes.addFlashAttribute("gagal", "data sudah ada!");
                return "redirect:cicilan?tahunAkademik="+tahunAkademik.getId()+"&nim="+nim;
            }
        }

        return "redirect:cicilan?tahunAkademik="+tahunAkademik.getId()+"&nim="+nim;
    }


    @GetMapping("/activation/tugasAkhir")
    public void formTugasAkhir(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) String nim,
                               @RequestParam(required = false) String status){

        model.addAttribute("selectTahun", tahunAkademik);
        model.addAttribute("selectNim", nim);
        Mahasiswa m = mahasiswaDao.findByNim(nim);
        model.addAttribute("mhs", m);
        model.addAttribute("status", status);

    }

    @PostMapping("/activation/tugas")
    public String prosesTugas(@RequestParam TahunAkademik tahunAkademik, @RequestParam(required = false) String nim,
                              @RequestParam(required = false) String status){

        if (status.equals("SEMPRO")) {
            Mahasiswa m = mahasiswaDao.findByNim(nim);
            EnableFiture validasi = enableFitureDao.findByMahasiswaAndFiturAndEnable(m, StatusRecord.SEMPRO, true);
            if (validasi == null) {
                EnableFiture enableFiture = new EnableFiture();
                enableFiture.setEnable(true);
                enableFiture.setFitur(StatusRecord.SEMPRO);
                enableFiture.setKeterangan("-");
                enableFiture.setMahasiswa(m);
                enableFiture.setTahunAkademik(tahunAkademik);
                enableFitureDao.save(enableFiture);
            }
        }
        if (status.equals("SKRIPSI")){
            Mahasiswa m = mahasiswaDao.findByNim(nim);
            EnableFiture validasi = enableFitureDao.findByMahasiswaAndFiturAndEnable(m, StatusRecord.SKRIPSI, true);
            if (validasi == null) {
                EnableFiture enableFiture = new EnableFiture();
                enableFiture.setMahasiswa(m);
                enableFiture.setFitur(StatusRecord.SKRIPSI);
                enableFiture.setEnable(true);
                enableFiture.setTahunAkademik(tahunAkademik);
                enableFiture.setKeterangan("-");
                enableFitureDao.save(enableFiture);
            }
        }

        if (status.equals("WISUDA")){
            Mahasiswa m = mahasiswaDao.findByNim(nim);
            EnableFiture validasi = enableFitureDao.findByMahasiswaAndFiturAndEnable(m, StatusRecord.WISUDA, true);
            if (validasi == null) {
                EnableFiture enableFiture = new EnableFiture();
                enableFiture.setMahasiswa(m);
                enableFiture.setFitur(StatusRecord.WISUDA);
                enableFiture.setEnable(true);
                enableFiture.setTahunAkademik(tahunAkademik);
                enableFiture.setKeterangan("-");
                enableFitureDao.save(enableFiture);
            }
        }

        return "redirect:tugasAkhir?tahunAkademik=" + tahunAkademik.getId()+"&nim="+nim+"&status="+status;

    }

    @GetMapping("/activation/peringanan")
    public void aktifasiPeringanan(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) String nim, @PageableDefault(size = 10) Pageable page){

        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        if (mhs != null) {
            model.addAttribute("tagihanMahasiswa", tagihanDao.findByStatusNotInAndMahasiswaAndTahunAkademik(Arrays.asList(StatusRecord.HAPUS), mhs, tahunAkademik, page));
        }else{
            model.addAttribute("message", "nim tidak ada");
        }
        model.addAttribute("selectTahun", tahunAkademik);
        model.addAttribute("selectNim", nim);

    }

    @PostMapping("/activation/peringanan")
    public String prosesPeringanan(@RequestParam TahunAkademik tahunAkademik, @RequestParam(required = false) String nim, RedirectAttributes attributes){
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        if (mhs != null) {
            EnableFiture cekFitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.PERINGANAN, true, tahunAkademik);
            if (cekFitur == null) {
                EnableFiture enableFiture = new EnableFiture();
                enableFiture.setEnable(true);
                enableFiture.setFitur(StatusRecord.PERINGANAN);
                enableFiture.setKeterangan("-");
                enableFiture.setMahasiswa(mhs);
                enableFiture.setTahunAkademik(tahunAkademik);
                enableFitureDao.save(enableFiture);
            }else{
                attributes.addFlashAttribute("gagal", "data sudah ada!");
                return "redirect:peringanan?tahunAkademik="+tahunAkademik.getId()+"&nim="+nim;
            }
        }

        return "redirect:peringanan?tahunAkademik="+tahunAkademik.getId()+"&nim="+nim;

    }

    // memo keuangan

    @GetMapping("/memoKeuangan/list")
    public void listMemo(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listMemo", memoKeuanganDao.findByStatusNotInAndNamaContainingIgnoreCaseOrderByCreateTime(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else{
            model.addAttribute("listMemo", memoKeuanganDao.findByStatusNotInOrderByCreateTime(Arrays.asList(StatusRecord.HAPUS), page));
        }

    }

    @GetMapping("/memoKeuangan/form")
    public void formMemo(Model model){

        model.addAttribute("newMemo", new MemoKeuangan());
        model.addAttribute("tahunAkademik", tahun());
        model.addAttribute("angkatan", angkatan());
        model.addAttribute("status", StatusMemo.values());

    }

    @GetMapping("/api/memo")
    @ResponseBody
    public List<MemoKeuangan> memo(@RequestParam(required = false) String idTahun,
                                   @RequestParam(required = false) String angkatan){

        TahunAkademik tahun = tahunAkademikDao.findById(idTahun).get();
        List<MemoKeuangan> mk = memoKeuanganDao.findByTahunAkademikAndAngkatanAndStatusOrderByCreateTime(tahun, angkatan, StatusRecord.AKTIF);

        return mk;

    }

    @PostMapping("/memoKeuangan/form")
    public String inputMemo(@ModelAttribute @Valid MemoKeuangan memoKeuangan, BindingResult errors, MultipartFile file, Authentication authentication,
                            RedirectAttributes attributes) throws IOException {

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);

        Integer jumlah = memoKeuanganDao.countAllByTahunAkademikAndAngkatanAndStatus(memoKeuangan.getTahunAkademik(), memoKeuangan.getAngkatan(), StatusRecord.AKTIF);
        if (jumlah < 4) {
            if (memoKeuangan.getStatusMemo() == StatusMemo.MENGGANTI) {
                List<MemoKeuangan> memo = memoKeuanganDao.findByTahunAkademikAndAngkatanAndStatusOrderByCreateTime(memoKeuangan.getTahunAkademik(), memoKeuangan.getAngkatan(), StatusRecord.AKTIF);
                for (MemoKeuangan m : memo){
                    m.setStatus(StatusRecord.NONAKTIF);
                    memoKeuanganDao.save(m);
                }
            }

            String namaFile = file.getName();
            String jenisFile = file.getContentType();
            String namaAsli = file.getOriginalFilename();
            Long ukuran = file.getSize();

            log.debug("nama file : {}", namaFile);
            log.debug("jenis file : {}", jenisFile);
            log.debug("nama asli : {}", namaAsli);
            log.debug("ukuran file : {}", ukuran);

            if (errors.hasErrors()) {
                log.debug("Error upload memo : {}", errors);
            }

            // Memisahkan file
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i+1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = uploadMemo + File.separator;
            log.info("Lokasi Upload : {}", lokasiUpload);
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            memoKeuangan.setDocument(idFile + "." + extension);
            file.transferTo(tujuan);
            log.debug("file sudah dicopy ke : {}", tujuan.getAbsolutePath());
            memoKeuangan.setCreateUser(karyawan);
            memoKeuanganDao.save(memoKeuangan);
        }else{
            attributes.addFlashAttribute("tahun", memoKeuangan.getTahunAkademik());
            attributes.addFlashAttribute("ang", memoKeuangan.getAngkatan());
            attributes.addFlashAttribute("gagal", "memo sudah maksimal");
            return "redirect:form";
        }

        return "redirect:list";
    }

    @GetMapping("/upload/{memo}/keuangan/")
    public ResponseEntity<byte[]> tampilkanMemo(@PathVariable MemoKeuangan memo) throws Exception {
        String lokasiFile = uploadMemo + File.separator + memo.getDocument();
        log.info("Lokasi file bukti : {}", lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (memo.getDocument().toLowerCase().endsWith("jpeg") || memo.getDocument().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (memo.getDocument().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (memo.getDocument().toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception err) {
            log.warn(err.getMessage(), err);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @SneakyThrows
    @PostMapping("/memoKeuangan/delete")
    public String deleteMemo(@RequestParam MemoKeuangan memo){

        memo.setStatus(StatusRecord.HAPUS);
        memoKeuanganDao.save(memo);

//        Path path = Paths.get(uploadMemo + File.separator + memo.getDocument());
//        try {
//            Files.delete(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return "redirect:list";
    }

}
