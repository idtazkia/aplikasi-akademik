package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.response.BaseResponse;
import id.ac.tazkia.smilemahasiswa.dto.user.IpkDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class KrsService {

    public static final BigDecimal BATAS_KRS = new BigDecimal(3);

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private KrsApprovalDao krsApprovalDao;

    @Autowired
    private IpkDao ipkDao;

    @Autowired
    private EnableKrsDao enableKrsDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private TahunProdiDao tahunProdiDao;
    @Autowired
    private GradeDao gradeDao;

    public BaseResponse updateIpKrsApi(List<Mahasiswa> mhs) {
        BaseResponse response = null;
        for (Mahasiswa m : mhs) {
            Integer setSemester = 0;
            System.out.println("setting MAHASISWA : " + m.getNim());
            List<Krs> listKrs = krsDao.findByMahasiswaAndStatusOrderByTahunAkademikKodeTahunAkademik(m,
                    StatusRecord.AKTIF);
            for (Krs krs : listKrs) {
                System.out.println("setting KRS : " + krs.getNim());

                // cari semester krs
                setSemester++;

                // cari ip semester
                IpkDto ipSemester = krsDao.cariIpSemester(krs.getTahunAkademik().getId(), krs.getMahasiswa().getId());
                // cari Ipk persemester
                IpkDto ipkPerSemester = krsDao.cariIpkTotalPerSemester(krs.getMahasiswa().getId(),
                        krs.getTahunAkademik().getKodeTahunAkademik());

                System.out.println(
                        "semester : " + setSemester + " kode tahun : " + krs.getTahunAkademik().getKodeTahunAkademik()
                                + " ip : " + ipSemester.getIpk() + " Ipk Semester : " + ipkPerSemester.getIpk());

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

    public void cekKrsApproval(Mahasiswa mahasiswa, TahunAkademik tahunAkademik) {
        Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, tahunAkademik, StatusRecord.AKTIF);
        if (krs == null) {
            log.info("{} dan nama {} belum melakukan daftar ulang / pembayaran krs", mahasiswa.getNim(),
                    mahasiswa.getNama());
        } else {
            KrsApproval cekApproval = krsApprovalDao.findByKrsAndMahasiswaAndStatusNotIn(krs, mahasiswa,
                    Arrays.asList(StatusApprove.HAPUS));
            if (cekApproval == null) {
                Integer tSemester = krsDetailDao.cariSemester(mahasiswa.getId(), tahunAkademik.getId());
                if (tSemester == 0) {
                    createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                } else {
                    Ipk cekIpk = ipkDao.findByMahasiswa(mahasiswa);
                    log.info("Proses KrsApproval Mahasiswa {} NIM {} Jenjang {} Prodi {}", mahasiswa.getNama(),
                            mahasiswa.getNim(), mahasiswa.getIdProdi().getIdJenjang().getNamaJenjang(),
                            mahasiswa.getIdProdi().getNamaProdi());
                    if ("S1".equalsIgnoreCase(mahasiswa.getIdProdi().getIdJenjang().getKodeJenjang())) {
                        if (cekIpk.getIpk().compareTo(BATAS_KRS) < 0) {
                            log.info("IPK dibawah 3!");
                            createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                        } else if (cekIpk.getIpk().compareTo(BATAS_KRS) > 0) {
                            log.info("IPK di atas 3!");
                            switch (tSemester) {
                                case 1:
                                    if (cekIpk.getSksTotal() < 12) {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                                    } else {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                                    }
                                    break;
                                case 2:
                                    if (cekIpk.getSksTotal() < 24) {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                                    } else {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                                    }
                                    break;
                                case 3:
                                    if (cekIpk.getSksTotal() < 44) {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                                    } else {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                                    }
                                    break;
                                case 4:
                                    if (cekIpk.getSksTotal() < 64) {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                                    } else {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                                    }
                                    break;
                                case 5:
                                    if (cekIpk.getSksTotal() < 84) {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                                    } else {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                                    }
                                    break;
                                case 6:
                                    if (cekIpk.getSksTotal() < 104) {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                                    } else {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                                    }
                                    break;
                                case 7:
                                    if (cekIpk.getSksTotal() < 124) {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                                    } else {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                                    }
                                    break;
                                case 8:
                                    if (cekIpk.getSksTotal() < 144) {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                                    } else {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                                    }
                                    break;
                                default:
                                    createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                                    break;
                            }
                        } else {
                            createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                        }
                    } else if ("S2".equalsIgnoreCase(mahasiswa.getIdProdi().getIdJenjang().getKodeJenjang())) {
                        if (cekIpk.getIpk().compareTo(BATAS_KRS) < 0) {
                            log.info("Ipk di bawah 3 S2!");
                            createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                        } else if (cekIpk.getIpk().compareTo(BATAS_KRS) > 0) {
                            log.info("Ipk di atas 3 S2!");
                            switch (tSemester) {
                                case 1:
                                    if (cekIpk.getSksTotal() < 12) {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                                    } else {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                                    }
                                    break;
                                case 2:
                                    if (cekIpk.getSksTotal() < 24) {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                                    } else {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                                    }
                                    break;
                                case 3:
                                    if (cekIpk.getSksTotal() < 24) {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                                    } else {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                                    }
                                    break;
                                case 4:
                                    if (cekIpk.getSksTotal() < 24) {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                                    } else {
                                        createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                                    }
                                    break;
                                default:
                                    log.info("Lebih dari semester 4 S2!");
                                    createKrsApproval(krs, mahasiswa, StatusApprove.WAITING);
                                    break;
                            }
                        } else {
                            createKrsApproval(krs, mahasiswa, StatusApprove.APPROVED);
                        }
                    }
                }
            }
        }
    }

    public void createKrsApproval(Krs krs, Mahasiswa mahasiswa, StatusApprove status) {
        KrsApproval cek = krsApprovalDao.findByKrsAndMahasiswaAndStatusNotIn(krs, mahasiswa,
                Arrays.asList(StatusApprove.REJECTED));
        if (cek != null) {
            cek.setStatus(status);
            krsApprovalDao.save(cek);
        } else {
            KrsApproval ka = new KrsApproval();
            ka.setKrs(krs);
            ka.setMahasiswa(mahasiswa);
            ka.setStatus(status);
            krsApprovalDao.save(ka);
        }
    }

    public Page<EnableKrs> listEnableKrs(Pageable page) {
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        return enableKrsDao.findByTahunAkademik(tahunAkademik, page);
    }

    public List<Mahasiswa> mhsKrs() {
        List<Mahasiswa> listMahasiswa = new ArrayList<>();
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        List<Krs> getMhs = krsDao.findByTahunAkademikAndStatus(tahunAkademik, StatusRecord.AKTIF);
        for (Krs m : getMhs) {
            Mahasiswa mhs = m.getMahasiswa();
            listMahasiswa.add(mhs);
        }
        return listMahasiswa;
    }

    public void saveEnabelKrs(String[] mahasiswa, LocalDate kadaluarsa) {
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        for (String m : mahasiswa) {
            Mahasiswa mhs = mahasiswaDao.findById(m).get();
            EnableKrs valid = enableKrsDao.findByTahunAkademikAndMahasiswaAndStatus(tahunAkademik, mhs,
                    StatusRecord.AKTIF);
            if (valid == null) {
                EnableKrs enableKrs = new EnableKrs();
                enableKrs.setMahasiswa(mhs);
                enableKrs.setTahunAkademik(tahunAkademik);
                enableKrs.setTanggalKadaluarsa(kadaluarsa);
                enableKrsDao.save(enableKrs);
            }
        }
    }

    public void updateEnabelKrs(EnableKrs enableKrs, LocalDate kadaluarsa) {
        enableKrs.setTanggalKadaluarsa(kadaluarsa);
        enableKrsDao.save(enableKrs);
    }

    public Krs createKrs(Mahasiswa mahasiswa, TahunAkademik tahunAkademik) {
        Krs krs = new Krs();
        krs.setMahasiswa(mahasiswa);
        krs.setNim(mahasiswa.getNim());
        krs.setProdi(mahasiswa.getIdProdi());
        krs.setTahunAkademik(tahunAkademik);
        krs.setTahunAkademikProdi(tahunProdiDao.findByTahunAkademikAndProdi(tahunAkademik, mahasiswa.getIdProdi()));
        krs.setStatus(StatusRecord.AKTIF);
        krs.setSemester(0);
        krs.setIp("0");
        krs.setIpk("0");
        krsDao.save(krs);
        return krs;
    }

    public KrsDetail createKrsDetailKonversi(Krs krs, Jadwal jadwal, String nilaiGrade) {
        Grade grade = gradeDao.findByNama(nilaiGrade);
        KrsDetail krsDetail = new KrsDetail();
        krsDetail.setKrs(krs);
        krsDetail.setMahasiswa(krs.getMahasiswa());
        krsDetail.setJadwal(jadwal);
        krsDetail.setMatakuliahKurikulum(jadwal.getMatakuliahKurikulum());
        krsDetail.setNilaiPresensi(BigDecimal.ZERO);
        krsDetail.setNilaiUts(BigDecimal.ZERO);
        krsDetail.setNilaiTugas(BigDecimal.ZERO);
        krsDetail.setFinalisasi("FINAL");
        krsDetail.setNilaiUas(BigDecimal.ZERO);
        krsDetail.setNilaiAkhir(grade.getBawah());
        krsDetail.setBobot(grade.getBobot());
        krsDetail.setGrade(grade.getNama());
        krsDetail.setJumlahKehadiran(0);
        krsDetail.setJumlahMangkir(0);
        krsDetail.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
        krsDetail.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
        krsDetail.setJumlahTerlambat(0);
        krsDetail.setJumlahIzin(0);
        krsDetail.setJumlahSakit(0);
        krsDetail.setStatusEdom(StatusRecord.UNDONE);
        krsDetail.setStatus(StatusRecord.AKTIF);
        krsDetail.setStatusKonversi(StatusRecord.AKTIF);
        krsDetail.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        krsDetailDao.save(krsDetail);

        return krsDetail;
    }

}
