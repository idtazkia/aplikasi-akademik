package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Controller
public class UploadController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private UserDao userDao;
    @Autowired
    private CurrentUserService currentUserService;
    @Autowired
    private KaryawanDao karyawanDao;
    @Autowired
    private DosenDao dosenDao;
    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private JadwalDosenDao jadwalDosenDao;
    @Autowired
    private JadwalDao jadwalDao;
    @Autowired
    private SoalDao soalDao;
    @Autowired

    @Value("${upload.soal}")
    private String uploadFolder;

    @Value("classpath:sample/soal.doc")
    private Resource contohSoal;

    @Value("classpath:sample/uas.doc")
    private Resource contohSoalUas;

    @GetMapping("/uploadsoal/listdosen")
    public void listSoalDosen(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        Iterable<JadwalDosen> jadwal = jadwalDosenDao.findByJadwalStatusAndStatusJadwalDosenAndJadwalTahunAkademikAndDosenAndJadwalHariNotNullAndJadwalKelasNotNull(StatusRecord.AKTIF,StatusJadwalDosen.PENGAMPU,tahunAkademik,dosen);
        model.addAttribute("jadwal", jadwal);

    }

    @GetMapping("/uploadsoal/listdosenuas")
    public void listUas(Model model, Authentication authentication){
        listSoalDosen(model,authentication);

    }

    @GetMapping("/uploadsoal/list")
    public void listSoal(Model model, @RequestParam(required = false)TahunAkademik tahun, Pageable page,
                         @RequestParam(required = false)StatusApprove status, @RequestParam(required = false)String search){

        model.addAttribute("akademik", tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(StatusRecord.HAPUS));

        if (tahun != null && status == null) {
            model.addAttribute("tahunAkademik",tahun);
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndDosenKaryawanNamaKaryawanContainingIgnoreCaseOrMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord.AKTIF,tahun,search,search,page));
            } else {
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndJamMulaiNotNullAndHariNotNullAndKelasNotNull(StatusRecord.AKTIF, tahun, page));
            }

        }

        if (tahun != null && status != null){
            model.addAttribute("tahunAkademik",tahun);
            model.addAttribute("status",status);
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndStatusUtsAndDosenKaryawanNamaKaryawanContainingIgnoreCaseOrMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord.AKTIF,tahun,status,search,search,page));
            } else {
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndStatusUtsAndJamMulaiNotNullAndHariNotNullAndKelasNotNull(StatusRecord.AKTIF, tahun,status, page));
            }
        }



    }

    @GetMapping("/uploadsoal/listuas")
    public void listSoalUas(Model model, @RequestParam(required = false)TahunAkademik tahun, Pageable page,
                         @RequestParam(required = false)StatusApprove status, @RequestParam(required = false)String search){

        model.addAttribute("akademik", tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(StatusRecord.HAPUS));

        if (tahun != null && status != null){
            model.addAttribute("status",status);
            model.addAttribute("tahunAkademik",tahun);
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndStatusUasAndDosenKaryawanNamaKaryawanContainingIgnoreCaseOrMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord.AKTIF,tahun,status,search,search,page));
            } else {
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndStatusUasAndJamMulaiNotNullAndHariNotNullAndKelasNotNull(StatusRecord.AKTIF, tahun,status, page));
            }
        }

        if (tahun != null && status == null) {
            model.addAttribute("tahunAkademik",tahun);
            if (StringUtils.hasText(search)) {
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndDosenKaryawanNamaKaryawanContainingIgnoreCaseOrMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord.AKTIF,tahun,search,search,page));
                model.addAttribute("search", search);
            } else {
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndJamMulaiNotNullAndHariNotNullAndKelasNotNull(StatusRecord.AKTIF, tahun, page));
            }

        }

    }

    @GetMapping("/uploadsoal/detail")
    public void detailUpload(@RequestParam Jadwal jadwal,@RequestParam StatusRecord status, Authentication authentication, Model model){

        if (StatusRecord.UTS == status) {
            Iterable<Soal> soal = soalDao.findByJadwalAndStatusAndStatusSoal(jadwal,StatusRecord.AKTIF,StatusRecord.UTS);
            User user = currentUserService.currentUser(authentication);
            Karyawan karyawan = karyawanDao.findByIdUser(user);
            Soal s = soalDao.findByJadwalAndStatusAndStatusApproveAndStatusSoal(jadwal, StatusRecord.AKTIF, StatusApprove.APPROVED,StatusRecord.UTS);
            Dosen dosen = dosenDao.findByKaryawan(karyawan);
            model.addAttribute("jadwal", jadwal);
            model.addAttribute("soal", soal);
            model.addAttribute("dosen", dosen);
            model.addAttribute("approve", s);
            model.addAttribute("status",status);
        }

        if (StatusRecord.UAS == status) {
            Iterable<Soal> soal = soalDao.findByJadwalAndStatusAndStatusSoal(jadwal,StatusRecord.AKTIF,StatusRecord.UAS);
            User user = currentUserService.currentUser(authentication);
            Karyawan karyawan = karyawanDao.findByIdUser(user);
            Soal s = soalDao.findByJadwalAndStatusAndStatusApproveAndStatusSoal(jadwal, StatusRecord.AKTIF, StatusApprove.APPROVED,StatusRecord.UAS);
            Dosen dosen = dosenDao.findByKaryawan(karyawan);
            model.addAttribute("jadwal", jadwal);
            model.addAttribute("soal", soal);
            model.addAttribute("dosen", dosen);
            model.addAttribute("status",status);
            model.addAttribute("approve", s);
        }

    }

    @GetMapping("/contoh/soal")
    public void contohUts(HttpServletResponse response) throws Exception {
        response.setContentType("application/msword");
        response.setHeader("Content-Disposition", "attachment; filename=Template-Soal.doc");
        FileCopyUtils.copy(contohSoal.getInputStream(), response.getOutputStream());
        response.getOutputStream().flush();
    }

    @GetMapping("/contoh/soaluas")
    public void contohUas(HttpServletResponse response) throws Exception {
        response.setContentType("application/msword");
        response.setHeader("Content-Disposition", "attachment; filename=Template-Soal.doc");
        FileCopyUtils.copy(contohSoalUas.getInputStream(), response.getOutputStream());
        response.getOutputStream().flush();
    }

    @PostMapping("/uploadsoal/detail")
    public String uploadBukti(@Valid Soal soal,
                              BindingResult error, MultipartFile file,
                              Authentication currentUser) throws Exception {


        String namaFile = file.getName();
        String jenisFile = file.getContentType();
        String namaAsli = file.getOriginalFilename();
        Long ukuran = file.getSize();

        System.out.println("Nama File : {}" + namaFile);
        System.out.println("Jenis File : {}" + jenisFile);
        System.out.println("Nama Asli File : {}" + namaAsli);
        System.out.println("Ukuran File : {}"+ ukuran);

//        memisahkan extensi
        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }


        String idFile = UUID.randomUUID().toString();
        String lokasiUpload = uploadFolder + File.separator + soal.getJadwal().getId();
        LOGGER.debug("Lokasi upload : {}", lokasiUpload);
        new File(lokasiUpload).mkdirs();
        File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
        file.transferTo(tujuan);
        LOGGER.debug("File sudah dicopy ke : {}", tujuan.getAbsolutePath());


        soal.setStatus(StatusRecord.AKTIF);
        soal.setTanggalUpload(LocalDate.now());
        soal.setStatusApprove(StatusApprove.WAITING);
        soal.setFileUpload(idFile + "." + extension);

        Soal s = soalDao.findByJadwalAndStatusAndStatusApproveNotIn(soal.getJadwal(),StatusRecord.AKTIF,StatusApprove.REJECTED);
        if (s == null){
            soalDao.save(soal);
        }

        if (s != null){
            s.setStatus(StatusRecord.NONAKTIF);
            soalDao.save(s);
            soalDao.save(soal);
        }


        Jadwal jadwal = jadwalDao.findById(soal.getJadwal().getId()).get();
        if (soal.getStatusSoal() == StatusRecord.UTS) {
            jadwal.setStatusUts(StatusApprove.WAITING);
        }
        if (soal.getStatusSoal() == StatusRecord.UAS) {
            jadwal.setStatusUas(StatusApprove.WAITING);
        }
        jadwalDao.save(jadwal);

        return "redirect:detail?jadwal=" +soal.getJadwal().getId()+"&status="+soal.getStatusSoal();

    }

    @GetMapping("/uploadsoal/approval")
    public void approvalList(Model model,@RequestParam Jadwal jadwal){
        model.addAttribute("jadwal",jadwal);
        model.addAttribute("soal",soalDao.findByJadwalAndStatusAndStatusApproveNotIn(jadwal,StatusRecord.AKTIF,StatusApprove.REJECTED));
    }

    @PostMapping("/uploadsoal/approval")
    public String prosesApprove(@RequestParam Soal soal,MultipartFile file) throws Exception {


        String namaFile = file.getName();
        String jenisFile = file.getContentType();
        String namaAsli = file.getOriginalFilename();
        Long ukuran = file.getSize();

        System.out.println("Nama File : {}" + namaFile);
        System.out.println("Jenis File : {}" + jenisFile);
        System.out.println("Nama Asli File : {}" + namaAsli);
        System.out.println("Ukuran File : {}"+ ukuran);

//        memisahkan extensi
        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }


        String idFile = soal.getJadwal().getTahunAkademik().getKodeTahunAkademik()+"-UTS-"+soal.getJadwal().getKelas().getNamaKelas()+"-"+soal.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah()+"-"+soal.getDosen().getKaryawan().getNamaKaryawan();
        idFile = idFile.replaceAll(" ", "-").toLowerCase();
        String lokasiUpload = uploadFolder + File.separator + soal.getJadwal().getId();
        LOGGER.debug("Lokasi upload : {}", lokasiUpload);
        new File(lokasiUpload).mkdirs();
        File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
        file.transferTo(tujuan);
        LOGGER.debug("File sudah dicopy ke : {}", tujuan.getAbsolutePath());
        soal.setFileApprove(idFile + "." + extension);
        soal.setStatusApprove(StatusApprove.APPROVED);
        soal.setKeteranganApprove(soal.getKeterangan());
        soalDao.save(soal);

        Jadwal jadwal =jadwalDao.findById(soal.getJadwal().getId()).get();
        if (soal.getStatusSoal() == StatusRecord.UTS) {
            jadwal.setStatusUts(StatusApprove.APPROVED);
        }
        if (soal.getStatusSoal() == StatusRecord.UAS) {
            jadwal.setStatusUas(StatusApprove.APPROVED);
        }
        jadwalDao.save(jadwal);

        if (soal.getStatusSoal() == StatusRecord.UAS){
            return "redirect:listuas?tahun="+jadwal.getTahunAkademik().getId()+"&status="+StatusApprove.APPROVED;
        }
        return "redirect:list?tahun="+jadwal.getTahunAkademik().getId()+"&status="+StatusApprove.APPROVED;
    }

    @PostMapping("/uploadsoal/rejected")
    public String prosesReject(@RequestParam Soal soal,@RequestParam(required = false) String keteranganApprove){
        soal.setStatusApprove(StatusApprove.REJECTED);
        soal.setKeteranganApprove(keteranganApprove);
        soalDao.save(soal);

        Jadwal jadwal = jadwalDao.findById(soal.getJadwal().getId()).get();
        if (soal.getStatusSoal() == StatusRecord.UTS) {
            jadwal.setStatusUts(StatusApprove.REJECTED);
        }
        if (soal.getStatusSoal() == StatusRecord.UAS) {
            jadwal.setStatusUas(StatusApprove.REJECTED);
        }
        jadwalDao.save(jadwal);

        if (soal.getStatusSoal() == StatusRecord.UAS) {
            return "redirect:listuas    ?tahun="+jadwal.getTahunAkademik().getId()+"&status="+StatusApprove.REJECTED;
        }

        return "redirect:list?tahun="+jadwal.getTahunAkademik().getId()+"&status="+StatusApprove.REJECTED;
    }

    @RequestMapping("/file/{fileName}")
    public void downloadPDFResource( HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam Soal soal,
                                     @PathVariable("fileName") String fileName)
    {
        //If user is not authorized - he should be thrown out from here itself

        //Authorized user will download the file
        String lokasi = uploadFolder+File.separator+soal.getJadwal().getId();
        String dataDirectory = request.getServletContext().getRealPath(lokasi);
        Path file = Paths.get(lokasi, fileName);
        System.out.println(file);
        if (Files.exists(file))
        {
            response.setContentType("application/application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.addHeader("Content-Disposition", "attachment; filename="+fileName);
            try
            {
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @RequestMapping("/fileUpload")
    public void downloadPDF ( HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam Jadwal jadwal)
    {
        //If user is not authorized - he should be thrown out from here itself
        Soal soal = soalDao.findByJadwalAndStatusAndStatusApprove(jadwal,StatusRecord.AKTIF,StatusApprove.APPROVED);
        String lokasi = uploadFolder+File.separator+jadwal.getId();
        String dataDirectory = request.getServletContext().getRealPath(lokasi);
        Path file = Paths.get(lokasi, soal.getFileApprove());
        System.out.println(file);
        if (Files.exists(file))
        {
            response.setContentType("application/application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.addHeader("Content-Disposition", "attachment; filename="+soal.getFileApprove());

            try
            {
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
            }

            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @PostMapping("/uploadsoal/cancel")
    public String cancel (@RequestParam Jadwal jadwal)
    {
        Soal soal = soalDao.findByJadwalAndStatusAndStatusApprove(jadwal,StatusRecord.AKTIF,StatusApprove.APPROVED);
        soal.setStatusApprove(StatusApprove.REJECTED);
        soal.setKeteranganApprove("Dibatalkan");
        soalDao.save(soal);

        jadwal.setStatusUts(StatusApprove.REJECTED);
        jadwalDao.save(jadwal);

        return "redirect:list?tahun="+jadwal.getTahunAkademik().getId()+"&status="+StatusApprove.APPROVED;

    }


}
