package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
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
                    List<KhsDto> khsDtos = new ArrayList<>();
                    model.addAttribute("search", tahunAkademik);
                    Page<KrsDetail> krsDetail = krsDetailDao.findByMahasiswaAndKrsTahunAkademik(mahasiswa, tahunAkademik, page);
                    for (KrsDetail kd : krsDetail) {
                        KhsDto khsDto = new KhsDto();
                        khsDto.setKode(kd.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());
                        khsDto.setId(kd);
                        khsDto.setMapel(kd.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliahEnglish());
                        khsDto.setPresensi(kd.getNilaiPresensi());
                        khsDto.setTugas(kd.getNilaiTugas());
                        khsDto.setSks(kd.getMatakuliahKurikulum().getJumlahSks());
                        khsDto.setUas(kd.getNilaiUas());
                        khsDto.setUts(kd.getNilaiUts());

                        BigDecimal tugas = khsDto.getTugas().multiply(kd.getJadwal().getBobotTugas().divide(new BigDecimal(100)));
                        BigDecimal uas = khsDto.getUas().multiply(kd.getJadwal().getBobotUas().divide(new BigDecimal(100)));
                        BigDecimal uts = khsDto.getUts().multiply(kd.getJadwal().getBobotUts().divide(new BigDecimal(100)));
                        BigDecimal presensi = khsDto.getPresensi().multiply(kd.getJadwal().getBobotPresensi().divide(new BigDecimal(100)));
//
                        khsDto.setTotal(tugas.add(uas).add(uts).add(presensi));
                        Grade a = gradeDao.findById("1").get();
                        Grade amin= gradeDao.findById("2").get();
                        Grade bplus= gradeDao.findById("3").get();
                        Grade b = gradeDao.findById("4").get();
                        Grade bmin = gradeDao.findById("5").get();
                        Grade cplus= gradeDao.findById("6").get();
                        Grade c = gradeDao.findById("7").get();
                        Grade d = gradeDao.findById("8").get();
                        Grade e = gradeDao.findById("9").get();

                        if (khsDto.getTotal().toBigInteger().intValueExact() > a.getBawah().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(a.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(a.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > amin.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < amin.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(amin.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(amin.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > bplus.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < bplus.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(bplus.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(bplus.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > b.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < b.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(b.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(b.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > bmin.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < bmin.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(bmin.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(bmin.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > cplus.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < cplus.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(cplus.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(cplus.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > c.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < c.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(c.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(c.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > d.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < d.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(d.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(d.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > e.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < e.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(e.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(e.getBobot()));
                        }

                        if (khsDto.getTotalBobot() == null){
                            khsDto.setTotalBobot(BigDecimal.ZERO);
                            khsDto.setBobot(BigDecimal.ZERO);
                        }

                        khsDtos.add(khsDto);
                        
                    }
                    int sks = khsDtos.stream().filter(o -> o.getSks() > 0).mapToInt(KhsDto::getSks).sum();
                    BigDecimal bobot = khsDtos.stream()
                            .map(KhsDto::getTotalBobot)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    MathContext mc = new MathContext(2, RoundingMode.HALF_EVEN);
                    model.addAttribute("ip", bobot.divide(BigDecimal.valueOf(sks),mc));
                    model.addAttribute("totalBobot", bobot);
                    model.addAttribute("jumlahSks", sks);
                    model.addAttribute("khs", khsDtos);
                } else {
                    List<KhsDto> khsDtos = new ArrayList<>();
                    Page<KrsDetail> krsDetail = krsDetailDao.findByKrsAndMahasiswa(krs, mahasiswa, page);
                    for (KrsDetail kd : krsDetail) {
                        KhsDto khsDto = new KhsDto();
                        khsDto.setKode(kd.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());
                        khsDto.setMapel(kd.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliahEnglish());
                        khsDto.setId(kd);
                        khsDto.setPresensi(kd.getNilaiPresensi());
                        khsDto.setUas(kd.getNilaiUas());
                        khsDto.setTugas(kd.getNilaiTugas());
                        khsDto.setSks(kd.getMatakuliahKurikulum().getJumlahSks());
                        khsDto.setUts(kd.getNilaiUts());


                        BigDecimal tugas = khsDto.getTugas().multiply(kd.getJadwal().getBobotTugas().divide(new BigDecimal(100)));
                        BigDecimal uas = khsDto.getUas().multiply(kd.getJadwal().getBobotUas().divide(new BigDecimal(100)));
                        BigDecimal uts = khsDto.getUts().multiply(kd.getJadwal().getBobotUts().divide(new BigDecimal(100)));
                        BigDecimal presensi = khsDto.getPresensi().multiply(kd.getJadwal().getBobotPresensi().divide(new BigDecimal(100)));
//
                        khsDto.setTotal(tugas.add(uas).add(uts).add(presensi));
                        Grade a = gradeDao.findById("1").get();
                        Grade amin= gradeDao.findById("2").get();
                        Grade bplus= gradeDao.findById("3").get();
                        Grade b = gradeDao.findById("4").get();
                        Grade bmin = gradeDao.findById("5").get();
                        Grade cplus= gradeDao.findById("6").get();
                        Grade c = gradeDao.findById("7").get();
                        Grade d = gradeDao.findById("8").get();
                        Grade e = gradeDao.findById("9").get();

                        if (khsDto.getTotal().toBigInteger().intValueExact() > a.getBawah().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(a.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(a.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > amin.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < amin.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(amin.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(amin.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > bplus.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < bplus.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(bplus.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(bplus.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > b.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < b.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(b.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(b.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > bmin.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < bmin.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(bmin.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(bmin.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > cplus.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < cplus.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(cplus.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(cplus.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > c.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < c.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(c.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(c.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > d.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < d.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(d.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(d.getBobot()));
                        }

                        if (khsDto.getTotal().toBigInteger().intValueExact() > e.getBawah().toBigInteger().intValueExact() && khsDto.getTotal().toBigInteger().intValueExact() < e.getAtas().toBigInteger().intValueExact()){
                            khsDto.setTotalBobot(e.getBobot());
                            khsDto.setBobot(BigDecimal.valueOf(kd.getMatakuliahKurikulum().getJumlahSks()).multiply(e.getBobot()));
                        }

                        if (khsDto.getTotalBobot() == null){
                            khsDto.setTotalBobot(BigDecimal.ZERO);
                            khsDto.setBobot(BigDecimal.ZERO);
                        }
                        
                        khsDtos.add(khsDto);
                    }
                    int sks = khsDtos.stream().filter(o -> o.getSks() > 0).mapToInt(KhsDto::getSks).sum();
                    BigDecimal bobot = khsDtos.stream()
                            .map(KhsDto::getTotalBobot)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    MathContext mc = new MathContext(2, RoundingMode.HALF_EVEN);
                    model.addAttribute("ip", bobot.divide(BigDecimal.valueOf(sks),mc));
                    model.addAttribute("totalBobot", bobot);
                    model.addAttribute("jumlahSks", sks);
                    model.addAttribute("khs", khsDtos);
                }

            }
        }
    }

}
