package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.KrsDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dto.response.BaseResponse;
import id.ac.tazkia.smilemahasiswa.dto.user.IpkDto;
import id.ac.tazkia.smilemahasiswa.entity.Krs;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class KrsService {

    @Autowired private MahasiswaDao mahasiswaDao;

    @Autowired private KrsDetailDao krsDetailDao;

    @Autowired private KrsDao krsDao;

    public BaseResponse updateIpKrsApi(List<Mahasiswa> mhs){
        BaseResponse response = null;
        for (Mahasiswa m : mhs){
            Integer setSemester = 0;
            System.out.println("setting MAHASISWA : " + m.getNim());
            List<Krs> listKrs = krsDao.findByMahasiswaAndStatusOrderByTahunAkademikKodeTahunAkademik(m, StatusRecord.AKTIF);
            for (Krs krs : listKrs){
                System.out.println("setting KRS : " + krs.getNim());

                // cari semester krs
                setSemester++;

                // cari ip semester
                IpkDto ipSemester = krsDao.cariIpSemester(krs.getTahunAkademik().getId(), krs.getMahasiswa().getId());
                // cari Ipk persemester
                IpkDto ipkPerSemester = krsDao.cariIpkTotalPerSemester(krs.getMahasiswa().getId(), krs.getTahunAkademik().getKodeTahunAkademik());

                System.out.println("semester : " + setSemester + " kode tahun : " + krs.getTahunAkademik().getKodeTahunAkademik() + " ip : " + ipSemester.getIpk() + " Ipk Semester : " + ipkPerSemester.getIpk());

                krs.setIp(ipSemester.getIpk().toString());
                krs.setIpk(ipkPerSemester.getIpk().toString());
                krs.setSemester(setSemester);
                krsDao.save(krs);
            }
        }

        response = BaseResponse.builder()
                .code("200").message("Berhasil di import")
                .build();

        return response;
    }

    public void updateIpKrs(List<Mahasiswa> mhs){
        for (Mahasiswa m : mhs){
            System.out.println("setting MAHASISWA : " + m.getNim());
            List<Krs> listKrs = krsDao.findByMahasiswaAndStatusOrderByTahunAkademikKodeTahunAkademik(m, StatusRecord.AKTIF);
            for (Krs krs : listKrs){
//                System.out.println("setting KRS : " + krs.getNim());
//                // cari ip semester
//                Object[] ipSemester = krsDao.cariIpSemester(krs.getTahunAkademik().getId(), krs.getMahasiswa().getId());
//
//                // cari semester krs
//                Integer semester = krsDetailDao.cariSemester(krs.getMahasiswa().getId(), krs.getTahunAkademik().getId());
//                Integer semesterSekarang = krsDetailDao.cariSemesterSekarang(krs.getMahasiswa().getId(), krs.getTahunAkademik().getId());
//                if (semester == null) {
//                    semester = 0;
//                }
//                if (semesterSekarang == null) {
//                    semesterSekarang = 0;
//                }
//                Integer semesterTotal = semester + semesterSekarang;
//                // cari Ipk persemester
//                Object[] ipkPerSemester = krsDao.cariIpkTotalPerSemester(krs.getMahasiswa().getId(), krs.getTahunAkademik().getKodeTahunAkademik());
//                krs.setIp(ipSemester[2].toString());
//                krs.setIpk(ipkPerSemester[2].toString());
//                krs.setSemester(semesterTotal);
//                krsDao.save(krs);
            }
        }
    }

}
