package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.xml.ws.RequestWrapper;
import java.util.List;

@Controller
public class KesediaanMengajarController {
    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    KesediaanMengajarPertanyaanDao kesediaanMengajarPertanyaanDao;

    @Autowired
    KesediaanMengajarSubPertanyaanDao kesediaanMengajarSubPertanyaanDao;

    @Autowired
    KesediaanMengajarJawabanDao kesediaanMengajarJawabanDao;

    @Autowired
    KaryawanDao karyawanDao;

    @Autowired
    DosenDao dosenDao;

    @Autowired
    KesediaanMengajarDosenDao kesediaanMengajarDosenDao;

    @Autowired
    KesediaanMengajarSesiDao kesediaanMengajarSesiDao;

    @Autowired
    TahunAkademikDao tahunAkademikDao;

    @Autowired
    HariDao hariDao;

    @GetMapping("/kesediaanMengajar/list")
    public void listKesediaanMengajar(Model model){
        model.addAttribute("listData", kesediaanMengajarPertanyaanDao.findByStatusOrderByUrutanAsc(StatusRecord.AKTIF));
        model.addAttribute("subPertanyaan", kesediaanMengajarSubPertanyaanDao.findByStatus(StatusRecord.AKTIF));
    }

    @GetMapping("/kesediaanMengajar/form")
    public void formKesediaanMengajar(){}

    @PostMapping("/kesediaanMengajar/form")
    public String saveSoalKesediaanMengajar(KesediaanMengajarPertanyaan kesediaanMengajar){
        kesediaanMengajar.setStatus(StatusRecord.AKTIF);
        kesediaanMengajarPertanyaanDao.save(kesediaanMengajar);
        System.out.println("test 1 : " + kesediaanMengajar);
        if (kesediaanMengajar.getTipeSoal().equals("pilihan")){
            KesediaanMengajarPertanyaan kesediaanMengajarPertanyaan = kesediaanMengajarPertanyaanDao.findById(kesediaanMengajar.getId()).get();
            KesediaanMengajarSubPertanyaan kesediaanMengajarSubPertanyaan = new KesediaanMengajarSubPertanyaan();
            kesediaanMengajarSubPertanyaan.setKesediaanMengajarPertanyaan(kesediaanMengajarPertanyaan);
            kesediaanMengajarSubPertanyaan.setPertanyaan("-");
            kesediaanMengajarSubPertanyaan.setStatus(StatusRecord.AKTIF);
            kesediaanMengajarSubPertanyaanDao.save(kesediaanMengajarSubPertanyaan);
            System.out.println("test 2 : " + kesediaanMengajarSubPertanyaan);
        }

        return "redirect:list";
    }

    @PostMapping("/kesediaanMengajar/subPertanyaan")
    public String saveSubPertanyaan(@RequestParam String[] pertanyaan, @RequestParam KesediaanMengajarPertanyaan kesediaanMengajarPertanyaan){
        for (String subPertanyaan : pertanyaan){
            KesediaanMengajarSubPertanyaan kesediaanMengajarSubPertanyaan = new KesediaanMengajarSubPertanyaan();

            kesediaanMengajarSubPertanyaan.setPertanyaan(subPertanyaan);
            kesediaanMengajarSubPertanyaan.setKesediaanMengajarPertanyaan(kesediaanMengajarPertanyaan);
            kesediaanMengajarSubPertanyaan.setStatus(StatusRecord.AKTIF);
            System.out.println("test : " + subPertanyaan);
            kesediaanMengajarSubPertanyaanDao.save(kesediaanMengajarSubPertanyaan);
        }

        return "redirect:list";
    }

    @PostMapping("/kesediaanMengajar/hapus")
    public String hapusPertanyaan(@RequestParam KesediaanMengajarPertanyaan id){
        id.setStatus(StatusRecord.HAPUS);
        kesediaanMengajarPertanyaanDao.save(id);
        return "redirect:list";
    }

    @PostMapping("/kesediaanMengajar/delete")
    public String deleteSubPertanyaan(@RequestParam KesediaanMengajarSubPertanyaan id){
        id.setStatus(StatusRecord.HAPUS);
        kesediaanMengajarSubPertanyaanDao.save(id);
        return "redirect:list";
    }

    @GetMapping("/kesediaanMengajar/dosen")
    public String kesediaanMengajarDosen(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        KesediaanMengajarDosen d = kesediaanMengajarDosenDao.findByDosenAndTahunAkademikAndStatus(dosen, tahunAkademikDao.findByStatus(StatusRecord.AKTIF), StatusRecord.AKTIF);
        List<KesediaanMengajarSesi> kesediaanSesi = kesediaanMengajarSesiDao.findByKesediaanDosenAndStatus(d, StatusRecord.AKTIF);

        if (d != null && !kesediaanSesi.isEmpty()) {
            model.addAttribute("cekDosen", "sudah mengisi");
        } else if (d != null && kesediaanSesi.isEmpty()) {
            return "redirect:sesi?kmd="+d.getId();
        }

        model.addAttribute("pertanyaan", kesediaanMengajarPertanyaanDao.findByStatusOrderByUrutanAsc(StatusRecord.AKTIF));
        model.addAttribute("subPertanyaan", kesediaanMengajarSubPertanyaanDao.findByStatus(StatusRecord.AKTIF));

        return "/kesediaanMengajar/dosen";

    }

    @PostMapping("/kesediaanMengajar/menjawab")
    public String menjawabKesedianMengajar(@RequestParam String[] kesediaanMengajarSubPertanyaan, @Valid KesediaanMengajarDosen kesediaanMengajarDosen,
                                           @RequestParam(required = false) String jawDosen, HttpServletRequest request, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        for (String pertanyaan : kesediaanMengajarSubPertanyaan){
            KesediaanMengajarJawaban kesediaanMengajarJawaban = new KesediaanMengajarJawaban();
            KesediaanMengajarSubPertanyaan kesediaanMengajarSubPertanyaan1 = kesediaanMengajarSubPertanyaanDao.findById(pertanyaan).get();
            String jawaban = request.getParameter(kesediaanMengajarSubPertanyaan1.getId() + "jwb");

            kesediaanMengajarJawaban.setKesediaanMengajarSubPertanyaan(kesediaanMengajarSubPertanyaan1);
            kesediaanMengajarJawaban.setJawaban(jawaban);
            kesediaanMengajarJawaban.setStatus(StatusRecord.AKTIF);
            kesediaanMengajarJawaban.setUser(user);

            kesediaanMengajarJawabanDao.save(kesediaanMengajarJawaban);

        }

        kesediaanMengajarDosen.setDosen(dosen);
        kesediaanMengajarDosen.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        kesediaanMengajarDosen.setJawaban(jawDosen);
        kesediaanMengajarDosenDao.save(kesediaanMengajarDosen);

        if (jawDosen.equals("Tidak")){
            return "redirect:../admin";
        }else if (jawDosen.equals("Ya")){
            return "redirect:sesi?kmd="+kesediaanMengajarDosen.getId();
        }

        return "redirect:dosen";
    }

    @GetMapping("/kesediaanMengajar/sesi")
    public void pilihSesi(Model model, @RequestParam String kmd){

        KesediaanMengajarDosen kesediaanDosen = kesediaanMengajarDosenDao.findById(kmd).get();
        model.addAttribute("kesediaanDosen", kesediaanDosen);
        model.addAttribute("hari", hariDao.findByStatus(StatusRecord.AKTIF));

    }

    @PostMapping("/kesediaanMengajar/sesi")
    public String submitSesi(@RequestParam String kesediaanDosen, HttpServletRequest request){

        KesediaanMengajarDosen kesediaanMengajarDosen = kesediaanMengajarDosenDao.findById(kesediaanDosen).get();

        List<Hari> hari = hariDao.findByStatus(StatusRecord.AKTIF);

        for (Hari h : hari){

            String sesi1 = request.getParameter(h.getId()+ "-1");
            String sesi2 = request.getParameter(h.getId()+ "-2");
            String sesi3 = request.getParameter(h.getId()+ "-3");
            String sesi4 = request.getParameter(h.getId()+ "-4");
            String sesi5 = request.getParameter(h.getId()+ "-5");

            System.out.println("Dosen : " + kesediaanMengajarDosen.getDosen().getKaryawan().getNamaKaryawan()+", Jawaban :" + kesediaanMengajarDosen.getJawaban());
            System.out.println("Hari : " + h.getNamaHari() + " --> " + " Sesi 1 :" + sesi1 + " Sesi 2 :" + sesi2 + " Sesi 3 :" + sesi3 + " Sesi 4 :" + sesi4 + " Sesi 5 :" + sesi5);

            KesediaanMengajarSesi kesediaanSesi = new KesediaanMengajarSesi();
            kesediaanSesi.setKesediaanDosen(kesediaanMengajarDosen);
            kesediaanSesi.setHari(h);

            if (sesi1 != null) {
                kesediaanSesi.setSesi1(Boolean.TRUE);
            }
            if (sesi2 != null) {
                kesediaanSesi.setSesi2(Boolean.TRUE);
            }
            if (sesi3 != null) {
                kesediaanSesi.setSesi3(Boolean.TRUE);
            }
            if (sesi4 != null) {
                kesediaanSesi.setSesi4(Boolean.TRUE);
            }
            if (sesi5 != null) {
                kesediaanSesi.setSesi5(Boolean.TRUE);
            }

            if (sesi1 != null || sesi2 != null || sesi3 != null || sesi4 != null || sesi5 != null) {
                kesediaanMengajarSesiDao.save(kesediaanSesi);
            }


        }

        return "redirect:../admin";
    }

}
