package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.KelasMahasiswaDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.MahasiswaDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import java.util.ArrayList;
import java.util.List;

@Controller
public class RuangController {

    @Autowired
    private RuanganDao ruanganDao;

    @Autowired
    private RuanganJenisDao ruanganJenisDao;

    @Autowired
    private GedungDao gedungDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private KelasDao kelasDao;

    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @GetMapping("/ruang/list")
    public void daftarRuang(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listRuang", ruanganDao.findByStatusNotInAndAndNamaRuanganContainingIgnoreCaseOrderByNamaRuangan(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("listRuang",ruanganDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/ruang/form")
    public void  formRuang(Model model,@RequestParam(required = false) String id){
        model.addAttribute("ruangan", new Ruangan());
        model.addAttribute("ruangJenis", ruanganJenisDao.findByStatus(StatusRecord.AKTIF));
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

    @PostMapping("/ruang/form")
    public String prosesForm(@Valid Ruangan ruangan){
        if (ruangan.getStatus() == null){
            ruangan.setStatus(StatusRecord.NONAKTIF);
        }
        ruanganDao.save(ruangan);
        return "redirect:list";
    }

    @PostMapping("/ruang/delete")
    public String deleteRuangan(@RequestParam Ruangan ruangan){
        ruangan.setStatus(StatusRecord.HAPUS);
        ruanganDao.save(ruangan);

        return "redirect:list";
    }

    @GetMapping("/kelas/mahasiswa")
    public void ruanganMahasiswa(@RequestParam(required = false) String angkatan, @RequestParam Kelas kelas, @RequestParam(required = false) Prodi prodi, Model model, Pageable page){
        model.addAttribute("selectedKelas", kelas);
        if (prodi != null){
            List<KelasMahasiswaDto> mahasiswaDtos = new ArrayList<>();
            model.addAttribute("selected", prodi);
            model.addAttribute("selectedAngkatan", angkatan);
            Iterable<String> mahasiswa = mahasiswaDao.carikelas(StatusRecord.AKTIF,angkatan,prodi);
            for (String m : mahasiswa){
                KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswaDao.findById(m).get(),StatusRecord.AKTIF);
                if (kelasMahasiswa == null){
                    Mahasiswa mhsw = mahasiswaDao.findById(m).get();
                    KelasMahasiswaDto km = new KelasMahasiswaDto();
                    km.setId(mhsw.getId());
                    km.setNama(mhsw.getNama());
                    if (mhsw.getKurikulum() != null) {
                        km.setKurikulum(mhsw.getKurikulum().getNamaKurikulum());
                    }else {
                        km.setKurikulum("");
                    }
                    km.setNim(mhsw.getNim());
                    km.setKelas("");
                    mahasiswaDtos.add(km);
                }
                if (kelasMahasiswa != null){
                    Mahasiswa mhsw = mahasiswaDao.findById(m).get();
                    KelasMahasiswaDto km = new KelasMahasiswaDto();
                    km.setId(mhsw.getId());
                    km.setNama(mhsw.getNama());
                    if (mhsw.getKurikulum() != null) {
                        km.setKurikulum(mhsw.getKurikulum().getNamaKurikulum());
                    }else {
                        km.setKurikulum("");
                    }
                    km.setNim(mhsw.getNim());
                    km.setKelas(kelasMahasiswa.getKelas().getNamaKelas());
                    mahasiswaDtos.add(km);

                }
            }
            model.addAttribute("mahasiswaList", mahasiswaDtos);
        }
    }

    @PostMapping("/kelas/mahasiswa")
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

    @GetMapping("/kelas/list")
    public void daftarKelas(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listKelas", kelasDao.findByStatusNotInAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("listKelas",kelasDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/kelas/form")
    public void  formKelas(Model model,@RequestParam(required = false) String id){
        model.addAttribute("kelas", new Kelas());


        if (id != null && !id.isEmpty()) {
            Kelas kelas = kelasDao.findById(id).get();
            if (kelas != null) {
                model.addAttribute("kelas", kelas);
                if (kelas.getStatus() == null){
                    kelas.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/kelas/form")
    public String buatKelas(@Valid Kelas kelas,@RequestParam Prodi prodi){
        if (kelas.getStatus() == null){
            kelas.setStatus(StatusRecord.NONAKTIF);
        }
        kelas.setIdProdi(prodi);
        kelasDao.save(kelas);
        return "redirect:list";
    }

    @GetMapping("/kelas/view")
    public void tampilMahasiswa(@RequestParam Kelas kelas, Model model, Pageable page){
            Iterable<KelasMahasiswa> kelasMahasiswas = kelasMahasiswaDao.findByKelasAndStatus(kelas,StatusRecord.AKTIF);
            model.addAttribute("kelasMahasiswa", kelasMahasiswas);
        }

    @PostMapping("/kelas/delete")
    public String deleteKelas(@RequestParam Kelas kelas){
        kelas.setStatus(StatusRecord.HAPUS);
        kelasDao.save(kelas);

        return "redirect:list";
    }

}
