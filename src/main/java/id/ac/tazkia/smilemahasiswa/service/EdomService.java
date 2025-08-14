package id.ac.tazkia.smilemahasiswa.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.tazkia.smilemahasiswa.dao.DosenDao;
import id.ac.tazkia.smilemahasiswa.dao.JadwalDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomEvaluationTargetDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomGeneralResponseDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomQuestionDao;
import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.KrsDetail;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomEvaluationTarget;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomGeneralResponse;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomQuestion;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class EdomService {

    @Autowired
    private EdomGeneralResponseDao edomGeneralResponseDao;
    @Autowired
    private EdomQuestionDao edomQuestionDao;
    @Autowired
    private DosenDao dosenDao;
    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired
    private JadwalDao jadwalDao;
    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private EdomEvaluationTargetDao edomEvaluationTargetDao;

    @Transactional
    public void processEvaluationResponse(Map<String, String> formData, Mahasiswa mahasiswa, String targetType,
            TahunAkademik tahunAkademik) {
        EdomEvaluationTarget target = edomEvaluationTargetDao.findByMahasiswaAndTahunAkademik(mahasiswa, tahunAkademik)
                .orElseGet(() -> {
                    EdomEvaluationTarget newTarget = new EdomEvaluationTarget();
                    newTarget.setMahasiswa(mahasiswa);
                    newTarget.setTahunAkademik(tahunAkademik);
                    return newTarget;
                });

        edomEvaluationTargetDao.save(target);

        for (Map.Entry<String, String> entry : formData.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("evaluasi["))
                continue;

            String inner = key.substring(9, key.length() - 1);
            String[] parts = inner.split("]\\[");

            String jadwalId = null;
            String questionId;
            String dosenId = null;
            String targetStatus;
            Dosen dosen = null;

            if (targetType.equals("DOSEN")) {
                if (parts.length != 3)
                    continue;
                jadwalId = parts[0];
                questionId = parts[1];
                dosenId = parts[2];
                dosen = dosenDao.findById(dosenId).orElse(null);
                targetStatus = "COMPLETED";
            } else if (targetType.equals("SARPRAS")) {
                if (parts.length != 1)
                    continue;
                questionId = parts[0];
                targetStatus = "OPEN";
            } else {
                log.warn("Unknown target type: {}", targetType);
                continue;
            }

            int nilai = Integer.parseInt(entry.getValue());
            EdomQuestion edomQuestion = edomQuestionDao.findById(questionId).orElse(null);

            if (edomQuestion == null) {
                log.warn("Question not found: {}", questionId);
                continue;
            }

            Jadwal jadwal = null;
            KrsDetail krsDetail = null;

            if (jadwalId != null) {
                jadwal = jadwalDao.findById(jadwalId).orElse(null);
                if (jadwal == null) {
                    log.warn("Jadwal not found: {}", jadwalId);
                    continue;
                }
                krsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatus(mahasiswa, jadwal, StatusRecord.AKTIF);
                if (krsDetail == null) {
                    log.warn("KrsDetail not found for mahasiswa: {}, jadwal: {}", mahasiswa.getId(), jadwalId);
                    continue;
                }
            }

            EdomGeneralResponse response = new EdomGeneralResponse();
            response.setTarget(targetType);
            response.setEvaluationTarget(target);
            response.setDosen(dosen);
            response.setQuestion(edomQuestion);
            response.setResponseValue(String.valueOf(nilai));
            response.setJadwal(jadwal);
            response.setKrsDetail(krsDetail);
            edomGeneralResponseDao.save(response);

            if (krsDetail != null) {
                krsDetail.setStatusEdom(StatusRecord.DONE);
                krsDetailDao.save(krsDetail);
            }

            target.setStatus(targetStatus);

            edomEvaluationTargetDao.save(target);
        }
    }
}
