package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapJadwalDosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapSksDosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusJadwalDosen;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademik;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RekapDosenController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RekapDosenController.class);

    @Autowired private JadwalDosenDao jadwalDosenDao;
    @Autowired private JadwalDao jadwalDao;
    @Autowired private MatakuliahKurikulumDao matakuliahKurikulumDao;
    @Autowired private ProdiDao prodiDao;
    @Autowired private  TahunAkademikDao tahunAkademikDao;
    @Autowired private  TahunAkademikProdiDao tahunAkademikProdiDao;
    @Autowired private DosenDao dosenDao;

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatusNotIn(StatusRecord.HAPUS);
    }
    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademik> tahunAkademik() {
        return tahunAkademikDao.findByStatusNotInOrderByKodeTahunAkademikDesc(StatusRecord.HAPUS);
    }

    @GetMapping ("/rekap/dosen")
    public void rekapDosen(Model model, @RequestParam(required = false) TahunAkademik  ta, @PageableDefault(size = Integer.MAX_VALUE) Pageable page){
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



}
