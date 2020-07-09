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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Controller
public class StudentBillController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentBillController.class);

    @Autowired
    private BankDao bankDao;

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

    @Value("classpath:kwitansi.odt")
    private Resource templateKwitansi;

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findAll();
    }

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

//    jenis tagihan

    @GetMapping("/studentBill/typeBill/list")
    public void listType(Model model, @PageableDefault(size = 10)Pageable page, String search){
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
            model.addAttribute("listValue", nilaiJenisTagihanDao.findByStatusNotInAndJenisTagihanContainingIgnoreCaseOrderByJenisTagihan(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else{
            model.addAttribute("listValue", nilaiJenisTagihanDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/studentBill/valueType/form")
    public void formNilai(Model model, @RequestParam(required = false) String id){
        model.addAttribute("valueBill", new NilaiJenisTagihan());
        model.addAttribute("jenisTagihan", jenisTagihanDao.findByStatusOrderByNama(StatusRecord.AKTIF));
        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("prodi", prodiDao.findByStatusOrderByNamaProdi(StatusRecord.AKTIF));

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

//          model.addAttribute("listBill", tagihanDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mhs = mahasiswaDao.findByUser(user);

        model.addAttribute("biayaMahasiswa", tagihanDao.biayaMahasiswa(mhs.getId()));
        model.addAttribute("pembayaran", pembayaranDao.pembayaranMahasiswa(mhs.getId()));
        model.addAttribute("totalTagihan", tagihanDao.totalTagihanMahasiswa(mhs.getId()));
        model.addAttribute("totalDibayar", pembayaranDao.totalDibayarMahasiswa(mhs.getId()));

    }

    @GetMapping("/studentBill/billAdmin/list")
    public void listBillAdmin(Model model, @PageableDefault(size = 10) Pageable page,
                              @RequestParam(required = false) TahunAkademik tahunAkademik,
                              @RequestParam(required = false) String nim){

        model.addAttribute("selectTahun", tahunAkademik);

        if (tahunAkademik != null) {
            Mahasiswa mhs = mahasiswaDao.findByNim(nim);
            model.addAttribute("sisaTagihan", tagihanDao.sisaTagihanQuery(tahunAkademik.getId(),mhs.getId()));
            model.addAttribute("daftarBiaya", tagihanDao.daftarBiaya(tahunAkademik.getId(), mhs.getId()));
            model.addAttribute("daftarPembayaran", pembayaranDao.daftarPembayaran(tahunAkademik.getId(), mhs.getId()));
            model.addAttribute("status", krsDao.findByTahunAkademikAndMahasiswaAndStatus(tahunAkademik, mhs, StatusRecord.AKTIF));
            model.addAttribute("mahasiswa", mhs);
            model.addAttribute("jumlahSks", krsDetailDao.jumlahSksMahasiswa(mhs.getId(), tahunAkademik.getId()));
            model.addAttribute("totalTagihan", tagihanDao.totalTagihan(tahunAkademik.getId(), mhs.getId()));
            model.addAttribute("totalDibayar", pembayaranDao.totalDibayar(tahunAkademik.getId(), mhs.getId()));
        }

    }

    @GetMapping("/studentBill/billAdmin/date")
    public void formDate(Model model, @RequestParam(required = false) String id){

        model.addAttribute("tagihan", tagihanDao.findById(id).get());


    }

    @GetMapping("/studentBill/billAdmin/generate")
    public void main(Model model, @PageableDefault(size = 10) Pageable page,
                    @RequestParam(required = false) TahunAkademik tahunAkademik,
                    @RequestParam(required = false) Prodi prodi){

        model.addAttribute("selectTahun", tahunAkademik);
        model.addAttribute("selectProdi", prodi);

    }

//    @GetMapping("/studentBill/billAdmin/form")
//    public void formBill(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik,
//                         @RequestParam(required = false) Prodi prodi){
//
//        model.addAttribute("newTagihan", new Tagihan());
//        List<NilaiJenisTagihan> njt = nilaiJenisTagihanDao.findByTahunAkademikAndProdi(tahunAkademik, prodi);
//        model.addAttribute("listNjt", njt);
//        model.addAttribute("mhs", mahasiswaDao.findByAngkatanAndIdProdiAndStatus( ));
//
//    }

    @PostMapping("/studentBill/billAdmin/generate")
    public String generateTagihan(@RequestParam(required = false) TahunAkademik tahun,
                                  @RequestParam(required = false) Prodi prodi){

        List<Mahasiswa> mahasiswas = mahasiswaDao.findByIdProdiAndStatus(prodi, StatusRecord.AKTIF);
        for (Mahasiswa mhs : mahasiswas){
            List<NilaiJenisTagihan> nilaiJenisTagihans = nilaiJenisTagihanDao.findByTahunAkademikAndAngkatanAndProdiAndStatus(tahun, mhs.getAngkatan(), mhs.getIdProdi(), StatusRecord.AKTIF);
            for (NilaiJenisTagihan njt : nilaiJenisTagihans){
                Tagihan tg = tagihanDao.findByMahasiswaAndNilaiJenisTagihanAndStatus(mhs, njt, StatusRecord.AKTIF);
                if (tg == null) {
                    Tagihan tagihan = new Tagihan();
                    tagihan.setMahasiswa(mhs);
                    tagihan.setNilaiJenisTagihan(njt);
                    tagihan.setKeterangan("-");
                    tagihan.setNilaiTagihan(njt.getNilai());
                    tagihan.setTanggalPembuatan(LocalDate.now());
                    tagihan.setTanggalJatuhTempo(njt.getTanggalJatuhTempo());
                    tagihan.setTanggalPenangguhan(njt.getTanggalJatuhTempo());
                    tagihan.setTahunAkademik(tahun);
                    tagihan.setStatus(StatusRecord.AKTIF);
                    tagihanDao.save(tagihan);
                }else {
                    Tagihan tagihan = new Tagihan();
                    tagihan.setMahasiswa(mhs);
                    tagihan.setNilaiJenisTagihan(njt);
                    tagihan.setKeterangan("Ditambah tagihan tahun sebelumnya.");
                    tagihan.setNilaiTagihan(tg.getNilaiTagihan().add(njt.getNilai()));
                    tagihan.setTanggalPembuatan(LocalDate.now());
                    tagihan.setTanggalJatuhTempo(njt.getTanggalJatuhTempo());
                    tagihan.setTanggalPenangguhan(njt.getTanggalJatuhTempo());
                    tagihan.setTahunAkademik(tahun);
                    tagihan.setStatus(StatusRecord.AKTIF);
                    tagihan.setIdTagihanSebelumnya(tg.getId());
                    tagihanDao.save(tagihan);

                    tg.setStatus(StatusRecord.NONAKTIF);
                    tagihanDao.save(tg);
                }
            }
        }
        return "redirect:list";
    }

    @PostMapping("/studentBill/billAdmin/new")
    public String newBill(@Valid Tagihan tagihan,
                          @RequestParam(required = false) TahunAkademik tahun){
        tagihan.setTahunAkademik(tahun);
        LocalDate date = tagihan.getTanggalJatuhTempo();
        tagihan.setTanggalPembuatan(date);
        tagihan.setTanggalPenangguhan(date);
        if (tagihan.getStatus() == null){
            tagihan.setStatus(StatusRecord.NONAKTIF);
        }
        tagihanDao.save(tagihan);
        return "redirect:list";
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

    @PostMapping("/studentBill/bill/delete")
    public String deleteBill(@RequestParam Tagihan tagihan){
        tagihan.setStatus(StatusRecord.HAPUS);
        tagihanDao.save(tagihan);
        return "redirect:list";
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
        requestPenangguhan.setStatus(StatusRecord.AKTIF);
        requestPenangguhan.setStatusApprove(StatusApprove.WAITING);
        requestPenangguhanDao.save(requestPenangguhan);
        return "redirect:list";
    }

    @PostMapping("/studentBill/penangguhan/approve")
    public String approvePenangguhan(@RequestParam RequestPenangguhan requestPenangguhan,
                                     @RequestParam(required = false) Tagihan tagihan){

        requestPenangguhan.setStatusApprove(StatusApprove.APPROVED);
        requestPenangguhan.setStatus(StatusRecord.AKTIF);
        requestPenangguhanDao.save(requestPenangguhan);

        tagihan.setTanggalPenangguhan(requestPenangguhan.getTanggalPenangguhan());
        tagihanDao.save(tagihan);
        return "redirect:../penangguhan";
    }

    @PostMapping("/studentBill/penangguhan/reject")
    public String rejectPenangguhan(@RequestParam RequestPenangguhan requestPenangguhan){
        requestPenangguhan.setStatusApprove(StatusApprove.REJECTED);
        requestPenangguhan.setStatus(StatusRecord.AKTIF);
        requestPenangguhanDao.save(requestPenangguhan);
        return "redirect:../penangguhan";
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

    @PostMapping("/studentBill/bill/cicilan")
    public String newCicilan(@Valid RequestCicilan requestCicilan,
                             @RequestParam(required = false) Tagihan tagihan){
        requestCicilan.setTagihan(tagihan);
        requestCicilan.setStatus(StatusRecord.AKTIF);
        requestCicilan.setStatusApprove(StatusApprove.WAITING);
        requestCicilanDao.save(requestCicilan);
        return "redirect:list";
    }

    @PostMapping("/studentBill/cicilan/approve")
    public String approvalCicilan(@RequestParam RequestCicilan requestCicilan,
                                  @RequestParam(required = false) Tagihan tagihan){



        requestCicilan.setStatus(StatusRecord.AKTIF);
        requestCicilan.setStatusApprove(StatusApprove.APPROVED);
        requestCicilanDao.save(requestCicilan);

        tagihan.setStatus(StatusRecord.NONAKTIF);
        tagihanDao.save(tagihan);
        return "redirect:";
    }

    @PostMapping("/studentBill/cicilan/reject")
    public String rejectCicilan(@RequestParam RequestCicilan requestCicilan){
        requestCicilan.setStatusApprove(StatusApprove.REJECTED);
        requestCicilan.setStatus(StatusRecord.AKTIF);
        requestCicilanDao.save(requestCicilan);
        return "redirect:";
    }

//    Pembayaran

    @GetMapping("/studentBill/payment/form")
    public void formPayment(Model model, @RequestParam(required = false) String tagihan){
        Tagihan tagihan1 = tagihanDao.findById(tagihan).get();

        model.addAttribute("pembayaran", pembayaranDao.findByTagihan(tagihan1));
        model.addAttribute("bank", bankDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("tagihan", tagihan1);

    }

    @GetMapping("/panduanPembayaran")
    public void getPanduanPembayaran(HttpServletResponse response) throws Exception{
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Panduan_Pembayaran.pdf");
        FileCopyUtils.copy(panduanPembayaran.getInputStream(), response.getOutputStream());
        response.getOutputStream().flush();
    }

    @PostMapping("/studentBill/payment/pay")
    public String savePayment(@Valid Pembayaran pembayaran,
                              @RequestParam(required = false) Tagihan tagihan){
        pembayaran.setTagihan(tagihan);
        pembayaranDao.save(pembayaran);

        tagihan.setStatus(StatusRecord.NONAKTIF);
        tagihanDao.save(tagihan);
        return "redirect:../bill";
    }

//    Report

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
            if (pembayaran.getTagihan().getNomor() == null){
                ctx.put("noTagihan", "-");
            }else{
                ctx.put("noTagihan", pembayaran.getTagihan().getNomor());
            }
            ctx.put("bank", pembayaran.getBank().getNama());
            ctx.put("jenisBiaya", pembayaran.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getNama());
            ctx.put("nilai", pembayaran.getAmount());
            ctx.put("tanggal", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

            response.setHeader("Content-Disposition", "attachment;filename=bukti_pembayaran_"+ pembayaran.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getNama() +".pdf");
            OutputStream out = response.getOutputStream();
            report.convert(ctx, options, out);
            out.flush();
        } catch (Exception err){
            LOGGER.error(err.getMessage(), err);
        }
    }


}
