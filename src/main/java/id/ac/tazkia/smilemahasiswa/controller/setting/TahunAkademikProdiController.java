package id.ac.tazkia.smilemahasiswa.controller.setting;

import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunProdiDao;
import id.ac.tazkia.smilemahasiswa.entity.EdomQuestion;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademikProdi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TahunAkademikProdiController {

    @Autowired
    private TahunProdiDao tahunProdiDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private ProdiDao prodiDao;


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
        model.addAttribute("tahunAkademik", tahunAkademikDao.findById(tahunAkademikProdi.getTahunAkademik().getId()).get());
        model.addAttribute("prodi", prodiDao.findById(tahunAkademikProdi.getProdi().getId()).get());

        return "prodi/tahun_akademik/form";

    }

    @PostMapping("/prodi/tahun/save")
    public String saveTahunAkademikProdi(@ModelAttribute TahunAkademikProdi tahunAkademikProdi,
                                         RedirectAttributes redirectAttributes){

        tahunAkademikProdi.setStatus(StatusRecord.AKTIF);
        tahunProdiDao.save(tahunAkademikProdi);

        redirectAttributes.addFlashAttribute("success", "Save Data Berhasil");
        return "redirect:../tahun";

    }


}
