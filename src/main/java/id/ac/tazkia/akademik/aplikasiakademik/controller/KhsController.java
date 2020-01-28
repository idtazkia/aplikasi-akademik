package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.Khs;
import id.ac.tazkia.akademik.aplikasiakademik.dto.KhsDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Controller
public class KhsController {

    @Autowired
    private KrsDao krsDao;
    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired
    private MahasiswaDao mahasiswaDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private GradeDao gradeDao;

    @GetMapping("/khs/list")
    public void krsList(Model model, @PageableDefault(size = 10) Pageable page, @RequestParam(required = false)String nim, @RequestParam(required = false) TahunAkademik tahunAkademik) {
        model.addAttribute("gradeA", gradeDao.findById("1").get());
        model.addAttribute("grademinA", gradeDao.findById("2").get());
        model.addAttribute("gradeplusB", gradeDao.findById("3").get());
        model.addAttribute("gradeB", gradeDao.findById("4").get());
        model.addAttribute("grademinB", gradeDao.findById("5").get());
        model.addAttribute("gradeplusC", gradeDao.findById("6").get());
        model.addAttribute("gradeC", gradeDao.findById("7").get());
        model.addAttribute("gradeD", gradeDao.findById("8").get());
        model.addAttribute("gradeE", gradeDao.findById("9").get());
        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusNotInOrderByKodeTahunAkademikDesc(StatusRecord.HAPUS));

        if (nim != null && tahunAkademik != null) {
            model.addAttribute("nim", nim);
            model.addAttribute("tahun", tahunAkademik);

            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            if (mahasiswa != null) {
                model.addAttribute("mahasiswa", mahasiswa);

                Krs krs = krsDao.findByTahunAkademikStatusAndMahasiswa(StatusRecord.AKTIF, mahasiswa);

                if (tahunAkademik != null) {
                    List<Khs> krsDetail = krsDetailDao.getKhs(tahunAkademik,mahasiswa);
                    List<KhsDto> khsDtos = new ArrayList<>();

                    for (Khs kd : krsDetail){
                        KhsDto khsDto = new KhsDto();
                        KrsDetail matakuliahKurikulum = krsDetailDao.findById(kd.getId()).get();
                        if (kd.getNilaiAkhir() == null){
                            khsDto.setBobot(BigDecimal.ZERO);
                            khsDto.setId("E");
                            khsDto.setTotal(BigDecimal.ZERO);
                            khsDto.setTotalBobot(BigDecimal.ZERO);

                        }
                        if (kd.getNilaiAkhir() != null) {
                            khsDto.setTotal(kd.getNilaiAkhir());
                            khsDto.setTotalBobot(kd.getNilaiAkhir());
                            khsDto.setId(kd.getGrade());
                            khsDto.setBobot(kd.getBobot());
                        }
                        khsDto.setSks(matakuliahKurikulum.getMatakuliahKurikulum().getJumlahSks());
                        khsDto.setKode(matakuliahKurikulum.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());
                        khsDto.setTugas(kd.getTugas());
                        khsDto.setMapel(matakuliahKurikulum.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        khsDto.setUas(kd.getUas());
                        khsDto.setUts(kd.getUts());
                        khsDto.setPresensi(kd.getPresensi());
                        khsDtos.add(khsDto);
                    }

                    model.addAttribute("khs",khsDtos);
                } else {
                    List<Khs> krsDetail = krsDetailDao.getKhs(tahunAkademikDao.findByStatus(StatusRecord.AKTIF),mahasiswa);
                    List<KhsDto> khsDtos = new ArrayList<>();

                    for (Khs kd : krsDetail){
                        KhsDto khsDto = new KhsDto();
                        KrsDetail matakuliahKurikulum = krsDetailDao.findById(kd.getId()).get();
                        if (kd.getNilaiAkhir() == null){
                            khsDto.setId("E");
                            khsDto.setBobot(BigDecimal.ZERO);
                            khsDto.setTotalBobot(BigDecimal.ZERO);
                            khsDto.setTotal(BigDecimal.ZERO);

                        }
                        if (kd.getNilaiAkhir() != null) {
                            khsDto.setTotal(kd.getNilaiAkhir());
                            khsDto.setId(kd.getGrade());
                            khsDto.setBobot(kd.getBobot());
                            khsDto.setTotalBobot(kd.getNilaiAkhir());
                        }
                        khsDto.setSks(matakuliahKurikulum.getMatakuliahKurikulum().getJumlahSks());
                        khsDto.setKode(matakuliahKurikulum.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());
                        khsDto.setTugas(kd.getTugas());
                        khsDto.setMapel(matakuliahKurikulum.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        khsDto.setUas(kd.getUas());
                        khsDto.setUts(kd.getUts());
                        khsDto.setPresensi(kd.getPresensi());
                        khsDtos.add(khsDto);
                    }

                    model.addAttribute("khs",khsDtos);
                }

            }
        }
    }

}
