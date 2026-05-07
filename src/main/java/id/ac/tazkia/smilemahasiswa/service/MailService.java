package id.ac.tazkia.smilemahasiswa.service;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import id.ac.tazkia.smilemahasiswa.constant.MagangStatus;
import id.ac.tazkia.smilemahasiswa.dto.graduation.WisudaDto;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.PendaftaranBebasKewajiban;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class MailService {
    @Autowired
    private GmailApiService gmailApiService;

    @Value("${redirect.email}")
    private String redirectEmail;

    @Autowired
    private MustacheFactory mustacheFactory;

    @Scheduled(cron = "0 00 20 * * *", zone = "Asia/Jakarta")
    public void testNotifEmail() {
        gmailApiService.kirimEmail(
                "Smile Notifikasi",
                "smile.notifikasi@tazkia.ac.id",
                "Test",
                "test"
        );
    }

    public void detailWisuda(WisudaDto wisudaDto) {
        Mustache templateEmail = mustacheFactory.compile("templates/email/wisuda/detail.html");
        Map<String, String> data = new HashMap<>();
        data.put("nama", wisudaDto.getNama());
        data.put("tanggal", wisudaDto.getTanggal().toString());
        data.put("kelamin", wisudaDto.getKelamin());
        data.put("ayah", wisudaDto.getAyah());
        data.put("ibu", wisudaDto.getIbu());
        data.put("nomor", wisudaDto.getNomor());
        if (wisudaDto.getBeasiswa() != null) {
            data.put("beasiswa", wisudaDto.getBeasiswa());
        } else {
            data.put("beasiswa", "-");

        }
        data.put("idBeasiswa", wisudaDto.getIdBeasiswa());
        data.put("toga", wisudaDto.getToga());
        data.put("judulIndo", wisudaDto.getJudulIndo());
        data.put("judulInggris", wisudaDto.getJudulInggris());

        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);

        gmailApiService.kirimEmail(
                "Smile Notifikasi",
                wisudaDto.getEmail(),
                "Pendaftaran Wisuda ",
                output.toString());

    }

    public void successWisuda(Wisuda wisuda) {
        Mustache templateEmail = mustacheFactory.compile("templates/email/wisuda/success.html");
        Map<String, String> data = new HashMap<>();
        data.put("nama", wisuda.getId());


        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);

        gmailApiService.kirimEmail(
                "Smile Notifikasi",
                wisuda.getMahasiswa().getEmailPribadi(),
                "Pendaftarn Wisuda Berhasil ",
                output.toString());

    }

    public void validasiSoal(Soal soal, String status) {
        if (status.equals("APPROVE")) {
            Mustache templateEmail = mustacheFactory.compile("templates/email/soal/approve.html");
            Map<String, String> data = new HashMap<>();
            data.put("dosen", soal.getJadwal().getDosen().getKaryawan().getNamaKaryawan());
            data.put("matakuliah", soal.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
            data.put("kelas", soal.getJadwal().getKelas().getNamaKelas());
            data.put("url", redirectEmail + "/filedownload/?soal=" + soal.getId());

            StringWriter output = new StringWriter();
            templateEmail.execute(output, data);

            gmailApiService.kirimEmail(
                    "Smile Notifikasi",
                    soal.getDosen().getKaryawan().getEmail(),
                    "Validasi Soal",
                    output.toString());
        }

        if (status.equals("REJECT")) {
            Mustache templateEmail = mustacheFactory.compile("templates/email/soal/reject.html");
            Map<String, String> data = new HashMap<>();
            data.put("dosen", soal.getJadwal().getDosen().getKaryawan().getNamaKaryawan());
            data.put("matakuliah", soal.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
            data.put("kelas", soal.getJadwal().getKelas().getNamaKelas());
            data.put("keterangan", soal.getKeteranganApprove());

            StringWriter output = new StringWriter();
            templateEmail.execute(output, data);

            gmailApiService.kirimEmail(
                    "Smile Notifikasi",
                    soal.getDosen().getKaryawan().getEmail(),
                    "Validasi Soal",
                    output.toString());
        }
    }

    public void suksesKrs(KrsApproval krsApproval, KelasMahasiswa kelasMahasiswa) {
        Mustache templateEmail = mustacheFactory.compile("templates/email/krs/sukses.html");
        Map<String, String> data = new HashMap<>();
        data.put("nama", krsApproval.getMahasiswa().getNama());
        data.put("tahunAkademik", krsApproval.getKrs().getTahunAkademik().getNamaTahunAkademik());
        data.put("kelas", kelasMahasiswa.getKelas().getNamaKelas());
        data.put("dosen", krsApproval.getMahasiswa().getDosen().getKaryawan().getNamaKaryawan());

        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);

        gmailApiService.kirimEmail("Smile Notifikasi",
                krsApproval.getMahasiswa().getEmailTazkia(),
                "Pengumuman KRS",
                output.toString());
    }

    public void mailApprovePerpus(PendaftaranBebasKewajiban bebasKewajiban, String komentar) {
        templateKewajiban(bebasKewajiban, komentar, "templates/email/bebasKewajiban/perpustakaan/suksesStaff.html");
    }

    public void mailRejectPerpus(PendaftaranBebasKewajiban bebasKewajiban, String komentar) {
        templateKewajiban(bebasKewajiban, komentar, "templates/email/bebasKewajiban/perpustakaan/rejectStaff.html");
    }

    public void mailApproveKabagPerpus(PendaftaranBebasKewajiban bebasKewajiban, String komentar) {
        templateKewajiban(bebasKewajiban, komentar, "templates/email/bebasKewajiban/perpustakaan/suksesKabag.html");
    }

    public void mailRejectKabagPerpus(PendaftaranBebasKewajiban bebasKewajiban, String komentar) {
        templateKewajiban(bebasKewajiban, komentar, "templates/email/bebasKewajiban/perpustakaan/rejectKabag.html");
    }

    public void mailApproveKeuangan(PendaftaranBebasKewajiban bebasKewajiban, String komentar) {
        templateKewajiban(bebasKewajiban, komentar, "templates/email/bebasKewajiban/keuangan/approve.html");
    }

    public void mailRejectKeuangan(PendaftaranBebasKewajiban bebasKewajiban, String komentar) {
        templateKewajiban(bebasKewajiban, komentar, "templates/email/bebasKewajiban/keuangan/reject.html");
    }

    public void mailApproveTlc(PendaftaranBebasKewajiban bebasKewajiban, String komentar) {
        templateKewajiban(bebasKewajiban, komentar, "templates/email/bebasKewajiban/tlc/approve.html");
    }

    public void mailRejectTlc(PendaftaranBebasKewajiban bebasKewajiban, String komentar) {
        templateKewajiban(bebasKewajiban, komentar, "templates/email/bebasKewajiban/tlc/reject.html");
    }

    public void mailApproveTQC(PendaftaranBebasKewajiban bebasKewajiban, String komentar) {
        templateKewajiban(bebasKewajiban, komentar, "templates/email/bebasKewajiban/tqc/approve.html");
    }

    public void mailRejectTQC(PendaftaranBebasKewajiban bebasKewajiban, String komentar) {
        templateKewajiban(bebasKewajiban, komentar, "templates/email/bebasKewajiban/tqc/reject.html");
    }

    public void mailApproveAkademik(PendaftaranBebasKewajiban bebasKewajiban, String komentar) {
        templateKewajiban(bebasKewajiban, komentar, "templates/email/bebasKewajiban/akademik/approve.html");
    }

    public void mailRejectAkademik(PendaftaranBebasKewajiban bebasKewajiban, String komentar) {
        templateKewajiban(bebasKewajiban, komentar, "templates/email/bebasKewajiban/akademik/reject.html");
    }

    private void templateKewajiban(PendaftaranBebasKewajiban bebasKewajiban, String komentar, String lokasiTemplate) {
        Mustache templateEmail = mustacheFactory.compile(lokasiTemplate);
        Map<String, String> data = new HashMap<>();
        data.put("nama", bebasKewajiban.getMahasiswa().getNama());
        data.put("nim", bebasKewajiban.getMahasiswa().getNim());
        data.put("noTelp", bebasKewajiban.getMahasiswa().getTeleponSeluler());
        data.put("prodi", bebasKewajiban.getMahasiswa().getIdProdi().getNamaProdi());
        if (bebasKewajiban.getMahasiswa().getIdKonsentrasi() == null) {
            data.put("konsentrasi", "-");
        } else {
            data.put("konsentrasi", bebasKewajiban.getMahasiswa().getIdKonsentrasi().getNamaKonsentrasi());
        }
        data.put("komentar", komentar);

        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);

        gmailApiService.kirimEmail("Smile Notifikasi",
                bebasKewajiban.getMahasiswa().getEmailTazkia(),
                "Pengumuman Bebas Kewajiban",
                output.toString());
    }

    public void templateNotifAdmin(PendaftaranBebasKewajiban bebasKewajiban, String bagian, String username) {
        Mustache templateEmail = mustacheFactory.compile("templates/email/bebasKewajiban/admin/request.html");
        Map<String, String> data = new HashMap<>();
        data.put("nama", bebasKewajiban.getMahasiswa().getNama());
        data.put("nim", bebasKewajiban.getMahasiswa().getNim());
        data.put("prodi", bebasKewajiban.getMahasiswa().getIdProdi().getNamaProdi());
        data.put("bagian", bagian);

        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);

        gmailApiService.kirimEmail("Smile Notifikasi",
                username,
                "Pengumuman Bebas Kewajiban",
                output.toString());
    }


    public void attendanceReminder(SesiKuliah sesiKuliah, Jadwal jadwal) {
        Mustache templateEmail = mustacheFactory.compile("templates/email/attendance/reminder.html");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id"));
        String tanggal = sesiKuliah.getWaktuMulai().format(formatter);

        Map<String, String> data = new HashMap<>();
        data.put("dosen", jadwal.getDosen().getKaryawan().getNamaKaryawan());
        data.put("matakuliah", jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
        data.put("kelas", jadwal.getKelas().getNamaKelas());
        data.put("tanggal", tanggal);
        data.put("pertemuan", sesiKuliah.getPertemuanKe().toString());

        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);

        gmailApiService.kirimEmail(
                "Smile Notifikasi",
                jadwal.getDosen().getKaryawan().getIdUser().getUsername(),
                "Attendance Reminder ",
                output.toString());

    }

    public void registerNote(Note note) {
        Mustache templateEmail = mustacheFactory.compile("templates/email/note/mahasiswa/register.html");
        Map<String, String> data = new HashMap<>();
        data.put("nim", note.getMahasiswa().getNim());
        data.put("nama", note.getMahasiswa().getNama());
        data.put("judulIndo", note.getJudul());
        data.put("judulInggris", note.getJudulInggris());
        data.put("dosen1", note.getDosen().getKaryawan().getNamaKaryawan());
        if (note.getDosen2() != null){
            data.put("dosen2", note.getDosen2().getKaryawan().getNamaKaryawan());

        }else {
            data.put("dosen2", "-");
        }
        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);

        gmailApiService.kirimEmail(
                "Smile Notifikasi",
                note.getMahasiswa().getEmailTazkia(),
                "Pendaftaran Konsep Note ",
                output.toString());

    }

    public void approvedNote(Note note) {
        Mustache templateEmail = mustacheFactory.compile("templates/email/note/mahasiswa/approved.html");
        Map<String, String> data = new HashMap<>();
        data.put("nim", note.getMahasiswa().getNim());
        data.put("nama", note.getMahasiswa().getNama());
        data.put("judulIndo", note.getJudul());
        data.put("judulInggris", note.getJudulInggris());
        data.put("dosen1", note.getDosen().getKaryawan().getNamaKaryawan());
        if (note.getDosen2() != null){
            data.put("dosen2", note.getDosen2().getKaryawan().getNamaKaryawan());

        }else {
            data.put("dosen2", "-");
        }
        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);

        gmailApiService.kirimEmail(
                "Smile Notifikasi",
                note.getMahasiswa().getEmailTazkia(),
                "Pendaftaran Konsep Note Disetujui",
                output.toString());

    }

    public void approvedSeminarAkademik(Seminar seminar) {
        Mustache templateEmail = mustacheFactory.compile("templates/email/seminar/approved.html");
        Map<String, String> data = new HashMap<>();
        data.put("nim", seminar.getNote().getMahasiswa().getNim());
        data.put("nama", seminar.getNote().getMahasiswa().getNama());
        data.put("judulIndo", seminar.getNote().getJudul());
        data.put("judulInggris", seminar.getNote().getJudulInggris());
        data.put("dosen1", seminar.getNote().getDosen().getKaryawan().getNamaKaryawan());
        data.put("dosen2", seminar.getNote().getDosen2() != null
                ? seminar.getNote().getDosen2().getKaryawan().getNamaKaryawan() : "-");
        data.put("jenisSeminar", seminar.getNote().getJenis() != null
                ? seminar.getNote().getJenis().toString() : "Proposal");
        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);

        String email = seminar.getNote().getMahasiswa().getEmailTazkia();
        if (email == null || email.isBlank()) {
            email = seminar.getNote().getMahasiswa().getEmailPribadi();
        }
        if (email != null && !email.isBlank()) {
            gmailApiService.kirimEmail(
                    "Smile Notifikasi",
                    email,
                    "Pendaftaran Seminar Disetujui Akademik",
                    output.toString());
        }
    }

    public void rejectedSeminarAkademik(Seminar seminar) {
        Mustache templateEmail = mustacheFactory.compile("templates/email/seminar/rejected.html");
        Map<String, String> data = new HashMap<>();
        data.put("nim", seminar.getNote().getMahasiswa().getNim());
        data.put("nama", seminar.getNote().getMahasiswa().getNama());
        data.put("judulIndo", seminar.getNote().getJudul());
        data.put("judulInggris", seminar.getNote().getJudulInggris());
        data.put("dosen1", seminar.getNote().getDosen().getKaryawan().getNamaKaryawan());
        data.put("dosen2", seminar.getNote().getDosen2() != null
                ? seminar.getNote().getDosen2().getKaryawan().getNamaKaryawan() : "-");
        data.put("jenisSeminar", seminar.getNote().getJenis() != null
                ? seminar.getNote().getJenis().toString() : "Proposal");
        data.put("keterangan", seminar.getKomentarAkademik() != null
                ? seminar.getKomentarAkademik() : "-");
        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);

        String email = seminar.getNote().getMahasiswa().getEmailTazkia();
        if (email == null || email.isBlank()) {
            email = seminar.getNote().getMahasiswa().getEmailPribadi();
        }
        if (email != null && !email.isBlank()) {
            gmailApiService.kirimEmail(
                    "Smile Notifikasi",
                    email,
                    "Pendaftaran Seminar Ditolak",
                    output.toString());
        }
    }

    public void intershipApproved(FileMagang fileMagang) {
        Mustache templateEmail = mustacheFactory.compile("templates/email/magang/approve.html");
        Map<String, String> data = new HashMap<>();
        String message = null;

        message = fileMagang.getJenis().equals(MagangStatus.MBKM)
                ? "Menunggu penilaian dari Program Studi."
                : "Menunggu penilaian dari Akademik.";

        data.put("nim", fileMagang.getMahasiswa().getNim());
        data.put("nama", fileMagang.getMahasiswa().getNama());
        data.put("jenis", fileMagang.getJenis().getDisplayName());
        data.put("message", message);
        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);

        gmailApiService.kirimEmail(
                "Smile Notifikasi",
                fileMagang.getMahasiswa().getUser().getUsername(),
                "Pendaftaran Magang Disetujui",
                output.toString());

    }

    public void intershipRejected(FileMagang fileMagang) {
        Mustache templateEmail = mustacheFactory.compile("templates/email/magang/reject.html");
        Map<String, String> data = new HashMap<>();
        data.put("nim", fileMagang.getMahasiswa().getNim());
        data.put("nama", fileMagang.getMahasiswa().getNama());
        data.put("jenis", fileMagang.getJenis().getDisplayName());
        data.put("keterangan", fileMagang.getKomentar());
        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);

        gmailApiService.kirimEmail(
                "Smile Notifikasi",
                fileMagang.getMahasiswa().getUser().getUsername(),
                "Pendaftaran Magang Disetujui",
                output.toString());

    }


    public void reminderTagihanBulanan(Tagihan tagihan, String nilaiTagihan) {
        Mustache templateEmail = mustacheFactory.compile("templates/email/keuangan/tagihan.html");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id"));
        String tanggalJatuhTempo = tagihan.getTanggalJatuhTempo().format(formatter);
        DateTimeFormatter formatterKirim = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id"));
        String tanggalKirim = LocalDate.now().format(formatterKirim);

        Map<String, String> data = new HashMap<>();
        data.put("tanggalKirim", tanggalKirim);
        data.put("nomorTagihan", tagihan.getNomor());
        data.put("nama", tagihan.getMahasiswa().getNama());
        data.put("nim", tagihan.getMahasiswa().getNim());
        data.put("email", tagihan.getMahasiswa().getEmailPribadi());
        data.put("nomorTelepon", tagihan.getMahasiswa().getTeleponSeluler());
        data.put("jenisTagihan", tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama());
        data.put("nilaiTagihan", nilaiTagihan);
        data.put("tanggalJatuhTempo", tanggalJatuhTempo);

        StringWriter output = new StringWriter();
        templateEmail.execute(output, data);

        gmailApiService.kirimEmail(
                "Tazkia Finance",
                tagihan.getMahasiswa().getEmailTazkia(),
                "Informasi Tagihan Biaya Pendidikan",
                output.toString());
    }

}
