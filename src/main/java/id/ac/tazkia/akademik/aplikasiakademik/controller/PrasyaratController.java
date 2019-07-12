package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.GradeDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.MatakuliahKurikulumDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.PrasyaratDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Kelas;
import id.ac.tazkia.akademik.aplikasiakademik.entity.MatakuliahKurikulum;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Prasyarat;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@Controller
public class PrasyaratController {

    @Autowired
    private MatakuliahKurikulumDao matakuliahKurikulumDao;

    @Autowired
    private PrasyaratDao prasyaratDao;

    @Autowired
    private GradeDao gradeDao;

    @GetMapping("/prasyarat/form")
    public void formPrasyarat(Model model, @RequestParam(name = "id", value = "id") MatakuliahKurikulum matakuliahKurikulum,
                              @RequestParam(required = false) String pras){
        model.addAttribute("pras", new Prasyarat());
        model.addAttribute("matakuliahKurikulum", matakuliahKurikulum);
        List<MatakuliahKurikulum> mk = matakuliahKurikulumDao.findByStatusAndKurikulumAndSemesterNotNull(StatusRecord.AKTIF,matakuliahKurikulum.getKurikulum());
        model.addAttribute("listPrasyarat", prasyaratDao.findByMatakuliahKurikulumAndStatus(matakuliahKurikulum,StatusRecord.AKTIF));
        model.addAttribute("matakuliahPrasyarat", mk);
        model.addAttribute("grade", gradeDao.findByStatus(StatusRecord.AKTIF));

        if (pras != null && !pras.isEmpty()) {
            Prasyarat prasyarat = prasyaratDao.findById(pras).get();
            if (pras != null) {
                model.addAttribute("pras", prasyarat);
            }
        }
    }

    @PostMapping("/prasyarat/form")
    public String buatPras(@Valid Prasyarat prasyarat){

        prasyarat.setMatakuliah(prasyarat.getMatakuliahKurikulum().getMatakuliah());
        prasyarat.setMatakuliahPras(prasyarat.getMatakuliahKurikulumPras().getMatakuliah());
        prasyaratDao.save(prasyarat);
        return "redirect:form?id="+prasyarat.getMatakuliahKurikulum().getId();
    }

    @PostMapping("/prasyarat/delete")
    public String deletePras(@RequestParam(value = "id", name = "id") Prasyarat prasyarat){

        prasyarat.setStatus(StatusRecord.HAPUS);
        prasyaratDao.save(prasyarat);
        return "redirect:form?id="+prasyarat.getMatakuliahKurikulum().getId();
    }

}
