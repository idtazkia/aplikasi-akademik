package id.ac.tazkia.smilemahasiswa.service;


import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.user.MahasiswaDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        user.setActive(false);
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
}
