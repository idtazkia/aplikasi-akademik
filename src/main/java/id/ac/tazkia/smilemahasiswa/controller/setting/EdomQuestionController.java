package id.ac.tazkia.smilemahasiswa.controller.setting;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import id.ac.tazkia.smilemahasiswa.dao.JadwalDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomCategoryDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomGeneralResponseDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomQuestionConfigDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomQuestionDao;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.Krs;
import id.ac.tazkia.smilemahasiswa.entity.KrsDetail;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomGeneralResponse;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomQuestion;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomQuestionConfig;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;

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

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private EdomGeneralResponseDao edomGeneralResponseDao;

    @GetMapping("/setting/edom/questions")
    public String edomQuestionList(Model model, Pageable pageable) {
        model.addAttribute("listEdomCategory", edomCategoryDao.findAll());
        model.addAttribute("listEdomQuestion", edomQuestionDao.findByStatus(StatusRecord.AKTIF, pageable));
        return "setting/edom/question/list";
    }

    @GetMapping("/setting/edom/eval-dosen")
    public String edomEvaluationList(Model model, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        model.addAttribute("listKrsDetail",
                krsDetailDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa,
                        tahunAkademikDao.findByStatus(StatusRecord.AKTIF), StatusRecord.AKTIF));
        model.addAttribute("listConfigDosen", edomQuestionConfigDao
                .findByTargetTypeAndTahunAkademik("DOSEN", tahunAkademikDao.findByStatus(StatusRecord.AKTIF)));
        return "setting/edom/evaluation/lecture";
    }

    @GetMapping("/setting/edom/configuration")
    public String edomConfigurationList(Model model) {
        model.addAttribute("akademikAktif", tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("pilihanDosen",
                edomQuestionDao.cariPertanyaanDosen(tahunAkademikDao.findByStatus(StatusRecord.AKTIF)));
        model.addAttribute("pilihanSaranaPrasarana",
                edomQuestionDao.cariPertanyaanSaranaPrasarana(tahunAkademikDao.findByStatus(StatusRecord.AKTIF)));
        model.addAttribute("listConfigDosen", edomQuestionConfigDao
                .findByTargetTypeAndTahunAkademik("DOSEN", tahunAkademikDao.findByStatus(StatusRecord.AKTIF)));
        model.addAttribute("listConfigSarpras", edomQuestionConfigDao
                .findByTargetTypeAndTahunAkademik("SARPRAS", tahunAkademikDao.findByStatus(StatusRecord.AKTIF)));
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

    @PostMapping("/edom/eval-dosen/submit")
    public String submitEvaluasi(@RequestParam Map<String, String> formData, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("evaluasi["))
                continue;

            String inner = key.substring(9, key.length() - 1);
            String[] parts = inner.split("]\\[");

            if (parts.length != 2)
                continue;

            String jadwalId = parts[0];
            String questionId = parts[1];
            int nilai = Integer.parseInt(entry.getValue());
            Jadwal jadwal = jadwalDao.findById(jadwalId).get();
            EdomQuestion edomQuestion = edomQuestionDao.findById(questionId).get();
            KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatus(mahasiswa, jadwal, StatusRecord.AKTIF);

            EdomGeneralResponse response = new EdomGeneralResponse();
            response.setTarget("DOSEN");
            response.setMahasiswa(mahasiswa);
            response.setQuestion(edomQuestion);
            response.setKrsDetail(krsDetail);
            response.setJadwal(jadwal);
            response.setResponseValue(String.valueOf(nilai));
            edomGeneralResponseDao.save(response);

            krsDetail.setStatusEdom(StatusRecord.DONE);
            krsDetailDao.save(krsDetail);
        }

        return "redirect:/setting/edom/eval-dosen";
    }

}
