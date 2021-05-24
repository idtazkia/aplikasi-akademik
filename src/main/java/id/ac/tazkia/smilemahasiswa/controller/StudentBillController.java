package id.ac.tazkia.smilemahasiswa.controller;


import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.payment.DaftarTagihanPerProdiDto;
import id.ac.tazkia.smilemahasiswa.dto.payment.UploadBerkasDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.TagihanService;
import jdk.net.SocketFlow;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.crosstabs.fill.JRPercentageCalculatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import javax.mail.Multipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller @Slf4j
public class StudentBillController {

    @Autowired
    private BankDao bankDao;

    @Autowired
    private JenisDiskonDao jenisDiskonDao;

    @Autowired
    private DiskonDao diskonDao;

    @Autowired
    private TagihanDao tagihanDao;

    @Autowired
    private TagihanDocumentDao tagihanDocumentDao;

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
    private MahasiswaBeasiswaDao mahasiswaBeasiswaDao;

    @Autowired
    private TagihanBeasiswaDao tagihanBeasiswaDao;

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

    @Value("${upload.berkasCicilan}")
    private String uploadBerkasCicilan;

    @Value("${upload.berkasPenangguhan}")
    private String uploadBerkasPenangguhan;

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademik> tahunAkademik() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() { return mahasiswaDao.cariAngkatan(); }

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

    @GetMapping("/studentBill/bill/list")
    public void listBill(Model model, @PageableDefault(size = 10) Pageable page,
                           Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mhs = mahasiswaDao.findByUser(user);
        TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByStatusAndProdi(StatusRecord.AKTIF, mhs.getIdProdi());

        model.addAttribute("mahasiswa", mhs);
        model.addAttribute("tahunAkademikProdi", tahunAkademikProdi);
        model.addAttribute("biayaMahasiswa", tagihanDao.biayaMahasiswa(mhs.getId()));
        model.addAttribute("pembayaran", pembayaranDao.pembayaranMahasiswa(mhs.getId()));
        model.addAttribute("totalTagihan", tagihanDao.totalTagihanPerMahasiswa(mhs.getId()));
        model.addAttribute("totalDibayar", pembayaranDao.totalDibayarMahasiswa(mhs.getId()));

    }

    @GetMapping("/studentBill/billAdmin/list")
    public void listBillAdmin(Model model,
                              @RequestParam(required = false) TahunAkademik tahunAkademik,
                              @RequestParam(required = false) String nim, @PageableDefault(size = 10) Pageable page,
                              @RequestParam(required = false) String date1, @RequestParam(required = false) String date2,
                              @RequestParam(required = false) String date3, @RequestParam(required = false) String date4){

        // list untuk per mahasiswa

        model.addAttribute("selectTahun", tahunAkademik);
        model.addAttribute("selectNim", nim);
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        if (mhs != null){
            model.addAttribute("mhs", mhs);
            if (tahunAkademik != null && mhs != null) {
                model.addAttribute("sisaTagihan", tagihanDao.sisaTagihanQuery(tahunAkademik.getId(),mhs.getId()));
                model.addAttribute("daftarBiaya", tagihanDao.findByStatusNotInAndMahasiswaAndTahunAkademik(Arrays.asList(StatusRecord.HAPUS), mhs, tahunAkademik, page));
                model.addAttribute("daftarPembayaran", pembayaranDao.daftarPembayaran(tahunAkademik.getId(), mhs.getId()));
                model.addAttribute("status", krsDao.findByTahunAkademikAndMahasiswaAndStatus(tahunAkademik, mhs, StatusRecord.AKTIF));
                model.addAttribute("mahasiswa", mhs);
                model.addAttribute("jumlahSks", krsDetailDao.jumlahSksMahasiswa(mhs.getId(), tahunAkademik.getId()));
                model.addAttribute("totalTagihan", tagihanDao.totalTagihanPerTahunAkademikDanMahasiswa(tahunAkademik.getId(), mhs.getId()));
                model.addAttribute("totalDibayar", pembayaranDao.totalDibayarPerTahunDanMahasiswa(tahunAkademik.getId(), mhs.getId()));
            }
        }
        if (mhs == null){
            model.addAttribute("message","message");
        }

        if (tahunAkademik == null){
            model.addAttribute("tahun", "tahun");
        }else {
            // list per prodi
            List<DaftarTagihanPerProdiDto> listProdi = tagihanDao.listTagihanPerProdi(tahunAkademik);
            model.addAttribute("listProdi", listProdi);
            model.addAttribute("toTagihan", tagihanDao.totalTagihan(tahunAkademik));
            model.addAttribute("toDibayar", pembayaranDao.totalDibayar(tahunAkademik));
            model.addAttribute("tahunPilihan", tahunAkademik);

            // list per prodi + date
            model.addAttribute("tanggal1", date1);
            model.addAttribute("tanggal2", date2);
            model.addAttribute("listProdiDate", tagihanDao.listTagihanPerProdiAndDate(date1, date2, tahunAkademik));

            // list per angkatan + date
            model.addAttribute("tanggal3", date3);
            model.addAttribute("tanggal4", date4);
            model.addAttribute("listAngkatanDate", tagihanDao.listTagihanPerAngkatanDate(date3, date4, tahunAkademik));
            model.addAttribute("listAngkatan", tagihanDao.listTagihanPerAngkatan(tahunAkademik));

            // list klasifikasi piutang
            model.addAttribute("listPiutang", tagihanDao.listPiutang(tahunAkademik.getId()));

        }


    }

//    public double calculatePercentage(double obstance, double total){
//        return obstance * 100 / total;
//    }

    @GetMapping("/studentBill/billAdmin/date")
    public void formDate(Model model, @RequestParam(required = false) String id){

        model.addAttribute("tagihan", tagihanDao.findById(id).get());

    }

    @GetMapping("/api/list")
    @ResponseBody
    public List<DaftarTagihanPerProdiDto> daftarTagihan(Model model, @RequestParam(required = false) String id){

        TahunAkademik tahun = tahunAkademikDao.findById(id).get();

//        model.addAttribute("listProdi", tagihanDao.listTagihanPerProdi(tahun));
//        model.addAttribute("toTagihan", tagihanDao.totalTagihan(tahun));
//        model.addAttribute("toDibayar", pembayaranDao.totalDibayar(tahun));

        return tagihanDao.listTagihanPerProdi(tahun);

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
        StatusTagihan info = tagihan1.getStatusTagihan();
        RequestCicilan cekCicilan = requestCicilanDao.cariCicilan(tagihan);
        if (info == StatusTagihan.DICICIL){
            model.addAttribute("message", "message");
        }

        model.addAttribute("cekJumlahPembayaran", pembayaranDao.countAllByTagihan(tagihan1));
        model.addAttribute("pembayaran", pembayaranDao.cekPembayaran(tagihan));
        model.addAttribute("detailPembayaran", pembayaranDao.findByTagihanAndStatus(tagihan1, StatusRecord.AKTIF, page));
        model.addAttribute("tagihan", tagihan1);
        model.addAttribute("virtualAccount", virtualAccountDao.listVa(tagihan1.getId()));
        if (cekCicilan != null){
            model.addAttribute("cekCicilan", "cicilan");
        }

        // bagian keterangan cicilan
        model.addAttribute("cicilan", requestCicilanDao.findByStatusNotInAndTagihanOrderByTanggalJatuhTempo(Arrays.asList(StatusRecord.HAPUS), page, tagihan1));

    }

    @GetMapping("/studentBill/billAdmin/detailProdi")
    public void detailProdi(Model model, @RequestParam(required = false) Prodi prodi,
                            @RequestParam(required = false) TahunAkademik tahunAkademik){

        model.addAttribute("prodi", prodi);
        model.addAttribute("tagihanProdi", tagihanDao.listTagihanPerMahasiswaByProdi(prodi.getId(), tahunAkademik.getId()));
        model.addAttribute("tahun", tahunAkademik);

    }

    @GetMapping("/studentBill/billAdmin/detailAngkatan")
    public void detailAngkatan(Model model, @RequestParam(required = false) String angkatan,
                               @RequestParam(required = false) TahunAkademik tahunAkademik){

        model.addAttribute("angkatan", angkatan);
        model.addAttribute("tagihanAngkatan", tagihanDao.listTagihanPerMahasiswaByAngkatan(angkatan, tahunAkademik.getId()));
        model.addAttribute("tahun", tahunAkademik);

    }

    @GetMapping("/studentBill/billAdmin/detailPiutang")
    public void detailPiutang(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik,
                              @RequestParam(required = false) String selisih){

        model.addAttribute("tahun", tahunAkademik);
        model.addAttribute("detailPiutang", tagihanDao.detailPiutang(tahunAkademik.getId(), selisih));

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

        List<Tagihan> tagihans = tagihanDao.findByNilaiJenisTagihanProdiAndNilaiJenisTagihanProgramAndNilaiJenisTagihanAngkatanAndTahunAkademikAndTanggalPembuatanAndStatusTagihanOrderByMahasiswaNama(prodi, program, angkatan, tahun, LocalDate.now(), StatusTagihan.AKTIF);

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

        for (Tagihan tgh : tagihanDao.findByNilaiJenisTagihanProdiAndNilaiJenisTagihanProgramAndNilaiJenisTagihanAngkatanAndTahunAkademikAndTanggalPembuatanAndStatusTagihanOrderByMahasiswaNama(prodi, program, angkatan, tahun, LocalDate.now(), StatusTagihan.AKTIF)){
            String pilihan = request.getParameter(tgh.getMahasiswa().getNim() + "nim");
            if (pilihan == null){
                log.info("Data tidak ditemukan");
                continue;
            }

            StatusTagihan statusTagihan = StatusTagihan.valueOf(pilihan);
            tgh.setStatusTagihan(statusTagihan);
            if (tgh.getStatusTagihan() == StatusTagihan.AKTIF) {
                log.debug("data aktif : {}" + tgh.getMahasiswa().getNama());
                tagihanService.requestCreateTagihan(tgh);

                if (uas != null){
                    EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tgh.getMahasiswa(),
                            StatusRecord.UAS, false, tahun);
                    if (fitur == null){
                        createEnableFitur(tahun, tgh, StatusRecord.UAS, false);
                    }
                }
                if (uts != null){
                    EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tgh.getMahasiswa(),
                            StatusRecord.UTS, false, tahun);
                    if (fitur == null) {
                        createEnableFitur(tahun, tgh, StatusRecord.UTS, false);
                    }
                }
                if (krs != null){
                    EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tgh.getMahasiswa(),
                            StatusRecord.KRS, false, tahun);
                    if (fitur == null) {
                        createEnableFitur(tahun, tgh, StatusRecord.KRS, false);
                    }
                }
            }
            if (tgh.getStatusTagihan() == StatusTagihan.LUNAS) {
                tgh.setStatusTagihan(StatusTagihan.LUNAS);
                tgh.setLunas(true);
                tgh.setNilaiTagihan(BigDecimal.ZERO);
                log.debug("data lunas : {}" + tgh.getMahasiswa().getNama());

                if (uas != null){
                    EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tgh.getMahasiswa(),
                            StatusRecord.UAS, false, tahun);
                    if (fitur == null){
                        createEnableFitur(tahun, tgh, StatusRecord.UAS, true);
                    }
                }
                if (uts != null){
                    EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tgh.getMahasiswa(),
                            StatusRecord.UTS, false, tahun);
                    if (fitur == null) {
                        createEnableFitur(tahun, tgh, StatusRecord.UTS, true);
                    }
                }
                if (krs != null){
                    EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tgh.getMahasiswa(),
                            StatusRecord.KRS, false, tahun);
                    if (fitur == null) {
                        createEnableFitur(tahun, tgh, StatusRecord.KRS, true);

                        Krs cariKrs = krsDao.findByTahunAkademikAndMahasiswaAndStatus(tgh.getTahunAkademik(), tgh.getMahasiswa(), StatusRecord.AKTIF);
                        if (cariKrs == null) {
                            TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByTahunAkademikAndProdi(tgh.getTahunAkademik(), tgh.getMahasiswa().getIdProdi());
                            tagihanService.createKrs(tgh, tahunAkademikProdi);
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

        return "redirect:list";
    }

    @PostMapping("/studentBill/billAdmin/generate")
    public String generateTagihan(@RequestParam(required = false) Prodi prodi, @RequestParam(required = false) Program program,
                                  @RequestParam(required = false) String angkatan, @RequestParam(required = false) TahunAkademik tahun){

        List<Mahasiswa> mahasiswas = mahasiswaDao.findByIdProdiAndIdProgramAndAngkatanAndStatusAktifAndStatus(prodi, program, angkatan, "AKTIF", StatusRecord.AKTIF);
        for (Mahasiswa mhs : mahasiswas){
            MahasiswaBeasiswa cariMahasiswa = mahasiswaBeasiswaDao.mBeasiswa(mhs.getId());
            if (cariMahasiswa != null){
//                System.out.println("mahasiswa beasiswa " + cariMahasiswa.getMahasiswa().getNim());
                List<MahasiswaBeasiswa> cariBeasiswa = mahasiswaBeasiswaDao.findByMahasiswaAndStatus(mhs, StatusRecord.AKTIF);
                for (MahasiswaBeasiswa mb : cariBeasiswa){
                    List<TagihanBeasiswa> cariTagihan = tagihanBeasiswaDao.findByBeasiswaAndStatus(mb.getBeasiswa(), StatusRecord.AKTIF);
                    for (TagihanBeasiswa tb : cariTagihan){
                        NilaiJenisTagihan njt = nilaiJenisTagihanDao.findByJenisTagihanIdAndTahunAkademikAndProdiAndAngkatanAndProgramAndStatus(tb.getJenisTagihan().getId(), tahun, mhs.getIdProdi(), angkatan, mhs.getIdProgram(), StatusRecord.AKTIF);
                        if (njt == null){
                            log.info("Tidak ada tagihan untuk {} ", tb.getJenisTagihan().getNama());
                        }else{
                            Tagihan tagihan1 = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndStatus(mhs, njt.getJenisTagihan(), StatusRecord.AKTIF);
                            if (tagihan1 == null){
                                String keteranganTagihan = "Tagihan " + njt.getJenisTagihan().getNama()
                                        + " a.n. " + mhs.getNama();

                                Tagihan tagihan = new Tagihan();
                                tagihan.setMahasiswa(mhs);
                                tagihan.setNilaiJenisTagihan(njt);
                                tagihan.setKeterangan(keteranganTagihan);
                                tagihan.setNilaiTagihan(njt.getNilai());
                                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                tagihan.setTanggalPembuatan(LocalDate.now());
                                tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                tagihan.setTahunAkademik(tahun);
                                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                tagihan.setStatus(StatusRecord.AKTIF);
                                tagihanDao.save(tagihan);
                            }
                        }
                    }
                }
            } else if (cariMahasiswa == null){
                List<NilaiJenisTagihan> nilaiJenisTagihans = nilaiJenisTagihanDao.findByTahunAkademikAndAngkatanAndProdiAndProgramAndStatus(tahun, angkatan, mhs.getIdProdi(), mhs.getIdProgram(), StatusRecord.AKTIF);
                for (NilaiJenisTagihan njt : nilaiJenisTagihans) {
                    Tagihan tagihan1 = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndStatus(mhs, njt.getJenisTagihan(), StatusRecord.AKTIF);
                    if (tagihan1 == null) {
                        String keteranganTagihan = "Tagihan " + njt.getJenisTagihan().getNama()
                                + " a.n. " + mhs.getNama();

                        Tagihan tagihan = new Tagihan();
                        tagihan.setMahasiswa(mhs);
                        tagihan.setNilaiJenisTagihan(njt);
                        tagihan.setKeterangan(keteranganTagihan);
                        tagihan.setNilaiTagihan(njt.getNilai());
                        tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                        tagihan.setTanggalPembuatan(LocalDate.now());
                        tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                        tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                        tagihan.setTahunAkademik(tahun);
                        tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                        tagihan.setStatus(StatusRecord.AKTIF);
                        tagihanDao.save(tagihan);

                    } else if (tagihan1.getTahunAkademik() != tahun && tagihan1.getLunas() == false) {
                        Tagihan tagihan = new Tagihan();
                        tagihan.setMahasiswa(mhs);
                        tagihan.setNilaiJenisTagihan(njt);
                        tagihan.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                                + " a.n. " + tagihan.getMahasiswa().getNama());
                        tagihan.setNilaiTagihan(njt.getNilai().add(tagihan1.getNilaiTagihan()));
                        tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                        tagihan.setTanggalPembuatan(LocalDate.now());
                        tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                        tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                        tagihan.setTahunAkademik(tahun);
                        tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                        tagihan.setStatus(StatusRecord.AKTIF);
                        tagihan.setIdTagihanSebelumnya(tagihan1.getId());
                        tagihanDao.save(tagihan);

                        tagihan1.setStatusTagihan(StatusTagihan.NONAKTIF);
                        tagihan1.setStatus(StatusRecord.NONAKTIF);
                        tagihanDao.save(tagihan1);
                    }
                }
            }
        }
//        return "redirect:generate";
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
        Tagihan tgh = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndStatusAndLunas(mhs, tagihan.getNilaiJenisTagihan().getJenisTagihan(), StatusRecord.AKTIF, false);
        if (tgh == null){
            tagihan.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                    + " a.n. " +tagihan.getMahasiswa().getNama());
            tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
            tagihan.setTanggalPembuatan(LocalDate.now());
            tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
            tagihan.setTahunAkademik(tahunAkademik);
            tagihan.setStatusTagihan(StatusTagihan.AKTIF);

            tagihanDao.save(tagihan);
            tagihanService.requestCreateTagihan(tagihan);

            if (uas != null){
                EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.UAS, false, tahunAkademik);
                if (fitur == null) {
                    EnableFiture enableFiture = new EnableFiture();
                    enableFiture.setMahasiswa(mhs);
                    enableFiture.setTahunAkademik(tahunAkademik);
                    enableFiture.setFitur(StatusRecord.UAS);
                    enableFiture.setEnable(false);
                    enableFiture.setKeterangan("-");
                    enableFitureDao.save(enableFiture);
                }
            }

            if (uts != null) {
                EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.UTS, false,  tahunAkademik);
                if (fitur == null){
                    EnableFiture enableFiture = new EnableFiture();
                    enableFiture.setMahasiswa(mhs);
                    enableFiture.setTahunAkademik(tahunAkademik);
                    enableFiture.setFitur(StatusRecord.UTS);
                    enableFiture.setEnable(false);
                    enableFiture.setKeterangan("-");
                    enableFitureDao.save(enableFiture);
                }
            }

            if (krs != null){
                EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, false, tahunAkademik);
                if (fitur == null){
                    EnableFiture enableFiture = new EnableFiture();
                    enableFiture.setMahasiswa(mhs);
                    enableFiture.setTahunAkademik(tahunAkademik);
                    enableFiture.setFitur(StatusRecord.KRS);
                    enableFiture.setEnable(false);
                    enableFiture.setKeterangan("-");
                    enableFitureDao.save(enableFiture);
                }
            }

        }else if (tgh.getTahunAkademik() != tahunAkademik && tgh.getLunas() == false){
            tagihan.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                    + " a.n. " +tagihan.getMahasiswa().getNama());
            tagihan.setTanggalPembuatan(LocalDate.now());
            tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
            tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
            tagihan.setTahunAkademik(tahunAkademik);
            tagihan.setNilaiTagihan(tgh.getNilaiTagihan().add(tagihan.getNilaiTagihan()));
            tagihan.setIdTagihanSebelumnya(tgh.getId());
            tagihan.setStatusTagihan(StatusTagihan.AKTIF);
            tagihanDao.save(tagihan);
            tagihanService.requestCreateTagihan(tagihan);

            tgh.setStatus(StatusRecord.NONAKTIF);
            tagihanDao.save(tgh);

            if (uas != null){
                EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.UAS, false, tahunAkademik);
                if (fitur == null) {
                    EnableFiture enableFiture = new EnableFiture();
                    enableFiture.setMahasiswa(mhs);
                    enableFiture.setTahunAkademik(tahunAkademik);
                    enableFiture.setFitur(StatusRecord.UAS);
                    enableFiture.setEnable(false);
                    enableFiture.setKeterangan("-");
                    enableFitureDao.save(enableFiture);
                }
            }

            if (uts != null) {
                EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.UTS, false,  tahunAkademik);
                if (fitur == null){
                    EnableFiture enableFiture = new EnableFiture();
                    enableFiture.setMahasiswa(mhs);
                    enableFiture.setTahunAkademik(tahunAkademik);
                    enableFiture.setFitur(StatusRecord.UTS);
                    enableFiture.setEnable(false);
                    enableFiture.setKeterangan("-");
                    enableFitureDao.save(enableFiture);
                }
            }

            if (krs != null){
                EnableFiture fitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, false, tahunAkademik);
                if (fitur == null){
                    EnableFiture enableFiture = new EnableFiture();
                    enableFiture.setMahasiswa(mhs);
                    enableFiture.setTahunAkademik(tahunAkademik);
                    enableFiture.setFitur(StatusRecord.KRS);
                    enableFiture.setEnable(false);
                    enableFiture.setKeterangan("-");
                    enableFitureDao.save(enableFiture);
                }
            }

        }else{
            attributes.addFlashAttribute("gagal", "Data sudah ada!!");
            return "redirect:form?tahunAkademik="+tahunAkademik.getId()+"&nim="+mhs.getNim();
        }

        return "redirect:list?tahunAkademik="+tahunAkademik.getId()+"&nim="+mhs.getNim();
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
        log.debug("pengedit : {}" + karyawan);
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
        tagihanService.hapusTagihan(tagihan);
        return "redirect:list?tahunAkademik="+tagihan.getTahunAkademik().getId()+"&nim="+tagihan.getMahasiswa().getNim();
    }

//    Request Penangguhan

    @GetMapping("/studentBill/requestPenangguhan/list")
    public void listPenangguhan(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listPenangguhan", requestPenangguhanDao.findByStatusNotInAndTanggalPenangguhanContainingIgnoreCaseOrderByTanggalPenangguhan(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else {
            model.addAttribute("listPenangguhan", requestPenangguhanDao.findByStatusNotInOrderByTanggalPengajuanDesc(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/studentBill/requestPenangguhan/date")
    public void holdDate(Model model, @RequestParam(required = false) String id, @PageableDefault(size = 10) Pageable page){
        Tagihan tagihan = tagihanDao.findById(id).get();
        model.addAttribute("uploadBerkas", new UploadBerkasDto());
        model.addAttribute("penangguhan", new RequestPenangguhan());
        model.addAttribute("bill", tagihan);
        model.addAttribute("tahun", tahunProdiDao.findByStatusAndProdi(StatusRecord.AKTIF, tagihan.getMahasiswa().getIdProdi()));
        model.addAttribute("jumlahFile", tagihanDocumentDao.countAllByTagihanAndStatusAndStatusDocument(tagihan, StatusRecord.AKTIF, StatusDocument.PENANGGUHAN));
        model.addAttribute("dokumen", tagihanDocumentDao.findByStatusNotInAndTagihanAndStatusDocument(Arrays.asList(StatusRecord.HAPUS), tagihan, StatusDocument.PENANGGUHAN, page));
    }

    @GetMapping("/studentBill/requestPenangguhan/approval")
    public void approval(Model model, @RequestParam(required = false) String id,
                         @RequestParam(required = false) Tagihan tagihan, @PageableDefault(size = 10) Pageable page){

        RequestPenangguhan requestPenangguhan = requestPenangguhanDao.findById(id).get();
        model.addAttribute("penangguhan", requestPenangguhan);
        model.addAttribute("bill", tagihan);
        model.addAttribute("dokumen", tagihanDocumentDao.findByStatusNotInAndTagihanAndStatusDocument(Arrays.asList(StatusRecord.HAPUS), tagihan, StatusDocument.PENANGGUHAN, page));
    }

    @PostMapping("/studentBill/bill/newDate")
    public String newDate(@Valid RequestPenangguhan requestPenangguhan,
                          @RequestParam(required = false) Tagihan tagihan){

        requestPenangguhan.setTagihan(tagihan);
        requestPenangguhan.setTanggalPengajuan(LocalDate.now());
        requestPenangguhan.setStatusApprove(StatusApprove.WAITING);
        requestPenangguhan.setStatus(StatusRecord.AKTIF);
        requestPenangguhanDao.save(requestPenangguhan);
        return "redirect:list";
    }

    @PostMapping("/studentBill/penangguhan/document")
    public String newDocumentPenangguhan(@ModelAttribute @Valid UploadBerkasDto berkasDto, BindingResult errors,
                                       MultipartFile fileBerkas1, MultipartFile fileBerkas2, MultipartFile fileBerkas3, MultipartFile fileBerkas4,
                                       MultipartFile fileBerkas5, Authentication authentication, String tagihan) throws Exception{

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        Tagihan t = tagihanDao.findById(tagihan).get();

        if (errors.hasErrors()){
            log.debug("Error upload supported documents : {}", errors.toString());
        }

        String idPeserta = mahasiswa.getNim();
        String lokasiUpload = uploadBerkasPenangguhan + File.separator + idPeserta;
        log.debug("Lokasi upload : {}", lokasiUpload);
        new File(lokasiUpload).mkdir();
        StatusDocument penangguhan = StatusDocument.PENANGGUHAN;

        saveBerkas(fileBerkas1, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument1(), penangguhan);
        saveBerkas(fileBerkas2, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument2(), penangguhan);
        saveBerkas(fileBerkas3, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument3(), penangguhan);
        saveBerkas(fileBerkas4, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument4(), penangguhan);
        saveBerkas(fileBerkas5, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument5(), penangguhan);

        return "redirect:/studentBill/requestPenangguhan/date?id=" + t.getId();

    }

    private void saveBerkas(MultipartFile berkasFile, Tagihan tagihan, UploadBerkasDto berkasDto, String lokasiUpload, JenisDocument jenisDocument, StatusDocument statusDocument){
        try {
            if (berkasFile == null || berkasFile.isEmpty()) {
                log.info("Document kosong, tidak di proses!");
                return;
            }

            TagihanDocument document = new TagihanDocument();
            document.setTagihan(tagihan);
            document.setNama(berkasFile.getOriginalFilename());
            document.setJenisDocument(berkasDto.getJenisDocument1());

            String namaFile = berkasFile.getName();
            String jenisFile = berkasFile.getContentType();
            String namaAsli = berkasFile.getOriginalFilename();
            Long ukuran = berkasFile.getSize();

            log.debug("Nama File : {}", namaFile);
            log.debug("Jenis File : {}", jenisFile);
            log.debug("namaAsli : {}", namaAsli);
            log.debug("Ukuran : {}", ukuran);

            //memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p){
                extension = namaAsli.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            document.setDocument(idFile + '.' + extension);
            File tujuan = new File(lokasiUpload + File.separator + document.getDocument());
            berkasFile.transferTo(tujuan);
            log.debug("Document sudah dicopy ke : {}", tujuan.getAbsolutePath());
            document.setStatusDocument(statusDocument);
            document.setJenisDocument(jenisDocument);
            tagihanDocumentDao.save(document);

        }catch (Exception err){
            log.error(err.getMessage(), err);
        }
    }

    @GetMapping("/tagihan/{document}/penangguhan")
    public ResponseEntity<byte[]> showDocumentPenangguhan(@PathVariable TagihanDocument document) throws Exception{
        String lokasiFile = uploadBerkasPenangguhan + File.separator + document.getTagihan().getMahasiswa().getNim() + File.separator +
                document.getDocument();

        log.debug("lokasi file : {}" + lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (document.getDocument().toLowerCase().endsWith("jpeg") || document.getDocument().toLowerCase().endsWith("jpg")){
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (document.getDocument().toLowerCase().endsWith("png")){
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (document.getDocument().toLowerCase().endsWith("pdf")){
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else{
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        }catch (Exception err){
            log.warn(err.getMessage(), err);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PostMapping("/studentBill/penangguhan/approve")
    public String approvePenangguhan(@RequestParam RequestPenangguhan requestPenangguhan,
                                     @RequestParam(required = false) Tagihan tagihan,
                                     Authentication authentication){

        String kode = tagihan.getNilaiJenisTagihan().getJenisTagihan().getKode();
        log.info("kode tagihan : {}", kode);
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        requestPenangguhan.setUserApprove(karyawan);
        requestPenangguhan.setTanggalApprove(LocalDate.now());
        requestPenangguhan.setStatusApprove(StatusApprove.APPROVED);
        requestPenangguhan.setStatus(StatusRecord.AKTIF);
        requestPenangguhanDao.save(requestPenangguhan);

        tagihan.setKaryawan(karyawan);
        tagihan.setTanggalPenangguhan(requestPenangguhan.getTanggalPenangguhan());
        tagihan.setStatusTagihan(StatusTagihan.DITANGGUHKAN);
        tagihanDao.save(tagihan);
        return "redirect:../requestPenangguhan/list";
    }

    @PostMapping("/studentBill/dokumen/deletep")
    public String hapusDokumenPenangguhan(@RequestParam TagihanDocument document){
        document.setStatus(StatusRecord.HAPUS);
        tagihanDocumentDao.save(document);

        return "redirect:../requestPenangguhan/date?id="+document.getTagihan().getId();
    }

//    @PostMapping("/studentBill/penangguhan/reject")
//    public String rejectPenangguhan(@RequestParam RequestPenangguhan requestPenangguhan,
//                                    Authentication authentication){
//        User user = currentUserService.currentUser(authentication);
//        Karyawan karyawan = karyawanDao.findByIdUser(user);
//        requestPenangguhan.setUserApprove(karyawan);
//        requestPenangguhan.setTanggalApprove(LocalDate.now());
//        requestPenangguhan.setStatusApprove(StatusApprove.REJECTED);
//        requestPenangguhan.setStatus(StatusRecord.AKTIF);
//        requestPenangguhanDao.save(requestPenangguhan);
//        return "redirect:../requestPenangguhan/list";
//    }

//    request cicilan

    @GetMapping("/studentBill/requestCicilan/list")
    public void listCicilan(Model model, @PageableDefault(size = 10) Pageable page){

        model.addAttribute("listCicilan", requestCicilanDao.listRequestCicilan1(page));

    }

    @GetMapping("/studentBill/requestCicilan/angsuran")
    public void cicilanTagihan(Model model, @PageableDefault(size = 10) Pageable page,
                               @RequestParam(required = false) String id){
        Tagihan tagihan = tagihanDao.findById(id).get();
        StatusTagihan info = tagihan.getStatusTagihan();
        model.addAttribute("cek", info);
        if (info == StatusTagihan.DICICIL){
            model.addAttribute("message", "message");
        }
        Integer jumlahCicilan = requestCicilanDao.countRequestCicilanByTagihanAndStatus(tagihan, StatusRecord.AKTIF);
        model.addAttribute("cicilan", new RequestCicilan());
        model.addAttribute("request", requestCicilanDao.findByStatusNotInAndTagihanOrderByTanggalJatuhTempo(Arrays.asList(StatusRecord.HAPUS), page, tagihan));
        model.addAttribute("jumlahCicilan", jumlahCicilan);
        model.addAttribute("jumlahNilai", requestCicilanDao.sisaCicilan(tagihan.getId()));
        model.addAttribute("jumlahFile", tagihanDocumentDao.countAllByTagihanAndStatusAndStatusDocument(tagihan, StatusRecord.AKTIF, StatusDocument.CICILAN));
        model.addAttribute("dokumen", tagihanDocumentDao.findByStatusNotInAndTagihanAndStatusDocument(Arrays.asList(StatusRecord.HAPUS), tagihan, StatusDocument.CICILAN, page));
        if (jumlahCicilan == 0){
            model.addAttribute("tanggal", LocalDate.now().plusMonths(1).plusDays(15));
        }else if (jumlahCicilan == 1){
            model.addAttribute("tanggal", LocalDate.now().plusMonths(3));
        }else if (jumlahCicilan == 2){
            model.addAttribute("tanggal", LocalDate.now().plusMonths(4).plusDays(15));
        }
        model.addAttribute("bill", tagihan);
    }

    @GetMapping("/studentBill/requestCicilan/approval")
    public void approvalCicilan(Model model, @PageableDefault(size = 10) Pageable page,
                                @RequestParam(required = false) Tagihan tagihan){

        StatusTagihan info = tagihan.getStatusTagihan();
        model.addAttribute("cek", info);
        if (info == StatusTagihan.DICICIL){
            model.addAttribute("message", "message");
        }
        model.addAttribute("request", requestCicilanDao.findByStatusNotInAndTagihanOrderByTanggalJatuhTempo(Arrays.asList(StatusRecord.HAPUS), page, tagihan));
        model.addAttribute("jumlahCicilan", requestCicilanDao.countRequestCicilanByTagihanAndStatus(tagihan, StatusRecord.AKTIF));
        model.addAttribute("jumlahNilai", requestCicilanDao.sisaCicilan(tagihan.getId()));
        model.addAttribute("jumlahFile", tagihanDocumentDao.countAllByTagihanAndStatusAndStatusDocument(tagihan, StatusRecord.AKTIF, StatusDocument.CICILAN));
        model.addAttribute("dokumen", tagihanDocumentDao.findByStatusNotInAndTagihanAndStatusDocument(Arrays.asList(StatusRecord.HAPUS), tagihan, StatusDocument.CICILAN, page));
        model.addAttribute("bill", tagihan);
    }

    @GetMapping("/studentBill/requestCicilan/detail")
    public void detailCicilan(Model model, @RequestParam(required = false) Tagihan tagihan,
                              @PageableDefault(size = 10) Pageable page){

        StatusTagihan info = tagihan.getStatusTagihan();
        model.addAttribute("cek", info);
        if (info == StatusTagihan.DICICIL){
            model.addAttribute("message", "message");
        }
        Integer jumlahCicilan = requestCicilanDao.countRequestCicilanByTagihanAndStatus(tagihan, StatusRecord.AKTIF);
        model.addAttribute("request", requestCicilanDao.findByStatusNotInAndTagihanOrderByTanggalJatuhTempo(Arrays.asList(StatusRecord.HAPUS), page, tagihan));
        model.addAttribute("bill", tagihan);
        model.addAttribute("pembayaran", pembayaranDao.findByTagihanAndStatus(tagihan, StatusRecord.AKTIF, page));

    }

    @GetMapping("/studentBill/requestCicilan/pelunasan")
    public void pelunasanCicilan(Model model, @RequestParam(required = false) Tagihan tagihan,
                                 Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mhs = mahasiswaDao.findByUser(user);
        TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByStatusAndProdi(StatusRecord.AKTIF, mhs.getIdProdi());

        model.addAttribute("mahasiswa", mhs);
        model.addAttribute("tahunAkademikProdi", tahunAkademikProdi);
        model.addAttribute("sisaCicilan", requestCicilanDao.pengajuanPelunasan(tagihan.getId()));
        model.addAttribute("tagihan", tagihan);

    }

    @PostMapping("/studentBill/cicilan/pelunasan")
    public String pelunasan(@Valid RequestCicilan requestCicilan,
                          @RequestParam(required = false) Tagihan tagihan,
                          @RequestParam(required = false) BigDecimal nilaiCicilan){

        tagihanService.hapusTagihan(tagihan);
        log.info("jumlah cicilan : {}" + nilaiCicilan);
        List<RequestCicilan> cekSisaCicilan = requestCicilanDao.findByTagihanAndStatusAndStatusCicilanNotIn(tagihan, StatusRecord.AKTIF, Arrays.asList(StatusCicilan.LUNAS));
        for (RequestCicilan sisaCicilan : cekSisaCicilan){
            sisaCicilan.setStatus(StatusRecord.HAPUS);
            sisaCicilan.setStatusCicilan(StatusCicilan.BATAL_CICIL);
            sisaCicilan.setStatusApprove(StatusApprove.HAPUS);
            requestCicilanDao.save(sisaCicilan);
        }

        requestCicilan.setTagihan(tagihan);
        requestCicilan.setTanggalPengajuan(LocalDate.now());
        requestCicilan.setTanggalJatuhTempo(LocalDate.now().plusMonths(1).withDayOfMonth(10));
        requestCicilan.setStatusApprove(StatusApprove.APPROVED);
        requestCicilan.setStatus(StatusRecord.AKTIF);
        requestCicilan.setStatusCicilan(StatusCicilan.PENGAJUAN_PELUNASAN);
        requestCicilanDao.save(requestCicilan);
        tagihanService.requestCreateCicilan(requestCicilan);

        return "redirect:../bill/list";
    }


    @PostMapping("/studentBill/bill/document")
    public String newDocument(@ModelAttribute @Valid UploadBerkasDto berkasDto, BindingResult errors,
                              MultipartFile fileBerkas1, MultipartFile fileBerkas2, MultipartFile fileBerkas3, MultipartFile fileBerkas4,
                              MultipartFile fileBerkas5, Authentication authentication, String tagihan) throws IOException{

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        Tagihan t = tagihanDao.findById(tagihan).get();

        if (errors.hasErrors()) {
            log.debug("Error upload supported documents : {}", errors.toString());
        }

        String idPeserta = t.getMahasiswa().getNim();
        String lokasiUpload = uploadBerkasCicilan + File.separator + idPeserta;
        log.info("Lokasi Upload : {}", lokasiUpload);
        new File(lokasiUpload).mkdir();
        StatusDocument cicilan = StatusDocument.CICILAN;

        saveBerkas(fileBerkas1, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument1(), cicilan);
        saveBerkas(fileBerkas2, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument2(), cicilan);
        saveBerkas(fileBerkas3, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument3(), cicilan);
        saveBerkas(fileBerkas4, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument4(), cicilan);
        saveBerkas(fileBerkas5, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument5(), cicilan);

        return "redirect:../requestCicilan/angsuran?id="+t.getId();
    }

    @GetMapping("/tagihan/{document}/cicilan")
    public ResponseEntity<byte[]> showDocument(@PathVariable TagihanDocument document) throws Exception{
        String lokasiFile = uploadBerkasCicilan + File.separator + document.getTagihan().getMahasiswa().getNim() + File.separator +
                document.getDocument();

        log.debug("lokasi file : {}" + lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (document.getDocument().toLowerCase().endsWith("jpeg") || document.getDocument().toLowerCase().endsWith("jpg")){
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (document.getDocument().toLowerCase().endsWith("png")){
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (document.getDocument().toLowerCase().endsWith("pdf")){
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else{
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        }catch (Exception err){
            log.warn(err.getMessage(), err);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PostMapping("/studentBill/bill/cicilan")
    public String newCicilan(@Valid RequestCicilan requestCicilan, RedirectAttributes attributes,
                             @RequestParam(required = false) Tagihan tagihan){

        requestCicilan.setStatusCicilan(StatusCicilan.CICILAN);
        requestCicilan.setTanggalPengajuan(LocalDate.now());
        requestCicilan.setTagihan(tagihan);
        requestCicilan.setStatusApprove(StatusApprove.WAITING);
        requestCicilan.setStatus(StatusRecord.AKTIF);
        requestCicilanDao.save(requestCicilan);

        return "redirect:../requestCicilan/angsuran?id="+tagihan.getId();
    }

    @PostMapping("/studentBill/billAdmin/cicilan")
    public String newCicilanAdmin(@Valid RequestCicilan requestCicilan,
                                  @RequestParam(required = false) Tagihan tagihan){

        List<RequestCicilan> rc = requestCicilanDao.findByTagihanAndStatusAndStatusApprove(tagihan, StatusRecord.AKTIF, StatusApprove.WAITING);
//        for (RequestCicilan requestCicilan1 : rc){
//            if (requestCicilan1 == null){
//                requestCicilan.setStatusCicilan(StatusCicilan.CICILAN_1);
//            }else if(requestCicilan1.getStatusCicilan() == StatusCicilan.CICILAN_1){
//                requestCicilan.setStatusCicilan(StatusCicilan.CICILAN_2);
//            }else if (requestCicilan1.getStatusCicilan() == StatusCicilan.CICILAN_2){
//                requestCicilan.setStatusCicilan(StatusCicilan.CICILAN_3);
//            }
//        }
        requestCicilan.setTagihan(tagihan);
        requestCicilan.setStatusApprove(StatusApprove.WAITING);
        requestCicilan.setStatus(StatusRecord.AKTIF);
        requestCicilanDao.save(requestCicilan);

        return "redirect:../requestCicilan/approval?tagihan="+tagihan.getId();
    }

    @PostMapping("/studentBill/cicilan/approve")
    public String approvalCicilan(@RequestParam(required = false) Tagihan bill,
                                  Authentication authentication){

        bill.setStatusTagihan(StatusTagihan.DICICIL);
        tagihanDao.save(bill);
        tagihanService.hapusTagihan(bill);

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        log.info("approved by : {}", karyawan);
        List<RequestCicilan> rc = requestCicilanDao.findByTagihanAndStatusAndStatusApprove(bill, StatusRecord.AKTIF, StatusApprove.WAITING);
        for (RequestCicilan rc1 : rc){
            rc1.setUserApprove(karyawan);
            rc1.setWaktuApprove(LocalDateTime.now());
            rc1.setStatus(StatusRecord.AKTIF);
            rc1.setStatusApprove(StatusApprove.APPROVED);
            requestCicilanDao.save(rc1);
        }
        RequestCicilan requestCicilan = requestCicilanDao.cariCicilanSelanjutnya(bill);
        if (requestCicilan != null) {
            requestCicilan.setUserApprove(karyawan);
            requestCicilan.setWaktuApprove(LocalDateTime.now());
            requestCicilan.setStatus(StatusRecord.AKTIF);
            requestCicilan.setStatusApprove(StatusApprove.APPROVED);
            requestCicilan.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
            requestCicilanDao.save(requestCicilan);
        }

        return "redirect:../requestCicilan/list";

    }

    @PostMapping("/studentBill/cicilan/reject")
    public String rejectCicilan(@RequestParam(required = false) Tagihan bill,
                                @RequestParam(required = false) String keterangan,
                                Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        log.debug("rejected by : {}", karyawan);
        List<RequestCicilan> rc = requestCicilanDao.findByTagihanAndStatusAndStatusApprove(bill, StatusRecord.AKTIF, StatusApprove.WAITING);
        for (RequestCicilan requestCicilan : rc){
            requestCicilan.setUserApprove(karyawan);
            requestCicilan.setWaktuApprove(LocalDateTime.now());
            requestCicilan.setKeterangan(keterangan);
            requestCicilan.setStatusApprove(StatusApprove.REJECTED);
            requestCicilan.setStatusCicilan(StatusCicilan.DITOLAK);
            requestCicilan.setStatus(StatusRecord.HAPUS);
            requestCicilanDao.save(requestCicilan);
        }
        List<TagihanDocument> td = tagihanDocumentDao.findByTagihanAndStatusAndStatusDocument(bill, StatusRecord.AKTIF, StatusDocument.CICILAN);
        for (TagihanDocument document : td){
            document.setStatus(StatusRecord.HAPUS);
            tagihanDocumentDao.save(document);
        }
        return "redirect:../requestCicilan/list";
    }

    @PostMapping("/studentBill/requestCicilan/delete")
    public String deleteCicilan(@RequestParam RequestCicilan cicilan){
        cicilan.setStatus(StatusRecord.HAPUS);
        cicilan.setStatusApprove(StatusApprove.HAPUS);
        cicilan.setStatusCicilan(StatusCicilan.BATAL_CICIL);
        requestCicilanDao.save(cicilan);

        return "redirect:angsuran?id="+cicilan.getTagihan().getId();
    }

    @PostMapping("/studentBill/approval/delete")
    public String hapusCicilan(@RequestParam RequestCicilan cicilan){
        cicilan.setStatus(StatusRecord.HAPUS);
        cicilan.setStatusApprove(StatusApprove.HAPUS);
        cicilan.setStatusCicilan(StatusCicilan.BATAL_CICIL);
        requestCicilanDao.save(cicilan);

        return "redirect:../requestCicilan/approval?tagihan="+cicilan.getTagihan().getId();
    }

    @PostMapping("/studentBill/dokumen/delete")
    public String hapusDokumen(@RequestParam TagihanDocument document){
        document.setStatus(StatusRecord.HAPUS);
        tagihanDocumentDao.save(document);

        return "redirect:../requestCicilan/angsuran?id="+document.getTagihan().getId();
    }

    // pembayaran

    @GetMapping("/studentBill/payment/form")
    public void formPayment(Model model, @RequestParam(required = false) String tagihan, @PageableDefault(size = 10) Pageable page ){
        Tagihan tagihan1 = tagihanDao.findById(tagihan).get();

        RequestCicilan cekCicilan = requestCicilanDao.cariCicilan(tagihan);
        if (cekCicilan != null){
            model.addAttribute("cekCicilan", "cicilan");
        }
        if (tagihan1.getStatusTagihan() == StatusTagihan.LUNAS){
            model.addAttribute("lunas", "lunas");
        }

        model.addAttribute("cekJumlahCicilan", requestCicilanDao.countByTagihanAndStatusAndStatusCicilanNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusCicilan.LUNAS)));
        model.addAttribute("cekJumlahPembayaran", pembayaranDao.countAllByTagihan(tagihan1));
        model.addAttribute("pembayaran", pembayaranDao.cekPembayaran(tagihan));
        model.addAttribute("bank", bankDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("virtualAccount", virtualAccountDao.listVa(tagihan1.getId()));
        model.addAttribute("tagihan", tagihan1);
        model.addAttribute("penangguhan", requestPenangguhanDao.findByTagihanAndStatusAndStatusApprove(tagihan1, StatusRecord.AKTIF, (StatusApprove.APPROVED)));

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

    @GetMapping("/api/pembayaran")
    @ResponseBody
    public VirtualAccount va(@RequestParam(required = false) String id, @RequestParam(required = false) String idTagihan){

        return virtualAccountDao.vaPembayaran(id, idTagihan);

    }

    @PostMapping("/studentBill/payment/manual")
    public String paymentManual(@ModelAttribute @Valid Pembayaran pembayaran,
                                MultipartFile fileBukti) throws IOException {

        String idPeserta = pembayaran.getTagihan().getMahasiswa().getNim();

        String namaFile = fileBukti.getName();
        String jenisFile = fileBukti.getContentType();
        String namaAsli = fileBukti.getOriginalFilename();
        Long ukuran = fileBukti.getSize();

        log.debug("nama file : {}" + namaFile);
        log.debug("jenis file : {}" + jenisFile);
        log.debug("nama asli file : {}" + namaAsli);
        log.debug("ukuran file : {}" + ukuran);

        // Memisahkan extension
        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

        if (i > p){
            extension = namaAsli.substring(i + 1);
        }

        String idFile = UUID.randomUUID().toString();
        String lokasiUpload = uploadFolder + File.separator + idPeserta;
        log.debug("Lokasi Upload : {}" + lokasiUpload);
        new File(lokasiUpload).mkdirs();
        File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
        pembayaran.setReferensi(idFile + "." + extension);
        fileBukti.transferTo(tujuan);
        log.debug("file sudah dicopy ke : {}" + tujuan.getAbsolutePath());

        Tagihan tagihan = pembayaran.getTagihan();
        log.debug("bank : " + pembayaran.getBank());
        pembayaran.setWaktuBayar(LocalDateTime.now());
        pembayaran.setAmount(tagihan.getNilaiTagihan());
        pembayaranDao.save(pembayaran);

        tagihan.setAkumulasiPembayaran(tagihan.getNilaiTagihan());
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
    public void report(Model model, @PageableDefault(size = 10) Pageable page){

    }

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
            log.error(err.getMessage(), err);
        }
    }

    // report

    @GetMapping("/studentBill/billReport/prodi")
    public void reportProdi(Model model, @RequestParam(required = false) String tahun){
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();
        String thn = tahunAkademik.getNamaTahunAkademik().substring(0,9);

        model.addAttribute("tahun", thn);
        model.addAttribute("listProdi", tagihanDao.listTagihanPerProdi(tahunAkademik));
        model.addAttribute("toTagihan", tagihanDao.totalTagihan(tahunAkademik));
        model.addAttribute("toDibayar", pembayaranDao.totalDibayar(tahunAkademik));
    }

    @GetMapping("/studentBill/billReport/mahasiswaProdi")
    public void reportMahasiswaProdi(Model model, @RequestParam(required = false) String prodi,
                                        @RequestParam(required = false) String tahun){

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();
        Prodi prd = prodiDao.findById(prodi).get();

        String tahun1 = tahunAkademik.getNamaTahunAkademik().substring(1,9);

        model.addAttribute("prodi", prd);
        model.addAttribute("tahun", tahun1);
        model.addAttribute("tagihanProdi", tagihanDao.listTagihanPerMahasiswaByProdi(prodi, tahun));



    }

    @GetMapping("/studentBill/billReport/angkatan")
    public void reportAngkatan(Model model, @RequestParam(required = false) String tahun){
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();
        String thn = tahunAkademik.getNamaTahunAkademik().substring(0,9);

        model.addAttribute("tahun", thn);
        model.addAttribute("listAngkatan", tagihanDao.listTagihanPerAngkatan(tahunAkademik));
        model.addAttribute("toTagihan", tagihanDao.totalTagihan(tahunAkademik));
        model.addAttribute("toDibayar", pembayaranDao.totalDibayar(tahunAkademik));

    }

    private void createEnableFitur(@RequestParam(required = false) TahunAkademik tahun, Tagihan tgh, StatusRecord status, Boolean enabled) {
        EnableFiture enableFiture = new EnableFiture();
        enableFiture.setMahasiswa(tgh.getMahasiswa());
        enableFiture.setTahunAkademik(tahun);
        enableFiture.setFitur(status);
        enableFiture.setEnable(enabled);
        enableFiture.setKeterangan("-");
        enableFitureDao.save(enableFiture);
    }

//    @Scheduled(cron = "0 21 11 11 * *", zone = "Asia/Jakarta")
//    public void akumulasiTagihan(){
//
//        List<RequestCicilan> requestCicilan = requestCicilanDao.findByStatusAndStatusCicilanAndTanggalJatuhTempo(StatusRecord.AKTIF, StatusCicilan.SEDANG_DITAGIHKAN, LocalDate.now().withDayOfMonth(10));
//        for (RequestCicilan cariCicilanHariIni : requestCicilan){
//            cariCicilanHariIni.setStatusCicilan(StatusCicilan.LEWAT_JATUH_TEMPO);
//            requestCicilanDao.save(cariCicilanHariIni);
//            log.info("Update status cicilan : {}", cariCicilanHariIni);
//
//            RequestCicilan cicilanSelanjutnya = requestCicilanDao.cariCicilanSelanjutnya(cariCicilanHariIni.getTagihan());
//            if (cicilanSelanjutnya != null){
//                tagihanService.hapusTagihan(cariCicilanHariIni.getTagihan());
//
//                cicilanSelanjutnya.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
//                cicilanSelanjutnya.setNilaiCicilan(cariCicilanHariIni.getNilaiCicilan().add(cicilanSelanjutnya.getNilaiCicilan()));
//                requestCicilanDao.save(cicilanSelanjutnya);
//
//                tagihanService.requestCreateCicilan(cicilanSelanjutnya);
//
//            }else{
//                log.info("Tidak ada cicilan selanjutnya!");
//                cariCicilanHariIni.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
//                requestCicilanDao.save(cariCicilanHariIni);
////                User userBlock = userDao.findById(cariCicilanHariIni.getTagihan().getMahasiswa().getUser().getId()).get();
////                userBlock.setActive(false);
////                userDao.save(userBlock);
////                log.info("block smile untuk user {}", userBlock);
//            }
//
//        }
//
//    }
//
//    @Scheduled(cron = "0 59 20 * * *", zone = "Asia/Jakarta")
//    public void lewatPenangguhan(){
//
//        List<RequestPenangguhan> requestPenangguhan = requestPenangguhanDao.findByStatusAndStatusApproveAndTanggalPenangguhan(StatusRecord.AKTIF, StatusApprove.APPROVED, LocalDate.now());
//        for (RequestPenangguhan cariPenangguhan : requestPenangguhan){
//
//            log.info("tes lewat tanggal penangguhan :) {}", cariPenangguhan.getId());
//
////            User userBlock = userDao.findById(cariPenangguhan.getTagihan().getMahasiswa().getUser().getId()).get();
////            userBlock.setActive(false);
////            userDao.save(userBlock);
////            log.info("Block Smile untuk user {}", userBlock);
//
//        }
//
//    }


}