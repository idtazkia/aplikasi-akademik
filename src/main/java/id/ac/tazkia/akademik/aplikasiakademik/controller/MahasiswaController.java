package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.constants.StatusConstants;
import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.MahasiswaDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.RegistrasiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class MahasiswaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MahasiswaController.class);


    @Autowired
    MahasiswaAkademikDao mahasiswaAkademikDao;

    @Autowired
    MahasiswaDao mahasiswaDao;

    @Autowired
    SekolahDao sekolahDao;

    @Autowired
    MahasiswaKostDao mahasiswaKostDao;

    @Autowired
    MahasiswaOrtuDao mahasiswaOrtuDao;

    @Autowired
    MahasiswaSekolahDao mahasiswaSekolahDao;

    @Autowired
    ProvinsiDao provinsiDao;

    @Autowired
    KokabDao kokabDao;

    @Autowired
    FakultasDao fakultasDao;

    @Autowired
    JenjangDao jenjangDao;

    @Autowired
    ProdiDao prodiDao;

    @Autowired
    UserDao userDao;

    @Autowired
    UserPasswordDao userPasswordDao;

    @Autowired
    RoleDao roleDao;

    @Autowired
    AgamaDao agamaDao;

    @Autowired
    KonsentrasiDao konsentrasiDao;

    @Autowired
    RegistrasiService registrasiService;

    @Value("${mahasiswa.folder}")
    private String uploadFolder;


    @Autowired
    private PasswordEncoder passwordEncoder;


    @ModelAttribute("daftarProp")
    public Iterable<Provinsi> daftaProp() {
        return provinsiDao.findAll();
    }

    @ModelAttribute("daftarFak")
    public Iterable<Fakultas> daftarFak() {
        return fakultasDao.findByStatus(StatusConstants.Aktif);
    }

    @ModelAttribute("daftarJenjang")
    public Iterable<Jenjang> daftarJenjang() {
        return jenjangDao.findByStatus(StatusConstants.Aktif);
    }

    @ModelAttribute("daftarAgama")
    public Iterable<Agama> daftarAgama() {
        return agamaDao.findAll();
    }

    @ModelAttribute("daftarProdi")
    public Iterable<Prodi> daftarProdi() {
        return prodiDao.findByStatus(StatusConstants.Aktif);
    }

    @ModelAttribute("daftarKonsen")
    public Iterable<Konsentrasi> daftarKonsen() {
        return konsentrasiDao.findByStatus(StatusConstants.Aktif);
    }

    @ModelAttribute("daftarSekolah")
    public Iterable<Sekolah> daftarSekolah() {
        return sekolahDao.findAll();
    }

    @ModelAttribute("daftarKokab")
    public Iterable<KabupatenKota> daftaKokab() {
        return kokabDao.findAll();
    }

    @GetMapping("/mahasiswa/list")
    public void daftarMahasiswa(){
    }

    @GetMapping("/mahasiswa/form")
    public void MahasiswaForm (){
    }

    @PostMapping("/mahasiswa/form")
    public String  formMahasiswa(@Valid MahasiswaDto data, BindingResult error, MultipartFile foto,
                                 Authentication currentUser) throws Exception {
        LOGGER.debug("Authentication class : {}", currentUser.getClass().getName());

        if (currentUser == null) {
            LOGGER.warn("Current user is null");
        }

        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User u = userDao.findByUsername(username);
        LOGGER.debug("User ID : {}", u.getId());
        if (u == null) {
            LOGGER.warn("Username {} not found in database ", username);
        }

        String namaFile = foto.getName();
        String jenisFile = foto.getContentType();
        String namaAsli = foto.getOriginalFilename();
        Long ukuran = foto.getSize();

        LOGGER.debug("Nama File : {}", namaFile);
        LOGGER.debug("Jenis File : {}", jenisFile);
        LOGGER.debug("Nama Asli File : {}", namaAsli);
        LOGGER.debug("Ukuran File : {}", ukuran);

//        memisahkan extensi
        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }

        String idFile = UUID.randomUUID().toString();
        String lokasiUpload = uploadFolder;
        LOGGER.debug("Lokasi upload : {}", lokasiUpload);
        new File(lokasiUpload).mkdirs();
        File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
        foto.transferTo(tujuan);
        LOGGER.debug("File sudah dicopy ke : {}", tujuan.getAbsolutePath());

        System.out.println(data.getFoto());

        String formatNim = data.getTahunAngkatan() + data.getIdFakultas().getKodeFakultas() + data.getIdProdi().getKodeProdi();

        Mahasiswa mahasiswa = new Mahasiswa();
        UserPassword userPassword = new UserPassword();
        MhswAkademik mhswAkademik = new MhswAkademik();
        MhswKost mhswKost = new MhswKost();
        MhswOrtu mhswOrtu = new MhswOrtu();
        MhswSekolahAsal mhswSekolahAsal = new MhswSekolahAsal();
        Role role = roleDao.findById("mahasiswa").get();

        mahasiswa.setNim(registrasiService.generateNomorRegistrasi(formatNim));
        mahasiswa.setNama(data.getNama());
        mahasiswa.setJk(data.getJk());
        mahasiswa.setAsalNegara(data.getAsalNegara());
        mahasiswa.setTmpLahir(data.getTempatLahir());
        mahasiswa.setTglLahir(data.getTglLahir());
        mahasiswa.setAgama(data.getAgama());
        mahasiswa.setStatusSipil(data.getStatusSipil());
        mahasiswa.setAlamat(data.getAlamat());
        mahasiswa.setKokab(data.getKokabMahasiswa());
        mahasiswa.setKodepos(data.getKodeposmhas());
        mahasiswa.setProvinsi(data.getProvinsiMahasiswa());
        mahasiswa.setNegara(data.getNegaraMahasiswa());
        mahasiswa.setTelepon(data.getTeleponMahasiswa());
        mahasiswa.setPonsel(data.getPonselMahasiswa());
        mahasiswa.setEmail(data.getEmailMahasiswa());
        mahasiswa.setPernahKuliah(data.getPernahKuliah());
        mahasiswa.setTempatTinggal(data.getTempatTinggalMahasiswa());
        mahasiswa.setFoto(idFile + "." + extension);
        mahasiswa.setTglInsert(data.getTglInsert());
        mahasiswa.setUserInsert(u);
        mahasiswa.setNa(data.getNaMahasiswa());

        mhswAkademik.setIdMhsw(mahasiswa);
        mhswAkademik.setIdFakultas(data.getIdFakultas());
        mhswAkademik.setIdJenjang(data.getIdJenjang());
        mhswAkademik.setIdProdi(data.getIdProdi());
        mhswAkademik.setIdKonsentrasi(data.getIdKonsentrasi());
        mhswAkademik.setNirm(data.getNirm());
        mhswAkademik.setTahunAngkatan(data.getTahunAngkatan());
        mhswAkademik.setNisn(data.getNisn());
        mhswAkademik.setTglMasuk(data.getTglMasuk());
        mhswAkademik.setIdStatusKeaktifan("adasds");
        mhswAkademik.setTglAktif(data.getTglAktif());
        mhswAkademik.setPendSebelum(data.getPendSebelum());
        mhswAkademik.setUserInsert(u);
        mhswAkademik.setTglInsert(LocalDateTime.now());

        mhswKost.setIdMhsw(mahasiswa);
        mhswKost.setAlamat(data.getAlamatKost());
        mhswKost.setIdPropinsi(data.getIdPropinsi());
        mhswKost.setIdKokab(data.getIdKokab());
        mhswKost.setKodepos(data.getKodeposKost());
        mhswKost.setNegara(data.getNegaraKost());
        mhswKost.setTglInsert(LocalDateTime.now());
        mhswKost.setUserInsert(u);


        mhswOrtu.setIdMhsw(mahasiswa);
        mhswOrtu.setNamaAyah(data.getNamaAyah());
        mhswOrtu.setAgamaAyah(data.getAgamaAyah());
        mhswOrtu.setPendAyah(data.getPendAyah());
        mhswOrtu.setPekerjaanAyah(data.getPekerjaanAyah());
        mhswOrtu.setStHidupAyah(data.getStHidupAyah());
        mhswOrtu.setNamaIbu(data.getNamaIbu());
        mhswOrtu.setAgamaIbu(data.getAgamaIbu());
        mhswOrtu.setPendIbu(data.getPendIbu());
        mhswOrtu.setPekerjaanIbu(data.getPekerjaanIbu());
        mhswOrtu.setStHidupIbu(data.getStHidupIbu());
        mhswOrtu.setHasilOrtuPerBulan(data.getHasilOrtuPerBulan());
        mhswOrtu.setAlamatOrtu(data.getAlamatOrtu());
        mhswOrtu.setKokab(data.getKokabOrtu());
        mhswOrtu.setKodepos(data.getKodepos());
        mhswOrtu.setProvinsi(data.getProvinsiOrtu());
        mhswOrtu.setNegara(data.getNegaraOrtu());
        mhswOrtu.setTelepon(data.getTeleponOrtu());
        mhswOrtu.setPonsel(data.getPonselOrtu());
        mhswOrtu.setEmail(data.getEmailOrtu());
        mhswOrtu.setUserInsert(u);
        mhswOrtu.setTglInsert(LocalDateTime.now());

        mhswSekolahAsal.setIdMhsw(mahasiswa);
        mhswSekolahAsal.setKodepos(data.getKodeposSekolah());
        mhswSekolahAsal.setIdProvinsi(data.getProvinsiSekolah());
        mhswSekolahAsal.setNegara(data.getNegaraSekolah());
        mhswSekolahAsal.setIdSekolah(data.getSekolah());
        mhswSekolahAsal.setAlamat(data.getAlamatSekolah());
        mhswSekolahAsal.setIdKokab(data.getKokabSekolah());
        mhswSekolahAsal.setJurusanSekolah(data.getJurusanSekolah());
        mhswSekolahAsal.setNilai(data.getNilai());
        mhswSekolahAsal.setTahunLulus(data.getTahunLulus());
        mhswSekolahAsal.setTglInsert(LocalDateTime.now());
        mhswSekolahAsal.setUserInsert(u);


        User user = new User();
        user.setUsername(registrasiService.generateNomorRegistrasi(formatNim));
        user.setEmail(data.getEmailMahasiswa());
        user.setActive(Boolean.TRUE);
        user.setRole(role);
        userDao.save(user);

        userPassword.setPassword(passwordEncoder.encode(data.getPassword()));
        userPassword.setUser(user);
        userPasswordDao.save(userPassword);

        mahasiswa.setIdUser(user);
        mahasiswaDao.save(mahasiswa);
        mhswAkademik.setNim(mahasiswa.getNim());
        mahasiswaAkademikDao.save(mhswAkademik);
        mahasiswaKostDao.save(mhswKost);
        mahasiswaOrtuDao.save(mhswOrtu);
        mahasiswaSekolahDao.save(mhswSekolahAsal);

        return "redirect:/mahasiswa/list";

    }
}
