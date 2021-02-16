package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.NilaiTugasDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dto.report.DataKhsDto;
import id.ac.tazkia.smilemahasiswa.dto.report.EdomDto;
import id.ac.tazkia.smilemahasiswa.dto.report.KhsDto;
import id.ac.tazkia.smilemahasiswa.dto.report.TugasDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Controller
public class ReportMahasiswaController {
    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private NilaiTugasDao nilaiTugasDao;

    @GetMapping("/report/khs")
    public String khs(Model model, Authentication authentication,@RequestParam(required = false) TahunAkademik tahunAkademik){

        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        List<TugasDto> tugasDtos = new ArrayList<>();
        model.addAttribute("tahun" , tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));
        if (tahunAkademik != null) {
            List<KrsDetail> validasiEdom = krsDetailDao.findByMahasiswaAndKrsTahunAkademikAndStatusAndStatusEdom(mahasiswa,tahunAkademik,StatusRecord.AKTIF, StatusRecord.UNDONE);

            model.addAttribute("selectedTahun" , tahunAkademik);

            List<DataKhsDto> krsDetail = krsDetailDao.getKhs(tahunAkademik,mahasiswa);
            for (DataKhsDto data : krsDetail) {
                List<TugasDto> nilaiTugas = nilaiTugasDao.findTaskScore(data.getId());
                tugasDtos.addAll(nilaiTugas);
            }
            if (!krsDetail.isEmpty()){
                model.addAttribute("tugas",tugasDtos);
                model.addAttribute("khs",krsDetail);
                model.addAttribute("ipk", krsDetailDao.ipk(mahasiswa));
                model.addAttribute("ip", krsDetailDao.ip(mahasiswa,tahunAkademik));
            }

            if (validasiEdom.isEmpty() || validasiEdom == null){
                return "report/khs";
            }else {
                return "redirect:edom?tahunAkademik="+tahunAkademik.getId();
            }
        }

        return "report/khs";


    }

    @GetMapping("/report/edom")
    public void edom(Authentication authentication, Model model,@RequestParam(required = false) TahunAkademik tahunAkademik) {
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);

        List<EdomDto> krsDetail = krsDetailDao.cariEdom(mahasiswa, tahunAkademik, StatusRecord.AKTIF, StatusRecord.UNDONE);

        model.addAttribute("detail", krsDetail);
        model.addAttribute("tahun", tahunAkademik);
    }

    @PostMapping("/report/edom")
    public String prosesForm(Authentication authentication, HttpServletRequest request,@RequestParam TahunAkademik tahunAkademik,
                             RedirectAttributes attributes) {
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        List<KrsDetail> krsDetail = krsDetailDao.findByMahasiswaAndKrsTahunAkademikAndStatusAndStatusEdom(mahasiswa,tahunAkademik,StatusRecord.AKTIF,StatusRecord.UNDONE);


        for(KrsDetail daftarEdom : krsDetail) {
            String pertanyaan1 = request.getParameter(daftarEdom.getId() + "1");
            String pertanyaan2 = request.getParameter(daftarEdom.getId() + "2");
            String pertanyaan3 = request.getParameter(daftarEdom.getId() + "3");
            String pertanyaan4 = request.getParameter(daftarEdom.getId() + "4");
            String pertanyaan5 = request.getParameter(daftarEdom.getId() + "5");

            if (pertanyaan1 == null){
                daftarEdom.setE1(Integer.valueOf("3"));
            }else {
                daftarEdom.setE1(Integer.valueOf(pertanyaan1));
            }

            if (pertanyaan2 == null){
                daftarEdom.setE2(Integer.valueOf("3"));
            }else {
                daftarEdom.setE2(Integer.valueOf(pertanyaan2));
            }

            if (pertanyaan3 == null){
                daftarEdom.setE3(Integer.valueOf("3"));
            }else {
                daftarEdom.setE3(Integer.valueOf(pertanyaan3));
            }

            if (pertanyaan4 == null){
                daftarEdom.setE4(Integer.valueOf("3"));
            }else {
                daftarEdom.setE4(Integer.valueOf(pertanyaan4));
            }

            if (pertanyaan5 == null){
                daftarEdom.setE5(Integer.valueOf("3"));
            }else {
                daftarEdom.setE5(Integer.valueOf(pertanyaan5));
            }
            daftarEdom.setStatusEdom(StatusRecord.DONE);
            krsDetailDao.save(daftarEdom);



        }

        return "redirect:khs";

    }

    @GetMapping("/report/transcript")
    public void checkTranscript(Authentication authentication,Model model){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        //tampilsemua
        model.addAttribute("transkrip", krsDetailDao.transkrip(mahasiswa));


        model.addAttribute("transkrip1", krsDetailDao.transkripSem(mahasiswa,"1"));
        model.addAttribute("transkrip2", krsDetailDao.transkripSem(mahasiswa,"2"));
        model.addAttribute("transkrip3", krsDetailDao.transkripSem(mahasiswa,"3"));
        model.addAttribute("transkrip4", krsDetailDao.transkripSem(mahasiswa,"4"));
        model.addAttribute("transkrip5", krsDetailDao.transkripSem(mahasiswa,"5"));
        model.addAttribute("transkrip6", krsDetailDao.transkripSem(mahasiswa,"6"));
        model.addAttribute("transkrip7", krsDetailDao.transkripSem(mahasiswa,"7"));
        model.addAttribute("transkrip8", krsDetailDao.transkripSem(mahasiswa,"8"));



    }



}
