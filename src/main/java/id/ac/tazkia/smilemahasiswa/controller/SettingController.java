package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    @Autowired
    private BeasiswaDao beasiswaDao;

    @Autowired
    private TagihanBeasiswaDao tagihanBeasiswaDao;

    @Autowired
    private JenisTagihanDao jenisTagihanDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private MahasiswaBeasiswaDao mahasiswaBeasiswaDao;

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

    @GetMapping("/setting/beasiswa/list")
    public void listBeasiswa(Model model){
        model.addAttribute("beasiswa", beasiswaDao.findByStatusOrderByNamaBeasiswa(StatusRecord.AKTIF));
    }

    @GetMapping("/setting/beasiswa/form")
    public void formBeasiswa(Model model,@RequestParam(required = false) String id){
        model.addAttribute("beasiswa", new Beasiswa());

        if (id != null && !id.isEmpty()) {
            Beasiswa Beasiswa = beasiswaDao.findById(id).get();
            if (Beasiswa != null) {
                model.addAttribute("beasiswa", Beasiswa);
//                if (Beasiswa.getStatus() == null){
//                    Beasiswa.setStatus(StatusRecord.NONAKTIF);
//                }
            }
        }

    }

    @PostMapping("/setting/beasiswa/form")
    public String prosesBeasiswa(@Valid Beasiswa beasiswa){
        beasiswa.setStatus(StatusRecord.AKTIF);
        beasiswaDao.save(beasiswa);
        return "redirect:list";
    }

    @PostMapping("/setting/beasiswa/delete")
    public String deleteBeasiswa(@RequestParam Beasiswa beasiswa){
        beasiswa.setStatus(StatusRecord.HAPUS);
        beasiswaDao.save(beasiswa);
        return "redirect:list";
    }

    @GetMapping("/setting/beasiswa/tagihan")
    public void tagihanList(Model model, @RequestParam Beasiswa id){
        model.addAttribute("id", id.getId());
        model.addAttribute("beasiswa", id);
        List<TagihanBeasiswa> tBeasiswa = tagihanBeasiswaDao.findByBeasiswaAndStatus(id, StatusRecord.AKTIF);
        List<String> idTagihan = new ArrayList<>();
        List<JenisTagihan> listTagihan = null;
        for (TagihanBeasiswa t : tBeasiswa){
            idTagihan.add(t.getJenisTagihan().getId());
        }
        if (idTagihan.isEmpty()){
            listTagihan = jenisTagihanDao.findByStatusOrderByKode(StatusRecord.AKTIF);
        }else {
            listTagihan = jenisTagihanDao.findByStatusAndIdNotInOrderByKode(StatusRecord.AKTIF, idTagihan);
        }
        model.addAttribute("listTagihanBeasiswa", tBeasiswa);
        model.addAttribute("listJenisTagihan", listTagihan);
    }

    @PostMapping("/setting/beasiswa/tagihan")
    public String tagihanBeasiswaPost(@RequestParam String[] selected, @RequestParam String idBeasiswa, Authentication authentication){
        Beasiswa beasiswa = beasiswaDao.findById(idBeasiswa).get();
        for (String idJenis : selected){
            JenisTagihan jenisTagihan = jenisTagihanDao.findById(idJenis).get();
            TagihanBeasiswa tagihanBeasiswa = new TagihanBeasiswa();
            tagihanBeasiswa.setBeasiswa(beasiswa);
            tagihanBeasiswa.setJenisTagihan(jenisTagihan);
            tagihanBeasiswa.setStatus(StatusRecord.AKTIF);

            tagihanBeasiswaDao.save(tagihanBeasiswa);
        }
        return "redirect:/setting/beasiswa/tagihan?id=" + idBeasiswa;
    }

    @PostMapping("/setting/beasiswa/potongan/persen")
    public String potonganPersen(@RequestParam TagihanBeasiswa tagihan, @RequestParam(required = false) Integer potongan){

        tagihan.setJenisPotongan("PERSEN");
        tagihan.setPotongan(potongan);
        tagihanBeasiswaDao.save(tagihan);

        return "redirect:../tagihan?id="+tagihan.getBeasiswa().getId();
    }

    @PostMapping("/setting/beasiswa/potongan/jumlah")
    public String potonganJumlah(@RequestParam TagihanBeasiswa tagihan, @RequestParam(required = false) Integer jumlah){

        tagihan.setJenisPotongan("JUMLAH");
        tagihan.setPotongan(jumlah);
        tagihanBeasiswaDao.save(tagihan);

        return "redirect:../tagihan?id="+tagihan.getBeasiswa().getId();
    }

    @PostMapping("/setting/beasiswa/deletetagihan")
    public String tagihanBeasiswaHapus(@RequestParam TagihanBeasiswa tagihanBeasiswa, Authentication authentication){
        tagihanBeasiswa.setStatus(StatusRecord.HAPUS);
        tagihanBeasiswaDao.save(tagihanBeasiswa);
        return "redirect:list";
    }

    @GetMapping("/setting/beasiswa/mahasiswa")
    public void listMahasiswa(Model model,@RequestParam String id){
        Beasiswa beasiswa = beasiswaDao.findById(id).get();
        model.addAttribute("beasiswa", beasiswa);
        model.addAttribute("mahasiswa", beasiswaDao.listBeasiswaMahasiwa(id));
        model.addAttribute("listMahasiswa", mahasiswaDao.findByStatusAndStatusAktifAndBeasiswaIsNull(StatusRecord.AKTIF, "AKTIF"));
    }

    @PostMapping("/setting/beasiswa/mahasiswa")
    public String inputMahasiswaBeasiswa(@RequestParam(required = false) String[] mahasiswa, @RequestParam(required = false) String tanggalMulai, @RequestParam(required = false) String beasiswa){
        Beasiswa b = beasiswaDao.findById(beasiswa).get();
        LocalDate mulai = LocalDate.parse(tanggalMulai);
        for (String m : mahasiswa){
            Mahasiswa mhs = mahasiswaDao.findById(m).get();
            MahasiswaBeasiswa mBeasiswa = new MahasiswaBeasiswa();
            mBeasiswa.setMahasiswa(mhs);
            mBeasiswa.setBeasiswa(b);
            mBeasiswa.setTanggalMulaiBerlaku(mulai);
            mahasiswaBeasiswaDao.save(mBeasiswa);

            mhs.setBeasiswa(b);
            mahasiswaDao.save(mhs);

        }

        return "redirect:mahasiswa?id="+b.getId();

    }

    @PostMapping("/setting/beasiswa/mahasiswa/delete")
    public String deleteMahasiswaBeasiswa(@RequestParam MahasiswaBeasiswa mahasiswa){

        mahasiswa.setStatus(StatusRecord.HAPUS);
        mahasiswaBeasiswaDao.save(mahasiswa);

        Mahasiswa m = mahasiswaDao.findByNim(mahasiswa.getMahasiswa().getNim());
        m.setBeasiswa(null);
        mahasiswaDao.save(m);

        return "redirect:../mahasiswa?id="+mahasiswa.getBeasiswa().getId();

    }

}
