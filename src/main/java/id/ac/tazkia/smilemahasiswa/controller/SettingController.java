package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SettingController {
    @Autowired
    private ProgramDao programDao;

    @Autowired
    private JenjangDao jenjangDao;

    @Autowired
    private EdomQuestionDao edomQuestionDao;

    @Autowired
    private FakultasDao fakultasDao;
    
    @Autowired
    private LembagaDao lembagaDao;
    
    @Autowired
    private ProdiDao prodiDao;
    
    @Autowired
    private JurusanDao jurusanDao;
    
    @Autowired
    private KonsentrasiDao konsentrasiDao;

    @Autowired
    private DosenDao dosenDao;

//    Attribute

    @ModelAttribute("dosen")
    public Iterable<Dosen> dosen() {
        return dosenDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }

//setting/program

    @GetMapping("/setting/program/list")
    public void daftarProgram(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("daftarProgram", programDao.findByStatusNotInAndNamaProgramContainingIgnoreCaseOrderByNamaProgram(Arrays.asList((StatusRecord.HAPUS)),search,page));
        } else {
            model.addAttribute("daftarProgram",programDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS),page));

        }
    }

    @GetMapping("/setting/program/form")
    public void formProgram(Model model,@RequestParam(required = false) String id){
        model.addAttribute("program", new Program());

        if (id != null && !id.isEmpty()) {
            Program program = programDao.findById(id).get();
            if (program != null) {
                model.addAttribute("program", program);
                if (program.getStatus() == null){
                    program.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/setting/program/form")
    public String prosesForm(@Valid Program program){
        if (program.getStatus() == null){
            program.setStatus(StatusRecord.AKTIF);
        }
        programDao.save(program);
        return "redirect:list";
    }

    @PostMapping("/setting/program/delete")
    public String deleteProgram(@RequestParam Program program){
        program.setStatus(StatusRecord.HAPUS);
        programDao.save(program);

        return "redirect:list";
    }

//    Level

    @GetMapping("/setting/level/list")
    public void daftarJenjang(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", jenjangDao.findByStatusNotInAndNamaJenjangContainingIgnoreCaseOrderByNamaJenjang(Arrays.asList(StatusRecord.HAPUS), search, page));
        } else {
            model.addAttribute("list",jenjangDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS),page));
        }
    }

    @GetMapping("/setting/level/form")
    public void jenjangForm(Model model,@RequestParam(required = false) String id){
        model.addAttribute("jenjang", new Jenjang());

        if (id != null && !id.isEmpty()) {
            Jenjang jenjang = jenjangDao.findById(id).get();
            if (jenjang != null) {
                model.addAttribute("jenjang", jenjang);
                if (jenjang.getStatus() == null){
                    jenjang.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/setting/level/form")
    public String prosesForm(@Valid Jenjang jenjang){
        if (jenjang.getStatus() == null){
            jenjang.setStatus(StatusRecord.NONAKTIF);
        }
        jenjangDao.save(jenjang);
        return "redirect:list";
    }

    @PostMapping("/setting/level/delete")
    public String deleteJenjang(@RequestParam Jenjang jenjang){
        jenjang.setStatus(StatusRecord.HAPUS);
        jenjangDao.save(jenjang);

        return "redirect:list";
    }

//    Faculty

    @GetMapping("/setting/faculty/list")
    public void daftarFakultas(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listFakultas", fakultasDao.findByStatusNotInAndAndNamaFakultasContainingIgnoreCaseOrderByNamaFakultas(Arrays.asList(StatusRecord.HAPUS), search, page));
        } else {
            model.addAttribute("listFakultas",fakultasDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS),page));

        }
    }

    @GetMapping("/setting/faculty/form")
    public void  formFakultas(Model model,@RequestParam(required = false) String id){
        model.addAttribute("fakultas", new Fakultas());
        model.addAttribute("lembaga",lembagaDao.findByStatus(StatusRecord.AKTIF));

        if (id != null && !id.isEmpty()) {
            Fakultas fakultas = fakultasDao.findById(id).get();
            if (fakultas != null) {
                model.addAttribute("fakultas", fakultas);
                if (fakultas.getStatus() == null){
                    fakultas.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/setting/faculty/form")
    public String prosesForm(@Valid Fakultas fakultas){
        if (fakultas.getStatus() == null){
            fakultas.setStatus(StatusRecord.NONAKTIF);
        }
        fakultasDao.save(fakultas);
        return "redirect:list";
    }

    @PostMapping("/setting/faculty/delete")
    public String deletefakultas(@RequestParam Fakultas fakultas){
        fakultas.setStatus(StatusRecord.HAPUS);
        fakultasDao.save(fakultas);

        return "redirect:list";
    }

//    Major
@GetMapping("/setting/major/list")
public void daftarJurusan(Model model, @PageableDefault(size = 10) Pageable page, String search){
    if (StringUtils.hasText(search)) {
        model.addAttribute("search", search);
        model.addAttribute("listJurusan", jurusanDao.findByStatusNotInAndNamaJurusanContainingIgnoreCaseOrderByNamaJurusan(Arrays.asList(StatusRecord.HAPUS), search, page));
    } else {
        model.addAttribute("listJurusan",jurusanDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS),page));

    }
}

    @GetMapping("/setting/major/form")
    public void  formJurusan(Model model,@RequestParam(required = false) String id){
        model.addAttribute("jurusan", new Jurusan());

        if (id != null && !id.isEmpty()) {
            Jurusan jurusan = jurusanDao.findById(id).get();
            if (jurusan != null) {
                model.addAttribute("jurusan", jurusan);
                if (jurusan.getStatus() == null){
                    jurusan.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }

        model.addAttribute("listFakultas",fakultasDao.findByStatus(StatusRecord.AKTIF));


    }

    @PostMapping("/setting/major/form")
    public String prosesFormJurusan(@Valid Jurusan jurusan){
        if (jurusan.getStatus() == null){
            jurusan.setStatus(StatusRecord.NONAKTIF);
        }
        jurusanDao.save(jurusan);
        return "redirect:list";
    }

    @PostMapping("/setting/major/delete")
    public String deleteJurusan(@RequestParam Jurusan jurusan){
        jurusan.setStatus(StatusRecord.HAPUS);
        jurusanDao.save(jurusan);

        return "redirect:list";
    }

//    Prody
@GetMapping("/setting/prody/list")
public void daftarProgramStudi(Model model, @PageableDefault(size = 10) Pageable page, String search){

    if (StringUtils.hasText(search)) {
        model.addAttribute("search", search);
        model.addAttribute("list", prodiDao.findByStatusNotInAndAndNamaProdiContainingIgnoreCaseOrderByNamaProdi(Arrays.asList(StatusRecord.HAPUS), search, page));
    } else {
        model.addAttribute("list",prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS),page));

    }
}

    @GetMapping("/setting/prody/form")
    public void  formProgramStudi(Model model,@RequestParam(required = false) String id){
        model.addAttribute("programStudy", new Prodi());
            model.addAttribute("listFakultas",fakultasDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("jenjang",jenjangDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("program", programDao.findByStatus(StatusRecord.AKTIF));

        if (id != null && !id.isEmpty()) {
            Prodi prodi = prodiDao.findById(id).get();
            if (prodi != null) {
                model.addAttribute("programStudy", prodi);
                if (prodi.getStatus() == null){
                    prodi.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/setting/prody/form")
    public String prosesForm(@Valid Prodi prodi){
        if (prodi.getStatus() == null){
            prodi.setStatus(StatusRecord.NONAKTIF);
        }
        prodiDao.save(prodi);
        return "redirect:list";
    }

    @PostMapping("/setting/prody/delete")
    public String deleteJenjang(@RequestParam Prodi prodi){
        prodi.setStatus(StatusRecord.HAPUS);
        prodiDao.save(prodi);

        return "redirect:list";
    }

//    Concentration

    @GetMapping("/setting/concentration/list")
    public void daftarKonsentrasi(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("daftarKonsentrasi", konsentrasiDao.findByStatusNotInAndAndNamaKonsentrasiContainingIgnoreCaseOrderByNamaKonsentrasi(Arrays.asList(StatusRecord.HAPUS), search, page));
        } else {
            model.addAttribute("daftarKonsentrasi",konsentrasiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS),page));

        }
    }

    @GetMapping("/setting/concentration/form")
    public void  formKonsentrasi(Model model,@RequestParam(required = false) String id){
        model.addAttribute("konsentrasi", new Konsentrasi());
        model.addAttribute("prodi",prodiDao.findByStatus(StatusRecord.AKTIF));

        if (id != null && !id.isEmpty()) {
            Konsentrasi konsentrasi = konsentrasiDao.findById(id).get();
            if (konsentrasi != null) {
                model.addAttribute("konsentrasi", konsentrasi);
                if (konsentrasi.getStatus() == null){
                    konsentrasi.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/setting/concentration/form")
    public String prosesForm(@Valid Konsentrasi konsentrasi){
        if (konsentrasi.getStatus() == null){
            konsentrasi.setStatus(StatusRecord.NONAKTIF);
        }
        konsentrasiDao.save(konsentrasi);
        return "redirect:list";
    }

    @PostMapping("/setting/concentration/delete")
    public String deletekonsentrasi(@RequestParam Konsentrasi konsentrasi){
        konsentrasi.setStatus(StatusRecord.HAPUS);
        konsentrasiDao.save(konsentrasi);

        return "redirect:list";
    }

    @GetMapping("/setting/edomquestion/list")
    public void listEdom(Model model){
        model.addAttribute("listEdomQuestion", edomQuestionDao.findByStatusOrderByNomor(StatusRecord.AKTIF));
    }
}
