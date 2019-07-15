package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class KrsMahasiswaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KrsMahasiswaController.class);

    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private TahunAkademikProdiDao tahunAkademikProdiDao;
    @Autowired
    private MahasiswaDao mahasiswaDao;
    @Autowired
    private KrsDao krsDao;
    @Autowired
    private JadwalDao jadwalDao;
    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired
    private CurrentUserService currentUserService;
    @Autowired
    private PrasyaratDao prasyaratDao;
    @Autowired
    private GradeDao gradeDao;
    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;

    @ModelAttribute("listTahunAkademik")
    public Iterable<TahunAkademik> daftarKonfig() {
        return tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(StatusRecord.HAPUS);
    }


    @GetMapping("/menumahasiswa/krs/list")
    public void daftarKRS(Model model, Authentication authentication,Pageable page,
                          @RequestParam(required = false) TahunAkademik tahunAkademik){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Krs k = krsDao.findByMahasiswaAndTahunAkademik(mahasiswa,ta);
        List<Jadwal> rekap = new ArrayList<>();
        Grade grade = gradeDao.findById("8").get();
        model.addAttribute("tahun", ta);
        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa,StatusRecord.AKTIF);


        if (k!= null && LocalDate.now().compareTo(ta.getTanggalMulaiKrs()) >= 0 == true && LocalDate.now().compareTo(ta.getTanggalSelesaiKrs()) <= 0 == true) {
            model.addAttribute("krsAktif", k);

            List<Jadwal> jadwal = jadwalDao.findByTahunAkademikAndAksesAndStatusAndIdHariNotNull(ta, Akses.UMUM, StatusRecord.AKTIF);
            if (jadwal.isEmpty() || jadwal == null) {
                System.out.printf("tidak ada jadwal");
            } else {
                if (kelasMahasiswa != null) {
                    for (Jadwal j : jadwal) {
                        KrsDetail krsDetail = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(j.getMatakuliahKurikulum(), mahasiswa, StatusRecord.AKTIF);
                        if (j.getIdKelas() != kelasMahasiswa.getKelas()) {
                            if (krsDetail == null) {
                                List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);
                                if (prasyarat.isEmpty()) {
                                    if (mahasiswa.getIdProgram() != null) {
                                        if (j.getMatakuliahKurikulum().getPrograms().isEmpty()) {
                                            rekap.add(j);
                                        } else {
                                            for (Program program : j.getMatakuliahKurikulum().getPrograms()) {
                                                if (mahasiswa.getIdProgram() == program) {
                                                    rekap.add(j);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    for (Prasyarat pras : prasyarat) {
                                        KrsDetail cariPras = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(pras.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF);
                                        if (cariPras == null) {
                                            System.out.printf("lengkapi pras");
                                        } else {
                                            rekap.add(j);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                if (krsDetail.getBobot() != null) {
                                    if (krsDetail.getBobot().compareTo(grade.getBobot()) > 0) {
                                        System.out.printf("grade  :  " + grade.getBobot());
                                        System.out.printf("anda sudah mengambil matakuliah ini dan lulus");
                                    } else {

                                        if (j.getMatakuliahKurikulum().getPrograms().isEmpty()) {
                                            List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);
                                            if (prasyarat.isEmpty()) {
                                                System.out.printf("gaada prasyarat jadi langsung ambil ");
                                                rekap.add(j);
                                            } else {
                                                for (Prasyarat pras : prasyarat) {
                                                    KrsDetail cariPrasyarat = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(pras.getMatakuliahKurikulum(), mahasiswa, StatusRecord.AKTIF);

                                                    if (cariPrasyarat == null) {
                                                        System.out.printf("selesaikan terlebih dahulu prasyarat");
                                                    } else {
                                                        if (cariPrasyarat.getBobot().compareTo(pras.getNilai()) > 0) {
                                                            System.out.printf("boleh ngambil karna beres prasyarat");
                                                            rekap.add(j);
                                                        } else {
                                                            System.out.printf("nilai anda masih di bawah target ");
                                                            System.out.printf("grade  :  " + grade.getBobot());
                                                        }
                                                    }
                                                }
                                            }

                                        } else {

                                        }

//
                                    }
                                }
                            }
                        }
                    }
                }
            }

            List<Jadwal> jadwalProdi = jadwalDao.findByTahunAkademikAndProdiAndAksesAndStatusAndIdHariNotNull(ta, mahasiswa.getIdProdi(), Akses.PRODI, StatusRecord.AKTIF);
            if (jadwalProdi.isEmpty() || jadwalProdi == null) {
                System.out.printf("tidak ada jadwal");
            } else {
                if (kelasMahasiswa != null) {
                    for (Jadwal j : jadwalProdi) {
                        KrsDetail krsDetail = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(j.getMatakuliahKurikulum(), mahasiswa, StatusRecord.AKTIF);
                        if (j.getIdKelas() != kelasMahasiswa.getKelas()) {
                            if (krsDetail == null) {
                                List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);
                                if (prasyarat.isEmpty()) {
                                    if (mahasiswa.getIdProgram() != null) {
                                        if (j.getMatakuliahKurikulum().getPrograms().isEmpty()) {
                                            rekap.add(j);
                                        } else {
                                            for (Program program : j.getMatakuliahKurikulum().getPrograms()) {
                                                if (mahasiswa.getIdProgram() == program) {
                                                    rekap.add(j);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    for (Prasyarat pras : prasyarat) {
                                        KrsDetail cariPras = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(pras.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF);
                                        if (cariPras == null) {
                                            System.out.printf("lengkapi pras");
                                        } else {
                                            rekap.add(j);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                if (krsDetail.getBobot() != null) {
                                    if (krsDetail.getBobot().compareTo(grade.getBobot()) > 0) {
                                        System.out.printf("anda sudah maengambil matakuliah ini dan lulus");
                                        System.out.printf("grade  :  " + grade.getBobot());
                                    } else {

                                        if (j.getMatakuliahKurikulum().getPrograms().isEmpty()) {
                                            List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);
                                            if (prasyarat.isEmpty()) {
                                                rekap.add(j);
                                                System.out.printf("gaada prasyarat jadi langsung ambil ");
                                            } else {
                                                for (Prasyarat pras : prasyarat) {
                                                    KrsDetail cariPrasyarat = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(pras.getMatakuliahKurikulum(), mahasiswa, StatusRecord.AKTIF);

                                                    if (cariPrasyarat == null) {

                                                        System.out.printf("selesaikan terlebih dahulu prasyarat");
                                                    } else {
                                                        if (cariPrasyarat.getBobot().compareTo(pras.getNilai()) > 0) {
                                                            System.out.printf("boleh ngambil karna beres prasyarat");
                                                            rekap.add(j);
                                                        } else {
                                                            System.out.printf("nilai anda masih di bawah target ");
                                                            System.out.printf("grade  :  " + grade.getBobot());
                                                        }
                                                    }
                                                }
                                            }

                                        } else {

                                        }

//
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (kelasMahasiswa != null){
            List<Jadwal> jadwals = jadwalDao.findByTahunAkademikAndIdKelasAndStatusAndIdHariNotNull(ta, kelasMahasiswa.getKelas(), StatusRecord.AKTIF);
            if (jadwals.isEmpty() || jadwals == null) {
            } else {
                for (Jadwal j : jadwals) {
                    KrsDetail krsDetail = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(j.getMatakuliahKurikulum(), mahasiswa, StatusRecord.AKTIF);
                    if (j.getIdKelas() != kelasMahasiswa.getKelas()) {
                        if (krsDetail == null) {
                            List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);
                            if (prasyarat.isEmpty()) {
                                if (mahasiswa.getIdProgram() != null) {
                                    if (j.getMatakuliahKurikulum().getPrograms().isEmpty()) {
                                        rekap.add(j);
                                    } else {
                                        for (Program program : j.getMatakuliahKurikulum().getPrograms()) {
                                            if (mahasiswa.getIdProgram() == program) {
                                                rekap.add(j);
                                            }
                                        }
                                    }
                                }
                            } else {
                                for (Prasyarat pras : prasyarat) {
                                    KrsDetail cariPras = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(pras.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF);
                                    if (cariPras == null) {
                                        System.out.printf("lengkapi pras");
                                    } else {
                                        rekap.add(j);
                                        break;
                                    }
                                }
                            }
                        } else {
                            if (krsDetail.getBobot() != null) {
                                if (krsDetail.getBobot().compareTo(grade.getBobot()) > 0) {
                                    System.out.printf("anda sudah maengambil matakuliah ini dan lulus");
                                    System.out.printf("grade  :  " + grade.getBobot());
                                } else {

                                    if (j.getMatakuliahKurikulum().getPrograms().isEmpty()) {
                                        List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);
                                        if (prasyarat.isEmpty()) {
                                            rekap.add(j);
                                            System.out.printf("gaada prasyarat jadi langsung ambil ");
                                        } else {
                                            for (Prasyarat pras : prasyarat) {
                                                KrsDetail cariPrasyarat = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(pras.getMatakuliahKurikulum(), mahasiswa, StatusRecord.AKTIF);

                                                if (cariPrasyarat == null) {

                                                    System.out.printf("selesaikan terlebih dahulu prasyarat");
                                                } else {
                                                    if (cariPrasyarat.getBobot().compareTo(pras.getNilai()) > 0) {
                                                        System.out.printf("boleh ngambil karna beres prasyarat");
                                                        rekap.add(j);
                                                    } else {
                                                        System.out.printf("nilai anda masih di bawah target ");
                                                        System.out.printf("grade  :  " + grade.getBobot());
                                                    }
                                                }
                                            }
                                        }

                                    } else {

                                    }

//
                                }
                            }
                        }
                    }
                }
            }
        }



        }
        model.addAttribute("mahasiswa",mahasiswa);
        TahunAkademikProdi tahunProdi = tahunAkademikProdiDao.findByTahunAkademikStatusAndProdi(StatusRecord.AKTIF,mahasiswa.getIdProdi());



        model.addAttribute("jadwal",rekap);

        if (tahunAkademik != null){
            model.addAttribute("search", tahunAkademik);
            Krs krs = krsDao.findByTahunAkademikAndMahasiswa(tahunAkademik,mahasiswa);
            model.addAttribute("krs",krs);


            model.addAttribute("data",krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF,krs,mahasiswa));
        }else {
            Krs krs = krsDao.findByTahunAkademikStatusAndMahasiswa(StatusRecord.AKTIF,mahasiswa);
            model.addAttribute("krs",krs);

            model.addAttribute("data",krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF,krs,mahasiswa));

        }



    }

    @PostMapping("/proses/krs")
    public String addKrs(@RequestParam String[] data,Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        TahunAkademikProdi tahunProdi = tahunAkademikProdiDao.findByTahunAkademikStatusAndProdi(StatusRecord.AKTIF, mahasiswa.getIdProdi());

        Krs cariKrs = krsDao.findByTahunAkademikStatusAndMahasiswa(StatusRecord.AKTIF, mahasiswa);
        List<KrsDetail> krsDetails = krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF, cariKrs, mahasiswa);

//        Buat Semester Pendek
        if (tahunAkademik.getJenis() == StatusRecord.PENDEK) {
            if (data != null){
                if (krsDetails.size() < 2) {
                    for (String idJadwal : data) {
                        System.out.println(idJadwal);
                        int total = data.length + krsDetails.size();
                        Jadwal jadwal = jadwalDao.findById(idJadwal).get();
                        KrsDetail krsDetail = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(jadwal.getMatakuliahKurikulum(), mahasiswa, StatusRecord.AKTIF);
                        System.out.println("total :  " + total);

                        if (krsDetail == null) {
                            if (total <= 2) {
                                KrsDetail kd = new KrsDetail();
                                kd.setJadwal(jadwal);
                                kd.setKrs(cariKrs);
                                kd.setMahasiswa(mahasiswa);
                                kd.setMatakuliahKurikulum(jadwal.getMatakuliahKurikulum());
                                kd.setNilaiPresensi(BigDecimal.ZERO);
                                kd.setNilaiTugas(BigDecimal.ZERO);
                                kd.setNilaiUas(BigDecimal.ZERO);
                                kd.setNilaiUts(BigDecimal.ZERO);
                                kd.setFinalisasi("N");
                                krsDetailDao.save(kd);
                            } else {
                                System.out.println("batasnya 2");
                            }
                        } else {
                            System.out.println("sudah ada");
                        }

                    }
                }

                if (krsDetails.size() == 2 || krsDetails.size() > 2) {
                    System.out.printf("Anda tidak bisa mengambil lagi matakuliah");
                }

            } else {


            }
        }

        return "redirect:/menumahasiswa/krs/list";
    }

    @PostMapping("/krs/delete")
    public String deleteKrs (@RequestParam KrsDetail id){
        krsDetailDao.delete(id);

        return "redirect:/menumahasiswa/krs/list";
    }



}