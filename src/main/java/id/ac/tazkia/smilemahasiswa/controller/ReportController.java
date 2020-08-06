package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.report.RekapDetailDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.report.RekapDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.report.RekapJadwalDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.report.RekapSksDosenDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class ReportController {
    @Autowired
    private JadwalDosenDao jadwalDosenDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private ProdiDao prodiDao;

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademik> tahunAkademik() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }


    @GetMapping("/report/recapitulation/lecturer")
    public void rekapDosen(Model model, @RequestParam(required = false) TahunAkademik ta, @PageableDefault(size = Integer.MAX_VALUE) Pageable page){
        model.addAttribute("selectedTahun", ta);

        Page<RekapJadwalDosenDto> rekap = jadwalDosenDao
                .rekapJadwalDosen(StatusJadwalDosen.PENGAMPU, ta, StatusRecord.AKTIF, page);

        Map<String, RekapSksDosenDto> rekapJumlahSks = new LinkedHashMap<>();
        Map<String, List<RekapJadwalDosenDto>> detailJadwalPerDosen = new LinkedHashMap<>();

        for (RekapJadwalDosenDto r : rekap.getContent()) {

            // hitung total sks
            RekapSksDosenDto rsks = rekapJumlahSks.get(r.getIdDosen());
            if (rsks == null) {
                rsks = new RekapSksDosenDto();
                rsks.setNamaDosen(r.getNamaDosen());
                rsks.setIdDosen(r.getIdDosen());
                rsks.setTotalSks(0);
            }

            rsks.tambahSks(r.getSks());
            rekapJumlahSks.put(r.getIdDosen(), rsks);

            // jadwal per dosen
            List<RekapJadwalDosenDto> rd = detailJadwalPerDosen.get(r.getIdDosen());
            if (rd == null) {
                rd = new ArrayList<>();
                detailJadwalPerDosen.put(r.getIdDosen(), rd);
            }

            rd.add(r);
        }

        model.addAttribute("rekapJumlahSks", rekapJumlahSks);
        model.addAttribute("rekapJadwalDosen", rekap);
        model.addAttribute("rekapJadwalPerDosen", detailJadwalPerDosen);

    }

    @GetMapping("/report/recapitulation/salary")
    public void rekapGajiDosen(Model model, @RequestParam(required = false) Integer tahun,@RequestParam(required = false) Integer bulan, @PageableDefault(size = Integer.MAX_VALUE) Pageable page){

        List<RekapDosenDto> rekap = jadwalDosenDao
                .rekapDosen(tahun,bulan);
        model.addAttribute("selectedTahun", tahun);
        model.addAttribute("selectedBulan", bulan);

        Map<String, RekapDetailDosenDto> rekapJumlahSks = new LinkedHashMap<>();
        Map<String, List<RekapDosenDto>> detailJadwalPerDosen = new LinkedHashMap<>();

        for (RekapDosenDto r : rekap) {

            // hitung total sks
            RekapDetailDosenDto rsks = rekapJumlahSks.get(r.getId());
            if (rsks == null) {
                rsks = new RekapDetailDosenDto();
                rsks.setNamaDosen(r.getNama());
                rsks.setIdDosen(r.getId());
                rsks.setSks1(r.getSks());
                rsks.setJumlah(r.getHadir());

            }

            rsks.tambahSks(r.getSks());
            rekapJumlahSks.put(r.getId(), rsks);

            // jadwal per dosen
            List<RekapDosenDto> rd = detailJadwalPerDosen.get(r.getId());
            if (rd == null) {
                rd = new ArrayList<>();
                detailJadwalPerDosen.put(r.getId(), rd);
            }

            rd.add(r);
        }

        model.addAttribute("rekapJumlahSks", rekapJumlahSks);
        model.addAttribute("rekapJadwalDosen", rekap);
        model.addAttribute("rekapJadwalPerDosen", detailJadwalPerDosen);

    }

    @GetMapping("/report/recapitulation/ipk")
    public void rekapSks(Model model,@RequestParam(required = false) TahunAkademik tahunAkademik,
                         @RequestParam(required = false) String angkatan){

        if (tahunAkademik != null) {
            model.addAttribute("selectedAngkatan", angkatan);
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("ipk", krsDetailDao.cariIpk(tahunAkademik,angkatan));
        }
    }

    @GetMapping("/report/recapitulation/edom")
    public void rekapEdom(Model model,@RequestParam(required = false) TahunAkademik tahunAkademik,
                          @RequestParam(required = false) Prodi prodi){

        if (tahunAkademik != null){
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("selectedProdi", prodi);
            model.addAttribute("rekapEdom",krsDetailDao.rekapEdom(tahunAkademik,prodi));
        }

    }
}
