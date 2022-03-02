package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.human.DosenDto;
import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.Valid;
import java.io.File;
import java.util.Arrays;
import java.util.SplittableRandom;
import java.util.UUID;

@Controller
public class HumanResourcesController {

    private static final Logger logger = LoggerFactory.getLogger(HumanResourcesController.class);


    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private RoleDao roleDao;

    @Value("${upload.fotoKaryawan}")
    private String uploadFolder;

//Employee

    @GetMapping("/humanResources/employee/list")
    public void daftarKaryawan(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listkaryawan", karyawanDao.findByStatusAndNamaKaryawanContainingIgnoreCaseOrderByNamaKaryawan(StatusRecord.AKTIF, search, page));
        }else{
            model.addAttribute("listkaryawan", karyawanDao.findByStatusOrderByNamaKaryawan(StatusRecord.AKTIF, page));
        }
    }

    @GetMapping("/humanResources/employee/form")
    public void  formKaryawan(Model model, @RequestParam(required = false) String id){
        model.addAttribute("karyawan", new Karyawan());

        if (id != null && !id.isEmpty()) {
            Karyawan karyawan = karyawanDao.findById(id).get();
            model.addAttribute("karyawan" , karyawan);
        }


    }

    @PostMapping("/humanResources/employee/save")
    public String saveKaryawan(@Valid Karyawan karyawan, BindingResult error, MultipartFile foto){


        try {
//           persiapan lokasi upload

            String idKaryawan = karyawan.getNamaKaryawan();
            String lokasiUpload = uploadFolder + File.separator + idKaryawan;
            logger.debug("Lokasi upload : {}", lokasiUpload);
            new File(lokasiUpload).mkdirs();

            if (foto == null || foto.isEmpty()) {
                logger.info("File berkas kosong, tidak diproses");
                return idKaryawan;
            }

            String namaFile = foto.getName();
            String jenisFile = foto.getContentType();
            String namaAsli = foto.getOriginalFilename();
            Long ukuran = foto.getSize();

            logger.debug("Nama File : {}", namaFile);
            logger.debug("Jenis File : {}", jenisFile);
            logger.debug("Nama Asli File : {}", namaAsli);
            logger.debug("Ukuran File : {}", ukuran);

            //memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            karyawan.setFoto(idFile + "." + extension);
            File tujuan = new File(lokasiUpload + File.separator + karyawan.getFoto());
            Integer idAbesen = karyawanDao.cariIDAbesen();
            karyawan.setIdAbsen(idAbesen);
            foto.transferTo(tujuan);
            logger.debug("File sudah dicopy ke : {}", tujuan.getAbsolutePath());
            karyawanDao.save(karyawan);


        } catch (Exception er) {
            logger.error(er.getMessage(), er);
        }

        return "redirect:list";
    }

    @PostMapping("/humanResources/employee/delete")
    public String deleteKaryawan(@Valid Karyawan karyawan){
        karyawan.setStatus(StatusRecord.HAPUS);
        karyawan.setIdAbsen(karyawan.getIdAbsen());
        karyawanDao.save(karyawan);
        return "redirect:list";
    }

//    Lecturer

    @GetMapping("/humanResources/lecturer/list")
    public void gedungList(Model model, Pageable page, String search) {

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listDosen", dosenDao.findByStatusNotInAndKaryawanNamaKaryawanContainingIgnoreCaseOrKaryawanNikContainingIgnoreCase(Arrays.asList(StatusRecord.HAPUS), search,search,page));
        } else {
            model.addAttribute("listDosen", dosenDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS),page));
        }
    }

    @GetMapping("/humanResources/lecturer/form")
    public void   formDosen(Model model,@RequestParam(required = false) String id){
        model.addAttribute("prodi", prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("karyawan", new DosenDto());

        if (id != null && !id.isEmpty()) {
            Karyawan karyawan = karyawanDao.findById(id).get();
            if (karyawan != null) {
                DosenDto dosenDto = new DosenDto();
                dosenDto.setEmail(karyawan.getEmail());
                dosenDto.setGelar(karyawan.getGelar());
                dosenDto.setId(karyawan.getId());
                dosenDto.setJenisKelamin(karyawan.getJenisKelamin());
                dosenDto.setNama(karyawan.getNamaKaryawan());
                dosenDto.setNidn(karyawan.getNidn());
                dosenDto.setNik(karyawan.getNik());
                dosenDto.setStatusDosen(dosenDao.findByKaryawan(karyawan).getStatusDosen());
                dosenDto.setHonor(dosenDao.findByKaryawan(karyawan).getHonor());
                dosenDto.setRfid(karyawan.getRfid());
                dosenDto.setAbsen(karyawan.getIdAbsen());
                if (karyawan.getIdUser() != null) {
                    dosenDto.setIdUser(karyawan.getIdUser());
                }
                dosenDto.setProdi(dosenDao.findByKaryawan(karyawan).getProdi().getId());
                dosenDto.setTanggalLahir(karyawan.getTanggalLahir());
                model.addAttribute("karyawan", dosenDto);
                model.addAttribute("dosen", dosenDao.findByKaryawan(karyawan));
            }
        }

    }

    @PostMapping("/humanResources/lecturer/form")
    public String prosesForm(Model model ,@Valid DosenDto dosenDto){

        Integer idAbesen = karyawanDao.cariIDAbesen();

        if (dosenDto.getId() == null || dosenDto.getId().isEmpty()) {
            User user1 = userDao.findByUsername(dosenDto.getEmail());
            if (user1 == null){
                User user = new User();
                user.setActive(Boolean.TRUE);
                user.setRole(roleDao.findById("dosen").get());
                user.setUsername(dosenDto.getEmail());
                userDao.save(user);

                //            SplittableRandom splittableRandom = new SplittableRandom();
//            int randomWithSplittableRandom = splittableRandom.nextInt(1, 999999);




                Karyawan karyawan = new Karyawan();
                karyawan.setEmail(dosenDto.getEmail());
                karyawan.setGelar(dosenDto.getGelar());
                karyawan.setJenisKelamin(dosenDto.getJenisKelamin());
                karyawan.setNamaKaryawan(dosenDto.getNama());
                karyawan.setNidn(dosenDto.getNidn());
                karyawan.setIdUser(user);
                karyawan.setNik(dosenDto.getNik());
                karyawan.setRfid(dosenDto.getRfid());
                karyawan.setTanggalLahir(dosenDto.getTanggalLahir());
                karyawan.setIdAbsen(idAbesen);
                karyawanDao.save(karyawan);

                Dosen d = new Dosen();
                d.setProdi(prodiDao.findById(dosenDto.getProdi()).get());
                d.setKaryawan(karyawan);
                d.setStatusDosen(dosenDto.getStatusDosen());
                d.setHonor(dosenDto.getHonor());
                d.setStatus(StatusRecord.AKTIF);
                dosenDao.save(d);
            }else {
                Karyawan cekKaryawan = karyawanDao.findByIdUser(user1);
                if (cekKaryawan != null){
                    model.addAttribute("email", dosenDto.getEmail());
                    return "/humanResources/lecturer/alert";
                }

                Karyawan karyawan = new Karyawan();
                karyawan.setEmail(dosenDto.getEmail());
                karyawan.setGelar(dosenDto.getGelar());
                karyawan.setJenisKelamin(dosenDto.getJenisKelamin());
                karyawan.setNamaKaryawan(dosenDto.getNama());
                karyawan.setNidn(dosenDto.getNidn());
                karyawan.setIdUser(user1);
                karyawan.setNik(dosenDto.getNik());
                karyawan.setRfid(dosenDto.getRfid());
                karyawan.setTanggalLahir(dosenDto.getTanggalLahir());
                karyawan.setIdAbsen(idAbesen);
                karyawanDao.save(karyawan);

                Dosen d = new Dosen();
                d.setProdi(prodiDao.findById(dosenDto.getProdi()).get());
                d.setKaryawan(karyawan);
                d.setStatusDosen(dosenDto.getStatusDosen());
                d.setHonor(dosenDto.getHonor());
                d.setStatus(StatusRecord.AKTIF);
                dosenDao.save(d);
            }



        }else {
            Karyawan karyawan = karyawanDao.findById(dosenDto.getId()).get();
            if (dosenDto.getIdUser() == null){
                User user1 = userDao.findByUsername(dosenDto.getEmail());
                if (user1 == null){
                    User user = new User();
                    user.setActive(Boolean.TRUE);
                    user.setRole(roleDao.findById("dosen").get());
                    user.setUsername(dosenDto.getEmail());
                    userDao.save(user);
                    karyawan.setIdUser(user);
                }else {
                    model.addAttribute("email", dosenDto.getEmail());
                    return "/humanResources/lecturer/alert";
                }

            }
            if (dosenDto.getIdUser() != null){
                karyawan.setIdUser(dosenDto.getIdUser());
                User user = userDao.findById(dosenDto.getIdUser().getId()).get();
                user.setUsername(dosenDto.getEmail());
                userDao.save(user);
            }

//            SplittableRandom splittableRandom = new SplittableRandom();
//            int randomWithSplittableRandom = splittableRandom.nextInt(1, 999999);


            karyawan.setEmail(dosenDto.getEmail());
            karyawan.setGelar(dosenDto.getGelar());
            karyawan.setJenisKelamin(dosenDto.getJenisKelamin());
            karyawan.setNamaKaryawan(dosenDto.getNama());
            karyawan.setNidn(dosenDto.getNidn());
            karyawan.setTanggalLahir(dosenDto.getTanggalLahir());
            karyawan.setRfid(dosenDto.getRfid());
            karyawan.setNik(dosenDto.getNik());

            karyawan.setIdAbsen(dosenDto.getAbsen());

            karyawanDao.save(karyawan);

            Dosen dosen = dosenDao.findByKaryawan(karyawan);
            dosen.setStatusDosen(dosenDto.getStatusDosen());
            dosen.setHonor(dosenDto.getHonor());
            dosen.setProdi(prodiDao.findById(dosenDto.getProdi()).get());
            dosenDao.save(dosen);
        }


        return "redirect:list";
    }

    @PostMapping("/humanResources/lecturer/delete")
    public String delete(@RequestParam Dosen dosen){
        Karyawan karyawan = karyawanDao.findById(dosen.getKaryawan().getId()).get();
        karyawan.setStatus(StatusRecord.HAPUS);
        karyawan.setIdUser(null);
        karyawanDao.save(karyawan);
        dosen.setStatus(StatusRecord.HAPUS);
        dosenDao.save(dosen);

        return "redirect:list";
    }

    public void uploadFotoKaryawan(@ModelAttribute @Valid Karyawan karyawan, BindingResult bindingResult,
                                   MultipartFile foto){

        try {

//           persiapan lokasi upload

            String idKaryawan = karyawan.getNamaKaryawan();
            String lokasiUpload = uploadFolder + File.separator + idKaryawan;
            logger.debug("Lokasi upload : {}", lokasiUpload);
            new File(lokasiUpload).mkdirs();

            if (foto == null || foto.isEmpty()) {
                logger.info("File berkas kosong, tidak diproses");
                return;
            }

            String namaFile = foto.getName();
            String jenisFile = foto.getContentType();
            String namaAsli = foto.getOriginalFilename();
            Long ukuran = foto.getSize();

            logger.debug("Nama File : {}", namaFile);
            logger.debug("Jenis File : {}", jenisFile);
            logger.debug("Nama Asli File : {}", namaAsli);
            logger.debug("Ukuran File : {}", ukuran);

            //memisahkan extensi
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            karyawan.setFoto(idFile + "." + extension);
            File tujuan = new File(lokasiUpload + File.separator + karyawan.getFoto());
            foto.transferTo(tujuan);
            logger.debug("File sudah dicopy ke : {}", tujuan.getAbsolutePath());
            karyawanDao.save(karyawan);

        } catch (Exception er) {
            logger.error(er.getMessage(), er);
        }

    }



}
