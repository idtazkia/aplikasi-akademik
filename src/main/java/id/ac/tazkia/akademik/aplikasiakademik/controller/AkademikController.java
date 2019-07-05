package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.ProdiDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.TahunAkademikDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.TahunAkademikProdiDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademik;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademikProdi;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@Controller
public class AkademikController {
    
    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private TahunAkademikProdiDao tahunAkademikProdiDao;
    @Autowired
    private ProdiDao prodiDao;

    @GetMapping("/akademik/list")
    public void akademikList(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", tahunAkademikDao.findByStatusNotInAndNamaTahunAkademikContainingIgnoreCaseOrderByKodeTahunAkademikDesc(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("list",tahunAkademikDao.findByStatusNotInOrderByKodeTahunAkademikDesc(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/akademik/form")
    public void akademikForm(Model model, @RequestParam(required = false) String id){
        model.addAttribute("tahunAkademik", new TahunAkademik());

        if (id != null && !id.isEmpty()) {
            TahunAkademik tahunAkademik = tahunAkademikDao.findById(id).get();
            if (tahunAkademik != null) {
                model.addAttribute("tahunAkademik", tahunAkademik);
            }
        }
    }

    @GetMapping("/akademik/detail")
    public void akademikDetail(Model model,String id){
        model.addAttribute("detailAkademik", tahunAkademikDao.findById(id).get());


    }

    @PostMapping("/akademik/form")
    public String prosesForm(@Valid TahunAkademik tahunAkademik){

        tahunAkademikDao.save(tahunAkademik);
        return "redirect:list";
    }

    @PostMapping("/akademik/delete")
    public String deleteJenjang(@RequestParam TahunAkademik tahunAkademik){
        tahunAkademik.setStatus(StatusRecord.HAPUS);
        tahunAkademikDao.save(tahunAkademik);

        return "redirect:list";
    }

    @PostMapping("/akademik/aktif")
    public String aktifAkademik(@RequestParam TahunAkademik tahunAkademik){
        List<TahunAkademikProdi> tahunAkademikProdi = tahunAkademikProdiDao.findByStatus(StatusRecord.AKTIF);

        for (TahunAkademikProdi tahunProdi : tahunAkademikProdi){
            tahunProdi.setStatus(StatusRecord.NONAKTIF);
            tahunAkademikProdiDao.save(tahunProdi);
        }
        TahunAkademik thnAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if (thnAkademik != null) {
            thnAkademik.setStatus(StatusRecord.NONAKTIF);
            tahunAkademikDao.save(thnAkademik);
        }

        tahunAkademik.setStatus(StatusRecord.AKTIF);

        tahunAkademikDao.save(tahunAkademik);

        TahunAkademik akademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        List<TahunAkademikProdi> akademikProdi = tahunAkademikProdiDao.findByTahunAkademik(akademik);
        if (akademikProdi != null){
            for (TahunAkademikProdi tahun : akademikProdi){
                tahun.setStatus(StatusRecord.AKTIF);
                tahunAkademikProdiDao.save(tahun);
            }
        }
        if (akademikProdi == null || akademikProdi.isEmpty()){
            List<Prodi> prodis = prodiDao.findByStatus(StatusRecord.AKTIF);
            for (Prodi prodi : prodis){
                TahunAkademikProdi tahunProdi = new TahunAkademikProdi();
                tahunProdi.setMulaiKuliah(tahunAkademik.getTanggalMulaiKuliah());
                tahunProdi.setMulaiNilai(tahunAkademik.getTanggalMulaiNilai());
                tahunProdi.setMulaiUas(tahunAkademik.getTanggalMulaiUas());
                tahunProdi.setMulaiKrs(tahunAkademik.getTanggalMulaiKrs());
                tahunProdi.setMulaiUts(tahunAkademik.getTanggalMulaiUts());
                tahunProdi.setProdi(prodi);
                tahunProdi.setSelesaiKrs(tahunAkademik.getTanggalSelesaiKrs());
                tahunProdi.setSelesaiKuliah(tahunAkademik.getTanggalSelesaiKuliah());
                tahunProdi.setSelesaiNilai(tahunAkademik.getTanggalSelesaiNilai());
                tahunProdi.setSelesaiUas(tahunAkademik.getTanggalSelesaiUas());
                tahunProdi.setSelesaiUts(tahunAkademik.getTanggalSelesaiUts());
                tahunProdi.setStatus(StatusRecord.AKTIF);
                tahunProdi.setTahunAkademik(tahunAkademik);
                tahunAkademikProdiDao.save(tahunProdi);

            }
        }


        return "redirect:list";
    }

    @PostMapping("/akademik/generateTahun")
    public String generateTahun(@RequestParam TahunAkademik tahunAkademik){
        List<Prodi> prodis = prodiDao.findByStatus(StatusRecord.AKTIF);
        for (Prodi prodi : prodis){
                TahunAkademikProdi tahunAkademikProdi = new TahunAkademikProdi();
                tahunAkademikProdi.setMulaiKrs(tahunAkademik.getTanggalMulaiKrs());
                tahunAkademikProdi.setMulaiKuliah(tahunAkademik.getTanggalMulaiKuliah());
                tahunAkademikProdi.setMulaiNilai(tahunAkademik.getTanggalMulaiNilai());
                tahunAkademikProdi.setMulaiUas(tahunAkademik.getTanggalMulaiUas());
                tahunAkademikProdi.setMulaiUts(tahunAkademik.getTanggalMulaiUts());
                tahunAkademikProdi.setProdi(prodi);
                tahunAkademikProdi.setSelesaiKrs(tahunAkademik.getTanggalSelesaiKrs());
                tahunAkademikProdi.setSelesaiKuliah(tahunAkademik.getTanggalSelesaiKuliah());
                tahunAkademikProdi.setSelesaiNilai(tahunAkademik.getTanggalSelesaiNilai());
                tahunAkademikProdi.setSelesaiUas(tahunAkademik.getTanggalSelesaiUas());
                tahunAkademikProdi.setSelesaiUts(tahunAkademik.getTanggalSelesaiUts());
                tahunAkademikProdi.setStatus(StatusRecord.AKTIF);
                tahunAkademikProdi.setTahunAkademik(tahunAkademik);
                tahunAkademikProdiDao.save(tahunAkademikProdi);

        }

        return "redirect:list";
    }
}
