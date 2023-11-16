package id.ac.tazkia.smilemahasiswa.controller;

import groovy.lang.GString;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.KewajibanPerpus;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.KewajibanPerpusMahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.PendaftaranBebasKewajiban;
import id.ac.tazkia.smilemahasiswa.service.BebasKewajibanService;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import jdk.jshell.Snippet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Controller @Slf4j
public class BebasKewajibanController {

    public static final List<String> USER_PERPUS = Arrays.asList("perpustakaan", "kabagperpus");
    public static final List<String> USER_FINANCE = Arrays.asList("finance", "kabagfinance");
    public static final List<String> USER_TLC = Arrays.asList("tlc");
    public static final List<String> USER_AKADEMIK = Arrays.asList("akademik1", "akademik2", "kabagakademik", "superuser");

    @Autowired
    BebasKewajibanDao bebasKewajibanDao;

    @Autowired
    MahasiswaDao mahasiswaDao;

    @Autowired
    KewajibanPerpusDao kewajibanPerpusDao;

    @Autowired
    KewajibanPerpusMahasiswaDao kewajibanPerpusMahasiswaDao;

    @Autowired
    TagihanDao tagihanDao;

    @Autowired
    PembayaranDao pembayaranDao;

    @Autowired
    JenjangDao jenjangDao;

    @Autowired
    CurrentUserService currentUserService;

    @Autowired
    BebasKewajibanService bebasKewajibanService;

    // halaman mahasiswa

    @GetMapping("/kewajiban/daftar")
    public String daftarBebasKewajiban(Model model, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        if ("LULUS".equals(mahasiswa.getStatusAktif())) {
            PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);
            if (bebasKewajiban != null) {
                return "redirect:/kewajiban/mahasiswa/detail";
            }else{
                bebasKewajibanService.saveDaftarKewajiban(mahasiswa);
                return "/kewajiban/daftar";
            }
        }else{
            return "/kewajiban/gagal";
        }
    }

    @GetMapping("/kewajiban/mahasiswa/detail")
    public void detailBebasKewajiban(Model model, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mhs = mahasiswaDao.findByUser(user);

        PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findByMahasiswa(mhs);
        log.info("Mahasiswa : {}", bebasKewajiban);

        model.addAttribute("bebasKewajiban", bebasKewajiban);
        model.addAttribute("listKewajibanMahasiswa", kewajibanPerpusMahasiswaDao.findByBebasKewajiban(bebasKewajiban));

    }

    // CETAK SKL
    @GetMapping("/cetakSkl")
    public String sklMahasiswa(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, HttpServletResponse response) throws IOException {

        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=SKL "+bebasKewajiban.getMahasiswa().getNim()+"_"+bebasKewajiban.getMahasiswa().getNama()+".pdf";

        response.setHeader(headerKey, headerValue);
        bebasKewajibanService.cetakSKL(bebasKewajiban, response);

        return "redirect:/kewajiban/mahasiswa/detail";
    }

    // halaman admin

    @GetMapping("/kewajiban/admin")
    public String setHalamanaAdmin(Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        if (USER_PERPUS.contains(user.getRole().getId())){
            return "redirect:/kewajiban/admin/perpus/list";
        }
        if (USER_FINANCE.contains(user.getRole().getId())) {
            return "redirect:/kewajiban/admin/keuangan/list";
        }
        if (USER_TLC.contains(user.getRole().getId())) {
            return "redirect:/kewajiban/admin/tlc/list";
        }
        if (USER_AKADEMIK.contains(user.getRole().getId())) {
            return "redirect:/kewajiban/admin/akademik/list";
        }else{
            return "redirect:/kewajiban/admin/perpus/list";
        }

    }

    // BAGIAN PERPUSTAKAAN

    @GetMapping("/kewajiban/admin/perpus/list")
    public void listKewajibanPerpus(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, String search){
        User user = currentUserService.currentUser(authentication);
        if ("perpustakaan".equalsIgnoreCase(user.getRole().getId()) || "superuser".equalsIgnoreCase(user.getRole().getId())) {
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByMahasiswaNim(StatusRecord.AKTIF, StatusApprove.WAITING, search, search, page));
            }else{
                model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveOrderByMahasiswaNim(StatusRecord.AKTIF, StatusApprove.WAITING, page));
            }
        }
        if ("kabagperpus".equalsIgnoreCase(user.getRole().getId()) || "superuser".equalsIgnoreCase(user.getRole().getId())) {
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("listMahasiswaKabag", bebasKewajibanDao.findByStatusAndStatusApproveAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByMahasiswaNim(StatusRecord.AKTIF, StatusApprove.APPROVED_PERPUS, search, search, page));
            }else{
                model.addAttribute("listMahasiswaKabag", bebasKewajibanDao.findByStatusAndStatusApproveOrderByMahasiswaNim(StatusRecord.AKTIF, StatusApprove.APPROVED_PERPUS, page));
            }
        }
    }

    @GetMapping("/kewajiban/admin/perpus/detail")
    public void detailKewajibanPerpus(Model model, @RequestParam String id, Authentication authentication){

        PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findById(id).get();
//        model.addAttribute("listKewajibanPerpus", kewajibanPerpusDao.findByStatusOrderById(StatusRecord.AKTIF));
        model.addAttribute("listKewajibanMahasiswa", kewajibanPerpusMahasiswaDao.findByBebasKewajiban(bebasKewajiban));
        model.addAttribute("mahasiswa", bebasKewajiban.getMahasiswa());
        model.addAttribute("bebasKewajiban", bebasKewajiban);
    }

    @PostMapping("/kewajiban/admin/perpus/input")
    public String saveKewajibanPerpus(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, HttpServletRequest request){

        List<KewajibanPerpusMahasiswa> list = kewajibanPerpusMahasiswaDao.findByBebasKewajiban(bebasKewajiban);
        for (KewajibanPerpusMahasiswa data : list){
            String pilihan = request.getParameter("check-"+data.getId());
            if (pilihan != null) {
                data.setStatus(StatusRecord.AKTIF);
                kewajibanPerpusMahasiswaDao.save(data);
            }
        }

        Integer jumlahKeterangan = kewajibanPerpusDao.countByStatus(StatusRecord.AKTIF).intValue();
        Integer jumlahdiCeklis = kewajibanPerpusMahasiswaDao.countByBebasKewajibanAndStatus(bebasKewajiban, StatusRecord.AKTIF).intValue();
        if (jumlahKeterangan == jumlahdiCeklis) {
            bebasKewajiban.setStatusApprove(StatusApprove.APPROVED_PERPUS);
            bebasKewajibanDao.save(bebasKewajiban);
        }

        return "redirect:/kewajiban/admin/perpus/list";
    }

    @PostMapping("/kewajiban/admin/perpus/approve")
    public String approveKabagPerpus(@RequestParam PendaftaranBebasKewajiban bebasKewajiban){
        bebasKewajiban.setStatusApprove(StatusApprove.APPROVED_KABAG_PERPUS);
        bebasKewajibanDao.save(bebasKewajiban);

        return "redirect:/kewajiban/admin/perpus/list";
    }

    // BAGIAN KEUANGAN

    @GetMapping("/kewajiban/admin/keuangan/list")
    public void listKewajibanKeuangan(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByMahasiswaNim(StatusRecord.AKTIF, StatusApprove.APPROVED_KABAG_PERPUS, search, search, page));
        }else{
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveOrderByMahasiswaNim(StatusRecord.AKTIF, StatusApprove.APPROVED_KABAG_PERPUS, page));
        }

    }

    @GetMapping("/kewajiban/admin/keuangan/detail")
    public void detailKewajibanKeuangan(Model model, @RequestParam String id){

        PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findById(id).get();

        model.addAttribute("mahasiswa", bebasKewajiban.getMahasiswa());
        model.addAttribute("bebasKewajiban", bebasKewajiban);
        model.addAttribute("listTagihan", tagihanDao.findByMahasiswaAndStatusNotOrderByTahunAkademikKodeTahunAkademik(bebasKewajiban.getMahasiswa(), StatusRecord.HAPUS));
        model.addAttribute("listPembayaran", pembayaranDao.findByTagihanMahasiswaAndTagihanStatusNotOrderByTagihanTahunAkademikKodeTahunAkademik(bebasKewajiban.getMahasiswa(), StatusRecord.HAPUS));
    }

    @PostMapping("/kewajiban/admin/keuangan/approve")
    public String approveKeuangan(@RequestParam PendaftaranBebasKewajiban bebasKewajiban){
        Jenjang jenjang = bebasKewajiban.getMahasiswa().getIdProdi().getIdJenjang();
        if ("S1".equalsIgnoreCase(jenjang.getKodeJenjang())) {
            bebasKewajiban.setStatusApprove(StatusApprove.APPROVED_KEUANGAN);
            bebasKewajibanDao.save(bebasKewajiban);
        } else if ("S2".equalsIgnoreCase(jenjang.getKodeJenjang())) {
            bebasKewajiban.setStatusApprove(StatusApprove.APPROVED_TLC);
            bebasKewajibanDao.save(bebasKewajiban);
        }else {
            bebasKewajiban.setStatusApprove(StatusApprove.APPROVED_KEUANGAN);
            bebasKewajibanDao.save(bebasKewajiban);
        }

        return "redirect:/kewajiban/admin/keuangan/list";
    }

    // BAGIAN TLC

    @GetMapping("/kewajiban/admin/tlc/list")
    public void listTlc(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByMahasiswaNim(StatusRecord.AKTIF, StatusApprove.APPROVED_KEUANGAN, search, search, page));
        }else{
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveOrderByMahasiswaNim(StatusRecord.AKTIF, StatusApprove.APPROVED_KEUANGAN, page));
        }

    }

    @PostMapping("/kewajiban/admin/tlc/approve")
    public String approveTLC(@RequestParam PendaftaranBebasKewajiban bebasKewajiban){
        bebasKewajiban.setStatusApprove(StatusApprove.APPROVED_TLC);
        bebasKewajibanDao.save(bebasKewajiban);

        return "redirect:/kewajiban/admin/tlc/list";
    }

    // BAGIAN AKADEMIK

    @GetMapping("/kewajiban/admin/akademik/list")
    public void listKewajibanAkademik(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveInAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByMahasiswaNim(StatusRecord.AKTIF, Arrays.asList(StatusApprove.APPROVED_TLC, StatusApprove.APPROVED), search, search, page));
        }else{
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveInOrderByMahasiswaNim(StatusRecord.AKTIF, Arrays.asList(StatusApprove.APPROVED_TLC, StatusApprove.APPROVED), page));
        }
    }

    @PostMapping("/kewajiban/admin/akademik/approve")
    public String approveAkademik(@RequestParam PendaftaranBebasKewajiban bebasKewajiban){
        bebasKewajiban.setStatusApprove(StatusApprove.APPROVED);
        bebasKewajibanDao.save(bebasKewajiban);

        return "redirect:/kewajiban/admin/akademik/list";
    }

}
