package id.ac.tazkia.smilemahasiswa.controller.setting;


import id.ac.tazkia.smilemahasiswa.dao.BahasaDao;
import id.ac.tazkia.smilemahasiswa.dao.EdomQuestionDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.entity.EdomQuestion;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
//import jdk.nashorn.internal.objects.annotations.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
public class EdomQuestionController {

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private EdomQuestionDao edomQuestionDao;

    @Autowired
    private BahasaDao bahasaDao;

    @GetMapping("/setting/edom/questions")
    public String edomQuestinList(Model model,
                                  @RequestParam(required = false) String search,
                                  @RequestParam(required = false) TahunAkademik tahunAkademik,
                                  @RequestParam(required = false) String bahasa,
                                  @PageableDefault(size = 10)Pageable page){
        model.addAttribute("listEdomQuestion", edomQuestionDao.findByStatusOrderByNomor(StatusRecord.AKTIF));


        return "setting/edomquestion/list";
    }

    @GetMapping("/setting/edom/question/new")
    public String newEdomQuestion(Model model){

        model.addAttribute("bahasa", bahasaDao.findByStatusOrderByBahasa(StatusRecord.AKTIF));
        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusOrderByKodeTahunAkademikDesc(StatusRecord.AKTIF));
        model.addAttribute("edomQuestion", new EdomQuestion());

        return "setting/edomquestion/form";

    }

    @GetMapping("/setting/edom/question/edit")
    public String editEdomQuestion(Model model,
                                   @RequestParam(required = false) String id){

        model.addAttribute("bahasa", bahasaDao.findByStatusOrderByBahasa(StatusRecord.AKTIF));
        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusOrderByKodeTahunAkademikDesc(StatusRecord.AKTIF));
        model.addAttribute("edomQuestion", edomQuestionDao.findById(id));

        return "setting/edomquestion/form";

    }

    @PostMapping("/setting/edom/question/save")
    public String saveEdomQuestion(@ModelAttribute EdomQuestion edomQuestion,
                                   RedirectAttributes redirectAttributes){

        edomQuestion.setStatus(StatusRecord.AKTIF);
        edomQuestionDao.save(edomQuestion);

        redirectAttributes.addFlashAttribute("success", "Save Data Berhasil");
        return "redirect:/setting/edomquestion/list";
    }

    @PostMapping("/setting/edom/question/delete")
    public String deleteEdom(@RequestParam EdomQuestion edomQuestion){
        edomQuestion.setStatus(StatusRecord.HAPUS);
        edomQuestionDao.save(edomQuestion);

        return "redirect:../questions";
    }

}
