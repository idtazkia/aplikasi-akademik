package id.ac.tazkia.smilemahasiswa.controller;



import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.TagihanService;
import org.apache.catalina.manager.StatusTransformer;
import org.apache.tomcat.jni.Local;
import org.apache.tomcat.jni.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class StudentBillController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentBillController.class);

    @Autowired
    private BankDao bankDao;

    @Autowired
    private JenisDiskonDao jenisDiskonDao;

    @Autowired
    private DiskonDao diskonDao;

    @Autowired
    private TagihanDao tagihanDao;

    @Autowired
    private JenisTagihanDao jenisTagihanDao;

    @Autowired
    private NilaiJenisTagihanDao nilaiJenisTagihanDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private TahunProdiDao tahunProdiDao;

    @Value("classpath:sample/panduanPembayaran.pdf")
    private Resource panduanPembayaran;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private PembayaranDao pembayaranDao;

    @Autowired
    private RequestPenangguhanDao requestPenangguhanDao;

    @Autowired
    private RequestCicilanDao requestCicilanDao;

    @Autowired
    private VirtualAccountDao virtualAccountDao;

    @Autowired
    private EnableFitureDao enableFitureDao;

    @Autowired
    private ProgramDao programDao;

    @Value("classpath:kwitansi.odt")
    private Resource templateKwitansi;

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findAll();
    }

    @ModelAttribute("program")
    public Iterable<Program> program() {
        return programDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }

    @Autowired
    public TagihanService tagihanService;

    @Value("${upload.buktiPembayaran}")
    private String uploadFolder;

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademik> tahunAkademik() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() { return mahasiswaDao.cariAngkatan(); }

//    Bank

    @GetMapping("/studentBill/bank/list")
    public void listBank(Model model, @PageableDefault(size = 10)Pageable page, String search){
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listBank", bankDao.findByStatusNotInAndNamaContainingIgnoreCaseOrderByNama(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else{
            model.addAttribute("listBank", bankDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/studentBill/bank/form")
    public void formBank(Model model, @RequestParam(required = false) String id){
        model.addAttribute("bank", new Bank());

        if (id != null && !id.isEmpty()){
            Bank bank = bankDao.findById(id).get();
            if (bank != null){
                model.addAttribute("bank", bank);
                if (bank.getStatus() == null){
                    bank.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/studentBill/bank/new")
    public String newBank(@Valid Bank bank){
        if (bank.getStatus() == null){
            bank.setStatus(StatusRecord.NONAKTIF);
        }
        bankDao.save(bank);
        return "redirect:list";
    }

    @PostMapping("/studentBill/bank/delete")
    public String deleteBank(@RequestParam Bank bank){
        bank.setStatus(StatusRecord.HAPUS);
        bankDao.save(bank);
        return "redirect:list";
    }


//    Jenis Diskon

    @GetMapping("/studentBill/jenisDiskon/list")
    public void listJenis(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listDiskon", jenisDiskonDao.findByStatusNotInAndNamaContainingIgnoreCaseOrderByNama(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else{
            model.addAttribute("listDiskon", jenisDiskonDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/studentBill/jenisDiskon/form")
    public void formDiskon(Model model, @RequestParam(required = false) String id){
        model.addAttribute("jenisDiskon", new JenisDiskon());

        if (id != null && !id.isEmpty()){
            JenisDiskon jenisDiskon = jenisDiskonDao.findById(id).get();
            if (jenisDiskon != null) {
                model.addAttribute("jenisDiskon", jenisDiskon);
                if (jenisDiskon.getStatus() == null) {
                    jenisDiskon.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/studentBill/jenisDiskon/new")
    public String newDiskon(@Valid JenisDiskon jenisDiskon){

        if(jenisDiskon.getStatus() == null){
            jenisDiskon.setStatus(StatusRecord.NONAKTIF);
        }
        jenisDiskonDao.save(jenisDiskon);

        return "redirect:list";
    }

    @PostMapping("/studentBill/jenisDiskon/delete")
    public String deleteDiskon(@RequestParam JenisDiskon jenisDiskon){
        jenisDiskon.setStatus(StatusRecord.HAPUS);
        jenisDiskonDao.save(jenisDiskon);
        return "redirect:list";
    }

//    Diskon

    @GetMapping("/studentBill/diskon/form")
    public void form(Model model, @RequestParam(required = false) String id){

        Tagihan tagihan = tagihanDao.findById(id).get();
        model.addAttribute("diskon", new Diskon());
        model.addAttribute("tagihan", tagihan);
        model.addAttribute("m", mahasiswaDao.findByNim(tagihan.getMahasiswa().getNim()));

        model.addAttribute("listDiskon", jenisDiskonDao.findByStatusOrderByNama(StatusRecord.AKTIF));

    }

    @PostMapping("/studentBill/diskon/new")
    public String newForm(@Valid Diskon diskon,
                          @RequestParam(required = false) String id,
                          @RequestParam(required = false) String mahasiswa){

        Mahasiswa m = mahasiswaDao.findById(mahasiswa).get();
        NilaiJenisTagihan nilaiJenisTagihan = nilaiJenisTagihanDao.findById(id).get();
        diskon.setNilaiJenisTagihan(nilaiJenisTagihan);
        diskon.setStatus(StatusRecord.AKTIF);
        diskonDao.save(diskon);

        return "redirect:../billAdmin/list?tahunAkademik=" + nilaiJenisTagihan.getTahunAkademik().getId() + "&nim=" + m.getNim();
    }


//    jenis tagihan

    @GetMapping("/studentBill/typeBill/list")
    public void listType(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listType", jenisTagihanDao.findByStatusNotInAndNamaContainingIgnoreCaseOrderByNama(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else{
            model.addAttribute("listType", jenisTagihanDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/studentBill/typeBill/form")
    public void formType(Model model, @RequestParam(required = false) String id){
        model.addAttribute("typeBill", new JenisTagihan());

        if (id != null && !id.isEmpty()){
            JenisTagihan jenisTagihan = jenisTagihanDao.findById(id).get();
            if (jenisTagihan != null){
                model.addAttribute("typeBill", jenisTagihan);
                if (jenisTagihan.getStatus() == null){
                    jenisTagihan.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/studentBill/typeBill/new")
    public String newType(@Valid JenisTagihan jenisTagihan){
        if (jenisTagihan.getStatus() == null){
            jenisTagihan.setStatus(StatusRecord.NONAKTIF);
        }
        jenisTagihanDao.save(jenisTagihan);
        return "redirect:list";
    }

    @PostMapping("studentBill/typeBill/delete")
    public String deleteType(@RequestParam JenisTagihan jenisTagihan){
        jenisTagihan.setStatus(StatusRecord.HAPUS);
        jenisTagihanDao.save(jenisTagihan);
        return "redirect:list";
    }

//    Nilai jenis tagihan

    @GetMapping("/studentBill/valueType/list")
    public void listNilai(Model model, @PageableDefault(size = 10) Pageable page, String search ){
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listValue", nilaiJenisTagihanDao.findByStatusNotInAndJenisTagihanNamaContainingIgnoreCaseOrProdiNamaProdiContainingIgnoreCaseOrderByAngkatanDesc(Arrays.asList(StatusRecord.HAPUS), search, search, page));
        }else{
            model.addAttribute("listValue", nilaiJenisTagihanDao.findByStatusNotInOrderByAngkatan(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/studentBill/valueType/form")
    public void formNilai(Model model, @RequestParam(required = false) String id){
        model.addAttribute("valueBill", new NilaiJenisTagihan());
        model.addAttribute("jenisTagihan", jenisTagihanDao.findByStatusOrderByNama(StatusRecord.AKTIF));
        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("prodi", prodiDao.findByStatusOrderByNamaProdi(StatusRecord.AKTIF));
        model.addAttribute("program", programDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("selectAngkatan", angkatan());

        if (id != null && !id.isEmpty()){
            NilaiJenisTagihan nilaiJenisTagihan = nilaiJenisTagihanDao.findById(id).get();
            if (nilaiJenisTagihan != null){
                model.addAttribute("valueBill", nilaiJenisTagihan);
                if (nilaiJenisTagihan.getStatus() == null){
                    nilaiJenisTagihan.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/studentBill/valueType/new")
    public String newNilai(@Valid NilaiJenisTagihan nilaiJenisTagihan){
        if (nilaiJenisTagihan.getStatus() == null){
            nilaiJenisTagihan.setStatus(StatusRecord.NONAKTIF);
        }
        nilaiJenisTagihanDao.save(nilaiJenisTagihan);
        return "redirect:list";
    }

    @PostMapping("/studentBill/valueType/delete")
    public String deleteNilai(@RequestParam NilaiJenisTagihan nilaiJenisTagihan){
        nilaiJenisTagihan.setStatus(StatusRecord.HAPUS);
        nilaiJenisTagihanDao.save(nilaiJenisTagihan);
        return "redirect:list";
    }

//    Tagihan

    @GetMapping("/studentBill/bill/list")
    public void listBill(Model model, @PageableDefault(size = 10) Pageable page,
                           Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mhs = mahasiswaDao.findByUser(user);

        model.addAttribute("biayaMahasiswa", tagihanDao.biayaMahasiswa(mhs.getId()));
        model.addAttribute("pembayaran", pembayaranDao.pembayaranMahasiswa(mhs.getId()));
        model.addAttribute("totalTagihan", tagihanDao.totalTagihanPerMahasiswa(mhs.getId()));
        model.addAttribute("totalDibayar", pembayaranDao.totalDibayarMahasiswa(mhs.getId()));

    }

    @GetMapping("/studentBill/billAdmin/list")
    public void listBillAdmin(Model model,
                              @RequestParam(required = false) TahunAkademik tahunAkademik,
                              @RequestParam(required = false) String nim,
                              @RequestParam(required = false) String date1, @RequestParam(required = false) String date2,
                              @RequestParam(required = false) String date3, @RequestParam(required = false) String date4){


        model.addAttribute("selectTahun", tahunAkademik);
        model.addAttribute("selectNim", nim);
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        model.addAttribute("mhs", mhs);
        if (tahunAkademik != null && mhs != null) {
            model.addAttribute("sisaTagihan", tagihanDao.sisaTagihanQuery(tahunAkademik.getId(),mhs.getId()));
            model.addAttribute("daftarBiaya", tagihanDao.daftarBiaya(tahunAkademik.getId(), mhs.getId()));
            model.addAttribute("daftarPembayaran", pembayaranDao.daftarPembayaran(tahunAkademik.getId(), mhs.getId()));
            model.addAttribute("status", krsDao.findByTahunAkademikAndMahasiswaAndStatus(tahunAkademik, mhs, StatusRecord.AKTIF));
            model.addAttribute("mahasiswa", mhs);
            model.addAttribute("jumlahSks", krsDetailDao.jumlahSksMahasiswa(mhs.getId(), tahunAkademik.getId()));
            model.addAttribute("totalTagihan", tagihanDao.totalTagihanPerTahunAkademikDanMahasiswa(tahunAkademik.getId(), mhs.getId()));
            model.addAttribute("totalDibayar", pembayaranDao.totalDibayarPerTahunDanMahasiswa(tahunAkademik.getId(), mhs.getId()));
        }

        // list per prodi
        model.addAttribute("listProdi", tagihanDao.listTagihanPerProdi());
        model.addAttribute("toTagihan", tagihanDao.totalTagihan());
        model.addAttribute("toDibayar", pembayaranDao.totalDibayar());

        // list per prodi + date
        model.addAttribute("tanggal1", date1);
        model.addAttribute("tanggal2", date2);
        model.addAttribute("listProdiDate", tagihanDao.listTagihanPerProdiAndDate(date1, date2));

        // list per angkatan + date
        model.addAttribute("tanggal3", date3);
        model.addAttribute("tanggal4", date4);
        model.addAttribute("listAngkatanDate", tagihanDao.listTagihanPerAngkatanDate(date3, date4));
        model.addAttribute("listAngkatan", tagihanDao.listTagihanPerAngkatan());

    }

    @GetMapping("/studentBill/billAdmin/date")
    public void formDate(Model model, @RequestParam(required = false) String id){

        model.addAttribute("tagihan", tagihanDao.findById(id).get());

    }

    @GetMapping("/studentBill/billAdmin/generate")
    public void main(Model model, @RequestParam(required = false) Prodi prodi, @RequestParam(required = false) TahunAkademik tahunAkademik,
                     @RequestParam(required = false) Program program, @RequestParam(required = false) String angkatan){

        model.addAttribute("selectAngkatan", angkatan);
        model.addAttribute("selectTahun", tahunAkademik);
        model.addAttribute("selectProdi", prodi);
        model.addAttribute("selectProgram", program);

    }

    @GetMapping("/studentBill/billAdmin/form")
    public void formBill(Model model,
                         @RequestParam(required = false) TahunAkademik tahunAkademik,
                         @RequestParam(required = false) String nim){

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        model.addAttribute("newTagihan", new Tagihan());
        model.addAttribute("tahun", tahunAkademik);
        model.addAttribute("nim", mahasiswa.getNim());
        model.addAttribute("mhs", mahasiswa);
        List<NilaiJenisTagihan> nilaiJenisTagihans = nilaiJenisTagihanDao.findByTahunAkademikAndAngkatanAndProdiAndProgramAndStatus(tahunAkademik, mahasiswa.getAngkatan(), mahasiswa.getIdProdi(),mahasiswa.getIdProgram(), StatusRecord.AKTIF);
        model.addAttribute("nilaiJenisTagihan", nilaiJenisTagihans);

    }

    @GetMapping("studentBill/billAdmin/edit")
    public void editBill(Model model, @RequestParam(required = false) String id){
        Tagihan tagihan = tagihanDao.findById(id).get();
        model.addAttribute("tagihan", tagihan);
    }

    @GetMapping("/studentBill/billAdmin/detail")
    public void detailBill(Model model, @RequestParam(required = false) String tagihan,
                           @PageableDefault(size = 10) Pageable page){

        Tagihan tagihan1 = tagihanDao.findById(tagihan).get();

        model.addAttribute("tagihan", tagihan1);
        model.addAttribute("pembayaran", pembayaranDao.findByTagihan(tagihan1));
        model.addAttribute("virtualAccount", virtualAccountDao.findByTagihan(tagihan1, page));

    }

    @GetMapping("/api/jenis")
    @ResponseBody
    public NilaiJenisTagihan njt(@RequestParam(required = false) String id){

        return nilaiJenisTagihanDao.findById(id).get();

    }

    @GetMapping("/studentBill/billAdmin/preview")
    public void previewTagihan(Model model, @RequestParam(required = false) TahunAkademik tahun,
                               @RequestParam(required = false) Prodi prodi,
                               @RequestParam(required = false) Program program,
                               @RequestParam(required = false) String angkatan){

        List<Tagihan> tagihans = tagihanDao.findByNilaiJenisTagihanProdiAndNilaiJenisTagihanProgramAndNilaiJenisTagihanAngkatanAndTahunAkademikAndTanggalPembuatanAndStatusTagihan(prodi, program, angkatan, tahun, LocalDate.now(), StatusTagihan.AKTIF);

        model.addAttribute("tahun", tahun.getId());
        model.addAttribute("prodi", prodi.getId());
        model.addAttribute("program", program.getId());
        model.addAttribute("angkatan", angkatan);
        model.addAttribute("listPreview", tagihans);
        model.addAttribute("statusTagihan", StatusTagihan.values());
    }

    @PostMapping("/studentBill/billAdmin/preview")
    public String preview(@RequestParam(required = false) TahunAkademik tahun, @RequestParam(required = false) Prodi prodi,
                          @RequestParam(required = false) Program program, @RequestParam(required = false) String angkatan,
                          @RequestParam(required = false) String uas, @RequestParam(required = false) String uts,
                          @RequestParam(required = false) String krs, HttpServletRequest request){

        for (Tagihan tgh : tagihanDao.findByNilaiJenisTagihanProdiAndNilaiJenisTagihanProgramAndNilaiJenisTagihanAngkatanAndTahunAkademikAndTanggalPembuatanAndStatusTagihan(prodi, program, angkatan, tahun, LocalDate.now(), StatusTagihan.AKTIF)){
            String pilihan = request.getParameter(tgh.getMahasiswa().getNim() + "nim");
            if (pilihan == null){
                LOGGER.info("Data tidak ditemukan");
            }else{
                StatusTagihan statusTagihan = StatusTagihan.valueOf(pilihan);
                tgh.setStatusTagihan(statusTagihan);
                if (tgh.getStatusTagihan() == StatusTagihan.AKTIF) {
                    LOGGER.debug("data aktif : {}" + tgh.getMahasiswa().getNama());
                    tagihanService.createTagihan(tgh);

                    if (uas != null){
                        EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tgh.getMahasiswa(), StatusRecord.UAS, "0", tahun);
                        if (fitur == null){
                            EnableFiture enableFiture = new EnableFiture();
                            enableFiture.setMahasiswa(tgh.getMahasiswa());
                            enableFiture.setTahunAkademik(tahun);
                            enableFiture.setFitur(StatusRecord.UAS);
                            enableFiture.setEnable("0");
                            enableFiture.setKeterangan("-");
                            enableFitureDao.save(enableFiture);
                        }
                    }
                    if (uts != null){
                        EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tgh.getMahasiswa(), StatusRecord.UTS, "0", tahun);
                        if (fitur == null) {
                            EnableFiture enableFiture = new EnableFiture();
                            enableFiture.setMahasiswa(tgh.getMahasiswa());
                            enableFiture.setTahunAkademik(tahun);
                            enableFiture.setFitur(StatusRecord.UTS);
                            enableFiture.setEnable("0");
                            enableFiture.setKeterangan("-");
                            enableFitureDao.save(enableFiture);
                        }
                    }
                    if (krs != null){
                        EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tgh.getMahasiswa(), StatusRecord.KRS, "0", tahun);
                        if (fitur == null) {
                            EnableFiture enableFiture = new EnableFiture();
                            enableFiture.setMahasiswa(tgh.getMahasiswa());
                            enableFiture.setTahunAkademik(tahun);
                            enableFiture.setFitur(StatusRecord.KRS);
                            enableFiture.setEnable("0");
                            enableFiture.setKeterangan("-");
                            enableFitureDao.save(enableFiture);
                        }
                    }
                }
                if (tgh.getStatusTagihan() == StatusTagihan.LUNAS) {
                    tgh.setStatusTagihan(StatusTagihan.LUNAS);
                    tgh.setLunas(true);
                    tgh.setNilaiTagihan(BigDecimal.ZERO);
                    LOGGER.debug("data lunas : {}" + tgh.getMahasiswa().getNama());

                    if (uas != null){
                        EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tgh.getMahasiswa(), StatusRecord.UAS, "0", tahun);
                        if (fitur == null){
                            EnableFiture enableFiture = new EnableFiture();
                            enableFiture.setMahasiswa(tgh.getMahasiswa());
                            enableFiture.setTahunAkademik(tahun);
                            enableFiture.setFitur(StatusRecord.UAS);
                            enableFiture.setEnable("1");
                            enableFiture.setKeterangan("-");
                            enableFitureDao.save(enableFiture);
                        }
                    }
                    if (uts != null){
                        EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tgh.getMahasiswa(), StatusRecord.UTS, "0", tahun);
                        if (fitur == null) {
                            EnableFiture enableFiture = new EnableFiture();
                            enableFiture.setMahasiswa(tgh.getMahasiswa());
                            enableFiture.setTahunAkademik(tahun);
                            enableFiture.setFitur(StatusRecord.UTS);
                            enableFiture.setEnable("1");
                            enableFiture.setKeterangan("-");
                            enableFitureDao.save(enableFiture);
                        }
                    }
                    if (krs != null){
                        EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tgh.getMahasiswa(), StatusRecord.KRS, "0", tahun);
                        if (fitur == null) {
                            EnableFiture enableFiture = new EnableFiture();
                            enableFiture.setMahasiswa(tgh.getMahasiswa());
                            enableFiture.setTahunAkademik(tahun);
                            enableFiture.setFitur(StatusRecord.KRS);
                            enableFiture.setEnable("1");
                            enableFiture.setKeterangan("-");
                            enableFitureDao.save(enableFiture);

                            Krs cariKrs = krsDao.findByTahunAkademikAndMahasiswaAndStatus(tgh.getTahunAkademik(), tgh.getMahasiswa(), StatusRecord.AKTIF);
                            if (cariKrs == null) {
                                TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByTahunAkademikAndProdi(tgh.getTahunAkademik(), tgh.getMahasiswa().getIdProdi());
                                Krs krs1 = new Krs();
                                krs1.setTahunAkademik(tgh.getTahunAkademik());
                                krs1.setTahunAkademikProdi(tahunAkademikProdi);
                                krs1.setProdi(tgh.getMahasiswa().getIdProdi());
                                krs1.setMahasiswa(tgh.getMahasiswa());
                                krs1.setNim(tgh.getMahasiswa().getNim());
                                krs1.setTanggalTransaksi(LocalDateTime.now());
                                krs1.setStatus(StatusRecord.AKTIF);
                                krsDao.save(krs1);
                            }
                        }
                    }

                }
                if (tgh.getStatusTagihan() == StatusTagihan.DITANGGUHKAN){
                    tgh.setStatus(StatusRecord.NONAKTIF);
                }
                if (tgh.getStatusTagihan() == StatusTagihan.PREVIEW){
                    tgh.setStatus(StatusRecord.PREVIEW);
                }
                tagihanDao.save(tgh);
            }
        }

        return "redirect:list";
    }

    @PostMapping("/studentBill/billAdmin/generate")
    public String generateTagihan(@RequestParam(required = false) Prodi prodi, @RequestParam(required = false) Program program,
                                  @RequestParam(required = false) String angkatan, @RequestParam(required = false) TahunAkademik tahun){

        List<Mahasiswa> mahasiswas = mahasiswaDao.findByIdProdiAndIdProgramAndAngkatanAndStatusAktifAndStatus(prodi, program, angkatan, "AKTIF", StatusRecord.AKTIF);
        for (Mahasiswa mhs : mahasiswas){
            List<NilaiJenisTagihan> nilaiJenisTagihans = nilaiJenisTagihanDao.findByTahunAkademikAndAngkatanAndProdiAndProgramAndStatus(tahun, angkatan, mhs.getIdProdi(), mhs.getIdProgram(), StatusRecord.AKTIF);
            for (NilaiJenisTagihan njt : nilaiJenisTagihans){
                Tagihan tagihan1 = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndLunasAndStatus(mhs, njt.getJenisTagihan(), false, StatusRecord.AKTIF);
                if (tagihan1 == null){
                    Tagihan tagihan = new Tagihan();
                    tagihan.setMahasiswa(mhs);
                    tagihan.setNilaiJenisTagihan(njt);
                    tagihan.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                            + " a.n. " +tagihan.getMahasiswa().getNama());
                    tagihan.setNilaiTagihan(njt.getNilai());
                    tagihan.setTanggalPembuatan(LocalDate.now());
                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                    tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                    tagihan.setTahunAkademik(tahun);
                    tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                    tagihan.setStatus(StatusRecord.AKTIF);
                    tagihanDao.save(tagihan);

                }else if (tagihan1.getTahunAkademik() != tahun){
                    Tagihan tagihan = new Tagihan();
                    tagihan.setMahasiswa(mhs);
                    tagihan.setNilaiJenisTagihan(njt);
                    tagihan.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                            + " a.n. " +tagihan.getMahasiswa().getNama());
                    tagihan.setNilaiTagihan(njt.getNilai().add(tagihan1.getNilaiTagihan()));
                    tagihan.setTanggalPembuatan(LocalDate.now());
                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                    tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                    tagihan.setTahunAkademik(tahun);
                    tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                    tagihan.setStatus(StatusRecord.AKTIF);
                    tagihan.setIdTagihanSebelumnya(tagihan1.getId());
                    tagihanDao.save(tagihan);

                    tagihan1.setStatusTagihan(StatusTagihan.DITANGGUHKAN);
                    tagihan1.setStatus(StatusRecord.NONAKTIF);
                    tagihanDao.save(tagihan1);

                }
            }
        }
        return "redirect:preview?tahun="+tahun.getId()+"&prodi=" +prodi.getId()+"&program="+program.getId()+"&angkatan="+angkatan;
    }

    @PostMapping("/studentBill/billAdmin/form")
    public String inputTagihan(@Valid Tagihan tagihan,
                               RedirectAttributes attributes,
                               @RequestParam(required = false) TahunAkademik tahunAkademik,
                               @RequestParam(required = false) Mahasiswa nim, @RequestParam(required = false) String uas,
                               @RequestParam(required = false) String uts, @RequestParam(required = false) String krs){

        Mahasiswa mhs = nim;
        tagihan.setMahasiswa(mhs);
        Tagihan tgh = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndLunasAndStatus(mhs, tagihan.getNilaiJenisTagihan().getJenisTagihan(), false, StatusRecord.AKTIF);
        if (tgh == null){
            tagihan.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                    + " a.n. " +tagihan.getMahasiswa().getNama());
            tagihan.setTanggalPembuatan(LocalDate.now());
            tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
            tagihan.setTahunAkademik(tahunAkademik);
            tagihan.setStatusTagihan(StatusTagihan.AKTIF);

            tagihanDao.save(tagihan);
            tagihanService.createTagihan(tagihan);

            if (uas != null){
                EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.UAS, "0", tahunAkademik);
                if (fitur == null) {
                    EnableFiture enableFiture = new EnableFiture();
                    enableFiture.setMahasiswa(mhs);
                    enableFiture.setTahunAkademik(tahunAkademik);
                    enableFiture.setFitur(StatusRecord.UAS);
                    enableFiture.setEnable("0");
                    enableFiture.setKeterangan("-");
                    enableFitureDao.save(enableFiture);
                }
            }

            if (uts != null) {
                EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.UTS, "0",  tahunAkademik);
                if (fitur == null){
                    EnableFiture enableFiture = new EnableFiture();
                    enableFiture.setMahasiswa(mhs);
                    enableFiture.setTahunAkademik(tahunAkademik);
                    enableFiture.setFitur(StatusRecord.UTS);
                    enableFiture.setEnable("0");
                    enableFiture.setKeterangan("-");
                    enableFitureDao.save(enableFiture);
                }
            }

            if (krs != null){
                EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, "0", tahunAkademik);
                if (fitur == null){
                    EnableFiture enableFiture = new EnableFiture();
                    enableFiture.setMahasiswa(mhs);
                    enableFiture.setTahunAkademik(tahunAkademik);
                    enableFiture.setFitur(StatusRecord.KRS);
                    enableFiture.setEnable("0");
                    enableFiture.setKeterangan("-");
                    enableFitureDao.save(enableFiture);
                }
            }

        }else if (tgh.getTahunAkademik() != tahunAkademik){
            tagihan.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                    + " a.n. " +tagihan.getMahasiswa().getNama());
            tagihan.setTanggalPembuatan(LocalDate.now());
            tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
            tagihan.setTahunAkademik(tahunAkademik);
            tagihan.setNilaiTagihan(tgh.getNilaiTagihan().add(tagihan.getNilaiTagihan()));
            tagihan.setIdTagihanSebelumnya(tgh.getId());
            tagihan.setStatusTagihan(StatusTagihan.AKTIF);
            tagihanDao.save(tagihan);
            tagihanService.createTagihan(tagihan);

            tgh.setStatus(StatusRecord.NONAKTIF);
            tagihanDao.save(tgh);

            if (uas != null){
                EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.UAS, "0", tahunAkademik);
                if (fitur == null) {
                    EnableFiture enableFiture = new EnableFiture();
                    enableFiture.setMahasiswa(mhs);
                    enableFiture.setTahunAkademik(tahunAkademik);
                    enableFiture.setFitur(StatusRecord.UAS);
                    enableFiture.setEnable("0");
                    enableFiture.setKeterangan("-");
                    enableFitureDao.save(enableFiture);
                }
            }

            if (uts != null) {
                EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.UTS, "0",  tahunAkademik);
                if (fitur == null){
                    EnableFiture enableFiture = new EnableFiture();
                    enableFiture.setMahasiswa(mhs);
                    enableFiture.setTahunAkademik(tahunAkademik);
                    enableFiture.setFitur(StatusRecord.UTS);
                    enableFiture.setEnable("0");
                    enableFiture.setKeterangan("-");
                    enableFitureDao.save(enableFiture);
                }
            }

            if (krs != null){
                EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, "0", tahunAkademik);
                if (fitur == null){
                    EnableFiture enableFiture = new EnableFiture();
                    enableFiture.setMahasiswa(mhs);
                    enableFiture.setTahunAkademik(tahunAkademik);
                    enableFiture.setFitur(StatusRecord.KRS);
                    enableFiture.setEnable("0");
                    enableFiture.setKeterangan("-");
                    enableFitureDao.save(enableFiture);
                }
            }

        }else{
            attributes.addFlashAttribute("gagal", "Data sudah ada!!");
            return "redirect:form?tahunAkademik="+tahunAkademik.getId()+"&nim="+mhs.getId();
        }

        return "redirect:list?tahunAkademik="+tahunAkademik.getId()+"&nim="+mhs.getId();
    }

    @PostMapping("/studentBill/billAdmin/edit")
    public String edit(@RequestParam Tagihan tagihan,
                       @RequestParam(required = false) TahunAkademik tahun,
                       @RequestParam(required = false) Mahasiswa nim, BigDecimal nilaiTagihan,
                       Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Mahasiswa mhs = nim;
        tagihan.setKaryawan(karyawan);
        LOGGER.debug("pengedit : {}" + karyawan);
        tagihan.setNilaiTagihan(nilaiTagihan);
        tagihanDao.save(tagihan);

        return "redirect:list?tahunAkademik="+tahun.getId()+"&nim="+mhs.getNim();
    }

    @PostMapping("/studentBill/billAdmin/date")
    public String formDate(@RequestParam(required = false) String id,
                           Authentication authentication){

        Tagihan tagihan = tagihanDao.findById(id).get();
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        tagihan.setStatus(StatusRecord.AKTIF);
        tagihan.setKaryawan(karyawan);
        tagihanDao.save(tagihan);

        return "redirect:list";
    }

    @PostMapping("/studentBill/billAdmin/delete")
    public String deleteBill(@RequestParam Tagihan tagihan){
        tagihan.setStatus(StatusRecord.HAPUS);
        tagihanDao.save(tagihan);
        return "redirect:list?tahunAkademik="+tagihan.getTahunAkademik().getId()+"&nim="+tagihan.getMahasiswa().getId();
    }

//    Request Penangguhan

    @GetMapping("/studentBill/requestPenangguhan/list")
    public void listPenangguhan(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listPenangguhan", requestPenangguhanDao.findByStatusNotInAndTanggalPenangguhanContainingIgnoreCaseOrderByTagihan(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else {
            model.addAttribute("listPenangguhan", requestPenangguhanDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/studentBill/requestPenangguhan/date")
    public void holdDate(Model model, @RequestParam(required = false) String id){
        Tagihan tagihan = tagihanDao.findById(id).get();
        model.addAttribute("penangguhan", new RequestPenangguhan());
        model.addAttribute("bill", tagihan);

    }

    @GetMapping("/studentBill/requestPenangguhan/approval")
    public void approval(Model model, @RequestParam(required = false) String id,
                         @RequestParam(required = false) Tagihan tagihan){

        RequestPenangguhan requestPenangguhan = requestPenangguhanDao.findById(id).get();
        model.addAttribute("penangguhan", requestPenangguhan);
        model.addAttribute("bill", tagihan);
    }

    @PostMapping("/studentBill/bill/newDate")
    public String newDate(@Valid RequestPenangguhan requestPenangguhan,
                          @RequestParam(required = false) Tagihan tagihan){
        requestPenangguhan.setTagihan(tagihan);
        requestPenangguhan.setStatusApprove(StatusApprove.WAITING);
        requestPenangguhan.setStatus(StatusRecord.AKTIF);
        requestPenangguhanDao.save(requestPenangguhan);
        return "redirect:list";
    }

    @PostMapping("/studentBill/penangguhan/approve")
    public String approvePenangguhan(@RequestParam RequestPenangguhan requestPenangguhan,
                                     @RequestParam(required = false) Tagihan tagihan,
                                     Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        requestPenangguhan.setUserApprove(karyawan);
        requestPenangguhan.setTanggalApprove(LocalDate.now());
        requestPenangguhan.setStatusApprove(StatusApprove.APPROVED);
        requestPenangguhan.setStatus(StatusRecord.AKTIF);
        requestPenangguhanDao.save(requestPenangguhan);

        tagihan.setKaryawan(karyawan);
        tagihan.setTanggalPenangguhan(requestPenangguhan.getTanggalPenangguhan());
        tagihanDao.save(tagihan);
        return "redirect:../requestPenangguhan/list";
    }

    @PostMapping("/studentBill/penangguhan/reject")
    public String rejectPenangguhan(@RequestParam RequestPenangguhan requestPenangguhan,
                                    Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        requestPenangguhan.setUserApprove(karyawan);
        requestPenangguhan.setTanggalApprove(LocalDate.now());
        requestPenangguhan.setStatusApprove(StatusApprove.REJECTED);
        requestPenangguhan.setStatus(StatusRecord.AKTIF);
        requestPenangguhanDao.save(requestPenangguhan);
        return "redirect:../requestPenangguhan/list";
    }

//    Request Cicilan

    @GetMapping("/studentBill/requestCicilan/list")
    public void listCicilan(Model model, @PageableDefault(size = 10) Pageable page, String search){
         if (StringUtils.hasText(search)){
             model.addAttribute("search", search);
             model.addAttribute("listCicilan", requestCicilanDao.findByStatusNotInAndBanyakCicilanContainingIgnoreCase(Arrays.asList(StatusRecord.HAPUS), search, page));
         }else{
             model.addAttribute("listCicilan", requestCicilanDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
         }
    }

    @GetMapping("/studentBill/requestCicilan/angsuran")
    public void cicilanTagihan(Model model, @RequestParam(required = false) String id){
        Tagihan tagihan = tagihanDao.findById(id).get();
        model.addAttribute("cicilan", new RequestCicilan());
        model.addAttribute("bill", tagihan);
    }

    @GetMapping("/studentBill/requestCicilan/approval")
    public void approvalCicilan(Model model, @RequestParam(required = false) String id,
                                  @RequestParam(required = false) Tagihan tagihan){

        model.addAttribute("tagihan", new Tagihan());
        RequestCicilan requestCicilan = requestCicilanDao.findById(id).get();
        model.addAttribute("nilaiCicilan", tagihanDao.pembagianNilaiCicilan(tagihan.getId()));
        model.addAttribute("cicilan", requestCicilan);
        model.addAttribute("bill", tagihan);
    }

    @PostMapping("/studentBill/bill/cicilan")
    public String newCicilan(@Valid RequestCicilan requestCicilan,
                             @RequestParam(required = false) Tagihan tagihan){
        requestCicilan.setTagihan(tagihan);
        requestCicilan.setStatusApprove(StatusApprove.WAITING);
        requestCicilan.setStatus(StatusRecord.AKTIF);
        requestCicilanDao.save(requestCicilan);
        return "redirect:list";
    }

    @PostMapping("/studentBill/cicilan/approve")
    public String approvalCicilan(@RequestParam(required = false) Tagihan bill,
                                  @RequestParam(required = false) BigDecimal nilaiTagihan1,
                                  @RequestParam(required = false) String  tanggalJatuhTempo1,
                                  @RequestParam(required = false) BigDecimal nilaiTagihan2,
                                  @RequestParam(required = false) String tanggalJatuhTempo2,
                                  @RequestParam(required = false) BigDecimal nilaiTagihan3,
                                  @RequestParam(required = false) String tanggalJatuhTempo3,
                                  @RequestParam(required = false) RequestCicilan requestCicilan,
                                  RedirectAttributes attributes,
                                  Authentication authentication){


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        requestCicilan.setUserApprove(karyawan);
        requestCicilan.setTanggalApprove(LocalDate.now());
        requestCicilan.setStatus(StatusRecord.AKTIF);
        requestCicilan.setStatusApprove(StatusApprove.APPROVED);
        requestCicilanDao.save(requestCicilan);

        bill.setStatus(StatusRecord.NONAKTIF);
        bill.setStatusTagihan(StatusTagihan.DICICIL);
        tagihanDao.save(bill);

        tagihanService.hapusTagihan(bill);

            if (requestCicilan.getBanyakCicilan() == 2){

                LocalDate tgl1 = LocalDate.parse(tanggalJatuhTempo1,formatter);
                LocalDate tgl2 = LocalDate.parse(tanggalJatuhTempo2,formatter);
                Tagihan tagihan1 = new Tagihan();
                tagihan1.setNilaiJenisTagihan(bill.getNilaiJenisTagihan());
                tagihan1.setMahasiswa(bill.getMahasiswa());
                tagihan1.setTanggalPembuatan(LocalDate.now());
                tagihan1.setTanggalPenangguhan(tgl1);
                tagihan1.setKeterangan(bill.getKeterangan());
                tagihan1.setTahunAkademik(bill.getTahunAkademik());
                tagihan1.setStatus(StatusRecord.AKTIF);
                tagihan1.setTanggalJatuhTempo(tgl1);
                tagihan1.setNilaiTagihan(nilaiTagihan1);
                tagihan1.setStatusTagihan(StatusTagihan.AKTIF);
                tagihan1.setIdTagihanSebelumnya(bill.getId());
                tagihanDao.save(tagihan1);

                tagihanService.createTagihan(tagihan1);

                Tagihan tagihan2 = new Tagihan();
                tagihan2.setNilaiJenisTagihan(bill.getNilaiJenisTagihan());
                tagihan2.setMahasiswa(bill.getMahasiswa());
                tagihan2.setTanggalPembuatan(LocalDate.now());
                tagihan2.setTanggalPenangguhan(tgl2);
                tagihan2.setKeterangan(bill.getKeterangan());
                tagihan2.setTahunAkademik(bill.getTahunAkademik());
                tagihan2.setStatus(StatusRecord.AKTIF);
                tagihan2.setTanggalJatuhTempo(tgl2);
                tagihan2.setNilaiTagihan(nilaiTagihan2);
                tagihan2.setStatusTagihan(StatusTagihan.CICILAN_1);
                tagihan2.setIdTagihanSebelumnya(bill.getId());
                tagihanDao.save(tagihan2);


            }

            if (requestCicilan.getBanyakCicilan() == 3) {

                LocalDate tgl1 = LocalDate.parse(tanggalJatuhTempo1,formatter);
                LocalDate tgl2 = LocalDate.parse(tanggalJatuhTempo2,formatter);
                LocalDate tgl3 = LocalDate.parse(tanggalJatuhTempo3,formatter);

                Tagihan tagihan1 = new Tagihan();
                tagihan1.setNilaiJenisTagihan(bill.getNilaiJenisTagihan());
                tagihan1.setMahasiswa(bill.getMahasiswa());
                tagihan1.setTanggalPembuatan(LocalDate.now());
                tagihan1.setTanggalPenangguhan(tgl1);
                tagihan1.setKeterangan(bill.getKeterangan());
                tagihan1.setTahunAkademik(bill.getTahunAkademik());
                tagihan1.setStatus(StatusRecord.AKTIF);
                tagihan1.setTanggalJatuhTempo(tgl1);
                tagihan1.setNilaiTagihan(nilaiTagihan1);
                tagihan1.setStatusTagihan(StatusTagihan.AKTIF);
                tagihan1.setIdTagihanSebelumnya(bill.getId());
                tagihanDao.save(tagihan1);

                tagihanService.createTagihan(tagihan1);

                Tagihan tagihan2 = new Tagihan();
                tagihan2.setNilaiJenisTagihan(bill.getNilaiJenisTagihan());
                tagihan2.setMahasiswa(bill.getMahasiswa());
                tagihan2.setTanggalPembuatan(LocalDate.now());
                tagihan2.setTanggalPenangguhan(tgl2);
                tagihan2.setKeterangan(bill.getKeterangan());
                tagihan2.setTahunAkademik(bill.getTahunAkademik());
                tagihan2.setStatus(StatusRecord.AKTIF);
                tagihan2.setTanggalJatuhTempo(tgl2);
                tagihan2.setNilaiTagihan(nilaiTagihan2);
                tagihan2.setStatusTagihan(StatusTagihan.CICILAN_1);
                tagihan2.setIdTagihanSebelumnya(bill.getId());
                tagihanDao.save(tagihan2);


                Tagihan tagihan3 = new Tagihan();
                tagihan3.setNilaiJenisTagihan(bill.getNilaiJenisTagihan());
                tagihan3.setMahasiswa(bill.getMahasiswa());
                tagihan3.setTanggalPembuatan(LocalDate.now());
                tagihan3.setTanggalPenangguhan(tgl3);
                tagihan3.setKeterangan(bill.getKeterangan());
                tagihan3.setTahunAkademik(bill.getTahunAkademik());
                tagihan3.setStatus(StatusRecord.AKTIF);
                tagihan3.setTanggalJatuhTempo(tgl3);
                tagihan3.setNilaiTagihan(nilaiTagihan3);
                tagihan3.setStatusTagihan(StatusTagihan.CICILAN_2);
                tagihan3.setIdTagihanSebelumnya(bill.getId());
                tagihanDao.save(tagihan3);


            }


        return "redirect:../requestCicilan/list";

    }

    @PostMapping("/studentBill/cicilan/reject")
    public String rejectCicilan(@RequestParam RequestCicilan requestCicilan,
                                @RequestParam String keteranganReject,
                                Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        requestCicilan.setUserApprove(karyawan);
        requestCicilan.setTanggalApprove(LocalDate.now());
        requestCicilan.setKeteranganReject(keteranganReject);
        requestCicilan.setStatusApprove(StatusApprove.REJECTED);
        requestCicilan.setStatus(StatusRecord.AKTIF);
        requestCicilanDao.save(requestCicilan);
        return "redirect:../requestCicilan/list";
    }

//    Pembayaran

    @GetMapping("/studentBill/payment/form")
    public void formPayment(Model model, @RequestParam(required = false) String tagihan, @PageableDefault(size = 10) Pageable page ){
        Tagihan tagihan1 = tagihanDao.findById(tagihan).get();

        model.addAttribute("pembayaran", pembayaranDao.findByTagihan(tagihan1));
        model.addAttribute("bank", bankDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("virtualAccount", virtualAccountDao.findByTagihan(tagihan1, page));
        model.addAttribute("tagihan", tagihan1);
        model.addAttribute("penangguhan", requestPenangguhanDao.findByTagihanAndStatus(tagihan1, StatusRecord.AKTIF));
        model.addAttribute("cicilan", requestCicilanDao.findByTagihanAndStatus(tagihan1, StatusRecord.AKTIF));

    }

    @GetMapping("/studentBill/payment/manual")
    public void manualPayment(Model model, @RequestParam(required = false) String id){

        Pembayaran p = new Pembayaran();
        Tagihan t = tagihanDao.findById(id).get();
        if (t != null){
            p.setAmount(t.getNilaiTagihan());
            p.setTagihan(t);
        }
        model.addAttribute("pembayaran", p);
        model.addAttribute("tagihan", t);
        model.addAttribute("bank", bankDao.findByStatus(StatusRecord.AKTIF));
    }

    @PostMapping("/studentBill/payment/manual")
    public String paymentManual(@ModelAttribute @Valid Pembayaran pembayaran,
                                MultipartFile fileBukti) throws IOException {

        String idPeserta = pembayaran.getTagihan().getMahasiswa().getNim();

        String namaFile = fileBukti.getName();
        String jenisFile = fileBukti.getContentType();
        String namaAsli = fileBukti.getOriginalFilename();
        Long ukuran = fileBukti.getSize();

        LOGGER.debug("nama file : {}" + namaFile);
        LOGGER.debug("jenis file : {}" + jenisFile);
        LOGGER.debug("nama asli file : {}" + namaAsli);
        LOGGER.debug("ukuran file : {}" + ukuran);

        // Memisahkan extension
        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

        if (i > p){
            extension = namaAsli.substring(i + 1);
        }

        String idFile = UUID.randomUUID().toString();
        String lokasiUpload = uploadFolder + File.separator + idPeserta;
        LOGGER.debug("Lokasi Upload : {}" + lokasiUpload);
        new File(lokasiUpload).mkdirs();
        File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
        pembayaran.setReferensi(idFile + "." + extension);
        fileBukti.transferTo(tujuan);
        LOGGER.debug("file sudah dicopy ke : {}" + tujuan.getAbsolutePath());

        Tagihan tagihan = pembayaran.getTagihan();
        LOGGER.debug("bank : " + pembayaran.getBank());
        VirtualAccount va = virtualAccountDao.findByBankIdAndTagihan(pembayaran.getBank().getId(), tagihan);
        LOGGER.debug("va : " + va);
        pembayaran.setWaktuBayar(LocalDateTime.now());
        pembayaran.setAmount(tagihan.getNilaiTagihan());
        pembayaranDao.save(pembayaran);

        tagihan.setStatusTagihan(StatusTagihan.LUNAS);
        tagihan.setLunas(true);
        tagihanDao.save(tagihan);

        return "redirect:../billAdmin/list?tahunAkademik="+tagihan.getTahunAkademik().getId()+"&nim="+tagihan.getMahasiswa().getNim();
    }

    @GetMapping("/panduanPembayaran")
    public void getPanduanPembayaran(HttpServletResponse response) throws Exception{
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Panduan_Pembayaran.pdf");
        FileCopyUtils.copy(panduanPembayaran.getInputStream(), response.getOutputStream());
        response.getOutputStream().flush();
    }

    @GetMapping("/studentBill/payment/report")
    public void report(){

    }

//    Cetak bukti pembayaran

    @GetMapping("/buktiPembayaran")
    public void buktiPembayaran(@RequestParam(required = false) Pembayaran pembayaran,
                                HttpServletResponse response){
        try {
            // 0. Setup converter
            Options options = Options.getFrom(DocumentKind.ODT).to(ConverterTypeTo.PDF);

            // 1. Load template dari file
            InputStream in = templateKwitansi.getInputStream();

            // 2. Inisialisasi template engine, menentukan sintaks penulisan variabel
            IXDocReport report = XDocReportRegistry.getRegistry().
                    loadReport(in, TemplateEngineKind.Freemarker);

            // 3. Context object, untuk mengisi variabel

            IContext ctx = report.createContext();
            ctx.put("tglBayar", pembayaran.getWaktuBayar());
            ctx.put("nama", pembayaran.getTagihan().getMahasiswa().getNama());
            ctx.put("program", pembayaran.getTagihan().getMahasiswa().getIdProdi().getIdJenjang().getNamaJenjang()+" - "+pembayaran.getTagihan().getMahasiswa().getIdProdi().getNamaProdi());
            ctx.put("email", pembayaran.getTagihan().getMahasiswa().getEmailPribadi());
            if (pembayaran.getTagihan().getMahasiswa().getTeleponSeluler() == null) {
                ctx.put("noHp", "-");
            }else{
                ctx.put("noHp", pembayaran.getTagihan().getMahasiswa().getTeleponSeluler());
            }
            ctx.put("noTagihan", pembayaran.getTagihan().getNomor());
            ctx.put("bank", pembayaran.getBank().getNama());
            ctx.put("jenisBiaya", pembayaran.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getNama());
            ctx.put("nilai", pembayaran.getAmount());
            ctx.put("tanggal", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

            response.setHeader("Content-Disposition", "attachment;filename=bukti_pembayaran_"+pembayaran.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getNama()+".pdf");
            OutputStream out = response.getOutputStream();
            report.  convert(ctx, options, out);
            out.flush();
        } catch (Exception err){
            LOGGER.error(err.getMessage(), err);
        }
    }

}