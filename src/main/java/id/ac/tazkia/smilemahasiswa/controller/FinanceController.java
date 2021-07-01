package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.payment.SisaTagihanDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.apache.commons.math3.geometry.enclosing.EnclosingBall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
public class FinanceController {

    @Autowired
    private KrsDao krsDao;
    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private MahasiswaDao mahasiswaDao;
    @Autowired
    private ProgramDao programDao;
    @Autowired
    private ProdiDao prodiDao;
    @Autowired
    private TahunProdiDao tahunAkademikProdiDao;
    @Autowired
    private EnableFitureDao enableFitureDao;
    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired
    private TagihanDao tagihanDao;


    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }

    @ModelAttribute("program")
    public Iterable<Program> program() {
        return programDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @ModelAttribute("tahun")
    public Iterable<TahunAkademik> tahun() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

    @GetMapping("/activation/krs")
    public void aktifasiKrs(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik,
                            @RequestParam(required = false) String nim){


        Mahasiswa mhsw = mahasiswaDao.findByNim(nim);
        if (mhsw != null){
            Krs krs = krsDao.findByTahunAkademikAndMahasiswaAndStatus(tahunAkademik,mhsw,StatusRecord.AKTIF);
            model.addAttribute("krsMahasiswa", krs);
        }else {
            model.addAttribute("message", "nim tidak ada");
        }
        model.addAttribute("selectedTahun", tahunAkademik);
        model.addAttribute("selectedNim", nim);


    }

    @PostMapping("/activation/process")
    public String prosesKrs(@RequestParam TahunAkademik tahunAkademik,
                            @RequestParam(required = false) String nim){
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

        if (mahasiswa != null){
            Krs cariKrs = krsDao.findByTahunAkademikAndMahasiswaAndStatus(tahunAkademik, mahasiswa,StatusRecord.AKTIF);
            TahunAkademikProdi tahunAkademikProdi = tahunAkademikProdiDao.findByTahunAkademikAndProdi(tahunAkademik, mahasiswa.getIdProdi());
            if (cariKrs == null) {
                Krs krs = new Krs();
                krs.setTanggalTransaksi(LocalDateTime.now());
                krs.setStatus(StatusRecord.AKTIF);
                krs.setTahunAkademik(tahunAkademik);
                krs.setNim(mahasiswa.getNim());
                krs.setProdi(mahasiswa.getIdProdi());
                krs.setMahasiswa(mahasiswa);
                krs.setTahunAkademikProdi(tahunAkademikProdi);
                krsDao.save(krs);

                List<KrsDetail> cek = krsDetailDao.findByMahasiswaAndTahunAkademikAndStatusAndKrsNull(mahasiswa, tahunAkademik, StatusRecord.AKTIF);
                if (!cek.isEmpty() || cek != null){
                    for (KrsDetail krsDetail : cek){
                        krsDetail.setKrs(krs);
                        krsDetailDao.save(krsDetail);
                    }
                }

            }else {
                cariKrs.setStatus(StatusRecord.AKTIF);
                krsDao.save(cariKrs);

                List<KrsDetail> cek = krsDetailDao.findByMahasiswaAndTahunAkademikAndStatusAndKrsNull(mahasiswa, tahunAkademik, StatusRecord.AKTIF);
                if (!cek.isEmpty() || cek != null){
                    for (KrsDetail krsDetail : cek){
                        krsDetail.setKrs(cariKrs);
                        krsDetailDao.save(krsDetail);
                    }
                }
            }
        }
        return "redirect:krs?mahasiswa=AKTIF" + "&tahunAkademik=" + tahunAkademik.getId()+"&nim="+nim;

    }

    @GetMapping("/activation/kartu")
    public void aktifasiKrs(Model model,
                            @RequestParam(required = false) TahunAkademik tahunAkademik,@RequestParam(required = false) String status,
                            @RequestParam(required = false) String nim,@RequestParam(required = false) String uas){

        model.addAttribute("selectedTahun", tahunAkademik);
        model.addAttribute("selectedNim", nim);
        Mahasiswa mhsw = mahasiswaDao.findByNim(nim);
        model.addAttribute("mahasiswa", mhsw);
        model.addAttribute("status", status);

    }

    @PostMapping("/activation/kartu")
    public String prosesKrs(@RequestParam TahunAkademik tahunAkademik,
                            @RequestParam(required = false) String nim,@RequestParam(required = false) String status){

        if (status == "UTS") {
            Mahasiswa mhsw = mahasiswaDao.findByNim(nim);
            EnableFiture validasiFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhsw,
                    StatusRecord.UTS,true,tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            if (validasiFiture == null) {
                EnableFiture enableFiture = new EnableFiture();
                enableFiture.setEnable(true);
                enableFiture.setFitur(StatusRecord.UTS);
                enableFiture.setKeterangan("-");
                enableFiture.setMahasiswa(mhsw);
                enableFiture.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
                enableFitureDao.save(enableFiture);
            }

        }else {
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            EnableFiture validasiFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mahasiswa,
                    StatusRecord.UAS, true, tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            if (validasiFiture == null) {
                EnableFiture enableFiture = new EnableFiture();
                enableFiture.setEnable(true);
                enableFiture.setFitur(StatusRecord.UAS);
                enableFiture.setMahasiswa(mahasiswa);
                enableFiture.setKeterangan("-");
                enableFiture.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
                enableFitureDao.save(enableFiture);
            }
        }

        return "redirect:kartu?tahunAkademik=" + tahunAkademik.getId()+"&nim="+nim+"&status="+status;

    }

    @GetMapping("/activation/rfid")
    public void rfid(@RequestParam(required = false) String nim, @RequestParam(required = false) String rfid, Model model){
        model.addAttribute("nim", nim);
        model.addAttribute("rfid", rfid);

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

        if (mahasiswa != null){
            model.addAttribute("mahasiswa", mahasiswa);
        }
    }

    @PostMapping("/activation/rfid")
    public String prosesRfid(@RequestParam String nim,@RequestParam String rfid){

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        mahasiswa.setRfid(rfid);
        mahasiswaDao.save(mahasiswa);

        return "redirect:rfid?nim="+nim+"&rfid="+rfid;
    }

    @GetMapping("/activation/cicilan")
    public void cicilan(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) String nim, @PageableDefault(size = 10) Pageable page){

        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        if (mhs != null){
            model.addAttribute("tagihanMahasiswa", tagihanDao.findByStatusNotInAndMahasiswaAndTahunAkademik(Arrays.asList(StatusRecord.HAPUS), mhs, tahunAkademik, page));
        }else {
            model.addAttribute("message", "nim tidak ada");
        }
        model.addAttribute("selectTahun", tahunAkademik);
        model.addAttribute("selectNim", nim);
    }

    @PostMapping("/activation/cicilan")
    public String prosesCicilan(@RequestParam TahunAkademik tahunAkademik, @RequestParam(required = false) String nim, RedirectAttributes attributes){
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        if (mhs != null) {
            EnableFiture cekEnableFitur = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.CICILAN, true, tahunAkademik);
            if (cekEnableFitur == null) {
                EnableFiture enableFiture = new EnableFiture();
                enableFiture.setFitur(StatusRecord.CICILAN);
                enableFiture.setEnable(true);
                enableFiture.setKeterangan("-");
                enableFiture.setMahasiswa(mhs);
                enableFiture.setTahunAkademik(tahunAkademik);
                enableFitureDao.save(enableFiture);
            }else{
                attributes.addFlashAttribute("gagal", "data sudah ada!");
                return "redirect:cicilan?tahunAkademik="+tahunAkademik.getId()+"&nim="+nim;
            }
        }

        return "redirect:cicilan?tahunAkademik="+tahunAkademik.getId()+"&nim="+nim;
    }

}
