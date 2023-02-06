package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.human.DosenDto;
import id.ac.tazkia.smilemahasiswa.dto.human.JabatanRequest;
import id.ac.tazkia.smilemahasiswa.dto.human.TableJabatan;
import id.ac.tazkia.smilemahasiswa.dto.response.BaseResponse;
import id.ac.tazkia.smilemahasiswa.dto.select2.SelectDosen;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.Valid;
import java.io.File;
import java.util.Arrays;
import java.util.List;
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

    @Autowired
    private JabatanDao jabatanDao;

    @Value("${upload.fotoKaryawan}")
    private String uploadFolder;

    @GetMapping("/select/dosen-jabatan")
    @ResponseBody
    public List<SelectDosen> cariDosenJabatan(@RequestParam String search, String jabatan){

        return dosenDao.searchDosenJabatan(search, Arrays.asList(jabatan));
    }

//Employee

    @GetMapping("/humanResources/employee/list")
    public void daftarKaryawan(Model model, @PageableDefault(size = 10) Pageable page, String search) {
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listkaryawan", karyawanDao.findByStatusAndNamaKaryawanContainingIgnoreCaseOrderByNamaKaryawan(StatusRecord.AKTIF, search, page));
        } else {
            model.addAttribute("listkaryawan", karyawanDao.findByStatusOrderByNamaKaryawan(StatusRecord.AKTIF, page));
        }
    }

    @GetMapping("/humanResources/lecturer/jabatan")
    public void viewJabatan() {

    }

    @GetMapping("/getdata-jabatan")
    @ResponseBody
    public List<JabatanDosen> getDataJabatan() {
        return jabatanDao.findByStatus(StatusRecord.AKTIF);
    }

    @GetMapping("/jabatan/{id}")
    @ResponseBody
    public JabatanDosen getDetailJabatan(@PathVariable String id) {
        return jabatanDao.findById(id).get();
    }

    @PostMapping(value = "/jabatan-post", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse simpanJabatan(@RequestBody JabatanRequest request) {
        JabatanDosen jabatanDosen = new JabatanDosen();
        jabatanDosen.setNama(request.getNama());
        jabatanDosen.setStatusDosen(request.getStatus());
        jabatanDosen.setMinimalSks(request.getMinimal());
        jabatanDosen.setMaksimalSks(request.getMaksimal());
        jabatanDao.save(jabatanDosen);

        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Jabatan Berhasil Disimpan");
        baseResponse.setCode(HttpStatus.CREATED.getReasonPhrase());

        return baseResponse;
    }

    @PostMapping(value = "/jabatan-dosen-post/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse simpanJabatanDosen(@PathVariable String id,@RequestParam List<String> dosenList) {
        JabatanDosen jabatanDosen = jabatanDao.findById(id).get();

        for (String idDosen : dosenList){
            Dosen dosen = dosenDao.findById(idDosen).get();
            dosen.setJabatanDosen(jabatanDosen);
            dosenDao.save(dosen);
        }

        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Dosen Berhasil Disimpan");
        baseResponse.setCode(HttpStatus.CREATED.getReasonPhrase());

        return baseResponse;
    }

    @PutMapping(value = "/jabatan-update", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse updateJabatan(@RequestBody JabatanRequest request) {
        JabatanDosen jabatanDosen = jabatanDao.findById(request.getId()).get();
        jabatanDosen.setNama(request.getNama());
        jabatanDosen.setStatusDosen(request.getStatus());
        jabatanDosen.setMinimalSks(request.getMinimal());
        jabatanDosen.setMaksimalSks(request.getMaksimal());
        jabatanDao.save(jabatanDosen);

        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Jabatan Berhasil Diupdate");
        baseResponse.setCode(HttpStatus.CREATED.getReasonPhrase());

        return baseResponse;
    }

    @PostMapping(value = "/jabatan-delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse deleteJabatan(@PathVariable String id) {
        JabatanDosen jabatanDosen = jabatanDao.findById(id).get();

        List<Dosen> dosen = dosenDao.findByStatusAndJabatanDosen(StatusRecord.AKTIF, jabatanDosen);

        if (dosen.size() > 0) {
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setMessage("Jabatan Gagal Dihapus");
            baseResponse.setCode(String.valueOf(HttpStatus.FORBIDDEN.value()));

            return baseResponse;
        } else {
            jabatanDosen.setStatus(StatusRecord.HAPUS);
            jabatanDao.save(jabatanDosen);

            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setMessage("Jabatan Berhasil Dihapus");
            baseResponse.setCode(String.valueOf(HttpStatus.CREATED.value()));

            return baseResponse;
        }


    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/get-jabatan-list/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableJabatan getDataMatakuliah(@PathVariable String id, @RequestParam(required = false) String search,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(required = false) String order,
                                          @RequestParam(required = false) Integer offset,
                                          @RequestParam(required = false) Integer limit) {
        JabatanDosen jabatanDosen = jabatanDao.findById(id).get();
        if (search.isEmpty()) {
            TableJabatan table = new TableJabatan();
            table.setTotal(dosenDao.countListJabatanDosen(jabatanDosen));
            table.setTotalNotFiltered(dosenDao.countListJabatanDosen(jabatanDosen));
            table.setRows(dosenDao.listJabatanDosen(jabatanDosen, limit, offset));
            return table;

        } else {
            TableJabatan table = new TableJabatan();
            table.setTotal(dosenDao.countListJabatanDosenSearch(jabatanDosen,search));
            table.setTotalNotFiltered(dosenDao.listJabatanDosenSearch(jabatanDosen,limit,offset,search).size());
            table.setRows(dosenDao.listJabatanDosenSearch(jabatanDosen,limit,offset,search));
            return table;

        }

    }

    @GetMapping("/humanResources/employee/form")
    public void formKaryawan(Model model, @RequestParam(required = false) String id) {
        model.addAttribute("karyawan", new Karyawan());
        if (id != null && !id.isEmpty()) {
            Karyawan karyawan = karyawanDao.findById(id).get();
            model.addAttribute("karyawan", karyawan);
        }


    }

    @PostMapping("/humanResources/employee/save")
    public String saveKaryawan(@Valid Karyawan karyawan, BindingResult error, MultipartFile foto) {


        try {
//           persiapan lokasi upload

            String idKaryawan = karyawan.getNamaKaryawan();
            String lokasiUpload = uploadFolder + File.separator + idKaryawan;
            logger.debug("Lokasi upload : {}", lokasiUpload);
            new File(lokasiUpload).mkdirs();

//            if (foto == null || foto.isEmpty()) {
//                logger.info("File berkas kosong, tidak diproses");
//                return idKaryawan;
//            }

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
    public String deleteKaryawan(@Valid Karyawan karyawan) {
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
            model.addAttribute("listDosen", dosenDao.findByStatusNotInAndKaryawanNamaKaryawanContainingIgnoreCaseOrKaryawanNikContainingIgnoreCase(Arrays.asList(StatusRecord.HAPUS), search, search, page));
        } else {
            model.addAttribute("listDosen", dosenDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/humanResources/lecturer/form")
    public void formDosen(Model model, @RequestParam(required = false) String id) {
        model.addAttribute("prodi", prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("karyawan", new DosenDto());
        model.addAttribute("listJabatan", jabatanDao.findByStatus(StatusRecord.AKTIF));

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
                dosenDto.setJabatan(dosenDao.findByKaryawan(karyawan).getJabatanDosen().getId());
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
    public String prosesForm(Model model, @Valid DosenDto dosenDto) {

        Integer idAbesen = karyawanDao.cariIDAbesen();

        if (dosenDto.getId() == null || dosenDto.getId().isEmpty()) {
            User user1 = userDao.findByUsername(dosenDto.getEmail());
            if (user1 == null) {
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
                d.setJabatanDosen(jabatanDao.findById(dosenDto.getJabatan()).get());
                dosenDao.save(d);
            } else {
                Karyawan cekKaryawan = karyawanDao.findByIdUser(user1);
                if (cekKaryawan != null) {
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
                d.setJabatanDosen(jabatanDao.findById(dosenDto.getJabatan()).get());
                d.setStatus(StatusRecord.AKTIF);
                dosenDao.save(d);
            }


        } else {
            Karyawan karyawan = karyawanDao.findById(dosenDto.getId()).get();
            if (dosenDto.getIdUser() == null) {
                User user1 = userDao.findByUsername(dosenDto.getEmail());
                if (user1 == null) {
                    User user = new User();
                    user.setActive(Boolean.TRUE);
                    user.setRole(roleDao.findById("dosen").get());
                    user.setUsername(dosenDto.getEmail());
                    userDao.save(user);
                    karyawan.setIdUser(user);
                } else {
                    model.addAttribute("email", dosenDto.getEmail());
                    return "/humanResources/lecturer/alert";
                }

            }
            if (dosenDto.getIdUser() != null) {
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
    public String delete(@RequestParam Dosen dosen) {
        Karyawan karyawan = karyawanDao.findById(dosen.getKaryawan().getId()).get();
        karyawan.setStatus(StatusRecord.HAPUS);
        karyawan.setIdUser(null);
        karyawanDao.save(karyawan);
        dosen.setStatus(StatusRecord.HAPUS);
        dosenDao.save(dosen);

        return "redirect:list";
    }


}
