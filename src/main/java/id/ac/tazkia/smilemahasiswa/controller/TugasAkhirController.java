package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.JenisTagihanDao;
import id.ac.tazkia.smilemahasiswa.dao.KategoriTugasAkhirDao;
import id.ac.tazkia.smilemahasiswa.dao.PeriodeWisudaDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Arrays;

@Controller
public class TugasAkhirController {

    @Autowired
    PeriodeWisudaDao periodeWisudaDao;

    @Autowired
    KategoriTugasAkhirDao kategoriDao;

    @Autowired
    ProdiDao prodiDao;

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findAll();
    }

    // PERIODE WISUDA

    @GetMapping("/graduation/periodeWisuda/list")
    public void listPeriode(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listPeriode", periodeWisudaDao.findByStatusNotInAndNamaContainingIgnoreCase(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else{
            model.addAttribute("listPeriode", periodeWisudaDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/graduation/periodeWisuda/form")
    public void formPeriode(Model model, @RequestParam(required = false) String id){
        model.addAttribute("newPeriode", new PeriodeWisuda());

        if (id != null && !id.isEmpty()){
            PeriodeWisuda periodeWisuda = periodeWisudaDao.findById(id).get();
            if (periodeWisuda != null){
                model.addAttribute("newPeriode", periodeWisuda);
                if (periodeWisuda.getStatus() == null){
                    periodeWisuda.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }

    }

    @PostMapping("/graduation/periodeWisuda/new")
    public String inputPeriode(@Valid PeriodeWisuda wisuda){

        periodeWisudaDao.save(wisuda);
        return "redirect:list";

    }

    @PostMapping("/graduation/periodeWisuda/delete")
    public String deletePeriode(@RequestParam PeriodeWisuda wisuda){

        wisuda.setStatus(StatusRecord.HAPUS);
        periodeWisudaDao.save(wisuda);
        return "redirect:list";

    }

    // KETEGORI TUGAS AKHIR

    @GetMapping("/graduation/kategori/list")
    public void listKategori(Model model, @RequestParam(required = false) Prodi prodi, @PageableDefault(size = 10) Pageable page){

        model.addAttribute("selectProdi", prodi);
        model.addAttribute("prodi", prodiDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("listNote", kategoriDao.findByProdiAndJenisAndStatusNotIn(prodi, Jenis.NOTE, Arrays.asList(StatusRecord.HAPUS), page));
        model.addAttribute("listSeminar", kategoriDao.findByProdiAndJenisAndStatusNotIn(prodi, Jenis.SEMINAR, Arrays.asList(StatusRecord.HAPUS), page));
        model.addAttribute("listSidang", kategoriDao.findByProdiAndJenisAndStatusNotIn(prodi, Jenis.SIDANG, Arrays.asList(StatusRecord.HAPUS), page));

        // modal add kategori
        model.addAttribute("jenis", Jenis.values());
        model.addAttribute("jenisValidasi", JenisValidasi.values());

    }

    @PostMapping("/graduation/kategori/new")
    public String saveKategori(@RequestParam(required = false) String[] prodi, @RequestParam(required = false) String nama,
                               @RequestParam(required = false) Jenis jenis, @RequestParam(required = false) JenisValidasi jenisValidasi){

        for (String p : prodi){
            Prodi prod = prodiDao.findById(p).get();
            KategoriTugasAkhir kategori = new KategoriTugasAkhir();
            kategori.setNama(nama);
            kategori.setProdi(prod);
            kategori.setJenis(jenis);
            kategori.setJenisValidasi(jenisValidasi);
            kategori.setStatus(StatusRecord.AKTIF);
            kategoriDao.save(kategori);
        }

        return "redirect:list";

    }

}
