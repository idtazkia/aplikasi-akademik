package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.MatakuliahKurikulumDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.PlotingDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class JadwalKuliahController {

    @Autowired
    private ProgramDao programDao;

    @Autowired
    private MatakuliahKurikulumDao matakuliahKurikulumDao;

    @Autowired
    private TahunAkademikProdiDao tahunAkademikProdiDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private KelasDao kelasDao;

    @Autowired
    private RuanganDao ruanganDao;

    @Autowired
    private HariDao hariDao;

    @Autowired
    private SesiDao sesiDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    KurikulumDao kurikulumDao;

    @Autowired
    JadwalDosenDao jadwalDosenDao;

    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;

    @GetMapping("/api/plotKelas")
    @ResponseBody
    public Page<Kelas> cariDataKelas(@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return kelasDao.findAll(page);
        }
        return kelasDao.findByStatusAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(StatusRecord.AKTIF,search, page);

    }

    @GetMapping("/api/hari")
    @ResponseBody
    public Iterable<Hari> cariHari(@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return hariDao.findAll();
        }
        return hariDao.findByNamaHariContainingIgnoreCaseOrNamaHariEngContainingIgnoreCase(search,search);

    }

    @GetMapping("/api/waktu")
    @ResponseBody
    public Sesi sesi(@RequestParam(required = false) String id){


        return sesiDao.findById(id).get();

    }

    @GetMapping("/api/ruangan")
    @ResponseBody
    public Iterable<Ruangan> cariRuangan(@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return ruanganDao.findAll();
        }
        return ruanganDao.findByStatusAndNamaRuanganContainingIgnoreCase(StatusRecord.AKTIF,search);

    }

    @GetMapping("/api/sesi")
    @ResponseBody
    public List<Sesi> cariSesi(@RequestParam(required = false) String idHari,@RequestParam(required = false) String kelas,
                               @RequestParam(required = false) String idRuangan, @RequestParam(required = false) Integer sks,
                               @RequestParam(required = false) String search, @RequestParam(required = false) String dosen){
        Ruangan ruangan = ruanganDao.findById(idRuangan).get();
        Kelas k = kelasDao.findById(kelas).get();
        Hari hari = hariDao.findById(idHari).get();
        Dosen d = dosenDao.findById(dosen).get();
        List<String> jadwal = jadwalDao.cariSesi(tahunAkademikDao.findByStatus(StatusRecord.AKTIF),hari,ruangan,StatusRecord.AKTIF);
        List<String> cariKelas = jadwalDao.cariKelas(tahunAkademikDao.findByStatus(StatusRecord.AKTIF),hari,k,StatusRecord.AKTIF);
        List<String> validasiDosen = jadwalDao.validasiDosen(tahunAkademikDao.findByStatus(StatusRecord.AKTIF),hari,d,StatusRecord.AKTIF);
        List<String> stringList = new ArrayList<>();
        stringList.addAll(jadwal);
        stringList.addAll(cariKelas);
        stringList.addAll(validasiDosen);

        List<String> newList = stringList.stream()
                .distinct()
                .collect(Collectors.toList());
        System.out.println(newList);
        System.out.println(validasiDosen);


        if (newList == null || newList.isEmpty()){
            return sesiDao.findBySks(sks);
        }else {
            return sesiDao.findBySesiNotInAndSks(newList, sks);
        }
    }

    @GetMapping("/api/plotMatakuliah")
    @ResponseBody
    public Page<MatakuliahKurikulum> cariMatakuliah(@RequestParam(required = false) String id,@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return matakuliahKurikulumDao.findAll(page);
        }
        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findFirstByKelasAndStatusAndMahasiswaKurikulumNotNull(kelasDao.findById(id).get(),StatusRecord.AKTIF);
        Kurikulum kurikulum = kurikulumDao.findByIdAndStatus(kelasMahasiswa.getMahasiswa().getKurikulum().getId(),StatusRecord.AKTIF);
        return matakuliahKurikulumDao.findByMatakuliahNamaMatakuliahContainingIgnoreCaseAndKurikulumAndStatusOrMatakuliahNamaMatakuliahEnglishContainingIgnoreCaseAndKurikulumAndStatus(search,kurikulum,StatusRecord.AKTIF,search,kurikulum,StatusRecord.AKTIF,page);

    }

    @GetMapping("/api/plotingMatkul")
    @ResponseBody
    public List<MatakuliahKurikulumDto> cariMatakuliah(@RequestParam(required = false) String id){
        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findFirstByKelasAndStatusAndMahasiswaKurikulumNotNull(kelasDao.findById(id).get(),StatusRecord.AKTIF);
        if (kelasMahasiswa == null){
            return kelasMatkul(id);
        }
        Kurikulum kurikulum = kurikulumDao.findByIdAndStatus(kelasMahasiswa.getMahasiswa().getKurikulum().getId(),StatusRecord.AKTIF);
        return matakuliahKurikulumDao.cariMk(StatusRecord.AKTIF,kurikulum);
    }

    @GetMapping("/api/kelasMatkul")
    @ResponseBody
    public List<MatakuliahKurikulumDto> kelasMatkul(@RequestParam(required = false) String id){
        Kelas kelas = kelasDao.findById(id).get();
        if (kelas.getKurikulum() == null){
            return null;
        }
        Kurikulum kurikulum = kurikulumDao.findByIdAndStatus(kelas.getKurikulum().getId(),StatusRecord.AKTIF);
        System.out.println(kurikulum.getNamaKurikulum());
        return matakuliahKurikulumDao.cariMk(StatusRecord.AKTIF,kurikulum);
    }

    @ModelAttribute("dosen")
    public Iterable<Dosen> dosen() {
        return dosenDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("ruangan")
    public Iterable<Ruangan> ruangan() {
        return ruanganDao.findByStatus(StatusRecord.AKTIF);
    }

    @ModelAttribute("kelas")
    public Iterable<Kelas> kelas() {
        return kelasDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademikProdi> tahunAkademik() {
        return tahunAkademikProdiDao.findByStatusNotInOrderByTahunAkademikDesc(StatusRecord.HAPUS);
    }

    @ModelAttribute("hari")
    public Iterable<Hari> hari() {
        return hariDao.findAll();
    }

    @ModelAttribute("program")
    public Iterable<Program> program() {
        return programDao.findByStatusNotIn(StatusRecord.HAPUS);
    }


    @GetMapping("/plotingdosen/list")
    public void plotList(Model model,@PageableDefault(direction = Sort.Direction.DESC) Pageable pageable,
                           @RequestParam(required = false)Program program,
                            @RequestParam(required = false) TahunAkademikProdi tahunAkademik){

        if (program != null && tahunAkademik != null){
            model.addAttribute("selectedTahun",tahunAkademik);
            model.addAttribute("selectedProgram",program);
            List<PlotingDto> plotingDto = jadwalDao.ploting(tahunAkademik.getProdi(),StatusRecord.HAPUS,tahunAkademik);
            List<PlotingDto> ploting = jadwalDao.plotingKosong(tahunAkademik.getProdi(),StatusRecord.HAPUS,tahunAkademik);

            List<PlotingDto> combinedList = new ArrayList<>();
            combinedList.addAll(ploting);
            combinedList.addAll(plotingDto);
            List<PlotingDto> newList = combinedList.stream()
                    .distinct()
                    .collect(Collectors.toList());
            model.addAttribute("team",jadwalDosenDao.cariTeam(tahunAkademik.getTahunAkademik(),StatusJadwalDosen.TEAM));

            model.addAttribute("jadwal", newList);
            model.addAttribute("kelas", kelasDao.findByStatus(StatusRecord.AKTIF));


        }
    }

    @PostMapping("/plotingdosen/delete")
    public String deleteJadwal(@RequestParam Jadwal jadwal){
        jadwal.setStatus(StatusRecord.HAPUS);
        jadwalDao.save(jadwal);
        return "redirect:list?tahunAkademik="+jadwal.getTahunAkademikProdi().getId()+"&program="+jadwal.getProgram().getId();
    }

    @PostMapping("/plotingdosen/list")
    public String buatPlot(@ModelAttribute @Valid Jadwal jadwal){
        jadwal.setJumlahSesi(1);
        jadwal.setBobotUts(BigDecimal.ZERO);
        jadwal.setBobotUas(BigDecimal.ZERO);
        jadwal.setBobotPresensi(BigDecimal.ZERO);
        jadwal.setBobotTugas(BigDecimal.ZERO);
        jadwalDao.save(jadwal);

        return "redirect:list?tahunAkademik="+jadwal.getTahunAkademikProdi().getId()+"&program="+jadwal.getProgram().getId();
    }

    @GetMapping("/jadwalkuliah/list")
    public void jadwalList(Model model, @RequestParam(required = false) Program program,
                           @RequestParam(required = false) TahunAkademikProdi tahunAkademik,
                           @RequestParam(required = false) Hari hari){
        model.addAttribute("selectedTahun",tahunAkademik);
        model.addAttribute("selectedHari", hari);
        model.addAttribute("selectedProgram",program);

        if (program != null && tahunAkademik != null && hari != null){
            model.addAttribute("jadwal", jadwalDao.schedule(tahunAkademik.getProdi(),StatusRecord.HAPUS,tahunAkademik,hari,program));
            model.addAttribute("ploting", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndHariNullAndJamMulaiNullAndJamSelesaiNullAndKelasNotNull(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik));
        }


        if (program != null && tahunAkademik != null && hari == null){
            model.addAttribute("ploting", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndHariNullAndJamMulaiNullAndJamSelesaiNullAndKelasNotNull(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik));
            model.addAttribute("minggu", jadwalDao.schedule(tahunAkademik.getProdi(),StatusRecord.HAPUS,tahunAkademik,hariDao.findById("0").get(),program));
            model.addAttribute("senin", jadwalDao.schedule(tahunAkademik.getProdi(),StatusRecord.HAPUS,tahunAkademik,hariDao.findById("1").get(),program));
            model.addAttribute("selasa", jadwalDao.schedule(tahunAkademik.getProdi(),StatusRecord.HAPUS,tahunAkademik,hariDao.findById("2").get(),program));
            model.addAttribute("rabu", jadwalDao.schedule(tahunAkademik.getProdi(),StatusRecord.HAPUS,tahunAkademik,hariDao.findById("3").get(),program));
            model.addAttribute("kamis", jadwalDao.schedule(tahunAkademik.getProdi(),StatusRecord.HAPUS,tahunAkademik,hariDao.findById("4").get(),program));
            model.addAttribute("jumat", jadwalDao.schedule(tahunAkademik.getProdi(),StatusRecord.HAPUS,tahunAkademik,hariDao.findById("5").get(),program));
            model.addAttribute("sabtu", jadwalDao.schedule(tahunAkademik.getProdi(),StatusRecord.HAPUS,tahunAkademik,hariDao.findById("6").get(),program));
        }

    }

    @GetMapping("/jadwalkuliah/form")
    public void formJadwal(Model model,@RequestParam(required = false) Jadwal jadwal,@RequestParam(required = false) String plot){
        if (plot != null){
            model.addAttribute("plot",plot);
        }

        model.addAttribute("jadwal",jadwal);
        model.addAttribute("team",jadwalDosenDao.findByJadwalAndStatusJadwalDosen(jadwal,StatusJadwalDosen.TEAM));
        model.addAttribute("hari", hariDao.findAll());
    }

    @PostMapping("/jadwalkuliah/form")
    public String prosesJadwal(@ModelAttribute @Valid Jadwal jadwal, @RequestParam(required = false) String plot,
                               @RequestParam(required = false) String dosens[],RedirectAttributes attributes){
        List<Jadwal> jdwl = jadwalDao.cariJadwal(jadwal.getId(),jadwal.getTahunAkademik(),jadwal.getHari(),jadwal.getRuangan(),jadwal.getSesi(),StatusRecord.AKTIF);


        if (jdwl == null || jdwl.isEmpty()) {
//            jadwal.setJamMulai(jadwal.getJamMulai());
//            jadwal.setJamSelesai(jadwal.getJamSelesai());
            jadwalDao.save(jadwal);

            JadwalDosen jadwalDosen = jadwalDosenDao.findByJadwalAndStatusJadwalDosen(jadwal,StatusJadwalDosen.PENGAMPU);
            if (jadwalDosen == null){
                JadwalDosen jd = new JadwalDosen();
                jd.setJadwal(jadwal);
                jd.setDosen(jadwal.getDosen());
                jd.setStatusJadwalDosen(StatusJadwalDosen.PENGAMPU);
                jadwalDosenDao.save(jd);
            }else {
                jadwalDosen.setDosen(jadwal.getDosen());
                jadwalDosenDao.save(jadwalDosen);
            }


            if (dosens != null) {
                for (String idDosen : dosens){
                    Dosen dosen = dosenDao.findById(idDosen).get();
                    JadwalDosen team = jadwalDosenDao.findByJadwalAndDosenAndStatusJadwalDosen(jadwal,dosen,StatusJadwalDosen.TEAM);
                    if (team == null){
                        JadwalDosen jd = new JadwalDosen();
                        jd.setJadwal(jadwal);
                        jd.setStatusJadwalDosen(StatusJadwalDosen.TEAM);
                        jd.setDosen(dosen);
                        jadwalDosenDao.save(jd);
                    }
                }

            }else {


            }
        }else {

            attributes.addFlashAttribute("validJadwal", jdwl);
            return "redirect:form?jadwal=" + jadwal.getId();
        }

        if (plot != null){
            return "redirect:/plotingdosen/list?tahunAkademik="+jadwal.getTahunAkademikProdi().getId()+"&program="+jadwal.getProgram().getId();
        }

        return "redirect:list?tahunAkademik="+ jadwal.getTahunAkademikProdi().getId() +"&program="+jadwal.getProgram().getId();
    }
}
