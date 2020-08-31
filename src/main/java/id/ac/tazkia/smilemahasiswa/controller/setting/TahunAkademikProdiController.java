package id.ac.tazkia.smilemahasiswa.controller.setting;

import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunProdiDao;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademikProdi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TahunAkademikProdiController {

    @Autowired
    private TahunProdiDao tahunProdiDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;


    @GetMapping("/prodi/tahun")
    public String tahunAkademikProdi(Model model,
                                     @PageableDefault(size = 10) Pageable page){

        model.addAttribute("listTahunAkademikProdi" , tahunProdiDao.findByStatusOrderByTahunAkademikKodeTahunAkademikDesc(StatusRecord.AKTIF, page));

        return "prodi/tahun_akademik/list";
    }

    @GetMapping("/prodi/tahun/edit")
    public String editTahunAkademikProdi(Model model,
                                         @RequestParam(required = false)TahunAkademikProdi tahunAkademikProdi){

        model.addAttribute("tahunAkademikProdi", tahunAkademikProdi);
        model.addAttribute("tahunAkademik", tahunAkademikDao.findById(tahunAkademikProdi.getTahunAkademik().getId()));

        return "prodi/tahun_akademik/form";

    }


}
