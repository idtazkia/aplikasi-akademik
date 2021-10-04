package id.ac.tazkia.smilemahasiswa.service;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.Soal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
public class MailService {
    @Autowired
    private GmailApiService gmailApiService;

    @Value("${redirect.email}")
    private String redirectEmail;

    @Autowired
    private MustacheFactory mustacheFactory;

    public void validasiSoal(Soal soal,String status) {
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

        if (status.equals("REJECT")){
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
}
