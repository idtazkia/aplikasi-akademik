package id.ac.tazkia.smilemahasiswa.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.formula.functions.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomCategoryDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomEvaluationTargetDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomQuestionConfigDao;
import id.ac.tazkia.smilemahasiswa.dto.report.DetailEdom;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomCategory;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomEvaluationTarget;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomQuestionConfig;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.EdomService;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class EdomController {
    @Autowired
    private EdomQuestionConfigDao edomQuestionConfigDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private EdomEvaluationTargetDao edomEvaluationTargetDao;

    @Autowired
    private EdomCategoryDao edomCategoryDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private EdomService edomService;

    @Autowired
    private CurrentUserService currentUserService;

    private static final String TARGET_TYPE_DOSEN = "DOSEN";
    private static final String TARGET_TYPE_SARPRAS = "SARPRAS";

    @GetMapping("/edom/eval-dosen")
    public String edomEvaluationList(Model model, Authentication authentication,
            @RequestParam(required = false) TahunAkademik tahunAkademik,
            @RequestParam(required = false) String redirect) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        List<DetailEdom> detailEdoms = krsDetailDao.getListEdom(mahasiswa,
                tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        if (detailEdoms.isEmpty()) {
            log.info("Tidak ada data evaluasi yang tersedia untuk mahasiswa: {}", mahasiswa.getNim());
            return "redirect:../../dashboard";

        } else {
            model.addAttribute("redirect", redirect);
            model.addAttribute("tahunAkademik", tahunAkademik);
            model.addAttribute("listKrsDetail",
                    detailEdoms);
            model.addAttribute("listConfigDosen", edomQuestionConfigDao
                    .findByTargetTypeAndTahunAkademik(TARGET_TYPE_DOSEN,
                            tahunAkademik));
            return "setting/edom/evaluation/lecture";
        }

    }

    @GetMapping("/edom/eval-facilities")
    public String edomFacilities(Model model, Authentication authentication,
            @RequestParam(required = false) TahunAkademik tahunAkademik,
            @RequestParam(required = false) String redirect) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        Optional<EdomEvaluationTarget> target = edomEvaluationTargetDao
                .findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, tahunAkademik, "OPEN");

        List<EdomCategory> categories = edomCategoryDao
                .findCategoriesWithQuestionsWithoutResponsesForMahasiswaAndTahunAkademik(mahasiswa, tahunAkademik);
        model.addAttribute("listSarpras", categories);

        Map<String, List<EdomQuestionConfig>> configPerCategory = new HashMap<>();
        for (EdomCategory category : categories) {
            List<EdomQuestionConfig> configs = edomQuestionConfigDao
                    .findUnansweredQuestionsByMahasiswaAndTahunAndCategory(
                            mahasiswa, tahunAkademik, category);
            configPerCategory.put(category.getId(), configs);
        }
        model.addAttribute("configPerCategory", configPerCategory);
        model.addAttribute("redirect", redirect);
        model.addAttribute("tahunAkademik", tahunAkademik);
        if (target.isEmpty()) {
            return "setting/edom/evaluation/facilities";
        } else {
            return "redirect:eval-dosen?tahunAkademik=" + tahunAkademik.getId() + "&redirect=" + redirect;

        }

    }

    @PostMapping("/edom/eval-dosen/submit")
    public String submitEvaluasi(@RequestParam Map<String, String> formData, Authentication authentication,
            @RequestParam(required = false) String redirect,
            @RequestParam(required = false) TahunAkademik tahunAkademik) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        edomService.processEvaluationResponse(formData, mahasiswa, TARGET_TYPE_DOSEN, tahunAkademik);

        if ("transkrip".equals(redirect)) {
            return "redirect:../../report/" + redirect;
        } else {
            return "redirect:../../report/" + redirect + "?tahunAkademik=" + tahunAkademik.getId();
        }
    }

    @PostMapping("/edom/eval-facilities/submit")
    public String submitEvaluasiFacilities(@RequestParam Map<String, String> formData, Authentication authentication,
            @RequestParam(required = false) String redirect,
            @RequestParam(required = false) TahunAkademik tahunAkademik) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        edomService.processEvaluationResponse(formData, mahasiswa, TARGET_TYPE_SARPRAS, tahunAkademik);
        return "redirect:../eval-dosen?tahunAkademik=" + tahunAkademik.getId() + "&redirect=" + redirect;
    }

    @GetMapping("/report/edom/lecture")
    public String getLectureReport() {
        return "report/edom/lecture/list";
    }

    @GetMapping("/report/edom/facilities")
    public String getFacilitiesReport(Model model,
            @RequestParam(required = false) TahunAkademik tahunAkademik) {
        model.addAttribute("tahunAkademik", tahunAkademik);
        model.addAttribute("listTahunAkademik", edomQuestionConfigDao.findDistinctTahunAkademik());
        return "report/edom/facilities/list";
    }

}
