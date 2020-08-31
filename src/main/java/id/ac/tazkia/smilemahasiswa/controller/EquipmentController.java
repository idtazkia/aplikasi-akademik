package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.room.KelasMahasiswaDto;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class EquipmentController {

    @Autowired
    private LembagaDao lembagaDao;

    @Autowired
    private GedungDao gedungDao;

    @Autowired
    private KampusDao kampusDao;

    @Autowired
    private KelasDao kelasDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private RuangJenisDao ruangJenisDao;

    @Autowired
    private RuanganDao ruanganDao;

    @Autowired
    private BahasaDao bahasaDao;

    @Autowired
    private JadwalDao jadwalDao;

    //    Attribute
    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }

//    Institution

    @GetMapping("/equipment/institution/list")
    public void curriculumList(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("list", lembagaDao.findByStatusNotInAndNamaLembagaContainingIgnoreCaseOrderByNamaLembaga(Arrays.asList(StatusRecord.HAPUS), search,page));
        }else {
            model.addAttribute("list", lembagaDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/equipment/institution/form")
    public void curriculumForm(Model model, @RequestParam(required = false) String id){
        model.addAttribute("lembaga", new Lembaga());

        if (id != null && !id.isEmpty()){
            Lembaga lembaga = lembagaDao.findById(id).get();
            if (lembaga != null){
                model.addAttribute("lembaga", lembaga);
                if (lembaga.getStatus() == null){
                    lembaga.setStatus(StatusRecord.AKTIF);
                }
            }
        }
    }

    @PostMapping("/equipment/institution/form")
    public String prosesInstitution(@Valid Lembaga lembaga){
        if (lembaga.getStatus() == null){
            lembaga.setStatus(StatusRecord.AKTIF);
        }
        lembagaDao.save(lembaga);

        return "redirect:list";

    }

    @PostMapping("/equipment/institution/delete")
    public String deleteInnstitution(@RequestParam Lembaga lembaga){
        lembaga.setStatus(StatusRecord.HAPUS);
        lembagaDao.save(lembaga);

        return "redirect:list";
    }

//    Building

    @GetMapping("/equipment/building/list")
    public void daftarGedung(Model model, @RequestParam(required = false) String search, Pageable page){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", gedungDao.findByStatusNotInAndAndNamaGedungContainingIgnoreCaseOrderByNamaGedung(Arrays.asList(StatusRecord.HAPUS), search, page));
        } else {
            model.addAttribute("list",gedungDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS),page));
        }
    }

    @GetMapping("/equipment/building/form")
    public void gedungForm(Model model,@RequestParam(required = false) String id){
        model.addAttribute("gedung", new Gedung());
        model.addAttribute("kampus", kampusDao.findByStatus(StatusRecord.AKTIF));

        if (id != null && !id.isEmpty()) {
            Gedung gedung = gedungDao.findById(id).get();
            if (gedung != null) {
                model.addAttribute("gedung", gedung);
                if (gedung.getStatus() == null){
                    gedung.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }


    @PostMapping("/equipment/building/form")
    public String prosesForm(@Valid Gedung gedung){
        if (gedung.getStatus() == null){
            gedung.setStatus(StatusRecord.NONAKTIF);
        }
        gedungDao.save(gedung);
        return "redirect:list";
    }

    @PostMapping("/equipment/building/delete")
    public String deleteLembaga(@RequestParam Gedung gedung){
        gedung.setStatus(StatusRecord.HAPUS);
        gedungDao.save(gedung);

        return "redirect:list";
    }

//    Campus

    @GetMapping("/equipment/campus/list")
    public void kampusList(Model model, @PageableDefault(size = 10)Pageable page, String search){
        if (StringUtils.hasText(search)){
            model.addAttribute("seacrh", search);
            model.addAttribute("list", kampusDao.findByStatusNotInAndNamaKampusContainingIgnoreCaseOrderByNamaKampus(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else{
            model.addAttribute("list", kampusDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/equipment/campus/form")
    public void formKampus(Model model,@RequestParam(required = false) String id){
        model.addAttribute("kampus", new Kampus());

        if (id != null && !id.isEmpty()) {
            Kampus kampus = kampusDao.findById(id).get();
            if (kampus != null) {
                model.addAttribute("kampus", kampus);
                if (kampus.getStatus() == null){
                    kampus.setStatus(StatusRecord.AKTIF);
                }
            }
        }
    }

    @PostMapping("/equipment/campus/form")
    public String prosesForm(@Valid Kampus kampus){
        if (kampus.getStatus() == null){
            kampus.setStatus(StatusRecord.NONAKTIF);
        }
        kampusDao.save(kampus);
        return "redirect:list";
    }

    @PostMapping("/equipment/campus/delete")
    public String deleteKampus(@RequestParam Kampus kampus){
        kampus.setStatus(StatusRecord.HAPUS);
        kampusDao.save(kampus);

        return "redirect:list";
    }

//    Room
    @GetMapping("/equipment/room/list")
    public void roomList(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listRuang", ruanganDao.findByStatusNotInAndAndNamaRuanganContainingIgnoreCaseOrderByNamaRuangan(Arrays.asList(StatusRecord.HAPUS), search, page));
        } else {
            model.addAttribute("listRuang",ruanganDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS),page));

        }
    }

    @GetMapping("/equipment/room/form")
    public void roomForm(Model model,@RequestParam(required = false) String id){
        model.addAttribute("ruangan", new Ruangan());
        model.addAttribute("ruangJenis", ruangJenisDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("gedung", gedungDao.findByStatus(StatusRecord.AKTIF));


        if (id != null && !id.isEmpty()) {
            Ruangan ruangan = ruanganDao.findById(id).get();
            if (ruangan != null) {
                model.addAttribute("ruangan", ruangan);
                if (ruangan.getStatus() == null){
                    ruangan.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/equipment/room/form")
    public String prosesForm(@Valid Ruangan ruangan){
        if (ruangan.getStatus() == null){
            ruangan.setStatus(StatusRecord.NONAKTIF);
        }
        ruanganDao.save(ruangan);
        return "redirect:list";
    }

    @PostMapping("/equipment/room/delete")
    public String deleteRuangan(@RequestParam Ruangan ruangan){
        ruangan.setStatus(StatusRecord.HAPUS);
        ruanganDao.save(ruangan);

        return "redirect:list";
    }


//    class
    @GetMapping("/equipment/class/list")
    public void classList(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listKelas", kelasDao.findByStatusNotInAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(Arrays.asList(StatusRecord.HAPUS), search, page));
        } else {
            model.addAttribute("listKelas",kelasDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS) ,page));
        }

    }

    @GetMapping("/equipment/class/form")
    public void classForm(Model model,@RequestParam(required = false) String id){
        model.addAttribute("kelas", new Kelas());


        if (id != null && !id.isEmpty()) {
            Kelas kelas = kelasDao.findById(id).get();
            if (kelas != null) {
                model.addAttribute("bahasa", bahasaDao.findByStatusOrderByBahasa(StatusRecord.AKTIF));
                model.addAttribute("kelas", kelas);
                if (kelas.getStatus() == null){
                    kelas.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/equipment/class/form")
    public String prosesClass(@Valid Kelas kelas, @RequestParam Prodi prodi,@RequestParam Kurikulum kurikulum){
        if (kelas.getStatus() == null){
            kelas.setStatus(StatusRecord.NONAKTIF);
        }
        kelas.setKurikulum(kurikulum);
        kelas.setIdProdi(prodi);
        kelasDao.save(kelas);
        return "redirect:list";
    }

    @PostMapping("/equipment/class/delete")
    public String deleteKelas(@RequestParam Kelas kelas,
                              RedirectAttributes attributes){

        Double jmlJdwal = jadwalDao.jmlJadwal(kelas.getId());

        if (jmlJdwal > 0){
            attributes.addFlashAttribute("gagal", "Save Data Berhasil");
            return "redirect:list";
        }else{
            kelas.setStatus(StatusRecord.HAPUS);
            kelasDao.save(kelas);
        }
        attributes.addFlashAttribute("success", "Save Data Berhasil");
        return "redirect:list";

    }

    @GetMapping("/equipment/class/view")
    public void viewMahasiswa(@RequestParam Kelas kelas, Model model, Pageable page){
        Iterable<KelasMahasiswa> kelasMahasiswas = kelasMahasiswaDao.findByKelasAndStatus(kelas,StatusRecord.AKTIF);
        model.addAttribute("kelasMahasiswa", kelasMahasiswas);
    }

    @GetMapping("/equipment/class/mahasiswa")
    public void ruanganMahasiswa(@RequestParam(required = false) String angkatan, @RequestParam Kelas kelas, @RequestParam(required = false) Prodi prodi, Model model, Pageable page){
        model.addAttribute("selectedKelas", kelas);
        if (prodi != null) {
            List<KelasMahasiswaDto> mahasiswaDtos = new ArrayList<>();
            model.addAttribute("selected", prodi);
            model.addAttribute("selectedAngkatan", angkatan);
            Iterable<Mahasiswa> mahasiswa = mahasiswaDao.carikelas(StatusRecord.AKTIF, angkatan, prodi);

            if (mahasiswa != null){
                for (Mahasiswa m : mahasiswa) {
                    KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(m, StatusRecord.AKTIF);
                    if (kelasMahasiswa == null) {
                        KelasMahasiswaDto km = new KelasMahasiswaDto();
                        km.setId(m.getId());
                        km.setNama(m.getNama());
                        if (m.getKurikulum() != null) {
                            km.setKurikulum(m.getKurikulum().getNamaKurikulum());
                        } else {
                            km.setKurikulum("");
                        }
                        km.setNim(m.getNim());
                        km.setKelas("");
                        mahasiswaDtos.add(km);
                    }
                    if (kelasMahasiswa != null) {
                        KelasMahasiswaDto km = new KelasMahasiswaDto();
                        km.setId(m.getId());
                        km.setNama(m.getNama());
                        if (m.getKurikulum() != null) {
                            km.setKurikulum(m.getKurikulum().getNamaKurikulum());
                        } else {
                            km.setKurikulum("");
                        }
                        km.setNim(m.getNim());
                        km.setKelas(kelasMahasiswa.getKelas().getNamaKelas());
                        mahasiswaDtos.add(km);

                    }
                }
        }
            model.addAttribute("mahasiswaList", mahasiswaDtos);
        }
    }

    @PostMapping("/equipment/class/mahasiswa")
    public String saveruanganMahasiswa(@RequestParam String[] data, @RequestParam Kelas kelas){

        for (String idMahasiswa : data){
            Mahasiswa mahasiswa = mahasiswaDao.findById(idMahasiswa).get();
            KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndKelas(mahasiswa,kelas);
            if (kelasMahasiswa != null){
                KelasMahasiswa kelasMhsw = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa,StatusRecord.AKTIF);
                if (kelasMhsw != null){
                    kelasMhsw.setStatus(StatusRecord.NONAKTIF);
                    kelasMahasiswaDao.save(kelasMhsw);
                }
                kelasMahasiswa.setStatus(StatusRecord.AKTIF);
                kelasMahasiswaDao.save(kelasMahasiswa);

            }else {
                KelasMahasiswa kelasMhsw = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa,StatusRecord.AKTIF);
                if (kelasMhsw != null) {
                    kelasMhsw.setStatus(StatusRecord.NONAKTIF);
                    kelasMahasiswaDao.save(kelasMhsw);
                }
                KelasMahasiswa km = new KelasMahasiswa();
                km.setStatus(StatusRecord.AKTIF);
                km.setKelas(kelas);
                km.setMahasiswa(mahasiswa);
                kelasMahasiswaDao.save(km);
            }


        }
        return "redirect:list";

    }

}
