package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public void aktifasiKrs(Model model, @RequestParam(required = false) String mahasiswa, Pageable pageable,
                            @RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) Prodi prodi,
                            @RequestParam(required = false) Program program, @RequestParam(required = false) String angkatan,
                            @RequestParam(required = false) String nim){
        model.addAttribute("validasi", mahasiswa);

        if (nim == null){
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("selectedProdi", prodi);
            model.addAttribute("selectedProgram", program);
            model.addAttribute("selectedAngkatan", angkatan);
            Page<Krs> krs = krsDao.findByTahunAkademikAndProdiAndMahasiswaIdProgramAndMahasiswaAngkatan(tahunAkademik,prodi,program,angkatan,pageable);
            model.addAttribute("krs",krs);
        }else {
            Mahasiswa mhsw = mahasiswaDao.findByNim(nim);
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("selectedNim", nim);
            Krs krs = krsDao.findByTahunAkademikAndMahasiswaAndStatus(tahunAkademik,mhsw,StatusRecord.AKTIF);
            model.addAttribute("krsMahasiswa", krs);
        }
    }

    @PostMapping("/activation/process")
    public String prosesKrs(@RequestParam TahunAkademik tahunAkademik, @RequestParam(required = false) Prodi prodi,
                            @RequestParam(required = false) Program program, @RequestParam(required = false) String angkatan,
                            @RequestParam(required = false) String nim){

        if (nim == null){
            List<Mahasiswa> mahasiswas = mahasiswaDao.findByStatusAndAngkatanAndIdProdiAndIdProgram(StatusRecord.AKTIF, angkatan, prodi, program);
            for (Mahasiswa mahasiswa : mahasiswas){
                Krs cariKrs = krsDao.findByTahunAkademikAndMahasiswaAndStatus(tahunAkademik,mahasiswa,StatusRecord.AKTIF);
                if (cariKrs == null){
                    TahunAkademikProdi tahunAkademikProdi = tahunAkademikProdiDao.findByTahunAkademikAndProdi(tahunAkademik,prodi);
                    Krs krs = new Krs();
                    krs.setTanggalTransaksi(LocalDateTime.now());
                    krs.setStatus(StatusRecord.AKTIF);
                    krs.setTahunAkademik(tahunAkademik);
                    krs.setNim(mahasiswa.getNim());
                    krs.setProdi(prodi);
                    krs.setMahasiswa(mahasiswa);
                    krs.setTahunAkademikProdi(tahunAkademikProdi);
                    krsDao.save(krs);
                }
            }
            return "redirect:krs?tahunAkademik=" + tahunAkademik.getId()+"&prodi="+prodi.getId()+"&program="+program.getId()+"&angkatan="+angkatan;

        } else {
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
                }else {
                    cariKrs.setStatus(StatusRecord.AKTIF);
                    krsDao.save(cariKrs);
                }
            }
            return "redirect:krs?mahasiswa=AKTIF" + "&tahunAkademik=" + tahunAkademik.getId()+"&nim="+nim;
        }
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
            EnableFiture validasiFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhsw,StatusRecord.UTS,"1",tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
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
            EnableFiture validasiFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mahasiswa, StatusRecord.UAS, "1", tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
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
}
