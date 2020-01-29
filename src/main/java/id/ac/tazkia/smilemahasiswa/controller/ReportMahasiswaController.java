package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dto.report.DataKhsDto;
import id.ac.tazkia.smilemahasiswa.dto.report.EdomDto;
import id.ac.tazkia.smilemahasiswa.dto.report.KhsDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Controller
public class ReportMahasiswaController {
    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @GetMapping("/report/khs")
    public String khs(Model model, Authentication authentication,@RequestParam(required = false) TahunAkademik tahunAkademik){

        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        List<KrsDetail> validasiEdom = krsDetailDao.findByMahasiswaAndKrsTahunAkademikAndStatusAndStatusEdom(mahasiswa,tahunAkademikDao.findByStatus(StatusRecord.AKTIF),StatusRecord.AKTIF, StatusRecord.UNDONE);

        model.addAttribute("tahun" , tahunAkademikDao.cariTahunAkademik(Arrays.asList(StatusRecord.HAPUS)));
        if (tahunAkademik != null) {
            model.addAttribute("selectedTahun" , tahunAkademik);

            List<DataKhsDto> krsDetail = krsDetailDao.getKhs(tahunAkademik,mahasiswa);
            List<KhsDto> khsDtos = new ArrayList<>();
        if (!krsDetail.isEmpty() && krsDetail == null) {
            for (DataKhsDto kd : krsDetail) {
                KhsDto khsDto = new KhsDto();
                KrsDetail matakuliahKurikulum = krsDetailDao.findById(kd.getId()).get();
                if (kd.getNilaiAkhir() == null) {
                    khsDto.setId("E");
                    khsDto.setTotal(BigDecimal.ZERO);
                    khsDto.setBobot(BigDecimal.ZERO);
                    khsDto.setTotalBobot(BigDecimal.ZERO);

                }
                if (kd.getNilaiAkhir() != null) {
                    khsDto.setId(kd.getGrade());
                    khsDto.setTotal(kd.getNilaiAkhir());
                    khsDto.setBobot(kd.getBobot());
                    khsDto.setTotalBobot(kd.getNilaiAkhir());
                }
                khsDto.setSks(matakuliahKurikulum.getMatakuliahKurikulum().getJumlahSks());
                khsDto.setKode(matakuliahKurikulum.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());
                khsDto.setMapel(matakuliahKurikulum.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                khsDto.setTugas(kd.getTugas());
                khsDto.setUas(kd.getUas());
                khsDto.setUts(kd.getUts());
                khsDto.setPresensi(kd.getPresensi());
                khsDtos.add(khsDto);
            }
        }
            model.addAttribute("khs",khsDtos);
        } else {
            List<DataKhsDto> krsDetail = krsDetailDao.getKhs(tahunAkademikDao.findByStatus(StatusRecord.AKTIF),mahasiswa);
            List<KhsDto> khsDtos = new ArrayList<>();
            if (!krsDetail.isEmpty() && krsDetail == null) {
                for (DataKhsDto kd : krsDetail) {
                    KhsDto khsDto = new KhsDto();
                    KrsDetail matakuliahKurikulum = krsDetailDao.findById(kd.getId()).get();
                    if (kd.getNilaiAkhir() == null) {
                        khsDto.setId("E");
                        khsDto.setBobot(BigDecimal.ZERO);
                        khsDto.setTotal(BigDecimal.ZERO);
                        khsDto.setTotalBobot(BigDecimal.ZERO);

                    }
                    if (kd.getNilaiAkhir() != null) {
                        khsDto.setId(kd.getGrade());
                        khsDto.setBobot(kd.getBobot());
                        khsDto.setTotal(kd.getNilaiAkhir());
                        khsDto.setTotalBobot(kd.getNilaiAkhir());
                    }
                    khsDto.setSks(matakuliahKurikulum.getMatakuliahKurikulum().getJumlahSks());
                    khsDto.setKode(matakuliahKurikulum.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());
                    khsDto.setMapel(matakuliahKurikulum.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                    khsDto.setTugas(kd.getTugas());
                    khsDto.setUas(kd.getUas());
                    khsDto.setUts(kd.getUts());
                    khsDto.setPresensi(kd.getPresensi());
                    khsDtos.add(khsDto);
                }
            }

            model.addAttribute("khs",khsDtos);
        }


        if (validasiEdom.isEmpty() || validasiEdom == null){
            return "report/khs";
        }else {
            return "redirect:edom";
        }

    }

    @GetMapping("/report/edom")
    public void edom(Authentication authentication, Model model) {
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);

        List<EdomDto> krsDetail = krsDetailDao.cariEdom(mahasiswa, tahunAkademikDao.findByStatus(StatusRecord.AKTIF), StatusRecord.AKTIF, StatusRecord.UNDONE);

        model.addAttribute("detail", krsDetail);
    }

    @PostMapping("/report/edom")
    public String prosesForm(Authentication authentication, HttpServletRequest request, RedirectAttributes attributes) {
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        List<KrsDetail> krsDetail = krsDetailDao.findByMahasiswaAndKrsTahunAkademikAndStatusAndStatusEdom(mahasiswa,tahunAkademikDao.findByStatus(StatusRecord.AKTIF),StatusRecord.AKTIF,StatusRecord.UNDONE);


        Map<String, BigInteger> mapNilaiKpi = new HashMap<>();
        for(KrsDetail daftarEdom : krsDetail) {
            String pertanyaan1 = request.getParameter(daftarEdom.getId() + "1");
            String pertanyaan2 = request.getParameter(daftarEdom.getId() + "2");
            String pertanyaan3 = request.getParameter(daftarEdom.getId() + "3");
            String pertanyaan4 = request.getParameter(daftarEdom.getId() + "4");
            String pertanyaan5 = request.getParameter(daftarEdom.getId() + "5");

            if (pertanyaan1 == null){
                daftarEdom.setE1(Integer.valueOf("3"));
            }else {
                daftarEdom.setE1(Integer.valueOf(pertanyaan1));
            }

            if (pertanyaan2 == null){
                daftarEdom.setE2(Integer.valueOf("3"));
            }else {
                daftarEdom.setE2(Integer.valueOf(pertanyaan2));
            }

            if (pertanyaan3 == null){
                daftarEdom.setE3(Integer.valueOf("3"));
            }else {
                daftarEdom.setE3(Integer.valueOf(pertanyaan3));
            }

            if (pertanyaan4 == null){
                daftarEdom.setE4(Integer.valueOf("3"));
            }else {
                daftarEdom.setE4(Integer.valueOf(pertanyaan4));
            }

            if (pertanyaan5 == null){
                daftarEdom.setE5(Integer.valueOf("3"));
            }else {
                daftarEdom.setE5(Integer.valueOf(pertanyaan5));
            }
            daftarEdom.setStatusEdom(StatusRecord.DONE);
            krsDetailDao.save(daftarEdom);



        }

        return "redirect:khs";

    }

    @GetMapping("/report/transcript")
    public void UserProfile(){

    }


}
