package id.ac.tazkia.smilemahasiswa.controller;


import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.api.akunting.TemplateDto;
import id.ac.tazkia.smilemahasiswa.dto.payment.DaftarTagihanPerAngkatanDto;
import id.ac.tazkia.smilemahasiswa.dto.payment.DaftarTagihanPerProdiDto;
import id.ac.tazkia.smilemahasiswa.dto.payment.PembayaranDto;
import id.ac.tazkia.smilemahasiswa.dto.payment.UploadBerkasDto;
import id.ac.tazkia.smilemahasiswa.dto.prediksitest.RekapTagihanDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.KafkaSender;
import id.ac.tazkia.smilemahasiswa.service.TagihanService;
import id.ac.tazkia.smilemahasiswa.service.api.akuntansi.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller @Slf4j
public class StudentBillController {

    public static final List<String> TAGIHAN_KRS = Arrays.asList("14", "22", "40", "44");

    public static final List<String> programS2 = Arrays.asList("03", "8ec26f2c-a48a-4948-be90-e03e9374c675");

    public static final String TAGIHAN_TOEFL = "81";
    public static final String TAGIHAN_IELTS = "82";

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
    private RequestPeringananDao requestPeringananDao;

    @Autowired
    private VirtualAccountDao virtualAccountDao;

    @Autowired
    private EnableFitureDao enableFitureDao;

    @Autowired
    private BeasiswaDao beasiswaDao;

    @Autowired
    private MahasiswaBeasiswaDao mahasiswaBeasiswaDao;

    @Autowired
    private MahasiswaCicilanDao mahasiswaCicilanDao;

    @Autowired
    private TagihanBeasiswaDao tagihanBeasiswaDao;

    @Autowired
    private ProgramDao programDao;

    @Autowired
    private RefundSpDao refundSpDao;

    @Autowired
    private KuotaOfflineDao kuotaOfflineDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PrediksiTestDao prediksiTestDao;

    @Autowired
    private PeriodeTestDao periodeTestDao;

    @Value("classpath:kwitansi.odt")
    private Resource templateKwitansi;

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatus(StatusRecord.AKTIF);
    }

    @ModelAttribute("beasiswa")
    public Iterable<Beasiswa> beasiswa(){return beasiswaDao.findByStatusOrderByNamaBeasiswa(StatusRecord.AKTIF);}

    @ModelAttribute("program")
    public Iterable<Program> program() {
        return programDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }

    @Autowired
    public TagihanService tagihanService;

    @Autowired
    public ApiService apiService;

    @Autowired
    public KafkaSender kafkaSender;

    @Value("${upload.buktiPembayaran}")
    private String uploadFolder;

    @Value("${upload.berkasCicilan}")
    private String uploadBerkasCicilan;

    @Value("${upload.berkasPenangguhan}")
    private String uploadBerkasPenangguhan;

    @Value("${upload.berkasPeringanan}")
    private String uploadBerkasPeringanan;

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademik> tahunAkademik() {
        return tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(Arrays.asList(StatusRecord.HAPUS));
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


    // Jenis Tagihan
    @GetMapping("/studentBill/typeBill/list")
    public void listType(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listType", jenisTagihanDao.findByStatusNotInAndNamaContainingIgnoreCaseOrderByKode(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else{
            model.addAttribute("listType", jenisTagihanDao.findByStatusNotInOrderByKode(Arrays.asList(StatusRecord.HAPUS), page));
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

    // Nilai Jenis Tagihan
    @GetMapping("/studentBill/valueType/list")
    public void listNilai(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik, @PageableDefault(size = 10) Pageable page, String search ){

        model.addAttribute("selectTahun", tahunAkademik);
        if (tahunAkademik == null) {
            model.addAttribute("tahun", "tahun");
        }else{
            if (StringUtils.hasText(search)){
                model.addAttribute("search", search);
                model.addAttribute("listValue", nilaiJenisTagihanDao.findByStatusNotInAndTahunAkademikAndProdiNotNullAndAngkatanContainingIgnoreCaseOrStatusNotInAndTahunAkademikAndProdiNotNullAndProdiNamaProdiContainingIgnoreCaseOrStatusNotInAndTahunAkademikAndProdiNotNullAndProgramNamaProgramContainingIgnoreCaseOrStatusNotInAndTahunAkademikAndProdiNotNullAndJenisTagihanNamaContainingIgnoreCaseOrderByAngkatanDesc(Arrays.asList(StatusRecord.HAPUS), tahunAkademik, search, Arrays.asList(StatusRecord.HAPUS), tahunAkademik, search, Arrays.asList(StatusRecord.HAPUS), tahunAkademik, search, Arrays.asList(StatusRecord.HAPUS), tahunAkademik, search, page));
            }else{
                model.addAttribute("listValue", nilaiJenisTagihanDao.findByStatusNotInAndTahunAkademikAndProdiNotNullOrderByAngkatanDesc(Arrays.asList(StatusRecord.HAPUS), tahunAkademik, page));
            }
        }

    }

    @GetMapping("/studentBill/valueType/form")
    public void formNilai(Model model, @RequestParam(required = false) String id){

        TemplateDto template = apiService.getTemplate();

        model.addAttribute("valueBill", new NilaiJenisTagihan());
        model.addAttribute("jenisTagihan", jenisTagihanDao.findByStatusOrderByKode(StatusRecord.AKTIF));
        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("prodi", prodiDao.findByStatusOrderByNamaProdi(StatusRecord.AKTIF));
        model.addAttribute("program", programDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("selectAngkatan", angkatan());
        model.addAttribute("kategori", StatusTagihan.values());
        model.addAttribute("template", template.getData());

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
        return "redirect:list?tahunAkademik="+nilaiJenisTagihan.getTahunAkademik().getId();
    }

    @PostMapping("/studentBill/valueType/delete")
    public String deleteNilai(@RequestParam NilaiJenisTagihan nilaiJenisTagihan){
        nilaiJenisTagihan.setStatus(StatusRecord.HAPUS);
        nilaiJenisTagihanDao.save(nilaiJenisTagihan);
        return "redirect:list";
    }

    // LIST TAGIHAN DAFTAR ULANG
    @GetMapping("/studentBill/daftarulang/list")
    public void listTagihanDaftarUlang(Model model, @PageableDefault(size = 10) Pageable page, @RequestParam(required = false) TahunAkademik tahunAkademik){
        model.addAttribute("selectTahun", tahunAkademik);
        if (tahunAkademik != null) {
            model.addAttribute("listTagihan", tagihanDao.listTagihanDaftarUlang(tahunAkademik, page));
        }else{
            TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            model.addAttribute("listTagihan", tagihanDao.listTagihanDaftarUlang(ta, page));
        }
    }

    @GetMapping("/get-cicilan-daftarulang/{id}")
    @ResponseBody
    public List<RequestCicilan> getDetailCicilanDaftarUlang(@PathVariable String id){

        Tagihan t = tagihanDao.findById(id).get();

        return requestCicilanDao.findByStatusAndTagihanOrderByTanggalJatuhTempo(StatusRecord.AKTIF,t);
    }

    // List Tagihan
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
                              @RequestParam(required = false) Prodi prodi,
                              @RequestParam(required = false) String nim, @PageableDefault(size = 10) Pageable page,
                              @RequestParam(required = false) String date1, @RequestParam(required = false) String date2,
                              @RequestParam(required = false) String date3, @RequestParam(required = false) String date4){

        // list untuk per mahasiswa

        model.addAttribute("selectTahun", tahunAkademik);
        model.addAttribute("selectNim", nim);
        model.addAttribute("selectProdi", prodi);
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
            String th = null;
            List<String> thak = new ArrayList<>();
            if (tahunAkademik.getJenis() == StatusRecord.GANJIL) {
                Integer ta = new Integer(tahunAkademik.getTahun());
                Integer k = ta-1;
                th = k + "2";
            }else{
                Integer ta = new Integer(tahunAkademik.getKodeTahunAkademik());
                Integer j = ta - 1;
                th = j.toString();
            }
            TahunAkademik th1 = tahunAkademikDao.findByKodeTahunAkademik(th);
            thak.add(tahunAkademik.getId());
            thak.add(th1.getId());
            model.addAttribute("listPiutang", tagihanDao.listNewPiutang(thak));
            if (prodi != null) {
                model.addAttribute("listPiutang", tagihanDao.listNewPiutangDanProdi(thak, prodi.getId()));
            }
            model.addAttribute("detailTahun", thak);
        }


    }

    // list semua tagihan paling baru
    @GetMapping("/studentBill/billAdmin/tagihan")
    public void allTagihan(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) String search, @PageableDefault(size = 10) Pageable page){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("allTagihan", tagihanDao.findByStatusNotInAndMahasiswaNamaContainingIgnoreCaseOrStatusNotInAndMahasiswaNimContainingIgnoreCaseOrStatusNotInAndMahasiswaIdProdiNamaProdiContainingIgnoreCaseOrderByTanggalPembuatanDesc(Arrays.asList(StatusRecord.HAPUS, StatusRecord.NONAKTIF), search, Arrays.asList(StatusRecord.HAPUS, StatusRecord.NONAKTIF), search, Arrays.asList(StatusRecord.HAPUS, StatusRecord.NONAKTIF), search, page));
            if (tahunAkademik != null) {
                model.addAttribute("selectTahun", tahunAkademik);
                model.addAttribute("allTagihan", tagihanDao.findByStatusNotInAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrStatusNotInAndTahunAkademikAndMahasiswaNimContainingIgnoreCaseOrStatusNotInAndTahunAkademikAndMahasiswaIdProdiNamaProdiContainingIgnoreCaseOrderByTanggalPembuatanDesc(Arrays.asList(StatusRecord.HAPUS, StatusRecord.NONAKTIF), tahunAkademik, search, Arrays.asList(StatusRecord.HAPUS, StatusRecord.NONAKTIF), tahunAkademik, search, Arrays.asList(StatusRecord.HAPUS, StatusRecord.NONAKTIF), tahunAkademik, search, page));
            }
        }else{
            model.addAttribute("allTagihan", tagihanDao.findByStatusNotInOrderByTanggalPembuatanDesc(Arrays.asList(StatusRecord.HAPUS, StatusRecord.NONAKTIF), page));
            if (tahunAkademik != null) {
                model.addAttribute("selectTahun", tahunAkademik);
                model.addAttribute("allTagihan", tagihanDao.findByStatusNotInAndTahunAkademikOrderByTanggalPembuatanDesc(Arrays.asList(StatusRecord.HAPUS, StatusRecord.NONAKTIF), tahunAkademik, page));
            }
        }
    }

    @PostMapping("/studentBill/billAdmin/resendTagihan")
    public String resendTagihan(@RequestParam Tagihan tagihan){
        if (tagihan.getStatusTagihan() == StatusTagihan.DICICIL) {
            RequestCicilan cicilan = requestCicilanDao.findByTagihanAndStatusCicilanAndStatus(tagihan, StatusCicilan.SEDANG_DITAGIHKAN, StatusRecord.AKTIF);
            if (cicilan != null) {
                tagihanService.mengirimCicilanSelanjutnya(cicilan);
            }
        }else{
            tagihanService.requestCreateTagihan(tagihan);
        }
        return "redirect:detail?tagihan="+tagihan.getId();
    }

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
                     @RequestParam(required = false) Program program, @RequestParam(required = false) String angkatan, @RequestParam(required = false) Integer cicilan){

        model.addAttribute("selectAngkatan", angkatan);
        model.addAttribute("selectTahun", tahunAkademik);
        model.addAttribute("selectProdi", prodi);
        model.addAttribute("selectProgram", program);
        model.addAttribute("cicilan", cicilan);

        if (tahunAkademik != null) {
            model.addAttribute("listJenisTagihan", nilaiJenisTagihanDao.findByTahunAkademikAndAngkatanAndProdiAndProgramAndStatus(tahunAkademik, angkatan,
                    prodi, program, StatusRecord.AKTIF));
            model.addAttribute("lTagihan", tagihanDao.listAfterGenerate(tahunAkademik.getId(), angkatan, prodi.getId(), program.getId()));
        }

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
                           @PageableDefault(size = 50) Pageable page){

        Tagihan tagihan1 = tagihanDao.findById(tagihan).get();
        RequestCicilan cekCicilan = requestCicilanDao.cariCicilan(tagihan);
        if (tagihan1.getStatusTagihan() == StatusTagihan.DICICIL){
            model.addAttribute("cekJumlahCicilan", requestCicilanDao.jumlahCicilan(tagihan1));
            model.addAttribute("sisaCicilan", requestCicilanDao.pengajuanPelunasan(tagihan1.getId()));
        }

        model.addAttribute("cekJumlahPembayaran", pembayaranDao.countAllByTagihan(tagihan1));
        model.addAttribute("pembayaran", pembayaranDao.cekPembayaran(tagihan));
        model.addAttribute("detailPembayaran", pembayaranDao.findByTagihanAndStatusOrderByWaktuBayar(tagihan1, StatusRecord.AKTIF, page));
        model.addAttribute("tagihan", tagihan1);
        model.addAttribute("virtualAccount", virtualAccountDao.listVa(tagihan1.getId()));
        if (cekCicilan != null){
            model.addAttribute("message", "message");
            model.addAttribute("cekCicilan", "cicilan");
        }

        // bagian keterangan cicilan
        model.addAttribute("cicilan", requestCicilanDao.findByStatusNotInAndTagihanOrderByTanggalJatuhTempo(Arrays.asList(StatusRecord.HAPUS), tagihan1, page));

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
                              @RequestParam(required = false) Prodi prodi,
                              @RequestParam(required = false) String status){

        model.addAttribute("tahun", tahunAkademik);
        model.addAttribute("prodi", prodi);
        String th;
        List<String> thak = new ArrayList<>();
        if (tahunAkademik.getJenis() == StatusRecord.GANJIL) {
            Integer ta = new Integer(tahunAkademik.getTahun());
            Integer k = ta-1;
            th = k + "2";
        }else{
            Integer ta = new Integer(tahunAkademik.getKodeTahunAkademik());
            Integer j = ta - 1;
            th = j.toString();
        }
        TahunAkademik th1 = tahunAkademikDao.findByKodeTahunAkademik(th);
        thak.add(tahunAkademik.getId());
        thak.add(th1.getId());
        model.addAttribute("detailPiutang", tagihanDao.newDetailPiutang(thak, status));
        if (prodi != null) {
            model.addAttribute("detailPiutang", tagihanDao.newDetailPiutangDanProdi(thak, status, prodi.getId()));
        }
        /*if (status.equals("LANCAR")){
            model.addAttribute("detailPiutang", tagihanDao.detailLancar(tahunAkademik.getKodeTahunAkademik(), status));
        }else if(status.equals("KURANG LANCAR")){
            model.addAttribute("detailPiutang", tagihanDao.detailKurangLancar(tahunAkademik.getKodeTahunAkademik(), status));
        }else if (status.equals("PERHATIAN KHUSUS")){
            model.addAttribute("detailPiutang", tagihanDao.detailPerhatianKhusus(tahunAkademik.getKodeTahunAkademik(), status));
        } else if (status.equals("HAMPIR DIRAGUKAN")) {
            model.addAttribute("detailPiutang", tagihanDao.detailHampirDiragukan(tahunAkademik.getKodeTahunAkademik(), status));
        } else if (status.equals("DIRAGUKAN")) {
            model.addAttribute("detailPiutang", tagihanDao.detailDiragukan(tahunAkademik.getKodeTahunAkademik(), status));
        } else if (status.equals("SANGAT DIRAGUKAN")) {
            model.addAttribute("detailPiutang", tagihanDao.detailSangatDiragukan(tahunAkademik.getKodeTahunAkademik(), status));
        } else if (status.equals("TERSENDAT")) {
            model.addAttribute("detailPiutang", tagihanDao.detailTersendat(tahunAkademik.getKodeTahunAkademik(), status));
        } else if (status.equals("MACET")) {
            model.addAttribute("detailPiutang", tagihanDao.detailMacet(tahunAkademik.getKodeTahunAkademik(), status));
        }*/
        model.addAttribute("status", status);
    }

    @GetMapping("/billReport/permahasiswa/piutang")
    public void reportPerMahasiswaPiutang(@RequestParam String tahun, @RequestParam(required = false) String prodi, @RequestParam String status, HttpServletResponse response) throws IOException {
        String[] columns = {"NO", "NIM", "NAMA", "TAGIHAN", "DIBAYAR", "SISA"};

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();

        List<Object[]> listPiutang = null;
        String th;
        List<String> thak = new ArrayList<>();
        if (tahunAkademik.getJenis() == StatusRecord.GANJIL) {
            Integer ta = new Integer(tahunAkademik.getTahun());
            Integer k = ta-1;
            th = k + "2";
        }else {
            Integer ta = new Integer(tahunAkademik.getKodeTahunAkademik());
            Integer j = ta - 1;
            th = j.toString();
        }
        TahunAkademik th1 = tahunAkademikDao.findByKodeTahunAkademik(th);
        thak.add(tahunAkademik.getId());
        thak.add(th1.getId());
        listPiutang = tagihanDao.newDetailPiutang(thak, status);

        if (prodi != null) {
            listPiutang = tagihanDao.newDetailPiutangDanProdi(thak, status, prodi);
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Detail Piutang Mahasiswa");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i<columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (Object[] list : listPiutang){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list[0].toString());
            row.createCell(2).setCellValue(list[1].toString());
            row.createCell(3).setCellValue(list[2].toString());
            row.createCell(4).setCellValue(list[3].toString());
            row.createCell(5).setCellValue(list[4].toString());
        }

        for (int i = 0; i<columns.length; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename = Detail Piutang "+status+"_"+"Tahun Akademik"+"_"+tahunAkademik.getNamaTahunAkademik()+".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @GetMapping("/api/jenis")
    @ResponseBody
    public NilaiJenisTagihan njt(@RequestParam(required = false) String id){

        return nilaiJenisTagihanDao.findById(id).get();

    }

    @PostMapping("/studentBill/billAdmin/generate")
    public String generateTagihan(RedirectAttributes attributes, Authentication authentication, @RequestParam(required = false) Prodi prodi,
                                  @RequestParam(required = false) Program program, @RequestParam(required = false) String angkatan,
                                  @RequestParam(required = false) TahunAkademik tahun, @RequestParam(required = false) Integer cicilan){

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);

        List<Mahasiswa> reguler = mahasiswaDao.findByIdProdiAndIdProgramAndAngkatanAndStatusAktifAndStatusAndBeasiswaIsNull(prodi, program, angkatan, "AKTIF", StatusRecord.AKTIF);
        List<Mahasiswa> beasiswa = mahasiswaDao.findByIdProdiAndIdProgramAndAngkatanAndStatusAndStatusAktifAndBeasiswaIsNotNull(prodi, program, angkatan, StatusRecord.AKTIF, "AKTIF");

        String jadi;
        if (tahun.getJenis() == StatusRecord.GANJIL) {
            Integer ta = new Integer(tahun.getTahun());
            Integer k = ta-1;
            jadi = k + "2";
        }else{
            Integer ta = new Integer(tahun.getKodeTahunAkademik());
            Integer j = ta - 1;
            jadi = j.toString();
        }
        TahunAkademik tahunBefore = tahunAkademikDao.findByKodeTahunAkademik(jadi);

        System.out.println("tahun Pilihan: " + tahun.getNamaTahunAkademik());
        System.out.println("tahun Sebelumnya: " + tahunBefore.getNamaTahunAkademik());

        if (reguler != null){
            for (Mahasiswa mhs : reguler){
                MahasiswaCicilan mcicilan = mahasiswaCicilanDao.findByStatusAndMahasiswa(StatusRecord.AKTIF, mhs);
                if (mcicilan == null){
                    List<NilaiJenisTagihan> nilaiJenisTagihans = nilaiJenisTagihanDao.findByTahunAkademikAndAngkatanAndProdiAndProgramAndStatus(tahun, angkatan, mhs.getIdProdi(), mhs.getIdProgram(), StatusRecord.AKTIF);
                    for (NilaiJenisTagihan njt : nilaiJenisTagihans) {
                        Tagihan tCekSudahAda = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndStatus(mhs, njt.getJenisTagihan(), tahun, StatusRecord.AKTIF);
                        if (tCekSudahAda == null) {
                            Tagihan tagihan1 = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndLunasAndStatus(mhs, njt.getJenisTagihan(), tahunBefore, false, StatusRecord.AKTIF);
                            if (tagihan1 == null) {
                                String keteranganTagihan = "Tagihan " + njt.getJenisTagihan().getNama()
                                        + " a.n. " + mhs.getNama();

                                Tagihan tagihan = new Tagihan();
                                tagihan.setMahasiswa(mhs);
                                tagihan.setNilaiJenisTagihan(njt);
                                tagihan.setKeterangan(keteranganTagihan);
                                tagihan.setNilaiTagihan(njt.getNilai());
                                tagihan.setNilaiTagihanAsli(njt.getNilai());
                                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                tagihan.setTanggalPembuatan(LocalDate.now());
                                tagihan.setTanggalJatuhTempo(LocalDate.now().plusMonths(8));
                                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                tagihan.setTahunAkademik(tahun);
                                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                tagihan.setStatus(StatusRecord.AKTIF);
                                tagihanDao.save(tagihan);

                                if (cicilan != null) {
                                    for (int i = 1; i<=cicilan; i++) {
                                        BigDecimal totalCicilan = requestCicilanDao.hitungCicilan(tagihan.getNilaiTagihan(), cicilan);
                                        RequestCicilan rc = new RequestCicilan();
                                        rc.setTagihan(tagihan);
                                        rc.setTanggalPengajuan(LocalDate.now());
                                        rc.setNilaiCicilan(totalCicilan);
                                        rc.setStatusCicilan(StatusCicilan.CICILAN);
                                        rc.setStatusApprove(StatusApprove.APPROVED);
                                        rc.setTanggalJatuhTempo(tagihan.getTanggalPembuatan().plusMonths(i));
                                        rc.setWaktuApprove(LocalDateTime.now());
                                        requestCicilanDao.save(rc);
                                    }
                                    RequestCicilan requestCicilan = requestCicilanDao.cariCicilanSelanjutnya(tagihan);
                                    if (requestCicilan != null) {
                                        requestCicilan.setUserApprove(karyawan);
                                        requestCicilan.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
                                        requestCicilanDao.save(requestCicilan);
                                        tagihanService.mengirimCicilanSelanjutnya(requestCicilan);
                                    }
                                    tagihan.setStatusTagihan(StatusTagihan.DICICIL);
                                    tagihanDao.save(tagihan);
                                }else {
                                    tagihanService.requestCreateTagihan(tagihan);
                                }

                                EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, false, tahun);
                                if (enableFiture == null) {
                                    createEnableFitur(tahun, tagihan, StatusRecord.KRS, false);
                                }

                            } else {

                                Integer sisaCicilan = tagihan1.getNilaiTagihan().intValue() - tagihan1.getAkumulasiPembayaran().intValue();

                                Tagihan tagihan = new Tagihan();
                                tagihan.setMahasiswa(mhs);
                                tagihan.setNilaiJenisTagihan(njt);
                                tagihan.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                                        + " a.n. " + tagihan.getMahasiswa().getNama());
                                tagihan.setNilaiTagihan(njt.getNilai().add(new BigDecimal(sisaCicilan)));
                                tagihan.setNilaiTagihanAsli(njt.getNilai());
                                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                tagihan.setTanggalPembuatan(LocalDate.now());
                                tagihan.setTanggalJatuhTempo(LocalDate.now().plusMonths(8));
                                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                tagihan.setTahunAkademik(tahun);
                                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                tagihan.setStatus(StatusRecord.AKTIF);
                                tagihan.setIdTagihanSebelumnya(tagihan1.getId());
                                tagihanDao.save(tagihan);

                                EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, false, tahun);
                                if (enableFiture == null) {
                                    createEnableFitur(tahun, tagihan, StatusRecord.KRS, false);
                                }

                                if (cicilan != null) {
                                    for (int i = 1; i<=cicilan; i++) {
                                        BigDecimal totalCicilan = requestCicilanDao.hitungCicilan(tagihan.getNilaiTagihan(), cicilan);
                                        RequestCicilan rc = new RequestCicilan();
                                        rc.setTagihan(tagihan);
                                        rc.setTanggalPengajuan(LocalDate.now());
                                        rc.setNilaiCicilan(totalCicilan);
                                        rc.setStatusCicilan(StatusCicilan.CICILAN);
                                        rc.setStatusApprove(StatusApprove.APPROVED);
                                        rc.setTanggalJatuhTempo(tagihan.getTanggalPembuatan().plusMonths(i));
                                        rc.setWaktuApprove(LocalDateTime.now());
                                        requestCicilanDao.save(rc);
                                    }
                                    RequestCicilan requestCicilan = requestCicilanDao.cariCicilanSelanjutnya(tagihan);
                                    if (requestCicilan != null) {
                                        requestCicilan.setUserApprove(karyawan);
                                        requestCicilan.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
                                        requestCicilanDao.save(requestCicilan);
                                        RequestCicilan cariSebelumnya = requestCicilanDao.findByTagihanAndStatusCicilanAndStatus(tagihan1, StatusCicilan.SEDANG_DITAGIHKAN, StatusRecord.AKTIF);
                                        if (cariSebelumnya.getTanggalJatuhTempo().isAfter(LocalDate.now()) || cariSebelumnya.getTanggalJatuhTempo().isEqual(LocalDate.now())) {
                                            tagihanService.ubahJadiCicilan(requestCicilan);
                                        }else{
                                            tagihanService.mengirimCicilanSelanjutnya(requestCicilan);
                                        }
                                    }
                                    tagihan.setStatusTagihan(StatusTagihan.DICICIL);
                                    tagihanDao.save(tagihan);
                                }else {
                                    if (tagihan1.getTanggalJatuhTempo().isAfter(LocalDate.now())) {
                                        tagihanService.editTagihan(tagihan);
                                    }else{
                                        tagihanService.requestCreateTagihan(tagihan);
                                    }
                                }

                                if (tagihan1.getStatusTagihan() == StatusTagihan.DICICIL) {
                                    List<RequestCicilan> cekSisaCicilan = requestCicilanDao.findByTagihanAndStatusAndStatusCicilanNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusCicilan.LUNAS));
                                    if (cekSisaCicilan != null) {
                                        for (RequestCicilan listCicilan : cekSisaCicilan){
                                            listCicilan.setStatus(StatusRecord.NONAKTIF);
                                            listCicilan.setStatusCicilan(StatusCicilan.LEWAT_JATUH_TEMPO);
                                            requestCicilanDao.save(listCicilan);
                                        }
                                    }
                                }

                                if (tagihan1.getStatusTagihan() == StatusTagihan.DITANGGUHKAN) {
                                    RequestPenangguhan rp = requestPenangguhanDao.findByTagihanAndStatusAndStatusApproveNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusApprove.WAITING));
                                    if (rp != null) {
                                        rp.setStatus(StatusRecord.HAPUS);
                                        rp.setStatusApprove(StatusApprove.HAPUS);
                                        requestPenangguhanDao.save(rp);
                                    }
                                }

                                tagihan1.setStatusTagihan(StatusTagihan.NONAKTIF);
                                tagihan1.setStatus(StatusRecord.NONAKTIF);
                                tagihanDao.save(tagihan1);
                                tagihanService.hapusTagihan(tagihan1);

                            }
                        }else{
                            log.info("Tagihan {} sudah ada!", tCekSudahAda.getMahasiswa().getNim());
                        }
                    }
                }else{
                    log.info("Nim {} cicilan 40x aktif, otomatis aktif krs", mhs.getNim());
                    EnableFiture ef = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, true, tahun);
                    if (ef == null) {
                        EnableFiture enableFiture = new EnableFiture();
                        enableFiture.setMahasiswa(mcicilan.getMahasiswa());
                        enableFiture.setTahunAkademik(tahun);
                        enableFiture.setFitur(StatusRecord.KRS);
                        enableFiture.setEnable(true);
                        enableFiture.setKeterangan("-");

                        enableFitureDao.save(enableFiture);
                    }
                    TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahun, mhs.getIdProdi());
                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahun, StatusRecord.AKTIF);
                    if (k == null) {
                        Krs krs = new Krs();
                        krs.setTahunAkademik(tahun);
                        krs.setTahunAkademikProdi(tahunProdi);
                        krs.setProdi(mcicilan.getMahasiswa().getIdProdi());
                        krs.setMahasiswa(mcicilan.getMahasiswa());
                        krs.setNim(mcicilan.getMahasiswa().getNim());
                        krs.setTanggalTransaksi(LocalDateTime.now());
                        krs.setStatus(StatusRecord.AKTIF);
                        krsDao.save(krs);
                    }
                }
            }
        }

        if (beasiswa != null){
            for(Mahasiswa mhs : beasiswa){
                MahasiswaCicilan mcicilan = mahasiswaCicilanDao.findByStatusAndMahasiswa(StatusRecord.AKTIF, mhs);
                if (mcicilan == null){
                    List<NilaiJenisTagihan> nilaiJenisTagihans = nilaiJenisTagihanDao.findByTahunAkademikAndAngkatanAndProdiAndProgramAndStatus(tahun, mhs.getAngkatan(), mhs.getIdProdi(), mhs.getIdProgram(), StatusRecord.AKTIF);
                    for (NilaiJenisTagihan njt : nilaiJenisTagihans){
                        TagihanBeasiswa tBeasiswa = tagihanBeasiswaDao.findByBeasiswaAndJenisTagihanAndStatus(mhs.getBeasiswa(), njt.getJenisTagihan(), StatusRecord.AKTIF);
                        if (tBeasiswa == null){
                            Tagihan tCekSudahAda = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndStatus(mhs, njt.getJenisTagihan(), tahun, StatusRecord.AKTIF);
                            if (tCekSudahAda == null) {
                                Tagihan tagihan1 = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndLunasAndStatus(mhs, njt.getJenisTagihan(), tahunBefore, false, StatusRecord.AKTIF);
                                if (tagihan1 == null) {
                                    String keteranganTagihan = mhs.getBeasiswa().getNamaBeasiswa();

                                    Tagihan tagihan = new Tagihan();
                                    tagihan.setMahasiswa(mhs);
                                    tagihan.setNilaiJenisTagihan(njt);
                                    tagihan.setKeterangan(keteranganTagihan);
                                    tagihan.setNilaiTagihan(BigDecimal.ZERO);
                                    tagihan.setNilaiTagihanAsli(BigDecimal.ZERO);
                                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                    tagihan.setTanggalPembuatan(LocalDate.now());
                                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusMonths(8));
                                    tagihan.setTanggalPenangguhan(LocalDate.now());
                                    tagihan.setTahunAkademik(tahun);
                                    tagihan.setLunas(true);
                                    tagihan.setStatusTagihan(StatusTagihan.LUNAS);
                                    tagihan.setStatus(StatusRecord.AKTIF);
                                    tagihanDao.save(tagihan);

                                    Pembayaran pembayaran = new Pembayaran();
                                    pembayaran.setTagihan(tagihan);
                                    pembayaran.setAmount(tagihan.getNilaiTagihan());
                                    pembayaran.setWaktuBayar(LocalDateTime.now());
                                    pembayaran.setNomorRekening("-");
                                    pembayaran.setReferensi("-");
                                    pembayaran.setStatus(StatusRecord.AKTIF);
                                    pembayaranDao.save(pembayaran);

                                    EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, true, tahun);
                                    if (enableFiture == null) {
                                        createEnableFitur(tahun, tagihan, StatusRecord.KRS, true);
                                    }

                                    TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahun, mhs.getIdProdi());

                                    Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahun, StatusRecord.AKTIF);
                                    if (krs == null) {
                                        tagihanService.createKrs(tagihan, tahunProdi);
                                    }

                                } else {

                                    tagihan1.setStatusTagihan(StatusTagihan.NONAKTIF);
                                    tagihan1.setStatus(StatusRecord.NONAKTIF);
                                    tagihanDao.save(tagihan1);
                                    tagihanService.hapusTagihan(tagihan1);

                                    Integer sisaCicilan = tagihan1.getNilaiTagihan().intValue() - tagihan1.getAkumulasiPembayaran().intValue();

                                    Tagihan tagihan = new Tagihan();
                                    tagihan.setMahasiswa(mhs);
                                    tagihan.setNilaiJenisTagihan(njt);
                                    tagihan.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                                            + " a.n. " + tagihan.getMahasiswa().getNama());
                                    tagihan.setNilaiTagihan(new BigDecimal(sisaCicilan));
                                    tagihan.setNilaiTagihanAsli(BigDecimal.ZERO);
                                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                    tagihan.setTanggalPembuatan(LocalDate.now());
                                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusMonths(8));
                                    tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                    tagihan.setTahunAkademik(tahun);
                                    tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                    tagihan.setStatus(StatusRecord.AKTIF);
                                    tagihan.setIdTagihanSebelumnya(tagihan1.getId());
                                    tagihanDao.save(tagihan);
                                    tagihanService.requestCreateTagihan(tagihan);

                                    EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, false, tahun);
                                    if (enableFiture == null) {
                                        createEnableFitur(tahun, tagihan, StatusRecord.KRS, false);
                                    }

                                    if (tagihan1.getStatusTagihan() == StatusTagihan.DICICIL) {
                                        List<RequestCicilan> cekSisaCicilan = requestCicilanDao.findByTagihanAndStatusAndStatusCicilanNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusCicilan.LUNAS));
                                        if (cekSisaCicilan != null) {
                                            for (RequestCicilan listCicilan : cekSisaCicilan){
                                                listCicilan.setStatus(StatusRecord.NONAKTIF);
                                                listCicilan.setStatusCicilan(StatusCicilan.LEWAT_JATUH_TEMPO);
                                                requestCicilanDao.save(listCicilan);
                                            }
                                        }
                                    }

                                    if (tagihan1.getStatusTagihan() == StatusTagihan.DITANGGUHKAN) {
                                        RequestPenangguhan rp = requestPenangguhanDao.findByTagihanAndStatusAndStatusApproveNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusApprove.WAITING));
                                        if (rp != null) {
                                            rp.setStatus(StatusRecord.HAPUS);
                                            rp.setStatusApprove(StatusApprove.HAPUS);
                                            requestPenangguhanDao.save(rp);
                                        }
                                    }

                                }
                            }else{
                                log.info("Tagihan {} sudah ada!", tCekSudahAda.getMahasiswa().getNim());
                            }

                        }else {
                            String jenis = tBeasiswa.getJenisPotongan();
                            Integer total = null;
                            Integer potongan = tBeasiswa.getPotongan();
                            if (jenis.equals("PERSEN")){
                                Integer totalPotongan = njt.getNilai().intValue() * potongan / 100;
                                total = njt.getNilai().intValue() - totalPotongan;
                            }else if (jenis.equals("JUMLAH")){
                                total = njt.getNilai().intValue() - potongan;
                            }

                            Tagihan tCekSudahAda = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndStatus(mhs, njt.getJenisTagihan(), tahun, StatusRecord.AKTIF);
                            if (tCekSudahAda == null) {
                                Tagihan tagihan1 = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndLunasAndStatus(mhs, njt.getJenisTagihan(), tahunBefore, false, StatusRecord.AKTIF);
                                if (tagihan1 == null) {

                                    String keteranganTagihan = "Tagihan " + njt.getJenisTagihan().getNama()
                                            + " a.n. " + mhs.getNama();

                                    Tagihan tagihan = new Tagihan();
                                    tagihan.setMahasiswa(mhs);
                                    tagihan.setNilaiJenisTagihan(njt);
                                    tagihan.setKeterangan(keteranganTagihan);
                                    tagihan.setNilaiTagihan(new BigDecimal(total));
                                    tagihan.setNilaiTagihanAsli(new BigDecimal(total));
                                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                    tagihan.setTanggalPembuatan(LocalDate.now());
                                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusMonths(8));
                                    tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                    tagihan.setTahunAkademik(tahun);
                                    tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                    tagihan.setStatus(StatusRecord.AKTIF);
                                    tagihanDao.save(tagihan);
                                    tagihanService.requestCreateTagihan(tagihan);

                                    EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, true, tahun);
                                    if (enableFiture == null) {
                                       createEnableFitur(tahun, tagihan, StatusRecord.KRS, false);
                                    }

                                } else {

                                    tagihan1.setStatusTagihan(StatusTagihan.NONAKTIF);
                                    tagihan1.setStatus(StatusRecord.NONAKTIF);
                                    tagihanDao.save(tagihan1);
                                    tagihanService.hapusTagihan(tagihan1);

                                    Integer sisaCicilan = tagihan1.getNilaiTagihan().intValue() - tagihan1.getAkumulasiPembayaran().intValue();

                                    Tagihan tagihan = new Tagihan();
                                    tagihan.setMahasiswa(mhs);
                                    tagihan.setNilaiJenisTagihan(njt);
                                    tagihan.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                                            + " a.n. " + tagihan.getMahasiswa().getNama());
                                    tagihan.setNilaiTagihan(new BigDecimal(total).add(new BigDecimal(sisaCicilan)));
                                    tagihan.setNilaiTagihanAsli(new BigDecimal(total));
                                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                    tagihan.setTanggalPembuatan(LocalDate.now());
                                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusMonths(8));
                                    tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                    tagihan.setTahunAkademik(tahun);
                                    tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                    tagihan.setStatus(StatusRecord.AKTIF);
                                    tagihan.setIdTagihanSebelumnya(tagihan1.getId());
                                    tagihanDao.save(tagihan);
                                    tagihanService.requestCreateTagihan(tagihan);

                                    EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, false, tahun);
                                    if (enableFiture == null) {
                                        createEnableFitur(tahun, tagihan, StatusRecord.KRS, false);
                                    }

                                    if (tagihan1.getStatusTagihan() == StatusTagihan.DICICIL) {
                                        List<RequestCicilan> cekSisaCicilan = requestCicilanDao.findByTagihanAndStatusAndStatusCicilanNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusCicilan.LUNAS));
                                        if (cekSisaCicilan != null) {
                                            for (RequestCicilan listCicilan : cekSisaCicilan){
                                                listCicilan.setStatus(StatusRecord.NONAKTIF);
                                                listCicilan.setStatusCicilan(StatusCicilan.LEWAT_JATUH_TEMPO);
                                                requestCicilanDao.save(listCicilan);
                                            }
                                        }
                                    }

                                    if (tagihan1.getStatusTagihan() == StatusTagihan.DITANGGUHKAN) {
                                        RequestPenangguhan rp = requestPenangguhanDao.findByTagihanAndStatusAndStatusApproveNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusApprove.WAITING));
                                        if (rp != null) {
                                            rp.setStatus(StatusRecord.HAPUS);
                                            rp.setStatusApprove(StatusApprove.HAPUS);
                                            requestPenangguhanDao.save(rp);
                                        }
                                    }

                                }
                            }else{
                                log.info("Tagihan {} sudah ada!", tCekSudahAda.getMahasiswa().getNim());
                            }
                        }
                    }
                }else{
                    log.info("Mhs beasiswa nim {} cicilan 40x aktif, otomatis aktif krs", mhs.getNim());
                    EnableFiture ef = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, true, tahun);
                    if (ef == null) {
                        EnableFiture enableFiture = new EnableFiture();
                        enableFiture.setMahasiswa(mcicilan.getMahasiswa());
                        enableFiture.setTahunAkademik(tahun);
                        enableFiture.setFitur(StatusRecord.KRS);
                        enableFiture.setEnable(true);
                        enableFiture.setKeterangan("-");

                        enableFitureDao.save(enableFiture);
                    }
                    TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahun, mhs.getIdProdi());
                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahun, StatusRecord.AKTIF);
                    if (k == null) {
                        Krs krs = new Krs();
                        krs.setTahunAkademik(tahun);
                        krs.setTahunAkademikProdi(tahunProdi);
                        krs.setProdi(mcicilan.getMahasiswa().getIdProdi());
                        krs.setMahasiswa(mcicilan.getMahasiswa());
                        krs.setNim(mcicilan.getMahasiswa().getNim());
                        krs.setTanggalTransaksi(LocalDateTime.now());
                        krs.setStatus(StatusRecord.AKTIF);
                        krsDao.save(krs);
                    }
                }
            }
        }

        if (cicilan != null) {
            return "redirect:generate?prodi="+prodi.getId()+"&program="+program.getId()+"&angkatan="+angkatan+"&tahunAkademik="+tahun.getId()+"&cicilan="+cicilan;
        }else{
            return "redirect:generate?prodi="+prodi.getId()+"&program="+program.getId()+"&angkatan="+angkatan+"&tahunAkademik="+tahun.getId();
        }
    }

    @PostMapping("/studentBill/billAdmin/form")
    public String inputTagihan(@Valid Tagihan tagihan, RedirectAttributes attributes, Authentication authentication,
                               @RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) Integer cicilan,
                               @RequestParam(required = false) Mahasiswa nim, @RequestParam(required = false) String uas,
                               @RequestParam(required = false) String uts, @RequestParam(required = false) String krs){

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);

        Mahasiswa mhs = nim;
        tagihan.setMahasiswa(mhs);
        Tagihan tgh = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndStatusAndLunas(mhs, tagihan.getNilaiJenisTagihan().getJenisTagihan(), StatusRecord.AKTIF, false);
        if (tgh == null){

            Tagihan t = new Tagihan();
            t.setMahasiswa(mhs);
            t.setNilaiJenisTagihan(tagihan.getNilaiJenisTagihan());
            t.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                    + " a.n. " +tagihan.getMahasiswa().getNama());
            t.setNilaiTagihan(tagihan.getNilaiTagihan());
            if (tagihan.getNilaiTagihanAsli() == null){
                t.setNilaiTagihanAsli(tagihan.getNilaiTagihan());
            }else{
                t.setNilaiTagihanAsli(tagihan.getNilaiTagihanAsli());
            }
            t.setAkumulasiPembayaran(BigDecimal.ZERO);
            t.setTanggalPembuatan(LocalDate.now());
            t.setTanggalJatuhTempo(LocalDate.now().plusMonths(8));
            t.setTanggalPenangguhan(LocalDate.now().plusYears(1));
            t.setTahunAkademik(tagihan.getTahunAkademik());
            t.setStatusTagihan(StatusTagihan.AKTIF);
            t.setStatus(StatusRecord.AKTIF);
            tagihanDao.save(t);
            log.info("tagihan : {}", t);

            if (cicilan != null) {
                for (int i = 1; i<=cicilan; i++) {
                    BigDecimal totalCicilan = requestCicilanDao.hitungCicilan(t.getNilaiTagihan(), cicilan);
                    RequestCicilan rc = new RequestCicilan();
                    rc.setTagihan(t);
                    rc.setTanggalPengajuan(LocalDate.now());
                    rc.setNilaiCicilan(totalCicilan);
                    rc.setStatusCicilan(StatusCicilan.CICILAN);
                    rc.setStatusApprove(StatusApprove.APPROVED);
                    rc.setTanggalJatuhTempo(t.getTanggalPembuatan().plusMonths(i));
                    rc.setWaktuApprove(LocalDateTime.now());
                    log.info("Request Cicilan : {}", rc);
                    requestCicilanDao.save(rc);
                }
                RequestCicilan requestCicilan = requestCicilanDao.cariCicilanSelanjutnya(t);
                if (requestCicilan != null) {
                    requestCicilan.setUserApprove(karyawan);
                    requestCicilan.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
                    requestCicilanDao.save(requestCicilan);
                    tagihanService.mengirimCicilanSelanjutnya(requestCicilan);
                }
                t.setStatusTagihan(StatusTagihan.DICICIL);
                tagihanDao.save(t);
            }else {
                tagihanService.requestCreateTagihan(t);
            }

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

            Integer sisaCicilan = tgh.getNilaiTagihan().intValue() - tgh.getAkumulasiPembayaran().intValue();

            Tagihan t = new Tagihan();
            t.setMahasiswa(mhs);
            t.setNilaiJenisTagihan(tagihan.getNilaiJenisTagihan());
            t.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                    + " a.n. " +tagihan.getMahasiswa().getNama());
            t.setNilaiTagihan(tagihan.getNilaiTagihan().add(new BigDecimal(sisaCicilan)));
            if (tagihan.getNilaiTagihanAsli() == null){
                t.setNilaiTagihanAsli(tagihan.getNilaiTagihan());
            }else{
                t.setNilaiTagihanAsli(tagihan.getNilaiTagihanAsli());
            }
            t.setAkumulasiPembayaran(BigDecimal.ZERO);
            t.setTanggalPembuatan(LocalDate.now());
            t.setTanggalJatuhTempo(LocalDate.now().plusMonths(8));
            t.setTanggalPenangguhan(LocalDate.now().plusYears(1));
            t.setTahunAkademik(tagihan.getTahunAkademik());
            t.setStatusTagihan(StatusTagihan.AKTIF);
            t.setStatus(StatusRecord.AKTIF);
            t.setIdTagihanSebelumnya(tgh.getId());
            log.info("tagihan : {}", t);
            tagihanDao.save(t);

            if (cicilan != null) {
                for (int i = 1; i<=cicilan; i++) {
                    BigDecimal totalCicilan = requestCicilanDao.hitungCicilan(t.getNilaiTagihan(), cicilan);
                    RequestCicilan rc = new RequestCicilan();
                    rc.setTagihan(t);
                    rc.setTanggalPengajuan(LocalDate.now());
                    rc.setNilaiCicilan(totalCicilan);
                    rc.setStatusCicilan(StatusCicilan.CICILAN);
                    rc.setStatusApprove(StatusApprove.APPROVED);
                    rc.setTanggalJatuhTempo(t.getTanggalPembuatan().plusMonths(i));
                    rc.setWaktuApprove(LocalDateTime.now());
                    requestCicilanDao.save(rc);
                }
                RequestCicilan requestCicilan = requestCicilanDao.cariCicilanSelanjutnya(t);
                if (requestCicilan != null) {
                    requestCicilan.setUserApprove(karyawan);
                    requestCicilan.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
                    requestCicilanDao.save(requestCicilan);
                    RequestCicilan cariSebelumnya = requestCicilanDao.findByTagihanAndStatusCicilanAndStatus(tgh, StatusCicilan.SEDANG_DITAGIHKAN, StatusRecord.AKTIF);
                    if (cariSebelumnya.getTanggalJatuhTempo().isAfter(LocalDate.now()) || cariSebelumnya.getTanggalJatuhTempo().isEqual(LocalDate.now())) {
                        tagihanService.ubahJadiCicilan(requestCicilan);
                    }else{
                        tagihanService.mengirimCicilanSelanjutnya(requestCicilan);
                    }
                }
                t.setStatusTagihan(StatusTagihan.DICICIL);
                tagihanDao.save(t);
            }else {
                if (tgh.getTanggalJatuhTempo().isAfter(LocalDate.now())) {
                    tagihanService.editTagihan(t);
                }else{
                    tagihanService.requestCreateTagihan(t);
                }
            }

            if (tgh.getStatusTagihan() == StatusTagihan.DICICIL) {
                List<RequestCicilan> cekSisaCicilan = requestCicilanDao.findByTagihanAndStatusAndStatusCicilanNotIn(tgh, StatusRecord.AKTIF, Arrays.asList(StatusCicilan.LUNAS));
                if (cekSisaCicilan != null) {
                    for (RequestCicilan listCicilan : cekSisaCicilan){
                        listCicilan.setStatus(StatusRecord.NONAKTIF);
                        listCicilan.setStatusCicilan(StatusCicilan.LEWAT_JATUH_TEMPO);
                        requestCicilanDao.save(listCicilan);
                    }
                }
            }

            if (tgh.getStatusTagihan() == StatusTagihan.DITANGGUHKAN) {
                RequestPenangguhan rp = requestPenangguhanDao.findByTagihanAndStatusAndStatusApproveNotIn(tgh, StatusRecord.AKTIF, Arrays.asList(StatusApprove.WAITING));
                if (rp != null) {
                    rp.setStatus(StatusRecord.NONAKTIF);
                    requestPenangguhanDao.save(rp);
                }
            }

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
            tgh.setStatusTagihan(StatusTagihan.NONAKTIF);
            tgh.setStatus(StatusRecord.NONAKTIF);
            tagihanDao.save(tgh);
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
        tagihan.setKaryawan(karyawan);
        log.debug("pengedit : {}" + karyawan);

        List<TagihanBeasiswa> listBeasiswaTagihan = tagihanBeasiswaDao.findByStatus(StatusRecord.AKTIF);

        List<String> listId = new ArrayList<>();
        for (TagihanBeasiswa list : listBeasiswaTagihan){
            listId.add(list.getBeasiswa().getId());
        }
        List<Beasiswa> listBeasiswa = beasiswaDao.findByStatusAndIdNotIn(StatusRecord.AKTIF, listId);

        if (listBeasiswa != null){
            for (Beasiswa beasiswa : listBeasiswa){
                if (nim.getBeasiswa() == beasiswa) {
                    Pembayaran p = pembayaranDao.findByStatusAndTagihan(StatusRecord.AKTIF, tagihan);
                    p.setAmount(nilaiTagihan);
                    tagihan.setAkumulasiPembayaran(nilaiTagihan);
                    pembayaranDao.save(p);
                }
            }
        }

        tagihan.setNilaiTagihan(nilaiTagihan);
        tagihan.setNilaiTagihanAsli(nilaiTagihan);
        tagihanDao.save(tagihan);
        if (tagihan.getStatusTagihan() != StatusTagihan.LUNAS){
            tagihanService.editTagihan(tagihan);
        }

        return "redirect:list?tahunAkademik="+tahun.getId()+"&nim="+tagihan.getMahasiswa().getNim();
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
        StatusTagihan info = tagihan.getStatusTagihan();
        if (info == StatusTagihan.DICICIL) {
            List<RequestCicilan> cekSisaCicilan = requestCicilanDao.findByTagihanAndStatusAndStatusCicilanNotIn(tagihan, StatusRecord.AKTIF, Arrays.asList(StatusCicilan.LUNAS));
            if (cekSisaCicilan != null) {
                for (RequestCicilan cicilan : cekSisaCicilan){
                    cicilan.setStatus(StatusRecord.HAPUS);
                    cicilan.setStatusCicilan(StatusCicilan.BATAL_CICIL);
                    cicilan.setStatusApprove(StatusApprove.HAPUS);
                    requestCicilanDao.save(cicilan);
                }
            }
        }
        List<VirtualAccount> va = virtualAccountDao.findByTagihan(tagihan);
        for(VirtualAccount listVa : va){
            virtualAccountDao.delete(listVa);
        }
        tagihan.setStatus(StatusRecord.HAPUS);
        tagihan.setStatusTagihan(StatusTagihan.HAPUS);
        tagihanDao.save(tagihan);
        tagihanService.hapusTagihan(tagihan);
        return "redirect:list?tahunAkademik="+tagihan.getTahunAkademik().getId()+"&nim="+tagihan.getMahasiswa().getNim();
    }

//    REKAPAN TAGIHAN

    @GetMapping("/studentBill/rekapTagihan/angkatan")
    public String listRekapTagihanAngkatan(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik){

        model.addAttribute("selectTahun", tahunAkademik);

        model.addAttribute("listData", tagihanDao.listAllTagihanPerAngkatan());
        model.addAttribute("toTagihan", tagihanDao.totalAllTagihan());
        model.addAttribute("toDibayar", pembayaranDao.totalAllDibayar());
        if (tahunAkademik != null) {
            model.addAttribute("listData", tagihanDao.listAllTagihanPerAngkatanByTahunAkademik(tahunAkademik.getId()));
            model.addAttribute("toTagihan", tagihanDao.totalAllTagihanByTahunAkademik(tahunAkademik.getId()));
            model.addAttribute("toDibayar", pembayaranDao.totalAllDibayarByTahunAkademik(tahunAkademik.getId()));
        }

        return "studentBill/rekapTagihan/listAngkatan";
    }
    @GetMapping("/studentBill/rekapTagihan/list")
    public String listRekapanTagihan(Model model, @RequestParam(required = false) String angkatan, @RequestParam(required = false) String tahunAkademik){

        model.addAttribute("selectAngkatan", angkatan);
        model.addAttribute("selectTahun", tahunAkademik);

        if (angkatan != null){
            model.addAttribute("listMahasiswa", tagihanDao.listAllTagihanPerMahasiswaByAngkatan(angkatan));
            if (tahunAkademik != null) {
                model.addAttribute("listMahasiswa", tagihanDao.listAllTagihanPerMahasiswaByAngkatanDanTahunAkademik(angkatan, tahunAkademik));
            }
        }

        return "studentBill/rekapTagihan/list";
    }

    @GetMapping("/studentBill/rekapTagihan/detail")
    public String detailRekapanTagihan(Model model, @RequestParam(required = false) String nim, @RequestParam(required = false) String tahunAkademik){

        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        List<Object[]> listDetail = tagihanDao.listDetailRekapTagihanPerMahasiswa(nim);
        model.addAttribute("selectTahun", tahunAkademik);

        model.addAttribute("listDetail", listDetail);
        model.addAttribute("totalTagihan", tagihanDao.totalAllTagihanPerMahasiswa(nim));
        model.addAttribute("totalDibayar", pembayaranDao.totalAllDibayarPerMahasiswa(nim));
        model.addAttribute("mhs", mhs);

        if (tahunAkademik != null) {
            model.addAttribute("listDetail", tagihanDao.listDetailRekapTagihanPerMahasiswaByTahunAkademik(nim, tahunAkademik));
            model.addAttribute("totalTagihan", tagihanDao.totalAllTagihanPerMahasiswaByTahunAkademik(nim, tahunAkademik));
            model.addAttribute("totalDibayar", pembayaranDao.totalAllDibayarPerMahasiswaByTahunAkademik(nim, tahunAkademik));
        }

        return "studentBill/rekapTagihan/detail";
    }

    @GetMapping("/studentBill/rekapTagihan/detailAsli")
    public void detailRekapanTagihanNilaiAsli(Model model, @RequestParam String nim){

        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        List<Object[]> listDetail = tagihanDao.listDetailRekapTagihanPerMahasiswa(nim);

        model.addAttribute("listDetail", listDetail);
        model.addAttribute("totalTagihan", tagihanDao.totalAllTagihanAsliPerMahasiswa(nim));
        model.addAttribute("totalDibayar", pembayaranDao.totalAllDibayarPerMahasiswa(nim));
        model.addAttribute("mhs", mhs);
    }

    @GetMapping("/rekapTagihan/detailMahasiswa")
    public void rekapTagihanPerMahasiswa(@RequestParam String nim, HttpServletResponse response) throws IOException {
        String[] columns = {"No", "Tahun Akademik", "Keterangan", "Tanggal ditagihkan", "Tagihan", "Tanggal dibayar", "Dibayar", "Sisa"};

        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        BigDecimal totalTagihan = tagihanDao.totalAllTagihanPerMahasiswa(nim);
        BigDecimal totalPembayaran = pembayaranDao.totalAllDibayarPerMahasiswa(nim);
        BigDecimal sisa = totalTagihan.subtract(totalPembayaran);
        List<Object[]> listDetail = tagihanDao.listDetailRekapTagihanPerMahasiswa(nim);
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Rekap Tagihan");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (Object[] list : listDetail){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list[1].toString());
            row.createCell(2).setCellValue(list[2].toString());
            row.createCell(3).setCellValue(list[3].toString());
            row.createCell(4).setCellValue(list[4].toString());
            row.createCell(5).setCellValue(list[5].toString());
            row.createCell(6).setCellValue(list[6].toString());
            row.createCell(7).setCellValue(list[7].toString());
        }

        Row row = sheet.createRow(rowNum);
        row.createCell(2).setCellValue("Total");
        row.createCell(4).setCellValue(totalTagihan.toString());
        row.createCell(6).setCellValue(totalPembayaran.toString());
        row.createCell(7).setCellValue(sisa.toString());

        for (int i = 0;i<columns.length;i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Rekap Tagihan Mahasiswa "+mhs.getNim()+"_"+mhs.getNama()+".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    // Request Penangguhan

    @GetMapping("/studentBill/requestPenangguhan/list")
    public void listPenangguhan(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listPenangguhan", requestPenangguhanDao.findByTagihanMahasiswaNamaContainingIgnoreCaseOrTagihanMahasiswaNimContainingIgnoreCaseAndStatusNotInOrderByTanggalPenangguhan(search, search, Arrays.asList(StatusRecord.HAPUS), page));
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
        model.addAttribute("minimalTgl", LocalDate.now().plusMonths(1));
        model.addAttribute("tahun", tahunProdiDao.findByTahunAkademikAndProdi(tagihan.getTahunAkademik(), tagihan.getMahasiswa().getIdProdi()));
        model.addAttribute("jumlahFile", tagihanDocumentDao.countAllByTagihanAndStatusAndStatusDocument(tagihan, StatusRecord.AKTIF, StatusDocument.PENANGGUHAN));
        model.addAttribute("dokumen", tagihanDocumentDao.findByStatusNotInAndTagihanAndStatusDocument(Arrays.asList(StatusRecord.HAPUS), tagihan, StatusDocument.PENANGGUHAN, page));
    }

    @GetMapping("/studentBill/requestPenangguhan/approval")
    public void approval(Model model, @RequestParam(required = false) String id,
                         @RequestParam(required = false) Tagihan tagihan, @PageableDefault(size = 10) Pageable page){

        RequestPenangguhan requestPenangguhan = requestPenangguhanDao.findById(id).get();
        StatusApprove info = requestPenangguhan.getStatusApprove();
        model.addAttribute("penangguhan", requestPenangguhan);
        model.addAttribute("bill", tagihan);
        model.addAttribute("dokumen", tagihanDocumentDao.findByStatusNotInAndTagihanAndStatusDocument(Arrays.asList(StatusRecord.HAPUS), tagihan, StatusDocument.PENANGGUHAN, page));
        if (info != StatusApprove.WAITING){
            model.addAttribute("message", "penangguhan sudah di approve/reject");
        }
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

        if (TAGIHAN_KRS.contains(tagihan.getNilaiJenisTagihan().getJenisTagihan().getKode())) {

            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(),
                    StatusRecord.KRS, false, tagihan.getTahunAkademik());
            if (enableFiture == null) {
                enableFiture = new EnableFiture();
                enableFiture.setFitur(StatusRecord.KRS);
                enableFiture.setMahasiswa(tagihan.getMahasiswa());
                enableFiture.setTahunAkademik(tagihan.getTahunAkademik());
                enableFiture.setKeterangan("-");
            }
            enableFiture.setEnable(true);
            enableFitureDao.save(enableFiture);

            TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(tagihan.getTahunAkademik(), tagihan.getMahasiswa().getIdProdi());

            Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(tagihan.getMahasiswa(), tagihan.getTahunAkademik(), StatusRecord.AKTIF);
            if (krs == null) {
                tagihanService.createKrs(tagihan, tahunProdi);
            }

        }

        return "redirect:../requestPenangguhan/list";
    }

    @PostMapping("/studentBill/dokumen/deletep")
    public String hapusDokumenPenangguhan(@RequestParam Tagihan tagihan){
        List<TagihanDocument> tagihanDoc = tagihanDocumentDao.findByTagihanAndStatusAndStatusDocument(tagihan, StatusRecord.AKTIF, StatusDocument.PENANGGUHAN);
        for (TagihanDocument document : tagihanDoc){
            document.setStatus(StatusRecord.HAPUS);
            tagihanDocumentDao.save(document);
        }

        return "redirect:../requestPenangguhan/date?id="+tagihan.getId();
    }

    @PostMapping("/studentBill/penangguhan/reject")
    public String rejectPenangguhan(@RequestParam RequestPenangguhan requestPenangguhan,
                                    @RequestParam(required = false) String keterangan,
                                    Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        requestPenangguhan.setUserApprove(karyawan);
        requestPenangguhan.setTanggalApprove(LocalDate.now());
        requestPenangguhan.setKeteranganReject(keterangan);
        requestPenangguhan.setStatusApprove(StatusApprove.REJECTED);
        requestPenangguhan.setStatus(StatusRecord.AKTIF);
        requestPenangguhanDao.save(requestPenangguhan);

        List<TagihanDocument> td = tagihanDocumentDao.findByTagihanAndStatusAndStatusDocument(requestPenangguhan.getTagihan(), StatusRecord.AKTIF, StatusDocument.PENANGGUHAN);
        for (TagihanDocument document : td){
            document.setStatus(StatusRecord.HAPUS);
            tagihanDocumentDao.save(document);
        }
        return "redirect:../requestPenangguhan/list";
    }

    @GetMapping("/penangguhan/download")
    public void downloadPenangguhan(@RequestParam TahunAkademik tahun, HttpServletResponse response) throws IOException {

        String[] columns = {"No", "NIM", "Nama", "Tagihan", "Tahun Akademik", "Tanggal Pengajuan", "Tanggal Penangguhan", "Status"};

        List<RequestPenangguhan> penangguhan = requestPenangguhanDao.findByStatusAndTagihanTahunAkademikOrderByTanggalPengajuan(StatusRecord.AKTIF, tahun);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Tahun "+tahun.getTahun()+" "+tahun.getJenis());

        sheet.addMergedRegion(CellRangeAddress.valueOf("C2:F2"));

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Font ketFont = workbook.createFont();
        ketFont.setBold(true);
        ketFont.setFontHeightInPoints((short) 14);
        ketFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle ketCellStyle = workbook.createCellStyle();
        ketCellStyle.setFont(ketFont);
        ketCellStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(3);
        for (int i = 0; i<columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowKet = 1;
        Row keterangan = sheet.createRow(rowKet);
        keterangan.createCell(2).setCellValue("Data Request Penangguhan Tahun " + tahun.getNamaTahunAkademik());
        keterangan.getCell(2).setCellStyle(ketCellStyle);

        int rowNum = 4;
        int baris = 1;

        for (RequestPenangguhan p : penangguhan){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(p.getTagihan().getMahasiswa().getNim());
            row.createCell(2).setCellValue(p.getTagihan().getMahasiswa().getNama());
            row.createCell(3).setCellValue(p.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getNama());
            row.createCell(4).setCellValue(p.getTagihan().getTahunAkademik().getNamaTahunAkademik());
            row.createCell(5).setCellValue(p.getTanggalPengajuan().toString());
            row.createCell(6).setCellValue(p.getTanggalPenangguhan().toString());
            row.createCell(7).setCellValue(p.getStatus().toString());
        }

        for (int i = 0; i<columns.length;i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vns.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Data Request Penangguhan Tahun Akademik " + tahun.getNamaTahunAkademik() + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    // request cicilan

    @GetMapping("/studentBill/requestCicilan/list")
    public void listCicilan(Model model, @PageableDefault(size = 10) Pageable page, @RequestParam(required = false) String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listCicilan", requestCicilanDao.listRequestCicilanSearch(search, page));
        }else{
            model.addAttribute("listCicilan", requestCicilanDao.listRequestCicilan1(page));
        }
    }

    @GetMapping("/studentBill/requestCicilan/approval")
    public void approvalCicilan(Model model, @PageableDefault(size = 50) Pageable page,
                                @RequestParam(required = false) Tagihan tagihan){

        StatusTagihan info = tagihan.getStatusTagihan();
        model.addAttribute("cek", info);
        if (info == StatusTagihan.DICICIL){
            model.addAttribute("message", "message");
        }
        model.addAttribute("request", requestCicilanDao.findByStatusNotInAndTagihanOrderByTanggalJatuhTempo(Arrays.asList(StatusRecord.HAPUS), tagihan, page));
        model.addAttribute("jumlahCicilan", requestCicilanDao.countRequestCicilanByTagihanAndStatus(tagihan, StatusRecord.AKTIF));
        model.addAttribute("jumlahNilai", requestCicilanDao.sisaCicilan(tagihan.getId()));
        model.addAttribute("jumlahFile", tagihanDocumentDao.countAllByTagihanAndStatusAndStatusDocument(tagihan, StatusRecord.AKTIF, StatusDocument.CICILAN));
        model.addAttribute("dokumen", tagihanDocumentDao.findByStatusNotInAndTagihanAndStatusDocument(Arrays.asList(StatusRecord.HAPUS), tagihan, StatusDocument.CICILAN, page));
        model.addAttribute("bill", tagihan);

//      Pembayaran manuan cicilan
        Pembayaran p = new Pembayaran();
        if (tagihan != null) {
            p.setTagihan(tagihan);
        }
        model.addAttribute("pembayaran", p);
        model.addAttribute("bank", bankDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("tipe", Pembayaran.TipePengiriman.values());

    }

    @PostMapping("/studentBill/cicilan/approve")
    public String approvalCicilan(@RequestParam(required = false) Tagihan bill,
                                  Authentication authentication){

        bill.setStatusTagihan(StatusTagihan.DICICIL);
        tagihanDao.save(bill);
        List<VirtualAccount> va = virtualAccountDao.findByTagihan(bill);
        for(VirtualAccount listVa : va){
            virtualAccountDao.delete(listVa);
        }

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
            requestCicilan.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
            requestCicilanDao.save(requestCicilan);
            tagihanService.ubahJadiCicilan(requestCicilan);
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

    @PostMapping("/studentBill/cicilan/payment/manual")
    public String manualPaymentCicilan(@ModelAttribute @Valid Pembayaran pembayaran, @RequestParam(required = false) String tagihan, @RequestParam String waktu,
                                       @RequestParam(required = false) String rc, MultipartFile fileBukti) throws IOException {

        Tagihan t = tagihanDao.findById(tagihan).get();
        RequestCicilan requestCicilan = requestCicilanDao.findById(rc).get();
        User usr = t.getMahasiswa().getUser();

        String idPeserta = t.getMahasiswa().getNim();

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

        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }

        String idFile = UUID.randomUUID().toString();
        String lokasiUpload = uploadFolder + File.separator + idPeserta;
        log.debug("Lokasi Upload : {}" + lokasiUpload);
        new File(lokasiUpload).mkdirs();
        File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
        pembayaran.setReferensi(idFile + '.' + extension);
        fileBukti.transferTo(tujuan);
        log.debug("file sudah dicopy ke : {}" + tujuan.getAbsolutePath());
        LocalDate date = LocalDate.parse(waktu);
        LocalDateTime waktuBayar = date.atTime(LocalTime.now());
        pembayaran.setWaktuBayar(waktuBayar);
        pembayaran.setAmount(requestCicilan.getNilaiCicilan());
        pembayaranDao.save(pembayaran);

        BigDecimal akumulasi = t.getAkumulasiPembayaran().add(requestCicilan.getNilaiCicilan());
        BigDecimal nilai = t.getNilaiTagihan();
        t.setAkumulasiPembayaran(akumulasi);
        log.info("Akumulasi : {}", akumulasi);
        if (akumulasi.compareTo(nilai) == 0) {
            t.setLunas(true);
            t.setStatusTagihan(StatusTagihan.LUNAS);
            log.info("nomor tagihan {} LUNAS", t.getNomor());

            if (usr.getRole().getId().equals("mahasiswanunggak")){
                Role role = roleDao.findById("mahasiswa").get();
                usr.setRole(role);
                userDao.save(usr);
            }
        }
        tagihanDao.save(t);

        requestCicilan.setStatusCicilan(StatusCicilan.LUNAS);
        requestCicilanDao.save(requestCicilan);

        RequestCicilan rCicilan = requestCicilanDao.cariCicilanSelanjutnya(t);
        if (rCicilan == null) {
            log.info("Tidak ada cicilan lagi. ");

            if (usr.getRole().getId().equals("mahasiswanunggak")){
                Role role = roleDao.findById("mahasiswa").get();
                usr.setRole(role);
                userDao.save(usr);
            }
        }else{
            rCicilan.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
            requestCicilanDao.save(rCicilan);
            tagihanService.ubahJadiCicilan(rCicilan);
            log.info("kirim cicilan selanjutnya : {}", rCicilan.getNilaiCicilan());

            if (usr.getRole().getId().equals("mahasiswanunggak")){
                Role role = roleDao.findById("mahasiswa").get();
                usr.setRole(role);
                userDao.save(usr);
            }
        }

        if (TAGIHAN_KRS.contains(t.getNilaiJenisTagihan().getJenisTagihan().getKode())) {

            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(t.getMahasiswa(),
                    StatusRecord.KRS, false, t.getTahunAkademik());
            if (enableFiture == null) {
                enableFiture = new EnableFiture();
                enableFiture.setFitur(StatusRecord.KRS);
                enableFiture.setMahasiswa(t.getMahasiswa());
                enableFiture.setTahunAkademik(t.getTahunAkademik());
                enableFiture.setKeterangan("-");
            }
            enableFiture.setEnable(true);
            enableFitureDao.save(enableFiture);

            TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(t.getTahunAkademik(), t.getMahasiswa().getIdProdi());

            Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(t.getMahasiswa(), t.getTahunAkademik(), StatusRecord.AKTIF);
            if (krs == null) {
                tagihanService.createKrs(t, tahunProdi);
            }

        }

        return "redirect:../../requestCicilan/approval?tagihan="+tagihan;

    }

    @GetMapping("/studentBill/requestCicilan/detail")
    public void detailCicilan(Model model, @RequestParam(required = false) Tagihan tagihan,
                              @PageableDefault(size = 50) Pageable page){

        RequestCicilan cicilan = requestCicilanDao.findFirstByTagihanAndStatusCicilanAndStatus(tagihan, StatusCicilan.EDITED, StatusRecord.AKTIF);
        if (cicilan == null) {
            model.addAttribute("message", "message");
        }
        Integer jumlahCicilan = requestCicilanDao.countRequestCicilanByTagihanAndStatus(tagihan, StatusRecord.AKTIF);
        model.addAttribute("request", requestCicilanDao.findByStatusNotInAndTagihanOrderByTanggalJatuhTempo(Arrays.asList(StatusRecord.HAPUS), tagihan, page));
        model.addAttribute("bill", tagihan);
        model.addAttribute("pembayaran", pembayaranDao.findByTagihanAndStatusOrderByWaktuBayar(tagihan, StatusRecord.AKTIF, page));

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
    public String pelunasan(@RequestParam(required = false) Tagihan tagihan, @RequestParam(required = false) BigDecimal nilaiCicilan){

        RequestCicilan requestCicilan = requestCicilanDao.findByTagihanAndStatusCicilanAndStatus(tagihan, StatusCicilan.SEDANG_DITAGIHKAN, StatusRecord.AKTIF);

        log.info("jumlah cicilan : {}", nilaiCicilan);
        List<RequestCicilan> cekSisaCicilan = requestCicilanDao.findByTagihanAndStatusAndStatusCicilanNotIn(tagihan, StatusRecord.AKTIF, Arrays.asList(StatusCicilan.LUNAS, StatusCicilan.SEDANG_DITAGIHKAN));
        for (RequestCicilan sisaCicilan : cekSisaCicilan){
            sisaCicilan.setStatus(StatusRecord.HAPUS);
            sisaCicilan.setStatusCicilan(StatusCicilan.BATAL_CICIL);
            sisaCicilan.setStatusApprove(StatusApprove.HAPUS);
            requestCicilanDao.save(sisaCicilan);
        }

        requestCicilan.setNilaiCicilan(nilaiCicilan);
        requestCicilan.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
        requestCicilan.setKeterangan("PENGAJUAN PELUNASAN");
        requestCicilanDao.save(requestCicilan);
        tagihanService.ubahJadiCicilan(requestCicilan);

        return "redirect:../billAdmin/detail?tagihan="+tagihan.getId();
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

    @GetMapping("/studentBill/requestCicilan/angsuran")
    public void cicilanTagihan(Model model, @PageableDefault(size = 10) Pageable page, Authentication authentication,
                               @RequestParam(required = false) String id, @RequestParam(required = false) String jumlah,
                               @RequestParam(required = false) String interval){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        Tagihan tagihan = tagihanDao.findById(id).get();
        Integer jumlahCicilan = requestCicilanDao.countRequestCicilanByTagihanAndStatus(tagihan, StatusRecord.AKTIF);
        model.addAttribute("cicilan", new RequestCicilan());
        model.addAttribute("request", requestCicilanDao.findByStatusNotInAndTagihanOrderByTanggalJatuhTempo(Arrays.asList(StatusRecord.HAPUS), tagihan, page));
        model.addAttribute("jumlahCicilan", jumlahCicilan);
        model.addAttribute("jumlahNilai", requestCicilanDao.sisaCicilan(tagihan.getId()));
        model.addAttribute("jumlahFile", tagihanDocumentDao.countAllByTagihanAndStatusAndStatusDocument(tagihan, StatusRecord.AKTIF, StatusDocument.CICILAN));
        model.addAttribute("dokumen", tagihanDocumentDao.findByStatusNotInAndTagihanAndStatusDocument(Arrays.asList(StatusRecord.HAPUS), tagihan, StatusDocument.CICILAN, page));
        List<RequestCicilan> editCicilan = requestCicilanDao.findByStatusAndStatusCicilanAndTagihanOrderByTanggalJatuhTempo(StatusRecord.AKTIF, StatusCicilan.EDITED, tagihan);
        if (jumlah == null) {
            model.addAttribute("message", "message");
        }
//        List<RequestCicilan> fixCicilan = requestCicilanDao.findByStatusAndStatusCicilanAndTagihanOrderByTanggalJatuhTempo(StatusRecord.AKTIF, StatusCicilan.CICILAN, tagihan);
//        System.out.println("cicilan : " + fixCicilan);
//        if (fixCicilan != null) {
//            model.addAttribute("fixCicilan", fixCicilan);
//        }else{
//            model.addAttribute("cicilan", "cicilan");
//        }
        EnableFiture cicilan = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mahasiswa, StatusRecord.CICILAN, true, tagihan.getTahunAkademik());
        List<RequestCicilan> cicilanAda = requestCicilanDao.findByStatusAndStatusCicilanAndTagihanOrderByTanggalJatuhTempo(StatusRecord.AKTIF, StatusCicilan.CICILAN, tagihan);
        model.addAttribute("adaCicilan", cicilanAda);
        model.addAttribute("custom", cicilan);
        model.addAttribute("listCicilan", editCicilan);
        model.addAttribute("bill", tagihan);
        model.addAttribute("jumlah", jumlah);
        model.addAttribute("interval", interval);

    }

    @PostMapping("/studentBill/requestCicilan/cicil")
    public String cicil(@RequestParam(required = false) int jumlah, @RequestParam(required = false) Integer interval,
                        @RequestParam(required = false) String tagihan){
        Tagihan t = tagihanDao.findById(tagihan).get();
        List<RequestCicilan> cicilanSudahAda = requestCicilanDao.findByStatusAndTagihanOrderByTanggalJatuhTempo(StatusRecord.AKTIF, t);
        for (RequestCicilan cicilan : cicilanSudahAda){
            requestCicilanDao.delete(cicilan);
        }

        for (int i = 1; i<=jumlah; i++) {
            BigDecimal totalCicilan = requestCicilanDao.hitungCicilan(t.getNilaiTagihan(), jumlah);
            RequestCicilan rc = new RequestCicilan();
            rc.setTagihan(t);
            rc.setTanggalPengajuan(LocalDate.now());
            rc.setNilaiCicilan(totalCicilan);
            rc.setStatusCicilan(StatusCicilan.EDITED);
            int j = 0;
            if (interval == null) {
                for (int a = 0; i > a; a++){
                    j += 45;
                }
                rc.setTanggalJatuhTempo(t.getTanggalPembuatan().plusDays(j));
            }else{
                for (int a = 0; i > a; a++){
                    j += interval;
                }
                rc.setTanggalJatuhTempo(t.getTanggalPembuatan().plusMonths(j));
            }
            requestCicilanDao.save(rc);
        }

        if (interval == null){
            return "redirect:angsuran?id="+t.getId()+"&jumlah="+jumlah;
        }else{
            return "redirect:angsuran?id="+t.getId()+"&jumlah="+jumlah+"&interval="+interval;
        }

    }

    @PostMapping("/studentBill/cicilan/ajukan")
    public String ajukanCicilan(@RequestParam String tagihan, @RequestParam(required = false) String jumlah, @RequestParam(required = false) String interval,
                                HttpServletRequest request, RedirectAttributes attributes){

        Tagihan t = tagihanDao.findById(tagihan).get();

        List<RequestCicilan> listCicilan = requestCicilanDao.findByStatusAndStatusCicilanAndTagihanOrderByTanggalJatuhTempo(StatusRecord.AKTIF, StatusCicilan.EDITED, t);
        System.out.println("cicilan : " + listCicilan);
        for (RequestCicilan cicilan : listCicilan){
            String pilihan = request.getParameter("nilaiCicilan-"+cicilan.getId()+cicilan.getTanggalJatuhTempo());
            if (pilihan != null && !pilihan.trim().isEmpty()) {

                RequestCicilan rc = requestCicilanDao.findById(cicilan.getId()).get();
                rc.setNilaiCicilan(new BigDecimal(pilihan));
                requestCicilanDao.save(rc);

            }
        }

        BigDecimal total = requestCicilanDao.sumTotalCicilan(tagihan);
        System.out.println("cicilan : " + total);
        System.out.println("tagihan : " + t.getNilaiTagihan());
        if (total.compareTo(t.getNilaiTagihan()) < 0) {

            attributes.addFlashAttribute("kurang", "jumlah kurang");
            int sisa = total.intValue() - t.getNilaiTagihan().intValue();
            attributes.addFlashAttribute("detail", sisa);
            return "redirect:../requestCicilan/angsuran?id="+t.getId()+"&jumlah="+jumlah;

        } else if (total.compareTo(t.getNilaiTagihan()) > 0) {
            attributes.addFlashAttribute("lebih", "jumah berlebih");
            int sisa = total.intValue() - t.getNilaiTagihan().intValue();
            attributes.addFlashAttribute("detail", sisa);
            return "redirect:../requestCicilan/angsuran?id="+t.getId()+"&jumlah="+jumlah;
        }else{
            for (RequestCicilan cicilan : listCicilan){
                String pilihan = request.getParameter("nilaiCicilan-"+cicilan.getId()+cicilan.getTanggalJatuhTempo());
                if (pilihan != null && !pilihan.trim().isEmpty()) {

                    RequestCicilan rc = requestCicilanDao.findById(cicilan.getId()).get();
                    rc.setStatusCicilan(StatusCicilan.CICILAN);
                    requestCicilanDao.save(rc);

                }
            }
        }

        return "redirect:../payment/form?tagihan="+t.getId();

    }

    @PostMapping("/studentBill/billAdmin/cicilan")
    public String newCicilanAdmin(@Valid RequestCicilan requestCicilan,
                                  @RequestParam(required = false) Tagihan tagihan){

        List<RequestCicilan> rc = requestCicilanDao.findByTagihanAndStatusAndStatusApprove(tagihan, StatusRecord.AKTIF, StatusApprove.WAITING);
        requestCicilan.setTagihan(tagihan);
        requestCicilan.setStatusApprove(StatusApprove.WAITING);
        requestCicilan.setStatus(StatusRecord.AKTIF);
        requestCicilanDao.save(requestCicilan);

        return "redirect:../requestCicilan/approval?tagihan="+tagihan.getId();
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
    public String hapusDokumen(@RequestParam Tagihan tagihan){
        List<TagihanDocument> doc = tagihanDocumentDao.findByTagihanAndStatusAndStatusDocument(tagihan, StatusRecord.AKTIF, StatusDocument.CICILAN);
        for (TagihanDocument document : doc){
            document.setStatus(StatusRecord.HAPUS);
            tagihanDocumentDao.save(document);
        }

        return "redirect:../requestCicilan/angsuran?id="+tagihan.getId();
    }

    @GetMapping("/studentBill/requestCicilan/rekap")
    public void rekapCicilan(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik, @PageableDefault(size = 10) Pageable page,
                             @RequestParam(required = false) String tglAwal,
                             @RequestParam(required = false) String tglAkhir){

        model.addAttribute("tglAwal", tglAwal);
        model.addAttribute("tglAkhir", tglAkhir);
        model.addAttribute("selectTahun", tahunAkademik);

        if (!StringUtils.hasText(tglAwal)  && !StringUtils.hasText(tglAkhir) && tahunAkademik != null) {
            model.addAttribute("listRekapan", requestCicilanDao.listRekapanCicilan(tahunAkademik.getId(), page));
        }

        if (StringUtils.hasText(tglAwal) && StringUtils.hasText(tglAkhir)  && tahunAkademik != null) {
            model.addAttribute("listRekapanDate", requestCicilanDao.listRekapanCicilanDate(tglAwal, tglAkhir, tahunAkademik.getId(), page));
        }
    }

    @GetMapping("/cicilan/rekap/download")
    public void downloadCicilan(@RequestParam String tahunAkademik, @RequestParam(required = false) String tglAwal, @RequestParam(required = false) String tglAkhir,
                                HttpServletResponse response, @PageableDefault(size = Integer.MAX_VALUE) Pageable page) throws IOException {

        TahunAkademik tahun = tahunAkademikDao.findById(tahunAkademik).get();

        Page<Object[]> cicilan = null;
        if ("".equals(tglAwal)) {
            cicilan = requestCicilanDao.listRekapanCicilan(tahunAkademik, page);
        }else{
            cicilan = requestCicilanDao.listRekapanCicilanDate(tglAwal, tglAkhir, tahunAkademik, page);
        }

        String[] columns = {"No", "NIM", "Mahasiswa", "Nama Prodi", "Tagihan", "Tanggal Pengajuan", "Cicilan", "Total Tagihan", "Dibayar", "Sisa"};

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("List rekapan cicilan");

        Font fontHeader = workbook.createFont();
        fontHeader.setBold(true);
        fontHeader.setFontHeightInPoints((short) 11);
        fontHeader.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(fontHeader);

        Row rowHeader = sheet.createRow(0);

        for (int i = 0; i<columns.length; i++){
            Cell cell = rowHeader.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (Object[] list : cicilan){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list[0].toString());
            row.createCell(2).setCellValue(list[1].toString());
            row.createCell(3).setCellValue(list[2].toString());
            row.createCell(4).setCellValue(list[3].toString());
            row.createCell(5).setCellValue(list[4].toString());
            row.createCell(6).setCellValue(list[5].toString());
            row.createCell(7).setCellValue(list[6].toString());
            row.createCell(8).setCellValue(list[7].toString());
            row.createCell(9).setCellValue(list[8].toString());
        }

        for (int i = 0; i<columns.length; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Rekapan cicilan " + tahun.getNamaTahunAkademik() + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    // request peringanan

    @GetMapping("/studentBill/requestPeringanan/pengajuan")
    public void requestKeringanan(Model model, @RequestParam String id, @PageableDefault(size = 10) Pageable page){

        model.addAttribute("uploadBerkas", new UploadBerkasDto());
        model.addAttribute("peringanan", new RequestPeringanan());
        Tagihan tagihan = tagihanDao.findById(id).get();
        model.addAttribute("bill", tagihan);
        model.addAttribute("jumlahFile", tagihanDocumentDao.countAllByTagihanAndStatusAndStatusDocument(tagihan, StatusRecord.AKTIF, StatusDocument.PERINGANAN));
        model.addAttribute("dokumen", tagihanDocumentDao.findByStatusNotInAndTagihanAndStatusDocument(Arrays.asList(StatusRecord.HAPUS), tagihan, StatusDocument.PERINGANAN, page));

    }


    @PostMapping("/studentBill/peringanan/pengajuan")
    public String submitPengajuan(@ModelAttribute @Valid RequestPeringanan peringanan,
                                  @RequestParam(required = false) Tagihan tagihan){

        peringanan.setTagihan(tagihan);
        peringanan.setTanggalPengajuan(LocalDate.now());
        requestPeringananDao.save(peringanan);

        return "redirect:../payment/form?tagihan="+tagihan.getId();
    }

    @PostMapping("/studentBill/peringanan/document")
    public String newDocumentPeringnan(@ModelAttribute @Valid UploadBerkasDto berkasDto, BindingResult errors,
                                       MultipartFile fileBerkas1, MultipartFile fileBerkas2, MultipartFile fileBerkas3, MultipartFile fileBerkas4,
                                       MultipartFile fileBerkas5, Authentication authentication, String tagihan) throws Exception{

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        Tagihan t = tagihanDao.findById(tagihan).get();

        if (errors.hasErrors()) {
            log.debug("Error upload berkas pendukung : {}", errors.toString());
        }

        String idPeserta = mahasiswa.getNim();
        String lokasiUpload = uploadBerkasPeringanan + File.separator + idPeserta;
        log.debug("Lokasi Upload : {}", lokasiUpload);
        new File(lokasiUpload).mkdirs();
        StatusDocument peringanan = StatusDocument.PERINGANAN;

        saveBerkas(fileBerkas1, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument1(), peringanan);
        saveBerkas(fileBerkas2, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument2(), peringanan);
        saveBerkas(fileBerkas3, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument3(), peringanan);
        saveBerkas(fileBerkas4, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument4(), peringanan);
        saveBerkas(fileBerkas5, t, berkasDto, lokasiUpload, berkasDto.getJenisDocument5(), peringanan);

        return "redirect:/studentBill/requestPeringanan/pengajuan?id="+t.getId();

    }

    @PostMapping("/studentBill/dokument/delete/peringanan")
    public String hapusDokumenPeringanan(@RequestParam Tagihan tagihan){

        List<TagihanDocument> tagihanDoc = tagihanDocumentDao.findByTagihanAndStatusAndStatusDocument(tagihan, StatusRecord.AKTIF, StatusDocument.PERINGANAN);


        return "redirect:../../requestPeringanan/pengajuan?id="+tagihan.getId();
    }

    @GetMapping("/tagihan/{document}/peringanan")
    public ResponseEntity<byte[]> showDocumentPeringanan(@PathVariable TagihanDocument document) throws Exception{
        String lokasiBerkas = uploadBerkasPeringanan + File.separator + document.getTagihan().getMahasiswa().getNim() + File.separator +
                document.getDocument();

        log.debug("lokasi file : {}", lokasiBerkas);

        try{
            HttpHeaders headers = new HttpHeaders();
            if (document.getDocument().toLowerCase().endsWith("jpeg") || document.getDocument().toLowerCase().endsWith("jpg")){
                headers.setContentType(MediaType.IMAGE_JPEG);
            }else if (document.getDocument().toLowerCase().endsWith("png")){
                headers.setContentType(MediaType.IMAGE_PNG);
            }else if (document.getDocument().toLowerCase().endsWith("pdf")){
                headers.setContentType(MediaType.APPLICATION_PDF);
            }else{
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiBerkas));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        }catch (Exception err){
            log.warn(err.getMessage(), err);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GetMapping("/studentBill/requestPeringanan/list")
    public void listPeringanan(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listPeringanan", requestPeringananDao.findByTagihanMahasiswaNamaContainingIgnoreCaseOrTagihanMahasiswaNimContainingIgnoreCaseAndStatusNotInOrderByTanggalPengajuanDesc(search, search, Arrays.asList(StatusRecord.HAPUS), page));
        }else{
            model.addAttribute("listPeringanan", requestPeringananDao.findByStatusNotInOrderByTanggalPengajuanDesc(Arrays.asList(StatusRecord.HAPUS), page));
        }

    }

    @GetMapping("/studentBill/requestPeringanan/approval")
    public void approvalPeringanan(Model model, @RequestParam(required = false) String id, @RequestParam(required = false) Tagihan tagihan,
                                   @PageableDefault(size = 10) Pageable page){

        RequestPeringanan peringanan = requestPeringananDao.findById(id).get();
        StatusApprove info = peringanan.getStatusApprove();
        model.addAttribute("bill", tagihan);
        model.addAttribute("peringanan", peringanan);
        model.addAttribute("dokumen", tagihanDocumentDao.findByStatusNotInAndTagihanAndStatusDocument(Arrays.asList(StatusRecord.HAPUS), tagihan, StatusDocument.PERINGANAN, page));
        if (info != StatusApprove.WAITING) {
            model.addAttribute("message", "request peringanan sudah di approve");
        }

    }

    @PostMapping("/studentBill/peringanan/approval")
    public String approvePeringanan(@RequestParam(required = false) RequestPeringanan peringanan, @RequestParam(required = false) Tagihan tagihan,
                                    @RequestParam(required = false) String nilai, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);

        peringanan.setStatusApprove(StatusApprove.APPROVED);
        peringanan.setUserApprove(karyawan);
        peringanan.setWaktuApprove(LocalDateTime.now());
        requestPeringananDao.save(peringanan);

        tagihan.setKaryawan(karyawan);
        tagihan.setNilaiTagihan(new BigDecimal(nilai));
        tagihan.setStatusTagihan(StatusTagihan.PERINGANAN);
        tagihanDao.save(tagihan);
        tagihanService.editTagihan(tagihan);

        return "redirect:../requestPeringanan/list";
    }

    @PostMapping("/studentBill/peringanan/reject")
    public String rejectPeringanan(@RequestParam RequestPeringanan peringanan, @RequestParam(required = false) String keterangan,
                                   Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        peringanan.setStatusApprove(StatusApprove.REJECTED);
        peringanan.setWaktuApprove(LocalDateTime.now());
        peringanan.setUserApprove(karyawan);
        peringanan.setKeterangan(keterangan);
        requestPeringananDao.save(peringanan);

        List<TagihanDocument> td = tagihanDocumentDao.findByTagihanAndStatusAndStatusDocument(peringanan.getTagihan(), StatusRecord.AKTIF, StatusDocument.PERINGANAN);
        for (TagihanDocument document : td){
            document.setStatus(StatusRecord.HAPUS);
            tagihanDocumentDao.save(document);
        }

        return "redirect:../requestPeringanan/list";

    }

    // detail tagihan mahasiswa

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

        model.addAttribute("cekJumlahCicilan", requestCicilanDao.jumlahCicilan(tagihan1));
        model.addAttribute("cekJumlahPembayaran", pembayaranDao.countAllByTagihan(tagihan1));
        model.addAttribute("pembayaran", pembayaranDao.cekPembayaran(tagihan));
        model.addAttribute("bank", bankDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("virtualAccount", virtualAccountDao.listVaCicilan(tagihan1.getId()));
        model.addAttribute("va", virtualAccountDao.listVa(tagihan1.getId()));
        model.addAttribute("tagihan", tagihan1);
        model.addAttribute("penangguhan", requestPenangguhanDao.findByTagihanAndStatusAndStatusApproveNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusApprove.REJECTED)));
        model.addAttribute("cekPeringanan", requestPeringananDao.findByTagihanAndStatusAndStatusApproveNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusApprove.REJECTED)));
        model.addAttribute("peringanan", enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan1.getMahasiswa(), StatusRecord.PERINGANAN, true, tagihan1.getTahunAkademik()));

    }

    // Pembayaran Manual

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
        model.addAttribute("tipe", Pembayaran.TipePengiriman.values());
    }

    @GetMapping("/api/pembayaran")
    @ResponseBody
    public VirtualAccount va(@RequestParam(required = false) String id, @RequestParam(required = false) String idTagihan){

        return virtualAccountDao.vaPembayaran(id, idTagihan);

    }

    @PostMapping("/studentBill/payment/manual")
    public String paymentManual(@ModelAttribute @Valid Pembayaran pembayaran, @RequestParam String waktu,
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
        LocalDate date = LocalDate.parse(waktu);
        LocalDateTime waktuBayar = date.atTime(LocalTime.now());
        pembayaran.setWaktuBayar(waktuBayar);
        pembayaran.setAmount(tagihan.getNilaiTagihan());
        pembayaranDao.save(pembayaran);

        tagihan.setAkumulasiPembayaran(tagihan.getNilaiTagihan());
        tagihan.setStatusTagihan(StatusTagihan.LUNAS);
        tagihan.setLunas(true);
        tagihanDao.save(tagihan);

        if (TAGIHAN_TOEFL.equals(tagihan.getNilaiJenisTagihan().getJenisTagihan().getKode())) {
            EnableFiture ef = enableFitureDao.findByMahasiswaAndFiturAndEnable(tagihan.getMahasiswa(), StatusRecord.TOEFL, false);
            if (ef != null) {
                ef.setEnable(true);
                ef.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
                enableFitureDao.save(ef);
            }
            PrediksiTest prediksi = prediksiTestDao.findByStatusAndTagihan(StatusRecord.BELUM_LUNAS, tagihan);
            if (prediksi != null) {
                prediksi.setStatus(StatusRecord.AKTIF);
                prediksiTestDao.save(prediksi);
            }
        }

        if (TAGIHAN_IELTS.equals(tagihan.getNilaiJenisTagihan().getJenisTagihan().getKode())) {
            EnableFiture ef = enableFitureDao.findByMahasiswaAndFiturAndEnable(tagihan.getMahasiswa(), StatusRecord.IELTS, false);
            if (ef != null) {
                ef.setEnable(true);
                ef.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
                enableFitureDao.save(ef);
            }
            PrediksiTest prediksi = prediksiTestDao.findByStatusAndTagihan(StatusRecord.BELUM_LUNAS, tagihan);
            if (prediksi != null) {
                prediksi.setStatus(StatusRecord.AKTIF);
                prediksiTestDao.save(prediksi);
            }
        }

        return "redirect:../billAdmin/list?tahunAkademik="+tagihan.getTahunAkademik().getId()+"&nim="+tagihan.getMahasiswa().getNim();
    }

    @GetMapping("/panduanPembayaran")
    public void panduanPembayaran(){


//        response.setContentType("application/pdf");
//        response.setHeader("Content-Disposition", "attachment; filename=Panduan_Pembayaran.pdf");
//        FileCopyUtils.copy(panduanPembayaran.getInputStream(), response.getOutputStream());
//        response.getOutputStream().flush();
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
            report.convert(ctx, options, out);
            out.flush();
        } catch (Exception err){
            log.error(err.getMessage(), err);
        }
    }

    @GetMapping("/studentBill/pembayaran/list")
    public void pembayaraList(Model model, @PageableDefault(size = 10) Pageable page, @RequestParam(required = false) String mulaiTanggal, @RequestParam(required = false) String sampaiTanggal){

        if (mulaiTanggal != null) {
            model.addAttribute("mulai", mulaiTanggal);
            model.addAttribute("sampai", sampaiTanggal);
            model.addAttribute("pembayaran", pembayaranDao.listPembayaran(mulaiTanggal, sampaiTanggal, page));
        }
    }

    @GetMapping("/rekap/pembayaran")
    public void rekapPembayaran(@RequestParam String mulai, @RequestParam String sampai, HttpServletResponse response) throws IOException{
        String[] columns = {"No", "Nim", "Nama", "Angaktan", "Program", "Prodi", "Jenis Tagihan", "Tahun Akademik", "Bank", "Jumlah", "Tanggal Transfer", "Referensi"};

        List<PembayaranDto> listPembayaran = pembayaranDao.downloadPembayaran(mulai, sampai);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Rekap Pembayaran");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for(int i = 0; i < columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (PembayaranDto p : listPembayaran){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(p.getNim());
            row.createCell(2).setCellValue(p.getNama());
            row.createCell(3).setCellValue(p.getAngkatan());
            row.createCell(4).setCellValue(p.getProgram());
            row.createCell(5).setCellValue(p.getProdi());
            row.createCell(6).setCellValue(p.getJenisTagihan());
            row.createCell(7).setCellValue(p.getTahunAkademik());
            row.createCell(8).setCellValue(p.getBank());
            row.createCell(9).setCellValue(p.getJumlah().intValue());
            row.createCell(10).setCellValue(p.getTanggalTransaksi().format(DateTimeFormatter.ISO_LOCAL_DATE));
            row.createCell(11).setCellValue(p.getReferensi());
        }

        for (int i = 0; i < columns.length; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Rekap_Pembayaran_"+mulai+"_"+sampai+".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    // report

    @GetMapping("/billReport/prodi")
    public void reportTagihanProdi(@RequestParam String tahun, HttpServletResponse response) throws IOException {
        String[] columns = {"No", "Nama Prodi", "Total Tagihan", "Dibayar", "Sisa", "Persentasi Sisa"};

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();
        List<DaftarTagihanPerProdiDto> listProdi = tagihanDao.listTagihanPerProdi(tahunAkademik);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Tagihan Prodi");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for(int i = 0; i < columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for(DaftarTagihanPerProdiDto t : listProdi){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(t.getProdi());
            row.createCell(2).setCellValue(t.getTagihan().toString());
            row.createCell(3).setCellValue(t.getDibayar().toString());
            row.createCell(4).setCellValue(t.getSisa().toString());
            row.createCell(5).setCellValue(t.getPercentage().toString()+" %");
        }

        for (int i = 0; i < columns.length; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Rekap_Tagihan_Prodi_"+LocalDate.now()+".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @GetMapping("/billReport/permahasiswa/prodi")
    public void reportPerMahasiswaProdi(@RequestParam String prodi, @RequestParam String tahun, HttpServletResponse response) throws IOException{
        String[] columns = {"No", "Angkatan", "Nim", "Nama", "Tagihan", "Dibayar", "Sisa", "Status Mahasiswa", "Program Mahasiswa", "Beasiswa"};

        Prodi p = prodiDao.findById(prodi).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();
        List<Object[]> listTagihan = tagihanDao.listTagihanPerMahasiswaByProdi(prodi, tahun);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Tagihan per mahasiswa by prodi");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (Object[] list : listTagihan){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list[2].toString());
            row.createCell(2).setCellValue(list[3].toString());
            row.createCell(3).setCellValue(list[4].toString());
            row.createCell(4).setCellValue(list[5].toString());
            row.createCell(5).setCellValue(list[6].toString());
            row.createCell(6).setCellValue(list[7].toString());
            row.createCell(7).setCellValue(list[8].toString());
            row.createCell(8).setCellValue(list[9].toString());
            row.createCell(9).setCellValue(list[10].toString());
        }

        for (int i = 0; i < columns.length; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Rekap Tagihan Prodi "+p.getNamaProdi()+" dan Tahun Akademik " + tahunAkademik.getNamaTahunAkademik() + " per tanggal "+LocalDate.now()+".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @GetMapping("/billReport/angkatan")
    public void reportTagihanAngkatan(@RequestParam String tahun, HttpServletResponse response) throws IOException{
        String[] colums = {"No","Angkatan","Total Tagihan", "Dibayar", "Sisa", "Persentase Sisa"};

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();
        List<DaftarTagihanPerAngkatanDto> listAngkatan = tagihanDao.listTagihanPerAngkatan(tahunAkademik);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Tagihan Angkatan");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < colums.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(colums[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (DaftarTagihanPerAngkatanDto ta : listAngkatan){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(ta.getAngkatan());
            row.createCell(2).setCellValue(ta.getTagihan().toString());
            row.createCell(3).setCellValue(ta.getDibayar().toString());
            row.createCell(4).setCellValue(ta.getSisa().toString());
            row.createCell(5).setCellValue(ta.getPercentage().toString()+" %");
        }

        for(int i = 0; i < colums.length; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Rekap_Tagihan_Angkatan_"+LocalDate.now()+".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @GetMapping("/billReport/permahasiswa/angkatan")
    public void reportPerMahasiswaAngkatan(@RequestParam String angkatan, @RequestParam String tahun, HttpServletResponse response) throws IOException{
        String[] columns = {"No", "Prodi", "Nim", "Nama", "Tagihan", "Dibayar", "Sisa", "Status Mahasiswa", "Program Mahasiswa", "Beasiswa"};

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();
        List<Object[]> listTagihan = tagihanDao.listTagihanPerMahasiswaByAngkatan(angkatan, tahun);
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Tagihan per mahasiswa by angkatan");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (Object[] list : listTagihan){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list[2].toString());
            row.createCell(2).setCellValue(list[3].toString());
            row.createCell(3).setCellValue(list[4].toString());
            row.createCell(4).setCellValue(list[5].toString());
            row.createCell(5).setCellValue(list[6].toString());
            row.createCell(6).setCellValue(list[7].toString());
            row.createCell(7).setCellValue(list[8].toString());
            row.createCell(8).setCellValue(list[9].toString());
            row.createCell(9).setCellValue(list[10].toString());
        }

        for (int i = 0; i < columns.length; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Rekap Tagihan Angkatan "+angkatan+" dan Tahun Akademik " + tahunAkademik.getNamaTahunAkademik()+" per tanggal "+ LocalDate.now()+".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    // refund sp

    @GetMapping("/studentBill/refund/list")
    public void listRefund(Model model, @PageableDefault(size = 10) Pageable page, @RequestParam(required = false) String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listRefund", refundSpDao.findByStatusNotInAndNamaBankContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrMahasiswaNamaContainingIgnoreCaseOrderByTimeUpdateDesc(Arrays.asList(StatusRecord.HAPUS), search, search, search, page));
        }else{
            model.addAttribute("listRefund", refundSpDao.findByStatusNotInOrderByTimeUpdateDesc(Arrays.asList(StatusRecord.HAPUS), page));
        }

    }

    @PostMapping("/studentBill/refund/done")
    private String doneRefund(@RequestParam(required = false) String refund, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);

        RefundSp refSp = refundSpDao.findById(refund).get();
        refSp.setStatusPengembalian(StatusRecord.DONE);
        refSp.setUserUpdate(karyawan);
        refundSpDao.save(refSp);

        return "redirect:list";
    }

    private void createEnableFitur(@RequestParam(required = false) TahunAkademik tahun, Tagihan tgh, StatusRecord status, Boolean enabled) {
        EnableFiture enableFiture = new EnableFiture();
        enableFiture.setMahasiswa(tgh.getMahasiswa());
        enableFiture.setTahunAkademik(tahun);
        enableFiture.setFitur(status);
        enableFiture.setEnable(enabled);
        enableFiture.setKeterangan("OTOMATIS");
        enableFiture.setWaktuInput(LocalDateTime.now());

        enableFitureDao.save(enableFiture);
    }


    @Scheduled(cron = "0 00 20 * * *", zone = "Asia/Jakarta")
    public void akumulasiTagihan(){

        List<RequestCicilan> setDashboardMhs = requestCicilanDao.findByStatusAndStatusCicilanAndTanggalJatuhTempo(StatusRecord.AKTIF, StatusCicilan.SEDANG_DITAGIHKAN, LocalDate.now().plusDays(3));
        for (RequestCicilan cariMahasiswa : setDashboardMhs){
            if ("AKTIF".equalsIgnoreCase(cariMahasiswa.getTagihan().getMahasiswa().getStatusAktif()) || "CUTI".equalsIgnoreCase(cariMahasiswa.getTagihan().getMahasiswa().getStatusAktif())){
                Role role = roleDao.findById("mahasiswanunggak").get();
                User usr = cariMahasiswa.getTagihan().getMahasiswa().getUser();
                usr.setRole(role);
                userDao.save(usr);

                log.info("set mhs nunggak : ", cariMahasiswa.getTagihan().getMahasiswa().getNim());
            }else{
                log.info("mahasiswa sudah tidak aktif.");
            }
        }

        List<RequestCicilan> requestCicilan = requestCicilanDao.findByStatusAndStatusCicilanAndTanggalJatuhTempo(StatusRecord.AKTIF, StatusCicilan.SEDANG_DITAGIHKAN, LocalDate.now());
        for (RequestCicilan cariCicilanHariIni : requestCicilan){

            if ("AKTIF".equalsIgnoreCase(cariCicilanHariIni.getTagihan().getMahasiswa().getStatusAktif()) || "CUTI".equalsIgnoreCase(cariCicilanHariIni.getTagihan().getMahasiswa().getStatusAktif())){
                log.info("Proses Akumulasi Tagihan Mahasiswa aktif / cuti");

                cariCicilanHariIni.setStatusCicilan(StatusCicilan.LEWAT_JATUH_TEMPO);
                requestCicilanDao.save(cariCicilanHariIni);

                log.info("Update status cicilan : {}", cariCicilanHariIni);

                Role role = roleDao.findById("mahasiswa").get();
                User usr = cariCicilanHariIni.getTagihan().getMahasiswa().getUser();
                usr.setRole(role);
                userDao.save(usr);

                RequestCicilan cicilanSelanjutnya = requestCicilanDao.cariCicilanSelanjutnya(cariCicilanHariIni.getTagihan());
                if (cicilanSelanjutnya != null){

                    cicilanSelanjutnya.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
                    cicilanSelanjutnya.setNilaiCicilan(cariCicilanHariIni.getNilaiCicilan().add(cicilanSelanjutnya.getNilaiCicilan()));
                    requestCicilanDao.save(cicilanSelanjutnya);

                    tagihanService.hapusTagihan(cariCicilanHariIni.getTagihan());
//                    tagihanService.ubahJadiCicilan(cicilanSelanjutnya);

                }else{
                    log.info("Tidak ada cicilan selanjutnya!");
                    cariCicilanHariIni.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
                    requestCicilanDao.save(cariCicilanHariIni);
//                User userBlock = userDao.findById(cariCicilanHariIni.getTagihan().getMahasiswa().getUser().getId()).get();
//                userBlock.setActive(false);
//                userDao.save(userBlock);
//                log.info("block smile untuk user {}", userBlock);
                }

            }else{
                log.info("Proses Akumulasi Tagihan Mahasiswa Keluar!!!");

                cariCicilanHariIni.setStatus(StatusRecord.NONAKTIF);
                cariCicilanHariIni.setStatusCicilan(StatusCicilan.BATAL_CICIL);
                requestCicilanDao.save(cariCicilanHariIni);

                List<RequestCicilan> cekSisaCicilan = requestCicilanDao.findByTagihanAndStatusAndStatusCicilanNotIn(cariCicilanHariIni.getTagihan(), StatusRecord.AKTIF, Arrays.asList(StatusCicilan.LUNAS, StatusCicilan.LEWAT_JATUH_TEMPO));
                if (cekSisaCicilan != null) {
                    for (RequestCicilan listCicilan : cekSisaCicilan){
                        listCicilan.setStatus(StatusRecord.NONAKTIF);
                        listCicilan.setStatusCicilan(StatusCicilan.BATAL_CICIL);
                        requestCicilanDao.save(listCicilan);
                    }
                }

                Tagihan tagihan = cariCicilanHariIni.getTagihan();
                tagihan.setStatus(StatusRecord.NONAKTIF);
                tagihan.setStatusTagihan(StatusTagihan.NONAKTIF);
                tagihanDao.save(tagihan);
            }

        }

    }

//    @Scheduled(cron = "0 44 11 * * *", zone = "Asia/Jakarta")
    public void kirimUlangNotifikasi(){

        List<RequestCicilan> cicilan = requestCicilanDao.findByStatusAndStatusCicilanAndTanggalJatuhTempo(StatusRecord.AKTIF, StatusCicilan.SEDANG_DITAGIHKAN, LocalDate.now().plusWeeks(1));
        for (RequestCicilan cariCicilan : cicilan){
            log.info("Scheduler untuk {} berhasil", cariCicilan.getTagihan().getKeterangan());

//            kafkaSender.sendNotifikasiTagihan(cariCicilan.getTagihan());
        }

    }

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


    // GENERATE POTONGAN

    @GetMapping("/studentBill/billAdmin/potongan")
    public void formPotongan(Model model, @RequestParam(required = false) Prodi prodi, @RequestParam(required = false) Program program,
                             @RequestParam(required = false) String angkatan){

        model.addAttribute("selectAngkatan", angkatan);
        model.addAttribute("selectProdi", prodi);
        model.addAttribute("selectProgram", program);

    }

    @PostMapping("/studentBill/billAdmin/potongan")
    public String inputPotongan(@RequestParam(required = false) Prodi prodi, @RequestParam(required = false) Program program,
                                @RequestParam(required = false) String angkatan){

        List<Tagihan> listPotongan = tagihanDao.generatePotongan(tahunAkademikDao.findByStatus(StatusRecord.AKTIF), prodi.getId(), program.getId(), angkatan);

        for (Tagihan t : listPotongan){

            Integer potongan;
            if (program.equals("03")){
                potongan = 1000000;
            }else{
                potongan = 500000;
            }

            Integer newNilai = t.getNilaiTagihan().intValue() - potongan;

            t.setNilaiTagihan(new BigDecimal(newNilai));
            t.setKeterangan("Potongan "+t.getKeterangan());
            tagihanDao.save(t);
            tagihanService.editTagihan(t);

        }

        return "redirect:potongan";
    }

    // Nilai Tagihan Beasiswa
    @GetMapping("/studentBill/scholarship/list")
    public void listNilaiBeasiswa(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik, @PageableDefault(size = 10) Pageable page, String search){

        model.addAttribute("selectTahun", tahunAkademik);
        if (tahunAkademik == null) {
            model.addAttribute("tahun", "tahun");
        }else {
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("list", nilaiJenisTagihanDao.findByStatusNotInAndTahunAkademikAndBeasiswaNotNullAndAngkatanContainingIgnoreCaseOrStatusNotInAndTahunAkademikAndBeasiswaNotNullAndProdiNamaProdiContainingIgnoreCaseOrderByAngkatanDesc(Arrays.asList(StatusRecord.HAPUS), tahunAkademik, search, Arrays.asList(StatusRecord.HAPUS), tahunAkademik, search, page));
            }else {
                model.addAttribute("list", nilaiJenisTagihanDao.findByStatusNotInAndTahunAkademikAndBeasiswaNotNullOrderByAngkatanDesc(Arrays.asList(StatusRecord.HAPUS), tahunAkademik, page));
            }
        }
    }

    @GetMapping("studentBill/scholarship/form")
    public void formNilaiBeasiswa(Model model, @RequestParam(required = false) String id){
        model.addAttribute("valueBill", new NilaiJenisTagihan());
        model.addAttribute("jenisTagihan", jenisTagihanDao.findByStatusOrderByKode(StatusRecord.AKTIF));
        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("beasiswa", beasiswaDao.findByStatusOrderByNamaBeasiswa(StatusRecord.AKTIF));
        model.addAttribute("program", programDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("selectAngkatan", angkatan());
        model.addAttribute("kategori", StatusTagihan.values());

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

    @PostMapping("/studentBill/scholarship/new")
    public String newNilaiBeasiswa(@Valid NilaiJenisTagihan nilaiJenisTagihan){
        if (nilaiJenisTagihan.getStatus() == null){
            nilaiJenisTagihan.setStatus(StatusRecord.NONAKTIF);
        }
        nilaiJenisTagihanDao.save(nilaiJenisTagihan);
        return "redirect:list?tahunAkademik="+nilaiJenisTagihan.getTahunAkademik().getId();
    }

    @PostMapping("/studentBill/scholarship/delete")
    public String deleteNilaiBeasiswa(@RequestParam NilaiJenisTagihan nilaiJenisTagihan){
        nilaiJenisTagihan.setStatus(StatusRecord.HAPUS);
        nilaiJenisTagihanDao.save(nilaiJenisTagihan);
        return "redirect:list";
    }

    @GetMapping("/studentBill/scholarship/generate")
    public void generateTagihanBeasiswa(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) Program program,
                                        @RequestParam(required = false) Beasiswa beasiswa, @RequestParam(required = false) String angkatan){

        model.addAttribute("selectAngkatan", angkatan);
        model.addAttribute("selectTahun", tahunAkademik);
        model.addAttribute("selectBeasiswa", beasiswa);
        model.addAttribute("selectProgram", program);

    }

    @PostMapping("/studentBill/scholarship/generate")
    public String generateBeasiswa(@RequestParam(required = false) Beasiswa beasiswa, @RequestParam(required = false) Program program,
                                   @RequestParam(required = false) String angkatan, @RequestParam(required = false) TahunAkademik tahun){

        List<Mahasiswa> listMahasiswa = mahasiswaDao.findByBeasiswaAndStatusAktifAndStatusAndAngkatanAndIdProgram(beasiswa, "AKTIF", StatusRecord.AKTIF, angkatan, program);
        log.info("generate tagihan beasiswa");

        tagihanService.generateTagihanBeasiswa(tahun, program, listMahasiswa);

        return "redirect:generate";

    }

    @GetMapping("/studentBill/billAdmin/tagihanAsli")
    public void listTagihanAsli(Model model, @RequestParam(required = false) String[] prodis, @RequestParam(required = false) String[] angkatan, @RequestParam(required = false) TahunAkademik tahun){

        if (prodis != null && angkatan != null && tahun != null) {
            List<Prodi> prodi = new ArrayList<>();
            List<String> nProdi = new ArrayList<>();
            for (String p : prodis){
                Prodi pr = prodiDao.findById(p).get();
                prodi.add(pr);
                nProdi.add(pr.getNamaProdi());
            }
            List<String> ang = new ArrayList<>();
            for (String a : angkatan){
                ang.add(a);
            }

            // buat download excel
            model.addAttribute("prodis", prodis);
            model.addAttribute("angkatans", angkatan);

            model.addAttribute("selectProdi", prodi);
            model.addAttribute("nProdi", nProdi.toString().replace("[", "").replace("]", ""));
            model.addAttribute("selectAngkatan", ang.toString().replace("[", "").replace("]", ""));
            model.addAttribute("selectTahun", tahun);
            model.addAttribute("listMhs", tagihanDao.findByStatusAndMahasiswaIdProdiInAndMahasiswaAngkatanInAndTahunAkademikOrderByMahasiswaNim(StatusRecord.AKTIF, prodi, ang, tahun));
        }
    }

    @GetMapping("/studentBill/billAdmin/tagihanAsli/download")
    public void downloadTagihanAsli(@RequestParam String[] prodis, @RequestParam String[] angkatan, @RequestParam TahunAkademik tahun, HttpServletResponse response) throws IOException {

        String[] columns = {"NO", "Angkatan", "NIM", "Nama", "Prodi", "Program", "Tagihan", "Jumlah", "Beasiswa", "Status Tagihan"};

        List<Prodi> prodi = new ArrayList<>();
        for (String p : prodis){
            Prodi pr = prodiDao.findById(p).get();
            prodi.add(pr);
        }
        List<String> ang = new ArrayList<>();
        for (String a : angkatan){
            ang.add(a);
        }

        List<Tagihan> tagihanMhs = tagihanDao.findByStatusAndMahasiswaIdProdiInAndMahasiswaAngkatanInAndTahunAkademikOrderByMahasiswaNim(StatusRecord.AKTIF, prodi, ang, tahun);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Tagihan Asli");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Font ketFont = workbook.createFont();
        ketFont.setBold(true);
        ketFont.setFontHeightInPoints((short) 14);
        ketFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle ketCellStyle = workbook.createCellStyle();
        ketCellStyle.setFont(ketFont);
        ketCellStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i<columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (Tagihan t : tagihanMhs){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(t.getMahasiswa().getAngkatan());
            row.createCell(2).setCellValue(t.getMahasiswa().getNim());
            row.createCell(3).setCellValue(t.getMahasiswa().getNama());
            row.createCell(4).setCellValue(t.getMahasiswa().getIdProdi().getNamaProdi());
            row.createCell(5).setCellValue(t.getMahasiswa().getIdProgram().getNamaProgram());
            row.createCell(6).setCellValue(t.getNilaiJenisTagihan().getJenisTagihan().getNama());
            row.createCell(7).setCellValue(t.getNilaiTagihanAsli().toString());
            if (t.getMahasiswa().getBeasiswa() != null) {
                row.createCell(8).setCellValue(t.getMahasiswa().getBeasiswa().getNamaBeasiswa());
            }else{
                row.createCell(8).setCellValue("-");
            }
            row.createCell(9).setCellValue(t.getStatusTagihan().toString());
        }

        for (int i = 0; i<columns.length;i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vns.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Data Tagihan Asli " + tahun.getNamaTahunAkademik() + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @GetMapping("/studentBill/tlc/list")
    public void rekapTagihanTest(Model model, @RequestParam(required = false) PeriodeTest periodeTest, @PageableDefault(size = 10) Pageable page){
        model.addAttribute("listPeriode", periodeTestDao.findAllByOrderByTanggalUjianDesc());
        if (periodeTest != null) {
            model.addAttribute("selectPeriode", periodeTest);
            model.addAttribute("listTagihan", prediksiTestDao.findByPeriodeTest(periodeTest, page));
        }
    }

    @GetMapping("/prediksiTest/rekapTagihan/download")
    public void downloadRekap(@RequestParam PeriodeTest periode, HttpServletResponse response) throws IOException {
        String[] columns = {"NO", "NIM", "Name", "Generation", "Exam to", "Test Type", "Value", "Status"};

        List<PrediksiTest> listMhs = prediksiTestDao.findByPeriodeTest(periode);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Prediksi Test " + periode.getJenisTest() + " Periode " + periode.getTanggalUjian());
        sheet.addMergedRegion(CellRangeAddress.valueOf("A1:F1"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A2:F2"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A3:F3"));

        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setUnderline(HSSFFont.U_SINGLE);
        titleFont.setFontHeightInPoints((short) 12);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);

        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);

        int rowTitle1 = 0;
        Row titleRow = sheet.createRow(rowTitle1);
        titleRow.createCell(0).setCellValue(periode.getJenisTest() + " PREDICTION TEST");
        titleRow.getCell(0).setCellStyle(titleStyle);

        int rowTitle2 = 1;
        Row titleRow2 = sheet.createRow(rowTitle2);
        titleRow2.createCell(0).setCellValue("TAZKIA LANGUAGE CENTER");
        titleRow2.getCell(0).setCellStyle(titleStyle);

        int rowTitle3 = 2;
        Row titleRow3 = sheet.createRow(rowTitle3);
        titleRow3.createCell(0).setCellValue("Sentul, " + periode.getTanggalUjian().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        titleRow3.getCell(0).setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(4);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 5;
        int baris = 1;

        for (PrediksiTest list : listMhs){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list.getMahasiswa().getNim());
            row.createCell(2).setCellValue(list.getMahasiswa().getNama());
            row.createCell(3).setCellValue(list.getMahasiswa().getAngkatan());
            row.createCell(4).setCellValue(list.getUjianKe());
            row.createCell(5).setCellValue(list.getJenisTest().toString());
            if (list.getTagihan() != null) {
                row.createCell(6).setCellValue(list.getTagihan().getNilaiTagihan().toString());
                row.createCell(7).setCellValue(list.getTagihan().getStatusTagihan().toString());
            }else{
                row.createCell(6).setCellValue("-");
                row.createCell(7).setCellValue("-");
            }

            row.getCell(0).setCellStyle(dataStyle);
            row.getCell(1).setCellStyle(dataStyle);
            row.getCell(2).setCellStyle(dataStyle);
            row.getCell(3).setCellStyle(dataStyle);
            row.getCell(4).setCellStyle(dataStyle);
            row.getCell(5).setCellStyle(dataStyle);
            row.getCell(6).setCellStyle(dataStyle);
            row.getCell(7).setCellStyle(dataStyle);
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Data Rekap Tagihan Prediksi Test " + periode.getJenisTest().toString() + " periode " + periode.getTanggalUjian() + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

}