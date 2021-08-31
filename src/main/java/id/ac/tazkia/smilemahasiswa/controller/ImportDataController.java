package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.ImportMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.MdlGradeItemsDto;
import id.ac.tazkia.smilemahasiswa.dto.response.BaseResponse;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.MahasiswaService;
import id.ac.tazkia.smilemahasiswa.utility.UrlUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RequestMapping(path = UrlUtil.Import.MAHASISWA)
@RestController
public class ImportDataController  {

    @Value("${spmb.mahaiswa.url}") private String apiUrl;

    @Autowired
    private MahasiswaService mahasiswaService;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private AgamaDao agamaDao;

    @Autowired
    private AyahDao ayahDao;

    @Autowired
    private IbuDao ibuDao;

    @Autowired
    private ProgramDao programDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private KurikulumDao kurikulumDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private MahasiswaDetailKeluargaDao mahasiswaDetailKeluargaDao;

    @GetMapping(value = "/{nim}")
    @ResponseBody
    public ResponseEntity<Object> getMahasiswaBaru(@PathVariable String nim) {

        RestTemplate restTemplate = new RestTemplate();
        ImportMahasiswaDto response = restTemplate
                .getForObject(apiUrl+"?nim="+nim, ImportMahasiswaDto.class);

        if (response.getNim() == null){
            return new ResponseEntity<>("Data Tidak ditemukan", HttpStatus.NOT_FOUND);
        }else {
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(response.getNim());
            if (mahasiswa != null) {
                return new ResponseEntity<>("Data Sudah Dilaporkan", HttpStatus.ALREADY_REPORTED);
            }else {
                save(response);
                return new ResponseEntity<>("Data Tersimpan", HttpStatus.CREATED);
            }
        }

    }

    @PostMapping
    public ResponseEntity<Object> save(@Valid @RequestBody ImportMahasiswaDto request){
        Mahasiswa response = mahasiswaService.importMahasiswa(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = "/proses")
    @ResponseBody
    public BaseResponse getMahasiswaBaru(@RequestBody ImportMahasiswaDto request) {

        User user = userDao.findByUsernameAndId(request.getUser(), request.getId());


        if (user != null) {
            if (request.getNim() == null || request.getNim().equals("null")) {

                return new BaseResponse(HttpStatus.NO_CONTENT.getReasonPhrase(),
                        String.valueOf(HttpStatus.NO_CONTENT.value()));
            } else {
                Mahasiswa mahasiswa = mahasiswaDao.findByNim(request.getNim());
                if (mahasiswa != null) {

                    return new  BaseResponse(HttpStatus.ALREADY_REPORTED.getReasonPhrase(),
                            String.valueOf(HttpStatus.ALREADY_REPORTED.value()));
                } else {

                    Ayah ayah = new Ayah();
                    ayah.setNamaAyah(request.getAyah());
                    ayah.setTanggalLahir(LocalDate.parse("1982-05-14"));
                    ayah.setAgama(agamaDao.findById(request.getIdAgama()).get());
                    ayah.setStatusHidup("H");
                    ayahDao.save(ayah);

                    Ibu ibu = new Ibu();
                    ibu.setNamaIbuKandung(request.getIbu());
                    ibu.setTanggalLahir(LocalDate.parse("1982-05-14"));
                    ibu.setAgama(agamaDao.findById(request.getIdAgama()).get());
                    ibu.setStatusHidup("H");
                    ibuDao.save(ibu);

                    Role rolePendaftar = roleDao.findById("mahasiswa").get();

                    User u = new User();
                    u.setUsername(request.getNim());
                    u.setActive(true);
                    u.setRole(rolePendaftar);
                    userDao.save(u);

                    Mahasiswa m = new Mahasiswa();
                    BeanUtils.copyProperties(request,m);
                    m.setIdProdi(prodiDao.findByKodeSpmb(request.getProdi()));
                    m.setIdKotaKabupaten(request.getKabupaten());
                    m.setIdProvinsi(request.getProvinsi());
                    m.setIdNegara(request.getNegara());
                    m.setDosen(dosenDao.findById("''").get());
                    m.setEmailPribadi(request.getEmail());
                    m.setTeleponRumah(request.getTelepon());
                    m.setTeleponSeluler(request.getTelepon());
                    m.setIdAgama(agamaDao.findById(request.getIdAgama()).get());
                    m.setStatusMatrikulasi("N");
                    m.setStatusAktif("AKTIF");
                    m.setAyah(ayah);
                    m.setIbu(ibu);
                    m.setUser(u);
                    m.setKurikulum(kurikulumDao.findByProdiAndStatus(m.getIdProdi(), StatusRecord.AKTIF));
                    m.setNamaJalan(request.getAlamat());
                    m.setIdAbsen(mahasiswaDao.cariMaxAbsen()+1);
                    if (request.getJenjang().equals("S1")){
                        m.setIdProgram(programDao.findById("01").get());
                    }

                    if (request.getJenjang().equals("S2")){
                        if (request.getProgram().equals("Reguler")){
                            m.setIdProgram(programDao.findById("8ec26f2c-a48a-4948-be90-e03e9374c675").get());
                        }

                        if (request.getProgram().equals("Eksekutif")){
                            m.setIdProgram(programDao.findById("03").get());
                        }
                    }
                    mahasiswaDao.save(m);

                    MahasiswaDetailKeluarga mahasiswaDetailKeluarga = new MahasiswaDetailKeluarga();
                    mahasiswaDetailKeluarga.setMahasiswa(m);
                    mahasiswaDetailKeluarga.setAyah(ayah);
                    mahasiswaDetailKeluarga.setIbu(ibu);

                    mahasiswaDetailKeluargaDao.save(mahasiswaDetailKeluarga);

                    return new BaseResponse(HttpStatus.CREATED.getReasonPhrase(),
                            String.valueOf(HttpStatus.CREATED.value()));
                }
            }
        }else {
            return new BaseResponse(HttpStatus.FORBIDDEN.getReasonPhrase(),
                    String.valueOf(HttpStatus.FORBIDDEN.value()));

        }
    }


}
