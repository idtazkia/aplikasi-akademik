package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.KelurahanDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.MahasiswaDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.ProvinsiDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Kelurahan;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MahasiswaController {
    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KelurahanDao kelurahanDao;

    @GetMapping("/api/kelurahan")
    @ResponseBody
    public Page<Kelurahan> cariData(@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return kelurahanDao.findAll(page);
        }
        return kelurahanDao.findByNamaContainingIgnoreCaseOrKabupatenKotaNamaContainingIgnoreCaseOrKecamatanNamaContainingIgnoreCaseOrProvinsiNamaContainingIgnoreCase(search,search,search,search,page);
    }

    @GetMapping("/mahasiswa/list")
    public void daftarMahasiswa(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", mahasiswaDao.findByStatusNotInAndNamaContainingIgnoreCaseOrNimContainingIgnoreCaseOrderByNama(StatusRecord.HAPUS, search,search, page));
        } else {
            model.addAttribute("list",mahasiswaDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/mahasiswa/form")
    public void  formMahasiswa(){
    }
}
