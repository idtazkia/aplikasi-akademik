package id.ac.tazkia.smilemahasiswa.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import id.ac.tazkia.smilemahasiswa.constant.JenisTest;
import id.ac.tazkia.smilemahasiswa.dao.DetailJadwalBahasaDao;
import id.ac.tazkia.smilemahasiswa.dao.DosenDao;
import id.ac.tazkia.smilemahasiswa.dao.JadwalBahasaDao;
import id.ac.tazkia.smilemahasiswa.dao.JadwalDosenBahasaDao;
import id.ac.tazkia.smilemahasiswa.dao.KaryawanDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.PresensiDosenBahasaDao;
import id.ac.tazkia.smilemahasiswa.dao.PresensiMahasiswaBahasaDao;
import id.ac.tazkia.smilemahasiswa.dao.RuanganDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dto.api.BaseResponseDto;
import id.ac.tazkia.smilemahasiswa.dto.response.BaseResponse;
import id.ac.tazkia.smilemahasiswa.dto.tlc.jadwal.*;
import id.ac.tazkia.smilemahasiswa.dto.tlc.attendance.DataPresensiDosen;
import id.ac.tazkia.smilemahasiswa.dto.tlc.attendance.PresensiDosenBahasaDto;
import id.ac.tazkia.smilemahasiswa.dto.tlc.jadwal.DataJadwalBahasa;
import id.ac.tazkia.smilemahasiswa.dto.tlc.jadwal.DataMahasiswa;
import id.ac.tazkia.smilemahasiswa.dto.tlc.jadwal.JadwalBahasaRequest;
import id.ac.tazkia.smilemahasiswa.dto.tlc.jadwal.MahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.tlc.periode.DataPeriodeTlc;
import id.ac.tazkia.smilemahasiswa.dto.tlc.periode.PeriodeDto;
import id.ac.tazkia.smilemahasiswa.dto.tlc.presensi.DataPresensiMahasiswa;
import id.ac.tazkia.smilemahasiswa.dto.tlc.presensi.GetDataPresensiMahasiswa;
import id.ac.tazkia.smilemahasiswa.dto.tlc.presensi.KehadiranRequest;
import id.ac.tazkia.smilemahasiswa.entity.DetailJadwalBahasa;
import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.JadwalBahasa;
import id.ac.tazkia.smilemahasiswa.entity.JadwalDosenBahasa;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.PresensiDosenBahasa;
import id.ac.tazkia.smilemahasiswa.entity.PresensiMahasiswaBahasa;
import id.ac.tazkia.smilemahasiswa.entity.StatusPresensi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class TlcController {
    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private JadwalBahasaDao jadwalBahasaDao;

    @Autowired
    private DetailJadwalBahasaDao detailJadwalBahasaDao;

    @Autowired
    private JadwalDosenBahasaDao jadwalDosenBahasaDao;

    @Autowired
    private RuanganDao ruanganDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private PresensiDosenBahasaDao presensiDosenBahasaDao;
    @Autowired
    private PresensiMahasiswaBahasaDao presensiMahasiswaBahasaDao;
    @Autowired
    private MahasiswaDao mahasiswaDao;
    @Autowired
    private CurrentUserService currentUserService;
    @Autowired
    private KaryawanDao karyawanDao;

    @GetMapping("/tlc/periode/list")
    public void periodeList(Model model) {}

    @GetMapping("/tlc/dosen/list")
    public void jadwalDosenList(Model model) {
    }

    @GetMapping("/tlc/periode/form")
    public void jadwalForm(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik) {
        model.addAttribute("ruangan", ruanganDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("tahunAkademik", tahunAkademik);
    }

    @GetMapping("/tlc/attendance/detail")
    public String detailAttendence(Model model, @RequestParam String id) {
        Optional<JadwalBahasa> jadwalBahasa = jadwalBahasaDao.findById(id);
        if (jadwalBahasa.isPresent()) {
            model.addAttribute("sesi", detailJadwalBahasaDao.findByJadwalBahasa(jadwalBahasa.get()));
            model.addAttribute("jadwalBahasa", jadwalBahasa.get());
            return "tlc/attendance/detail";
        } else {

            return "error/404";
        }

    }

    @GetMapping("/tlc/periode/edit")
    public void editForm(Model model, @RequestParam String id) {
        model.addAttribute("ruangan", ruanganDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("listDosen", dosenDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS, StatusRecord.NONAKTIF)));
        if (StringUtils.hasText(id)) {
            JadwalBahasa jadwalBahasa = jadwalBahasaDao.findById(id).get();
            DetailJadwalBahasa sesi1 = detailJadwalBahasaDao.findByJadwalBahasaAndSesi(jadwalBahasa, "1");
            DetailJadwalBahasa sesi2 = detailJadwalBahasaDao.findByJadwalBahasaAndSesi(jadwalBahasa, "2");
            DetailJadwalBahasa sesi3 = detailJadwalBahasaDao.findByJadwalBahasaAndSesi(jadwalBahasa, "3");
            DetailJadwalBahasa sesi4 = detailJadwalBahasaDao.findByJadwalBahasaAndSesi(jadwalBahasa, "4");

            model.addAttribute("jadwal", jadwalBahasa);
            model.addAttribute("sesi1", sesi1);

            if (sesi2 != null) {
                model.addAttribute("sesi2", sesi2);
            }

            if (sesi3 != null) {
                model.addAttribute("sesi3", sesi3);
            }

            if (sesi4 != null) {
                model.addAttribute("sesi4", sesi4);
            }

        }
    }

    @PostMapping("/tlc/periode/edit")
    public String editForm(@Valid JadwalBahasaRequest jadwalBahasaRequest, BindingResult bindingResult) {
        JadwalBahasa jadwalBahasa = jadwalBahasaDao.findById(jadwalBahasaRequest.getId()).get();
        jadwalBahasa.setNamaJadwal(jadwalBahasaRequest.getJadwal());
        jadwalBahasa.setNamaKelas(jadwalBahasaRequest.getKelas());
        jadwalBahasa.setJenis(jadwalBahasaRequest.getJenis());
        jadwalBahasa.setDosens(jadwalBahasaRequest.getDosen());
        jadwalBahasaDao.save(jadwalBahasa);

        DetailJadwalBahasa sesi1 = detailJadwalBahasaDao.findByJadwalBahasaAndSesi(jadwalBahasa, "1");
        DetailJadwalBahasa sesi2 = detailJadwalBahasaDao.findByJadwalBahasaAndSesi(jadwalBahasa, "2");
        DetailJadwalBahasa sesi3 = detailJadwalBahasaDao.findByJadwalBahasaAndSesi(jadwalBahasa, "3");
        DetailJadwalBahasa sesi4 = detailJadwalBahasaDao.findByJadwalBahasaAndSesi(jadwalBahasa, "4");
        sesi1.setJadwalBahasa(jadwalBahasa);
        sesi1.setHari(jadwalBahasaRequest.getHariSesi1());
        sesi1.setRuangan(jadwalBahasaRequest.getRuanganSesi1());
        sesi1.setSesi("1");
        sesi1.setJamMulai(jadwalBahasaRequest.getJamMulaiSesi1());
        sesi1.setJamSelesai(jadwalBahasaRequest.getJamSelesaiSesi1());
        detailJadwalBahasaDao.save(sesi1);

        if (sesi2 != null) {
            if (jadwalBahasaRequest.getHariSesi2() != null) {
                sesi2.setJadwalBahasa(jadwalBahasa);
                sesi2.setHari(jadwalBahasaRequest.getHariSesi2());
                sesi2.setRuangan(jadwalBahasaRequest.getRuanganSesi2());
                sesi2.setSesi("2");
                sesi2.setJamMulai(jadwalBahasaRequest.getJamMulaiSesi2());
                sesi2.setJamSelesai(jadwalBahasaRequest.getJamSelesaiSesi2());
                detailJadwalBahasaDao.save(sesi2);
            }
            {
                detailJadwalBahasaDao.delete(sesi2);
            }
        } else {
            if (jadwalBahasaRequest.getHariSesi2() != null) {
                DetailJadwalBahasa newSesi2 = new DetailJadwalBahasa();
                newSesi2.setJadwalBahasa(jadwalBahasa);
                newSesi2.setHari(jadwalBahasaRequest.getHariSesi2());
                newSesi2.setRuangan(jadwalBahasaRequest.getRuanganSesi2());
                newSesi2.setSesi("2");
                newSesi2.setJamMulai(jadwalBahasaRequest.getJamMulaiSesi2());
                newSesi2.setJamSelesai(jadwalBahasaRequest.getJamSelesaiSesi2());
                detailJadwalBahasaDao.save(newSesi2);
            }
        }

        if (sesi3 != null) {
            if (jadwalBahasaRequest.getHariSesi3() != null) {
                sesi3.setJadwalBahasa(jadwalBahasa);
                sesi3.setHari(jadwalBahasaRequest.getHariSesi3());
                sesi3.setRuangan(jadwalBahasaRequest.getRuanganSesi3());
                sesi3.setSesi("3");
                sesi3.setJamMulai(jadwalBahasaRequest.getJamMulaiSesi3());
                sesi3.setJamSelesai(jadwalBahasaRequest.getJamSelesaiSesi3());
                detailJadwalBahasaDao.save(sesi3);
            }
            {
                detailJadwalBahasaDao.delete(sesi3);
            }
        } else {
            if (jadwalBahasaRequest.getHariSesi3() != null) {
                DetailJadwalBahasa newSesi3 = new DetailJadwalBahasa();
                newSesi3.setJadwalBahasa(jadwalBahasa);
                newSesi3.setHari(jadwalBahasaRequest.getHariSesi3());
                newSesi3.setRuangan(jadwalBahasaRequest.getRuanganSesi3());
                newSesi3.setSesi("3");
                newSesi3.setJamMulai(jadwalBahasaRequest.getJamMulaiSesi3());
                newSesi3.setJamSelesai(jadwalBahasaRequest.getJamSelesaiSesi3());
                detailJadwalBahasaDao.save(newSesi3);
            }
        }

        if (sesi4 != null) {
            if (jadwalBahasaRequest.getHariSesi4() != null) {
                sesi4.setJadwalBahasa(jadwalBahasa);
                sesi4.setHari(jadwalBahasaRequest.getHariSesi4());
                sesi4.setRuangan(jadwalBahasaRequest.getRuanganSesi4());
                sesi4.setSesi("4");
                sesi4.setJamMulai(jadwalBahasaRequest.getJamMulaiSesi4());
                sesi4.setJamSelesai(jadwalBahasaRequest.getJamSelesaiSesi4());
                detailJadwalBahasaDao.save(sesi4);
            }
            {
                detailJadwalBahasaDao.delete(sesi4);
            }
        } else {
            if (jadwalBahasaRequest.getHariSesi4() != null) {
                DetailJadwalBahasa newSesi4 = new DetailJadwalBahasa();
                newSesi4.setJadwalBahasa(jadwalBahasa);
                newSesi4.setHari(jadwalBahasaRequest.getHariSesi4());
                newSesi4.setRuangan(jadwalBahasaRequest.getRuanganSesi4());
                newSesi4.setSesi("4");
                newSesi4.setJamMulai(jadwalBahasaRequest.getJamMulaiSesi4());
                newSesi4.setJamSelesai(jadwalBahasaRequest.getJamSelesaiSesi4());
                detailJadwalBahasaDao.save(newSesi4);
            }
        }

        return "redirect:list";
    }

    @PostMapping("/tlc/periode/form")
    public String submitForm(@Valid JadwalBahasaRequest jadwalBahasaRequest, BindingResult bindingResult) {
        JadwalBahasa jadwalBahasa = new JadwalBahasa();
        jadwalBahasa.setNamaJadwal(jadwalBahasaRequest.getJadwal());
        jadwalBahasa.setNamaKelas(jadwalBahasaRequest.getKelas());
        jadwalBahasa.setJenis(jadwalBahasaRequest.getJenis());
        jadwalBahasa.setTahunAkademik(jadwalBahasaRequest.getTahunAkademik());
        jadwalBahasa.setDosens(jadwalBahasaRequest.getDosen());
        jadwalBahasaDao.save(jadwalBahasa);

        DetailJadwalBahasa sesi1 = new DetailJadwalBahasa();
        sesi1.setJadwalBahasa(jadwalBahasa);
        sesi1.setHari(jadwalBahasaRequest.getHariSesi1());
        sesi1.setRuangan(jadwalBahasaRequest.getRuanganSesi1());
        sesi1.setSesi("1");
        sesi1.setJamMulai(jadwalBahasaRequest.getJamMulaiSesi1());
        sesi1.setJamSelesai(jadwalBahasaRequest.getJamSelesaiSesi1());
        detailJadwalBahasaDao.save(sesi1);

        if (jadwalBahasaRequest.getHariSesi2() != null) {
            DetailJadwalBahasa sesi2 = new DetailJadwalBahasa();
            sesi2.setJadwalBahasa(jadwalBahasa);
            sesi2.setHari(jadwalBahasaRequest.getHariSesi2());
            sesi2.setRuangan(jadwalBahasaRequest.getRuanganSesi2());
            sesi2.setSesi("2");
            sesi2.setJamMulai(jadwalBahasaRequest.getJamMulaiSesi2());
            sesi2.setJamSelesai(jadwalBahasaRequest.getJamSelesaiSesi2());
            detailJadwalBahasaDao.save(sesi2);

        }

        if (jadwalBahasaRequest.getHariSesi3() != null) {
            DetailJadwalBahasa sesi3 = new DetailJadwalBahasa();
            sesi3.setJadwalBahasa(jadwalBahasa);
            sesi3.setHari(jadwalBahasaRequest.getHariSesi3());
            sesi3.setRuangan(jadwalBahasaRequest.getRuanganSesi3());
            sesi3.setSesi("3");
            sesi3.setJamMulai(jadwalBahasaRequest.getJamMulaiSesi3());
            sesi3.setJamSelesai(jadwalBahasaRequest.getJamSelesaiSesi3());
            detailJadwalBahasaDao.save(sesi3);

        }

        if (jadwalBahasaRequest.getHariSesi4() != null) {
            DetailJadwalBahasa sesi4 = new DetailJadwalBahasa();
            sesi4.setJadwalBahasa(jadwalBahasa);
            sesi4.setHari(jadwalBahasaRequest.getHariSesi4());
            sesi4.setRuangan(jadwalBahasaRequest.getRuanganSesi4());
            sesi4.setSesi("4");
            sesi4.setJamMulai(jadwalBahasaRequest.getJamMulaiSesi4());
            sesi4.setJamSelesai(jadwalBahasaRequest.getJamSelesaiSesi4());
            detailJadwalBahasaDao.save(sesi4);
        }

        return "redirect:list";
    }


    @GetMapping("/tlc/periode-list")
    @ResponseBody
    public DataPeriodeTlc listKelas(@RequestParam(required = false) String search,
                                    @RequestParam(required = false) String sort,
                                    @RequestParam(required = false) String order,
                                    @RequestParam(required = false) Integer offset,
                                    @RequestParam(required = false) Integer limit) {

        if (search.isEmpty()) {
            List<PeriodeDto> tahunAkademiks = tahunAkademikDao.listTahunAkademik(limit, offset);
            DataPeriodeTlc response = new DataPeriodeTlc();
            response.setTotal(tahunAkademikDao.countTahunAkademik());
            response.setTotalNotFiltered(tahunAkademikDao.countTahunAkademik());
            response.setRows(tahunAkademiks);
            return response;
        } else {
            List<PeriodeDto> tahunAkademiks = tahunAkademikDao.listTahunAkademikSearch(search, limit, offset);
            DataPeriodeTlc response = new DataPeriodeTlc();
            response.setTotal(tahunAkademikDao.listTahunAkademikSearch(search, Integer.MAX_VALUE, offset).size());
            response.setTotalNotFiltered(tahunAkademiks.size());
            response.setRows(tahunAkademiks);
            return response;
        }

    }

    @GetMapping("/tlc/jadwal-list/{tahun}")
    @ResponseBody
    public List<JadwalRequest> listJadwal(@PathVariable String tahun,
                                                 @RequestParam(defaultValue = "0") int offset,
                                         @RequestParam(defaultValue = "10") int limit) {
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).orElse(null);

        List<JadwalRequest> rows = jadwalBahasaDao.getJadwalBahasa(tahunAkademik);
        return rows;

    }

    @GetMapping("/tlc/jadwal-dosen-list/{tahun}")
    @ResponseBody
    public DataJadwalBahasa listJadwalDosen(Authentication authentication, @PathVariable String tahun) {
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();

        DataJadwalBahasa response = new DataJadwalBahasa();
        response.setTotal(jadwalBahasaDao.findByDosen(dosen, tahunAkademik).size());
        response.setTotalNotFiltered(jadwalBahasaDao.findByDosen(dosen, tahunAkademik).size());
        response.setRows(jadwalBahasaDao.findByDosen(dosen, tahunAkademik));
        return response;

    }

    @GetMapping("/tlc/jadwal-mahasiswa/{id}")
    @ResponseBody
    public DataMahasiswa listMahasiswaJadwal(@PathVariable String id) {
        JadwalBahasa jadwalBahasa = jadwalBahasaDao.findById(id).get();

        DataMahasiswa response = new DataMahasiswa();
        response.setTotal(jadwalBahasa.getMahasiswas().size());
        response.setTotalNotFiltered(jadwalBahasa.getMahasiswas().size());
        response.setRows(jadwalBahasa.getMahasiswas());
        return response;

    }

    @GetMapping("/tlc/presensi-mahasiswa/{id}")
    @ResponseBody
    public DataPresensiMahasiswa listPresensiMahasiswa(@PathVariable String id, @RequestParam String presensiId) {
        Optional<JadwalBahasa> jadwalBahasaOpt = jadwalBahasaDao.findById(id);
        if (!jadwalBahasaOpt.isPresent()) {
            return null;
        }

        Optional<PresensiDosenBahasa> presensiDosenBahasaOpt = presensiDosenBahasaDao.findById(presensiId);
        if (!presensiDosenBahasaOpt.isPresent()) {
            return null;
        }

        JadwalBahasa jadwalBahasa = jadwalBahasaOpt.get();
        PresensiDosenBahasa presensiDosenBahasa = presensiDosenBahasaOpt.get();
        List<GetDataPresensiMahasiswa> presensi = presensiMahasiswaBahasaDao.listPresensiMahasiswa(jadwalBahasa, presensiDosenBahasa);

        DataPresensiMahasiswa response = new DataPresensiMahasiswa();
        response.setTotal(presensi.size());
        response.setTotalNotFiltered(presensi.size());
        response.setRows(presensi);
        return response;
    }


    @GetMapping("/tlc/jadwal-dosen-bahasa/{id}")
    @ResponseBody
    public List<Dosen> listJadwalDosenBahasa(@PathVariable String id) {
        JadwalBahasa jadwalBahasa = jadwalBahasaDao.findById(id).get();

        return jadwalDosenBahasaDao.findByJadwalBahasa(jadwalBahasa).stream()
                .map(JadwalDosenBahasa::getDosen)
                .collect(Collectors.toList());

    }

    @GetMapping("/select/mahasiswa")
    @ResponseBody
    public List<MahasiswaDto> cariMahasiswa(@RequestParam String search, String tahun) {


        return krsDao.cariMahasiswa(search, tahun);
    }

    @PostMapping(value = "/jadwal-mahasiswa-post/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse simpanJabatanDosen(@PathVariable String id, @RequestParam Set<Mahasiswa> mahasiswaList) {
        JadwalBahasa jadwalBahasa = jadwalBahasaDao.findById(id).get();
        jadwalBahasa.setMahasiswas(mahasiswaList);
        jadwalBahasaDao.save(jadwalBahasa);

        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Mahasiswa Berhasil Disimpan");
        baseResponse.setCode(HttpStatus.CREATED.getReasonPhrase());

        return baseResponse;
    }

    @GetMapping("/tlc/presensi-dosen/{id}")
    @ResponseBody
    public DataPresensiDosen listPresensiDosen(@PathVariable String id) {
        JadwalBahasa jadwalBahasa = jadwalBahasaDao.findById(id).get();
        List<PresensiDosenBahasa> presensiDosenBahasa = presensiDosenBahasaDao.findByJadwalBahasaAndStatusOrderByPertemuanKeAsc(jadwalBahasa, StatusRecord.AKTIF);

        DataPresensiDosen response = new DataPresensiDosen();
        response.setTotal(presensiDosenBahasa.size());
        response.setTotalNotFiltered(presensiDosenBahasa.size());
        response.setRows(presensiDosenBahasa);
        return response;

    }

    @GetMapping("/tlc/detail-presensi-dosen/{id}")
    @ResponseBody
    public PresensiDosenBahasa listDetailresensiDosen(@PathVariable String id) {
        PresensiDosenBahasa presensiDosenBahasa = presensiDosenBahasaDao.findById(id).get();
        return presensiDosenBahasa;

    }

    @PostMapping("/tlc/presensi-dosen/{id}")
    @ResponseBody
    public ResponseEntity<BaseResponseDto> editInstitutionStatus(@PathVariable String id,
                                                                 @RequestBody PresensiDosenBahasaDto requestDto,
                                                                 BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new BaseResponseDto("ERR01", "Invalid Request Body, " + bindingResult.getFieldError().getDefaultMessage()));
            }

            Integer pertemuan = 0;
            Optional<JadwalBahasa> optionalJadwalBahasa = jadwalBahasaDao.findById(id);
            if (optionalJadwalBahasa.isPresent()) {
                PresensiDosenBahasa presensiDosenBahasa = null;
                if (requestDto.getId() != null) {
                    presensiDosenBahasa = presensiDosenBahasaDao.findById(requestDto.getId()).get();
                } else {
                    presensiDosenBahasa = new PresensiDosenBahasa();
                }
                presensiDosenBahasa.setJadwalBahasa(optionalJadwalBahasa.get());

                Optional<Dosen> dosen = dosenDao.findById(requestDto.getDosen());
                if (dosen.isPresent()) {
                    presensiDosenBahasa.setDosen(dosen.get());
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new BaseResponseDto("404", "Dosen not found"));
                }

                if (presensiDosenBahasaDao.totalPertertemuan(optionalJadwalBahasa.get()) <= 0) {
                    pertemuan = 1;
                } else {
                    pertemuan = presensiDosenBahasaDao.totalPertertemuan(optionalJadwalBahasa.get()) + 1;
                }
                presensiDosenBahasa.setPertemuanKe(pertemuan);
                presensiDosenBahasa.setDetailJadwalBahasa(detailJadwalBahasaDao.findById(requestDto.getSesi()).get());
                presensiDosenBahasa.setPertemuan(requestDto.getPertemuan());
                presensiDosenBahasa.setBeritaAcara(requestDto.getBerita());
                presensiDosenBahasa.setJenis(JenisTest.valueOf(requestDto.getJenis()));
                presensiDosenBahasa.setWaktuMengajar(LocalDateTime.now().plusHours(7));

                presensiDosenBahasaDao.save(presensiDosenBahasa);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(BaseResponseDto.builder().responseCode("00").responseMessage("success").build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(BaseResponseDto.builder().responseCode("404").responseMessage("Jadwal not found").build());
            }
        } catch (Exception e) {
            log.error("[SAVE - ATTENDANCE] - [ERROR] : Unable to save data , {} ", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDto.builder().responseCode("ERR500").responseMessage(e.getLocalizedMessage()).build());
        }
    }

    @PostMapping("/tlc/attendance/update-kehadiran")
    @ResponseBody
    public ResponseEntity<BaseResponseDto> updateKehadiran(@RequestBody KehadiranRequest request) {
        try {
            Optional<Mahasiswa> optionalMahasiswa = mahasiswaDao.findById(request.getMahasiswaId());
            if (optionalMahasiswa.isEmpty()) {
                log.info("Mahasiswa id {} not found", request.getMahasiswaId());
            }

            Optional<PresensiDosenBahasa> optionalPresensiDosenBahasa = presensiDosenBahasaDao.findById(request.getPresensiId());
            if (optionalPresensiDosenBahasa.isEmpty()) {
                log.info("Presensi id {} not found", request.getPresensiId());
            }


            PresensiMahasiswaBahasa presensiMahasiswaBahasa = presensiMahasiswaBahasaDao.findByMahasiswaAndPresensiDosenBahasa(optionalMahasiswa.get(), optionalPresensiDosenBahasa.get());
            if (presensiMahasiswaBahasa == null) {
                presensiMahasiswaBahasa = new PresensiMahasiswaBahasa();
                presensiMahasiswaBahasa.setMahasiswa(optionalMahasiswa.get());
                presensiMahasiswaBahasa.setPresensiDosenBahasa(optionalPresensiDosenBahasa.get());
                presensiMahasiswaBahasa.setStatusPresensi(StatusPresensi.valueOf(request.getKehadiran()));
                presensiMahasiswaBahasa.setWaktuMasuk(LocalDateTime.now().plusHours(7));
                presensiMahasiswaBahasaDao.save(presensiMahasiswaBahasa);
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("00")
                                .responseMessage("Kehadiran Berhasil Diproses")
                                .data(presensiMahasiswaBahasa)
                                .build());
            } else {
                presensiMahasiswaBahasa.setStatusPresensi(StatusPresensi.valueOf(request.getKehadiran()));
                presensiMahasiswaBahasaDao.save(presensiMahasiswaBahasa);
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("00")
                                .responseMessage("Kehadiran Berhasil Diproses")
                                .data(presensiMahasiswaBahasa)
                                .build());
            }
        } catch (Exception e) {
            log.error("[POST ATTENDANCE] - [ERROR] : Unable to Get data , {} ", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDto.builder()
                            .responseCode("ERR500")
                            .responseMessage(e.getLocalizedMessage())
                            .build());
        }
    }

    @GetMapping("/tlc/periode/delete")
    public String deleteJadwalBahasa(@RequestParam String id) {
       JadwalBahasa jadwalBahasa = jadwalBahasaDao.findById(id).get();
       jadwalBahasa.setStatus(StatusRecord.HAPUS);
       jadwalBahasaDao.save(jadwalBahasa);

       return "redirect:list?tahun=" + jadwalBahasa.getTahunAkademik().getId();
    }

}
