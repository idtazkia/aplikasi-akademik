package id.ac.tazkia.smilemahasiswa.service;


import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.ImportMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.user.MahasiswaDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class MahasiswaService {
    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private AyahDao ayahDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private IbuDao ibuDao;

    @Autowired
    private WaliDao waliDao;

    @Autowired
    private ProgramDao programDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private AgamaDao agamaDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private MahasiswaDetailKeluargaDao mahasiswaDetailKeluargaDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(MahasiswaService.class);

    public Mahasiswa prosesMahasiswa(MahasiswaDto mahasiswaDto){
        Mahasiswa mahasiswa = new Mahasiswa();
                BeanUtils.copyProperties(mahasiswaDto, mahasiswa);
        mahasiswa.setTempatLahir(mahasiswaDto.getTempat());

        if (mahasiswa.getUser() == null) {
            createUser(mahasiswa);
        }

        mahasiswaDao.save(mahasiswa);

        return mahasiswa;
    }

    private void createUser(Mahasiswa mahasiswa) {
        Role rolePendaftar = roleDao.findById("mahasiswa").get();

        User user = new User();
        user.setUsername(mahasiswa.getNim());
        user.setActive(true);
        user.setRole(rolePendaftar);
        userDao.save(user);

        mahasiswa.setUser(user);
    }

    public Ayah prosesAyah(MahasiswaDto mahasiswaDto, Mahasiswa mahasiswa){
        Ayah ayah = new Ayah();

        BeanUtils.copyProperties(mahasiswaDto, ayah);
        ayah.setId(mahasiswaDto.getAyah());
        ayah.setTanggalLahir(mahasiswaDto.getTanggalLahirAyah());
        ayah.setTempatLahir(mahasiswaDto.getTempatLahirAyah());
        ayah.setStatusHidup(mahasiswaDto.getHidup());


        ayahDao.save(ayah);

        return ayah;
    }

    public Ibu prosesIbu(MahasiswaDto mahasiswaDto, Mahasiswa mahasiswa){
        Ibu ibu = new Ibu();
        ibu.setId(mahasiswaDto.getId());
        ibu.setNamaIbuKandung(mahasiswaDto.getNamaIbuKandung());
        ibu.setKebutuhanKhusus(mahasiswaDto.getKebutuhanKhususIbu());
        ibu.setTempatLahir(mahasiswaDto.getTempatLahirIbu());
        ibu.setTanggalLahir(mahasiswaDto.getTanggalLahirIbu());
        ibu.setIdJenjangPendidikan(mahasiswaDto.getIdJenjangPendidikanIbu());
        ibu.setIdPekerjaan(mahasiswaDto.getIdPekerjaanIbu());
        ibu.setPenghasilan(mahasiswaDto.getPenghasilanIbu());
        ibu.setAgama(mahasiswaDto.getAgamaIbu());
        ibu.setStatusHidup(mahasiswaDto.getStatusHidupIbu());



        ibuDao.save(ibu);

        return ibu;
    }

    public Wali prosesWali(MahasiswaDto mahasiswaDto, Mahasiswa mahasiswa){
        Wali wali = new Wali();

        wali.setNamaWali(mahasiswaDto.getNamaWali());
        wali.setId(mahasiswaDto.getWali());
        wali.setKebutuhanKhusus(mahasiswaDto.getKebutuhanKhususWali());
        wali.setTempatLahir(mahasiswaDto.getTempatLahirWali());
        wali.setTanggalLahir(mahasiswaDto.getTanggalLahirWali());
        wali.setIdJenjangPendidikan(mahasiswaDto.getIdJenjangPendidikanWali());
        wali.setIdPekerjaan(mahasiswaDto.getIdPekerjaanWali());
        wali.setIdPenghasilan(mahasiswaDto.getIdPenghasilanWali());
        wali.setAgama(mahasiswaDto.getAgamaWali());
        waliDao.save(wali);

        return wali;
    }

    public Mahasiswa importMahasiswa(ImportMahasiswaDto importMahasiswaDto){

        Ayah ayah = new Ayah();
        ayah.setNamaAyah(importMahasiswaDto.getAyah());
        ayah.setTanggalLahir(LocalDate.parse("1982-05-14"));
        ayah.setAgama(agamaDao.findById(importMahasiswaDto.getIdAgama()).get());
        ayah.setStatusHidup("H");
        ayahDao.save(ayah);

        Ibu ibu = new Ibu();
        ibu.setNamaIbuKandung(importMahasiswaDto.getIbu());
        ibu.setTanggalLahir(LocalDate.parse("1982-05-14"));
        ibu.setAgama(agamaDao.findById(importMahasiswaDto.getIdAgama()).get());
        ibu.setStatusHidup("H");
        ibuDao.save(ibu);

        Mahasiswa mahasiswa = new Mahasiswa();
        BeanUtils.copyProperties(importMahasiswaDto,mahasiswa);
        mahasiswa.setIdProdi(prodiDao.findByKodeSpmb(importMahasiswaDto.getProdi()));
        mahasiswa.setIdKotaKabupaten(importMahasiswaDto.getKabupaten());
        mahasiswa.setIdProvinsi(importMahasiswaDto.getProvinsi());
        mahasiswa.setIdNegara(importMahasiswaDto.getNegara());
        mahasiswa.setDosen(dosenDao.findById("''").get());
        mahasiswa.setEmailPribadi(importMahasiswaDto.getEmail());
        mahasiswa.setTeleponRumah(importMahasiswaDto.getTelepon());
        mahasiswa.setTeleponSeluler(importMahasiswaDto.getTelepon());
        mahasiswa.setIdAgama(agamaDao.findById(importMahasiswaDto.getIdAgama()).get());
        mahasiswa.setStatusMatrikulasi("N");
        mahasiswa.setAyah(ayah);
        mahasiswa.setIbu(ibu);
        mahasiswa.setNamaJalan(importMahasiswaDto.getAlamat());
        mahasiswa.setIdAbsen(mahasiswaDao.cariMaxAbsen()+1);
        if (importMahasiswaDto.getJenjang().equals("S1")){
            mahasiswa.setIdProgram(programDao.findById("01").get());
        }

        if (importMahasiswaDto.getJenjang().equals("S2")){
            if (importMahasiswaDto.getProgram().equals("Reguler")){
                mahasiswa.setIdProgram(programDao.findById("8ec26f2c-a48a-4948-be90-e03e9374c675").get());
            }

            if (importMahasiswaDto.getProgram().equals("Eksekutif")){
                mahasiswa.setIdProgram(programDao.findById("03").get());
            }
        }
        mahasiswaDao.save(mahasiswa);
        createUser(mahasiswa);
        createDetail(mahasiswa);

        return mahasiswa;
    }

    public MahasiswaDetailKeluarga createDetail(Mahasiswa mahasiswa){
        MahasiswaDetailKeluarga mahasiswaDetailKeluarga = new MahasiswaDetailKeluarga();
        mahasiswaDetailKeluarga.setMahasiswa(mahasiswa);
        mahasiswaDetailKeluarga.setAyah(mahasiswa.getAyah());
        mahasiswaDetailKeluarga.setIbu(mahasiswa.getIbu());

        mahasiswaDetailKeluargaDao.save(mahasiswaDetailKeluarga);

        return mahasiswaDetailKeluarga;
    }
}
