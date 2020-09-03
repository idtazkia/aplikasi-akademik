package id.ac.tazkia.smilemahasiswa.controller.setting;

import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunProdiDao;
import id.ac.tazkia.smilemahasiswa.entity.*;
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

import javax.management.Query;
import java.util.Collections;
import java.util.List;

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


        model.addAttribute("listTahunAkademikProdi" , tahunProdiDao.findByTahunAkademikKodeTahunAkademikContainingOrderByTahunAkademikKodeTahunAkademikDesc("2", page));

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

    @PostMapping("/prodi/tahun/active")
    public String activeTahunAkademikProdi(@RequestParam (required = false) String tahunAkademikProdi,
                                         RedirectAttributes redirectAttributes){

        TahunAkademikProdi tahunAkademikProdi1 = tahunProdiDao.findById(tahunAkademikProdi).get();
//        int sda = tahunProdiDao.updateTahunAKademikProdi2(tahunAkademikProdi, tahunAkademikProdi1.getProdi(), StatusRecord.NONAKTIF);

        List<TahunAkademikProdi> tahunAkademikProdis = tahunProdiDao.findByTahunAkademikAndProdiAndStatus(tahunAkademikProdi1.getTahunAkademik(), tahunAkademikProdi1.getProdi(), StatusRecord.AKTIF);

        for (TahunAkademikProdi tap : tahunAkademikProdis){
            tap.setStatus(StatusRecord.NONAKTIF);
            tahunProdiDao.save(tap);
        }

        tahunAkademikProdi1.setStatus(StatusRecord.AKTIF);
        tahunProdiDao.save(tahunAkademikProdi1);

        redirectAttributes.addFlashAttribute("success", "Save Data Berhasil");
        return "redirect:../tahun";

    }

    @PostMapping("/prodi/tahun/deactive")
    public String deactiveTahunAkademikProdi(@RequestParam (required = false) String tahunAkademikProdi,
                                         RedirectAttributes redirectAttributes){

        TahunAkademikProdi tahunAkademikProdi1 = tahunProdiDao.findById(tahunAkademikProdi).get();
        tahunAkademikProdi1.setStatus(StatusRecord.NONAKTIF);
        tahunProdiDao.save(tahunAkademikProdi1);

        redirectAttributes.addFlashAttribute("success", "Save Data Berhasil");
        return "redirect:../tahun";

    }


}
