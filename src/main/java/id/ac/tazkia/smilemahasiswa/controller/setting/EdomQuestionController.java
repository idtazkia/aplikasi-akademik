package id.ac.tazkia.smilemahasiswa.controller.setting;

import java.util.List;

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

import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomCategoryDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomQuestionConfigDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomQuestionDao;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomQuestion;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomQuestionConfig;

@Controller
public class EdomQuestionController {

    @Autowired
    private EdomQuestionConfigDao edomQuestionConfigDao;
    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private EdomQuestionDao edomQuestionDao;

    @Autowired
    private EdomCategoryDao edomCategoryDao;

    @GetMapping("/setting/edom/questions")
    public String edomQuestionList(Model model, Pageable pageable) {
        model.addAttribute("listEdomCategory", edomCategoryDao.findAll());
        model.addAttribute("listEdomQuestion", edomQuestionDao.findByStatus(StatusRecord.AKTIF, pageable));
        return "setting/edom/question/list";
    }

    @GetMapping("/setting/edom/assessment")
    public String edomEvaluationList(Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TahunAkademik tahunAkademik,
            @RequestParam(required = false) String bahasa,
            @PageableDefault(size = 10) Pageable page) {
        // model.addAttribute("listEdomQuestion",
        // edomQuestionDao.findByStatus(StatusRecord.AKTIF));

        return "setting/edom/assesment/list";
    }

    @GetMapping("/setting/edom/configuration")
    public String edomConfigurationList(Model model) {
        model.addAttribute("akademikAktif", tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("pilihanDosen",
                edomQuestionDao.cariPertanyaanDosen(tahunAkademikDao.findByStatus(StatusRecord.AKTIF)));
        model.addAttribute("pilihanSaranaPrasarana",
                edomQuestionDao.cariPertanyaanSaranaPrasarana(tahunAkademikDao.findByStatus(StatusRecord.AKTIF)));
        return "setting/edom/config/list";
    }

    @PostMapping("/setting/edom/configuration")
    public String saveEdomConfiguration(
            @RequestParam String tahunAkademik,
            @RequestParam List<String> question,
            @RequestParam String targetType,
            RedirectAttributes redirectAttributes) {

        TahunAkademik ta = tahunAkademikDao.findByNamaTahunAkademikAndStatus(tahunAkademik, StatusRecord.AKTIF);

        for (String questionId : question) {
            EdomQuestion edomQuestion = edomQuestionDao.findById(questionId).orElse(null);
            if (edomQuestion == null) {
                System.out.println("Question not found: " + questionId);
                continue;
            }
            System.out.println("Question : " + edomQuestion.getQuestionText());
            System.out.println(ta.getNamaTahunAkademik());

            EdomQuestionConfig config = new EdomQuestionConfig();
            config.setTahunAkademik(ta);
            config.setQuestion(edomQuestion);
            config.setTargetType(targetType);
            edomQuestionConfigDao.save(config);
        }

        redirectAttributes.addFlashAttribute("success", "Save Data Berhasil");
        return "redirect:/setting/edom/configuration";
    }

    @PostMapping("/setting/edom/question/save")
    public String saveEdomQuestion(@ModelAttribute EdomQuestion edomQuestion,
            RedirectAttributes redirectAttributes) {

        edomQuestion.setStatus(StatusRecord.AKTIF);
        edomQuestionDao.save(edomQuestion);

        redirectAttributes.addFlashAttribute("success", "Save Data Berhasil");
        return "redirect:/setting/edom/questions";
    }

    @PostMapping("/setting/edom/question/delete")
    public String deleteEdom(@RequestParam EdomQuestion edomQuestion) {
        edomQuestion.setStatus(StatusRecord.HAPUS);
        edomQuestionDao.save(edomQuestion);

        return "redirect:../questions";
    }

}
