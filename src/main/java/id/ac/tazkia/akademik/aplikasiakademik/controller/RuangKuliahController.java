package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.SesiDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class RuangKuliahController {

    @Autowired
    private GedungDao gedungDao;

    @Autowired
    private RuanganDao ruanganDao;

    @Autowired
    private HariDao hariDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;


    @GetMapping("/ruangkuliah/list")
    public void listRuangKuliah(Model model, @RequestParam(required = false) Gedung gedung, @RequestParam(required = false) Hari hari) {

        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        model.addAttribute("hari", hariDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("gedung", gedungDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("selectedHari", hari);
        model.addAttribute("selectedGedung",gedung);


        if (gedung !=null && hari != null){

            Iterable<Ruangan> ruangan = ruanganDao.findByStatusAndGedung(StatusRecord.AKTIF,gedung);
            List<SesiDto> sesi = new ArrayList<>();
            for (Ruangan ruang1 : ruangan ){

                SesiDto sesiDto = new SesiDto();
                sesiDto.setId(ruang1.getId());
                sesiDto.setRuangan1(ruang1.getNamaRuangan());

                List<Jadwal> jadwal1= jadwalDao.findByStatusAndTahunAkademikAndHariAndRuangan
                        (StatusRecord.AKTIF,tahunAkademik,hari,ruang1);

                for (Jadwal j : jadwal1){

                        if (j.getSesi().equals("1")){
                            sesiDto.setSesi1(j.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                            sesiDto.setDosen1(j.getDosen().getKaryawan().getNamaKaryawan());
                            sesiDto.setKelas1(j.getKelas().getNamaKelas());
                        }

                        if (j.getSesi().equals("2")){
                            sesiDto.setSesi2(j.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                            sesiDto.setDosen2(j.getDosen().getKaryawan().getNamaKaryawan());
                            sesiDto.setKelas2(j.getKelas().getNamaKelas());
                        }

                        if (j.getSesi().equals("3")){
                            sesiDto.setSesi3(j.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                            sesiDto.setDosen3(j.getDosen().getKaryawan().getNamaKaryawan());
                            sesiDto.setKelas3(j.getKelas().getNamaKelas());
                        }

                        if (j.getSesi().equals("4")){
                            sesiDto.setSesi4(j.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                            sesiDto.setDosen4(j.getDosen().getKaryawan().getNamaKaryawan());
                            sesiDto.setKelas4(j.getKelas().getNamaKelas());
                        }

                    }

                sesi.add(sesiDto);

            }

            model.addAttribute("jadwalRuang",sesi);

        }

    }
}
