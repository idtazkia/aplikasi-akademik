package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.apache.kafka.common.metrics.Stat;
import org.bouncycastle.ocsp.Req;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private PembayaranDao pembayaranDao;

    @Autowired
    private RequestPenangguhanDao requestPenangguhanDao;

    @Autowired
    private RequestCicilanDao requestCicilanDao;



    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findAll();
    }

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademik> tahunAkademik() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

//    Bank

    @GetMapping("/studentBill/bank")
    public String listBank(Model model, @PageableDefault(size = 10)Pageable page, String search){
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listBank", bankDao.findByStatusNotInAndNamaContainingIgnoreCaseOrderByNama(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else{
            model.addAttribute("listBank", bankDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }

        return "/studentBill/bank/list";
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
        return "redirect:../bank ";
    }

    @PostMapping("/studentBill/bank/delete")
    public String deleteBank(@RequestParam Bank bank){
        bank.setStatus(StatusRecord.HAPUS);
        bankDao.save(bank);
        return "redirect:../bank";
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
        return "redirect:../typeBill";
    }

    @PostMapping("studentBill/typeBill/delete")
    public String deleteType(@RequestParam JenisTagihan jenisTagihan,
                             RedirectAttributes attributes){

        Integer njt = nilaiJenisTagihanDao.countByStatusAndJenisTagihan(StatusRecord.AKTIF, jenisTagihan);

        if (njt > 0) {
            attributes.addFlashAttribute("gagal");
            return "redirect:../typeBill";
        }

        jenisTagihan.setStatus(StatusRecord.HAPUS);
        jenisTagihanDao.save(jenisTagihan);
        return "redirect:../typeBill";
    }

//    Nilai jenis tagihan

    @GetMapping("/studentBill/valueType")
    public String listNilai(Model model, @PageableDefault(size = 10) Pageable page, String search ){
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listValue", nilaiJenisTagihanDao.findByStatusNotInAndJenisTagihanContainingIgnoreCaseOrderByJenisTagihan(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else{
            model.addAttribute("listValue", nilaiJenisTagihanDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }

        return "/studentBill/valueType/list";
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
        return "redirect:../valueType";
    }

    @PostMapping("/studentBill/valueType/delete")
    public String deleteNilai(@RequestParam NilaiJenisTagihan nilaiJenisTagihan){
        nilaiJenisTagihan.setStatus(StatusRecord.HAPUS);
        nilaiJenisTagihanDao.save(nilaiJenisTagihan);
        return "redirect:../valueType";
    }

//    Tagihan

    @GetMapping("/studentBill/bill")
    public String listBill(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listBill", tagihanDao.findByStatusNotInAndMahasiswaContainingIgnoreCaseOrderByMahasiswa(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else{
            model.addAttribute("listBill", tagihanDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
        return "/studentBill/bill/list";
    }

    @GetMapping("/studentBill/billAdmin")
    public String listBillAdmin(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listBill", tagihanDao.findByStatusNotInAndMahasiswaContainingIgnoreCaseOrderByMahasiswa(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else{
            model.addAttribute("listBill", tagihanDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
        return "/studentBill/billAdmin/list";
    }

    @GetMapping("/studentBill/billAdmin/date")
    public String formDate(Model model, @RequestParam(required = false) String id){

        model.addAttribute("tagihan", tagihanDao.findById(id).get());

        return "/studentBill/billAdmin/date";
    }

    @GetMapping("/studentBill/billAdmin/generate")
    public void main(Model model, @PageableDefault(size = 10) Pageable page,
                    @RequestParam(required = false) TahunAkademik tahunAkademik,
                    @RequestParam(required = false) Prodi prodi){

        model.addAttribute("selectTahun", tahunAkademik);
        model.addAttribute("selectProdi", prodi);


    }

//    @GetMapping("/studentBill/bill/form")
//    public void formBill(Model model, @RequestParam(required = false) String id,
//                         @RequestParam(required = false) TahunAkademik tahunAkademik,
//                         @RequestParam(required = false) Prodi prodi){
//
//        model.addAttribute("selectTahun", tahunAkademik);
//        model.addAttribute("selectProdi", prodi);
//
//        if (tahunAkademik != null && prodi != null){
//            model.addAttribute("bill", new Tagihan());
//            model.addAttribute("krs", krsDao.findByTahunAkademikAndProdiAndStatus(tahunAkademik, prodi, StatusRecord.AKTIF));
//            model.addAttribute("valueBill", nilaiJenisTagihanDao.findByStatusOrderByJenisTagihan(StatusRecord.AKTIF));
//        }
//
//        if (id != null && !id.isEmpty()){
//            Tagihan tagihan = tagihanDao.findById(id).get();
//            if (tagihan != null) {
//                model.addAttribute("bill", tagihan);
//                if (tagihan.getStatus() == null) {
//                    tagihan.setStatus(StatusRecord.NONAKTIF);
//                }
//            }
//        }
//    }

    @PostMapping("/studentBill/billAdmin/generate")
    public String generateTagihan(@RequestParam(required = false) TahunAkademik tahun,
                                  @RequestParam(required = false) Prodi prodi){

        List<Mahasiswa> mahasiswas = mahasiswaDao.findByIdProdiAndStatus(prodi, StatusRecord.AKTIF);
        for (Mahasiswa mhs : mahasiswas){
            List<NilaiJenisTagihan> nilaiJenisTagihans = nilaiJenisTagihanDao.findByAngkatanAndProdiAndStatus(mhs.getAngkatan(), mhs.getIdProdi(), StatusRecord.AKTIF);
            for (NilaiJenisTagihan njt : nilaiJenisTagihans){
                Tagihan tg = tagihanDao.findByMahasiswaAndNilaiJenisTagihanAndStatus(mhs, njt, StatusRecord.AKTIF);
                if (tg == null) {
                    Tagihan tagihan = new Tagihan();
                    tagihan.setMahasiswa(mhs);
                    tagihan.setNilaiJenisTagihan(njt);
                    tagihan.setKeterangan("-");
                    tagihan.setNilaiTagihan(njt.getNilai());
                    tagihan.setTanggalPembuatan(LocalDate.now());
                    tagihan.setTanggalJatuhTempo(LocalDate.now());
                    tagihan.setTanggalPenangguhan(LocalDate.now());
                    tagihan.setTahunAkademik(tahun);
                    tagihan.setStatus(StatusRecord.AKTIF);
                    tagihanDao.save(tagihan);
                }
            }
        }
        return "redirect:";
    }

    @PostMapping("/studentBill/bill/new")
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
        return "redirect:../bill";
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

        return "redirect:../billAdmin";
    }

    @PostMapping("/studentBill/bill/delete")
    public String deleteBill(@RequestParam Tagihan tagihan){
        tagihan.setStatus(StatusRecord.HAPUS);
        tagihanDao.save(tagihan);
        return "redirect:../bill";
    }

//    Request Penangguhan

    @GetMapping("/studentBill/penangguhan")
    public String listPenangguhan(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listPenangguhan", requestPenangguhanDao.findByStatusNotInAndTanggalPenangguhanContainingIgnoreCaseOrderByTagihan(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else {
            model.addAttribute("listPenangguhan", requestPenangguhanDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
        return "/studentBill/requestPenangguhan/list";
    }

    @GetMapping("/studentBill/bill/date")
    public String holdDate(Model model, @RequestParam(required = false) String id){
        Tagihan tagihan = tagihanDao.findById(id).get();
        model.addAttribute("penangguhan", new RequestPenangguhan());
        model.addAttribute("bill", tagihan);

        return "/studentBill/requestPenangguhan/date";
    }

    @GetMapping("/studentBill/penangguhan/approval")
    public String approval(Model model, @RequestParam(required = false) String id,
                           @RequestParam(required = false) Tagihan tagihan){

        RequestPenangguhan requestPenangguhan = requestPenangguhanDao.findById(id).get();
        model.addAttribute("penangguhan", requestPenangguhan);
        model.addAttribute("bill", tagihan);

        return "/studentBill/requestPenangguhan/approval";
    }

    @PostMapping("/studentBill/bill/newDate")
    public String newDate(@Valid RequestPenangguhan requestPenangguhan,
                          @RequestParam(required = false) Tagihan tagihan){
        requestPenangguhan.setTagihan(tagihan);
        requestPenangguhan.setStatus(StatusRecord.AKTIF);
        requestPenangguhan.setStatusApprove(StatusApprove.WAITING);
        requestPenangguhanDao.save(requestPenangguhan);
        return "redirect:../bill";
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

    @GetMapping("/studentBill/cicilan")
    public String listCicilan(Model model, @PageableDefault(size = 10) Pageable page, String search){
         if (StringUtils.hasText(search)){
             model.addAttribute("search", search);
             model.addAttribute("listCicilan", requestCicilanDao.findByStatusNotInAndBanyakCicilanContainingIgnoreCase(Arrays.asList(StatusRecord.HAPUS), search, page));
         }else{
             model.addAttribute("listCicilan", requestCicilanDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
         }
        return "/studentBill/requestCicilan/list";
    }

    @GetMapping("/studentBill/bill/instalment")
    public String cicilanTagihan(Model model, @RequestParam(required = false) String id){
        Tagihan tagihan = tagihanDao.findById(id).get();
        model.addAttribute("cicilan", new RequestCicilan());
        model.addAttribute("bill", tagihan);

        return "/studentBill/requestCicilan/angsuran";
    }

    @PostMapping("/studentBill/bill/cicilan")
    public String newCicilan(@Valid RequestCicilan requestCicilan,
                             @RequestParam(required = false) Tagihan tagihan){
        requestCicilan.setTagihan(tagihan);
        requestCicilan.setStatus(StatusRecord.AKTIF);
        requestCicilan.setStatusApprove(StatusApprove.WAITING);
        requestCicilanDao.save(requestCicilan);
        return "redirect:../bill";
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

    @GetMapping("/studentBill/payment")
    public String formPayment(Model model, @RequestParam(required = false) String tagihan){
        Tagihan tagihan1 = tagihanDao.findById(tagihan).get();

        model.addAttribute("pembayaran", pembayaranDao.findByTagihan(tagihan1));
        model.addAttribute("bank", bankDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("tagihan", tagihan1);

        return "/studentBill/payment/form";
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
}
