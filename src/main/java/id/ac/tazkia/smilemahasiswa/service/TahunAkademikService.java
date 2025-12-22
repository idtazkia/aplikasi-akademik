package id.ac.tazkia.smilemahasiswa.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import id.ac.tazkia.smilemahasiswa.dao.DetailTahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.EdomQuestionOldDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.dao.ProgramDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunProdiDao;
import id.ac.tazkia.smilemahasiswa.dto.tahunakademik.CreateTahunAkademikDto;
import id.ac.tazkia.smilemahasiswa.dto.tahunakademik.DetailTahunAkademikDto;
import id.ac.tazkia.smilemahasiswa.entity.DetailTahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.EdomQuestionOld;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.Program;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademikProdi;
import id.ac.tazkia.smilemahasiswa.util.TahunAkademikUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TahunAkademikService {

    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private DetailTahunAkademikDao detailTahunAkademikDao;
    @Autowired
    private ProdiDao prodiDao;
    @Autowired
    private ProgramDao programDao;
    @Autowired
    private EdomQuestionOldDao edomQuestionOldDao;
    @Autowired
    private TahunProdiDao tahunProdiDao;
    @Autowired
    private MahasiswaDao mahasiswaDao;

    /**
     * Create tahun akademik beserta detail untuk setiap kombinasi prodi-program
     */
    @Transactional
    public TahunAkademik createTahunAkademikWithDetails(CreateTahunAkademikDto dto) {
        try {
            dto.deriveFromKode();

            TahunAkademik tahunAkademik = mapToEntity(dto);
            tahunAkademik = tahunAkademikDao.save(tahunAkademik);

            // createEdomQuestions(tahunAkademik);

            List<DetailTahunAkademik> details = createDetailsForAllProdiProgram(tahunAkademik);

            log.info("Successfully created TahunAkademik {} with {} detail rules",
                    tahunAkademik.getKodeTahunAkademik(), details.size());

            return tahunAkademik;

        } catch (Exception e) {
            log.error("Error creating TahunAkademik: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal membuat tahun akademik: " + e.getMessage(), e);
        }
    }

    /**
     * Update tahun akademik yang sudah ada (tanpa membuat detail baru)
     */
    @Transactional
    public TahunAkademik updateTahunAkademik(CreateTahunAkademikDto dto) {
        try {
            // Cari tahun akademik yang akan diupdate
            TahunAkademik existingTahunAkademik = tahunAkademikDao.findById(dto.getId())
                    .orElseThrow(
                            () -> new RuntimeException("Tahun akademik tidak ditemukan dengan ID: " + dto.getId()));

            // Derive from kode untuk mendapatkan tahun dan jenis
            dto.deriveFromKode();

            // Update data dari DTO
            updateEntityFromDto(existingTahunAkademik, dto);

            // Save tanpa membuat detail baru
            existingTahunAkademik = tahunAkademikDao.save(existingTahunAkademik);

            log.info("Successfully updated TahunAkademik {} without creating new details",
                    existingTahunAkademik.getKodeTahunAkademik());

            return existingTahunAkademik;

        } catch (Exception e) {
            log.error("Error updating TahunAkademik: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal mengupdate tahun akademik: " + e.getMessage(), e);
        }
    }

    /**
     * Update entity dari DTO
     */
    private void updateEntityFromDto(TahunAkademik entity, CreateTahunAkademikDto dto) {
        entity.setKodeTahunAkademik(dto.getKodeTahunAkademik());
        entity.setNamaTahunAkademik(dto.getNamaTahunAkademik());
        entity.setTanggalMulai(dto.getTanggalMulai());
        entity.setTanggalSelesai(dto.getTanggalSelesai());
        entity.setTanggalMulaiKrs(dto.getTanggalMulaiKrs());
        entity.setTanggalSelesaiKrs(dto.getTanggalSelesaiKrs());
        entity.setTanggalMulaiKuliah(dto.getTanggalMulaiKuliah());
        entity.setTanggalSelesaiKuliah(dto.getTanggalSelesaiKuliah());
        entity.setTanggalMulaiUts(dto.getTanggalMulaiUts());
        entity.setTanggalSelesaiUts(dto.getTanggalSelesaiUts());
        entity.setTanggalMulaiUas(dto.getTanggalMulaiUas());
        entity.setTanggalSelesaiUas(dto.getTanggalSelesaiUas());
        entity.setTanggalMulaiNilai(dto.getTanggalMulaiNilai());
        entity.setTanggalSelesaiNilai(dto.getTanggalSelesaiNilai());
        entity.setTahun(dto.getTahun());
        entity.setJenis(dto.getJenis());
        entity.setStatus(dto.getStatus());
    }

    /**
     * Get detail tahun akademik yang berlaku untuk mahasiswa tertentu
     */
    public DetailTahunAkademik getApplicableDetail(String tahunAkademikId, String prodiId,
            String programId, String angkatan) {
        List<DetailTahunAkademik> applicableRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademikId, prodiId, programId, angkatan, StatusRecord.AKTIF);

        return applicableRules.isEmpty() ? null : applicableRules.get(0);
    }

    /**
     * Get detail tahun akademik yang berlaku untuk mahasiswa tertentu (return DTO)
     */
    public DetailTahunAkademikDto getApplicableDetailDto(String tahunAkademikId, String prodiId,
            String programId, String angkatan) {
        DetailTahunAkademik detail = getApplicableDetail(tahunAkademikId, prodiId, programId, angkatan);
        return detail != null ? mapToDto(detail) : null;
    }

    /**
     * Get all details by tahun akademik
     */
    public List<DetailTahunAkademikDto> getDetailsByTahunAkademik(String tahunAkademikId) {
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahunAkademikId)
                .orElseThrow(() -> new RuntimeException("Tahun akademik tidak ditemukan"));

        List<DetailTahunAkademik> details = detailTahunAkademikDao.findByTahunAkademikAndStatus(
                tahunAkademik, StatusRecord.AKTIF);

        return details.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Create detail untuk semua kombinasi prodi-program
     */
    private List<DetailTahunAkademik> createDetailsForAllProdiProgram(TahunAkademik tahunAkademik) {
        List<DetailTahunAkademik> details = new ArrayList<>();

        List<Prodi> prodis = prodiDao.findByStatus(StatusRecord.AKTIF);

        for (Prodi prodi : prodis) {
            List<Program> programs = prodi.getPrograms() == null ? null : new ArrayList<>(prodi.getPrograms());
            if (programs == null)
                continue;

            for (Program program : programs) {
                Optional<Prodi> prodiOpt = prodiDao.findById(prodi.getId());
                Optional<Program> programOpt = programDao.findById(program.getId());

                if (prodiOpt.isPresent() && programOpt.isPresent()) {
                    DetailTahunAkademik detail = createDetailEntity(tahunAkademik, prodiOpt.get(), programOpt.get());
                    detail = detailTahunAkademikDao.save(detail);
                    details.add(detail);

                    log.debug("Created detail for {} - {}", prodi.getNamaProdi(), program.getNamaProgram());
                }
            }
        }

        log.info("Created {} detail rules for tahun akademik {}", details.size(), tahunAkademik.getKodeTahunAkademik());
        return details;
    }

    /**
     * Create detail entity
     */
    private DetailTahunAkademik createDetailEntity(TahunAkademik tahunAkademik, Prodi prodi, Program program) {
        DetailTahunAkademik detail = new DetailTahunAkademik();
        detail.setTahunAkademik(tahunAkademik);
        detail.setProdi(prodi);
        detail.setProgram(program);

        detail.setAngkatanMulai(null);
        detail.setAngkatanSelesai(null);

        copyDatesFromTahunAkademik(detail, tahunAkademik);

        detail.setPrioritas(0);
        detail.setKeterangan("Auto-generated untuk " + prodi.getNamaProdi() + " - " + program.getNamaProgram());
        detail.setStatus(StatusRecord.AKTIF);

        return detail;
    }

    /**
     * Copy tanggal dari tahun akademik ke detail
     */
    private void copyDatesFromTahunAkademik(DetailTahunAkademik detail, TahunAkademik tahunAkademik) {
        detail.setTanggalMulaiKrs(tahunAkademik.getTanggalMulaiKrs());
        detail.setTanggalSelesaiKrs(tahunAkademik.getTanggalSelesaiKrs());
        detail.setTanggalMulaiKuliah(tahunAkademik.getTanggalMulaiKuliah());
        detail.setTanggalSelesaiKuliah(tahunAkademik.getTanggalSelesaiKuliah());
        detail.setTanggalMulaiUts(tahunAkademik.getTanggalMulaiUts());
        detail.setTanggalSelesaiUts(tahunAkademik.getTanggalSelesaiUts());
        detail.setTanggalMulaiUas(tahunAkademik.getTanggalMulaiUas());
        detail.setTanggalSelesaiUas(tahunAkademik.getTanggalSelesaiUas());
        detail.setTanggalMulaiNilai(tahunAkademik.getTanggalMulaiNilai());
        detail.setTanggalSelesaiNilai(tahunAkademik.getTanggalSelesaiNilai());
    }

    /**
     * Create EDOM questions (copy dari tahun akademik aktif)
     */
    private void createEdomQuestions(TahunAkademik tahunAkademik) {
        TahunAkademik activeTahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if (activeTahunAkademik != null) {
            List<EdomQuestionOld> existingQuestions = edomQuestionOldDao
                    .findByStatusAndTahunAkademikOrderByNomorAsc(StatusRecord.AKTIF, activeTahunAkademik);

            for (EdomQuestionOld question : existingQuestions) {
                EdomQuestionOld newQuestion = new EdomQuestionOld();
                newQuestion.setStatus(StatusRecord.AKTIF);
                newQuestion.setBahasa(question.getBahasa());
                newQuestion.setNomor(question.getNomor());
                newQuestion.setPertanyaan(question.getPertanyaan());
                newQuestion.setTahunAkademik(tahunAkademik);
                edomQuestionOldDao.save(newQuestion);
            }

            log.info("Copied {} EDOM questions to new tahun akademik", existingQuestions.size());
        }
    }

    /**
     * Map DTO to Entity
     */
    private TahunAkademik mapToEntity(CreateTahunAkademikDto dto) {
        TahunAkademik entity = new TahunAkademik();
        entity.setKodeTahunAkademik(dto.getKodeTahunAkademik());
        entity.setNamaTahunAkademik(dto.getNamaTahunAkademik());
        entity.setTanggalMulai(dto.getTanggalMulai());
        entity.setTanggalSelesai(dto.getTanggalSelesai());
        entity.setTanggalMulaiKrs(dto.getTanggalMulaiKrs());
        entity.setTanggalSelesaiKrs(dto.getTanggalSelesaiKrs());
        entity.setTanggalMulaiKuliah(dto.getTanggalMulaiKuliah());
        entity.setTanggalSelesaiKuliah(dto.getTanggalSelesaiKuliah());
        entity.setTanggalMulaiUts(dto.getTanggalMulaiUts());
        entity.setTanggalSelesaiUts(dto.getTanggalSelesaiUts());
        entity.setTanggalMulaiUas(dto.getTanggalMulaiUas());
        entity.setTanggalSelesaiUas(dto.getTanggalSelesaiUas());
        entity.setTanggalMulaiNilai(dto.getTanggalMulaiNilai());
        entity.setTanggalSelesaiNilai(dto.getTanggalSelesaiNilai());
        entity.setTahun(dto.getTahun());
        entity.setJenis(dto.getJenis());
        entity.setStatus(dto.getStatus());
        return entity;
    }

    /**
     * Map Entity to DTO
     */
    private DetailTahunAkademikDto mapToDto(DetailTahunAkademik entity) {
        DetailTahunAkademikDto dto = new DetailTahunAkademikDto();
        dto.setId(entity.getId());
        dto.setIdTahunAkademik(entity.getTahunAkademik().getId());

        if (entity.getProdi() != null) {
            dto.setIdProdi(entity.getProdi().getId());
            dto.setNamaProdi(entity.getProdi().getNamaProdi());
        }

        if (entity.getProgram() != null) {
            dto.setIdProgram(entity.getProgram().getId());
            dto.setNamaProgram(entity.getProgram().getNamaProgram());
        }

        dto.setAngkatanMulai(entity.getAngkatanMulai());
        dto.setAngkatanSelesai(entity.getAngkatanSelesai());
        dto.setTanggalMulaiKrs(entity.getTanggalMulaiKrs());
        dto.setTanggalSelesaiKrs(entity.getTanggalSelesaiKrs());
        dto.setTanggalMulaiKuliah(entity.getTanggalMulaiKuliah());
        dto.setTanggalSelesaiKuliah(entity.getTanggalSelesaiKuliah());
        dto.setTanggalMulaiUts(entity.getTanggalMulaiUts());
        dto.setTanggalSelesaiUts(entity.getTanggalSelesaiUts());
        dto.setTanggalMulaiUas(entity.getTanggalMulaiUas());
        dto.setTanggalSelesaiUas(entity.getTanggalSelesaiUas());
        dto.setTanggalMulaiNilai(entity.getTanggalMulaiNilai());
        dto.setTanggalSelesaiNilai(entity.getTanggalSelesaiNilai());
        dto.setPrioritas(entity.getPrioritas());
        dto.setKeterangan(entity.getKeterangan());

        return dto;
    }

    /**
     * Setup form untuk mode edit
     */
    public void setupEditMode(String id, Model model) {
        model.addAttribute("stringId", id);
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Tahun Akademik tidak ditemukan"));

        // Convert entity to DTO for form binding
        CreateTahunAkademikDto dto = convertEntityToDto(tahunAkademik);
        model.addAttribute("tahunAkademikDto", dto);

        model.addAttribute("kode", tahunAkademik.getKodeTahunAkademik());
        model.addAttribute("nama", tahunAkademik.getNamaTahunAkademik());
        model.addAttribute("tahunAkademik", tahunAkademik);
    }

    /**
     * Convert TahunAkademik entity to CreateTahunAkademikDto
     */
    private CreateTahunAkademikDto convertEntityToDto(TahunAkademik entity) {
        CreateTahunAkademikDto dto = new CreateTahunAkademikDto();
        dto.setId(entity.getId());
        dto.setKodeTahunAkademik(entity.getKodeTahunAkademik());
        dto.setNamaTahunAkademik(entity.getNamaTahunAkademik());
        dto.setTanggalMulai(entity.getTanggalMulai());
        dto.setTanggalSelesai(entity.getTanggalSelesai());
        dto.setTanggalMulaiKrs(entity.getTanggalMulaiKrs());
        dto.setTanggalSelesaiKrs(entity.getTanggalSelesaiKrs());
        dto.setTanggalMulaiKuliah(entity.getTanggalMulaiKuliah());
        dto.setTanggalSelesaiKuliah(entity.getTanggalSelesaiKuliah());
        dto.setTanggalMulaiUts(entity.getTanggalMulaiUts());
        dto.setTanggalSelesaiUts(entity.getTanggalSelesaiUts());
        dto.setTanggalMulaiUas(entity.getTanggalMulaiUas());
        dto.setTanggalSelesaiUas(entity.getTanggalSelesaiUas());
        dto.setTanggalMulaiNilai(entity.getTanggalMulaiNilai());
        dto.setTanggalSelesaiNilai(entity.getTanggalSelesaiNilai());
        dto.setTahun(entity.getTahun());
        dto.setJenis(entity.getJenis());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    /**
     * Setup form untuk mode create
     */
    public void setupCreateMode(Model model) {
        TahunAkademik tahunSebelum = getLatestTahunAkademik();
        String kode = generateNextKode(tahunSebelum);
        String nama = generateNamaTahunAkademik(kode);

        // Create DTO for form binding
        CreateTahunAkademikDto dto = new CreateTahunAkademikDto();
        dto.setKodeTahunAkademik(kode);
        dto.setNamaTahunAkademik(nama);
        dto.setStatus(StatusRecord.NONAKTIF); // Default status

        model.addAttribute("tahunAkademikDto", dto);
        model.addAttribute("kode", kode);
        model.addAttribute("nama", nama);
    }

    /**
     * Get tahun akademik terbaru (non-deleted)
     */
    private TahunAkademik getLatestTahunAkademik() {
        return tahunAkademikDao.findTopByStatusNotInOrderByKodeTahunAkademikDesc(
                Arrays.asList(StatusRecord.HAPUS));
    }

    /**
     * Generate kode tahun akademik selanjutnya
     */
    private String generateNextKode(TahunAkademik tahunSebelum) {
        return TahunAkademikUtils.generateNextKode(
                tahunSebelum.getKodeTahunAkademik(),
                tahunSebelum.getJenis().name(),
                tahunSebelum.getTahun());
    }

    /**
     * Generate nama tahun akademik berdasarkan kode
     */
    private String generateNamaTahunAkademik(String kode) {
        return TahunAkademikUtils.generateNamaTahunAkademik(kode);
    }

    /**
     * Aktivasi tahun akademik dan sinkronisasi dengan prodi
     */
    @Transactional
    public void activateTahunAkademik(TahunAkademik tahunAkademik) {
        deactivateAllActiveTahunAkademik();
        activateSpecificTahunAkademik(tahunAkademik);
        syncTahunAkademikProdi(tahunAkademik);
    }

    /**
     * Deactivate semua tahun akademik yang aktif
     */
    private void deactivateAllActiveTahunAkademik() {
        // Deactivate all active TahunAkademikProdi
        List<TahunAkademikProdi> activeTahunAkademikProdi = tahunProdiDao.findByStatus(StatusRecord.AKTIF);
        activeTahunAkademikProdi.forEach(tahunProdi -> {
            tahunProdi.setStatus(StatusRecord.NONAKTIF);
            tahunProdiDao.save(tahunProdi);
        });

        // Deactivate active TahunAkademik
        TahunAkademik activeTahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if (activeTahunAkademik != null) {
            activeTahunAkademik.setStatus(StatusRecord.NONAKTIF);
            tahunAkademikDao.save(activeTahunAkademik);
        }
    }

    /**
     * Activate tahun akademik tertentu
     */
    private void activateSpecificTahunAkademik(TahunAkademik tahunAkademik) {
        tahunAkademik.setStatus(StatusRecord.AKTIF);
        tahunAkademikDao.save(tahunAkademik);
    }

    /**
     * Sinkronisasi TahunAkademikProdi dengan TahunAkademik yang aktif
     */
    private void syncTahunAkademikProdi(TahunAkademik tahunAkademik) {
        TahunAkademik aktiveTahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        List<TahunAkademikProdi> existingProdiDetails = tahunProdiDao.findByTahunAkademik(aktiveTahunAkademik);

        if (!existingProdiDetails.isEmpty()) {
            // Activate existing prodi details
            existingProdiDetails.forEach(tahun -> {
                tahun.setStatus(StatusRecord.AKTIF);
                tahunProdiDao.save(tahun);
            });
        } else {
            createTahunAkademikProdiForAllProdi(tahunAkademik);
        }
    }

    /**
     * Create TahunAkademikProdi untuk semua prodi aktif
     */
    private void createTahunAkademikProdiForAllProdi(TahunAkademik tahunAkademik) {
        List<Prodi> aktiveProdis = prodiDao.findByStatus(StatusRecord.AKTIF);

        aktiveProdis.forEach(prodi -> {
            TahunAkademikProdi tahunProdi = new TahunAkademikProdi();
            tahunProdi.setMulaiKuliah(tahunAkademik.getTanggalMulaiKuliah());
            tahunProdi.setMulaiNilai(tahunAkademik.getTanggalMulaiNilai());
            tahunProdi.setMulaiUas(tahunAkademik.getTanggalMulaiUas());
            tahunProdi.setMulaiKrs(tahunAkademik.getTanggalMulaiKrs());
            tahunProdi.setMulaiUts(tahunAkademik.getTanggalMulaiUts());
            tahunProdi.setProdi(prodi);
            tahunProdi.setSelesaiKrs(tahunAkademik.getTanggalSelesaiKrs());
            tahunProdi.setSelesaiKuliah(tahunAkademik.getTanggalSelesaiKuliah());
            tahunProdi.setSelesaiNilai(tahunAkademik.getTanggalSelesaiNilai());
            tahunProdi.setSelesaiUas(tahunAkademik.getTanggalSelesaiUas());
            tahunProdi.setSelesaiUts(tahunAkademik.getTanggalSelesaiUts());
            tahunProdi.setStatus(StatusRecord.AKTIF);
            tahunProdi.setTahunAkademik(tahunAkademik);
            tahunProdiDao.save(tahunProdi);
        });
    }

    /**
     * Delete (soft delete) tahun akademik
     */
    @Transactional
    public void deleteTahunAkademik(TahunAkademik tahunAkademik) {
        tahunAkademik.setStatus(StatusRecord.HAPUS);
        tahunAkademikDao.save(tahunAkademik);
    }

    public void setupTahunAkademikProdiEditMode(DetailTahunAkademik detailTahunAkademik,
            Model model) {

        // Guard clause for null input
        if (detailTahunAkademik == null) {
            log.warn("DetailTahunAkademik is null, setting up empty form");
            model.addAttribute("detail", new DetailTahunAkademik());
            model.addAttribute("programList", new ArrayList<>());
            model.addAttribute("angkatanList", new ArrayList<>());
            return;
        }

        model.addAttribute("detail", detailTahunAkademik);

        if (detailTahunAkademik.getTahunAkademik() != null) {
            model.addAttribute("tahunAkademik",
                    tahunAkademikDao.findById(detailTahunAkademik.getTahunAkademik().getId()).get());
        }

        if (detailTahunAkademik.getProdi() != null) {
            model.addAttribute("prodi", prodiDao.findById(detailTahunAkademik.getProdi().getId()).get());
            model.addAttribute("programList", programDao.findAll());

            List<String> availableAngkatan = getAvailableAngkatanForDetail(
                    detailTahunAkademik.getProdi(),
                    detailTahunAkademik.getProgram(),
                    detailTahunAkademik.getId());
            model.addAttribute("angkatanList", availableAngkatan);
        } else {
            model.addAttribute("programList", new ArrayList<>());
            model.addAttribute("angkatanList", new ArrayList<>());
        }
    }

    public void setupTahunAkademikProdiMode(TahunAkademik tahunAkademik, Prodi prodi, Model model) {
        model.addAttribute("detail", new DetailTahunAkademik());
        model.addAttribute("tahunAkademik", tahunAkademik);
        model.addAttribute("prodi", prodi);

        if (prodi != null) {
            model.addAttribute("programList", programDao.findAll());
            // For create mode, we don't have a program selected yet, so show all angkatan
            model.addAttribute("angkatanList",
                    mahasiswaDao.findDistinctAngkatanByJenjang(prodi.getIdJenjang().getId()));
        } else {
            model.addAttribute("programList", new ArrayList<>());
            model.addAttribute("angkatanList", new ArrayList<>());
        }
    }

    @Transactional
    public void saveTahunAkademikProdi(TahunAkademikProdi tahunAkademikProdi) {
        tahunAkademikProdi.setStatus(StatusRecord.AKTIF);
        tahunProdiDao.save(tahunAkademikProdi);
    }

    /**
     * Activate TahunAkademikProdi (deactivate others for same tahun akademik and
     * prodi)
     */
    @Transactional
    public void activateTahunAkademikProdi(String tahunAkademikProdiId) {
        TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findById(tahunAkademikProdiId)
                .orElseThrow(
                        () -> new IllegalArgumentException("TahunAkademikProdi not found: " + tahunAkademikProdiId));

        // Deactivate all active TahunAkademikProdi for the same TahunAkademik and Prodi
        List<TahunAkademikProdi> tahunAkademikProdis = tahunProdiDao.findByTahunAkademikAndProdiAndStatus(
                tahunAkademikProdi.getTahunAkademik(), tahunAkademikProdi.getProdi(), StatusRecord.AKTIF);

        tahunAkademikProdis.forEach(tap -> {
            tap.setStatus(StatusRecord.NONAKTIF);
            tahunProdiDao.save(tap);
        });

        // Activate the selected one
        tahunAkademikProdi.setStatus(StatusRecord.AKTIF);
        tahunProdiDao.save(tahunAkademikProdi);
    }

    /**
     * Deactivate TahunAkademikProdi
     */
    @Transactional
    public void deactivateTahunAkademikProdi(String tahunAkademikProdiId) {
        TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findById(tahunAkademikProdiId)
                .orElseThrow(
                        () -> new IllegalArgumentException("TahunAkademikProdi not found: " + tahunAkademikProdiId));

        tahunAkademikProdi.setStatus(StatusRecord.NONAKTIF);
        tahunProdiDao.save(tahunAkademikProdi);
    }

    /**
     * Save DetailTahunAkademik dengan validasi dinamis
     */
    @Transactional
    public DetailTahunAkademik saveDetailTahunAkademikWithValidation(DetailTahunAkademik detail) {
        // Jalankan validasi dinamis sebelum save
        validateDetailTahunAkademikDynamic(detail);

        // Save jika validasi berhasil
        return detailTahunAkademikDao.save(detail);
    }

    /**
     * Get available angkatan for detail tahun akademik (exclude already used ones)
     */
    private List<String> getAvailableAngkatanForDetail(Prodi prodi, Program program, String currentDetailId) {
        if (prodi == null) {
            return new ArrayList<>();
        }

        // Get all angkatan for this jenjang
        List<String> allAngkatan = mahasiswaDao.findDistinctAngkatanByJenjang(prodi.getIdJenjang().getId());

        // Extract used angkatan ranges, excluding current detail if editing
        List<String> usedAngkatan = new ArrayList<>();

        if (program == null) {
            // Program null means this rule applies to ALL programs in prodi
            // Check angkatan used by any program in this prodi
            List<DetailTahunAkademik> existingDetails = detailTahunAkademikDao
                    .findByProdiAndProgramAndStatusOrderByAngkatanMulaiAsc(prodi, null, StatusRecord.AKTIF);

            // Also get details for specific programs that might conflict
            List<Program> programs = prodi.getPrograms() == null ? new ArrayList<>()
                    : new ArrayList<>(prodi.getPrograms());
            for (Program specificProgram : programs) {
                List<DetailTahunAkademik> programDetails = detailTahunAkademikDao
                        .findByProdiAndProgramAndStatusOrderByAngkatanMulaiAsc(prodi, specificProgram,
                                StatusRecord.AKTIF);
                existingDetails.addAll(programDetails);
            }

            extractUsedAngkatan(existingDetails, usedAngkatan, currentDetailId);

        } else {
            // Program is specified, check conflicts with:
            // 1. Same prodi+program combination
            // 2. Rules that apply to all programs (program = null) in this prodi

            List<DetailTahunAkademik> existingDetails = detailTahunAkademikDao
                    .findByProdiAndProgramAndStatusOrderByAngkatanMulaiAsc(prodi, program, StatusRecord.AKTIF);

            // Also check rules that apply to all programs (program = null)
            List<DetailTahunAkademik> generalRules = detailTahunAkademikDao
                    .findByProdiAndProgramAndStatusOrderByAngkatanMulaiAsc(prodi, null, StatusRecord.AKTIF);
            existingDetails.addAll(generalRules);

            extractUsedAngkatan(existingDetails, usedAngkatan, currentDetailId);
        }

        // Filter out used angkatan
        return allAngkatan.stream()
                .filter(angkatan -> !usedAngkatan.contains(angkatan))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Helper method to extract used angkatan from list of details
     */
    private void extractUsedAngkatan(List<DetailTahunAkademik> existingDetails, List<String> usedAngkatan,
            String currentDetailId) {
        for (DetailTahunAkademik detail : existingDetails) {
            // Skip current detail when editing
            if (currentDetailId != null && currentDetailId.equals(detail.getId())) {
                continue;
            }

            String mulai = detail.getAngkatanMulai();
            String selesai = detail.getAngkatanSelesai();

            if (mulai != null && selesai != null) {
                // Add all angkatan in range
                try {
                    int start = Integer.parseInt(mulai);
                    int end = Integer.parseInt(selesai);
                    for (int i = start; i <= end; i++) {
                        usedAngkatan.add(String.valueOf(i));
                    }
                } catch (NumberFormatException e) {
                    // If not numeric, add as is
                    usedAngkatan.add(mulai);
                    if (!mulai.equals(selesai)) {
                        usedAngkatan.add(selesai);
                    }
                }
            } else if (mulai != null) {
                usedAngkatan.add(mulai);
            }
        }
    }

    /**
     * Get available angkatan for AJAX call when program is selected
     */
    public List<String> getAvailableAngkatanByProdiAndProgram(String prodiId, String programId,
            String currentDetailId) {
        if (prodiId == null) {
            return new ArrayList<>();
        }

        Optional<Prodi> prodiOpt = prodiDao.findById(prodiId);
        if (!prodiOpt.isPresent()) {
            return new ArrayList<>();
        }

        Prodi prodi = prodiOpt.get();
        Program program = null;

        if (programId != null) {
            Optional<Program> programOpt = programDao.findById(programId);
            if (programOpt.isPresent()) {
                program = programOpt.get();
            }
        }

        return getAvailableAngkatanForDetail(prodi, program, currentDetailId);
    }

    public void validateDetailTahunAkademikDynamic(DetailTahunAkademik newDetail) {
        if (newDetail.getProdi() == null) {
            return; // No validation needed if prodi is not set
        }

        // Get existing details that could conflict with new detail
        List<DetailTahunAkademik> existingDetails = new ArrayList<>();

        if (newDetail.getProgram() == null) {
            // New detail applies to ALL programs - check conflicts with:
            // 1. Other rules that apply to all programs (program = null)
            // 2. Any rules for specific programs in this prodi

            // Rules for all programs
            List<DetailTahunAkademik> generalRules = detailTahunAkademikDao
                    .findByProdiAndProgramAndStatusOrderByAngkatanMulaiAsc(newDetail.getProdi(), null,
                            StatusRecord.AKTIF);
            existingDetails.addAll(generalRules);

            // Rules for specific programs
            List<Program> programs = newDetail.getProdi().getPrograms() == null ? new ArrayList<>()
                    : new ArrayList<>(newDetail.getProdi().getPrograms());
            for (Program program : programs) {
                List<DetailTahunAkademik> programRules = detailTahunAkademikDao
                        .findByProdiAndProgramAndStatusOrderByAngkatanMulaiAsc(newDetail.getProdi(), program,
                                StatusRecord.AKTIF);
                existingDetails.addAll(programRules);
            }

        } else {
            // New detail applies to specific program - check conflicts with:
            // 1. Rules for same prodi+program combination
            // 2. Rules that apply to all programs (program = null) in this prodi

            List<DetailTahunAkademik> programRules = detailTahunAkademikDao
                    .findByProdiAndProgramAndStatusOrderByAngkatanMulaiAsc(newDetail.getProdi(), newDetail.getProgram(),
                            StatusRecord.AKTIF);
            existingDetails.addAll(programRules);

            List<DetailTahunAkademik> generalRules = detailTahunAkademikDao
                    .findByProdiAndProgramAndStatusOrderByAngkatanMulaiAsc(newDetail.getProdi(), null,
                            StatusRecord.AKTIF);
            existingDetails.addAll(generalRules);
        }

        if (existingDetails.isEmpty()) {
            return; // Tidak ada data existing, boleh input apapun
        }

        // Validasi overlap dengan existing data
        String newAngkatanMulai = newDetail.getAngkatanMulai();
        String newAngkatanSelesai = newDetail.getAngkatanSelesai();

        for (DetailTahunAkademik existing : existingDetails) {
            // Skip validasi jika ini adalah data yang sama (update case)
            if (newDetail.getId() != null && newDetail.getId().equals(existing.getId())) {
                continue;
            }

            // Skip validasi jika ini adalah range angkatan yang sama dalam tahun akademik
            // yang sama
            // (misal: 2024-2024 untuk angkatan khusus 2024)
            if (newDetail.getTahunAkademik() != null &&
                    newDetail.getTahunAkademik().getId().equals(existing.getTahunAkademik().getId()) &&
                    newAngkatanMulai != null && newAngkatanMulai.equals(newAngkatanSelesai) &&
                    existing.getAngkatanMulai() != null
                    && existing.getAngkatanMulai().equals(existing.getAngkatanSelesai()) &&
                    newAngkatanMulai.equals(existing.getAngkatanMulai())) {
                continue; // Izinkan multiple detail untuk angkatan khusus yang sama dalam tahun akademik
                          // yang sama
            }

            if (TahunAkademikUtils.isOverlapping(newAngkatanMulai, newAngkatanSelesai,
                    existing.getAngkatanMulai(), existing.getAngkatanSelesai())) {

                String conflictDescription = buildConflictDescription(newDetail, existing);

                throw new IllegalArgumentException(
                        String.format("Range angkatan %s-%s untuk %s " +
                                "bertumpang tindih dengan data existing: %s. " +
                                "Silakan gunakan range angkatan yang berbeda.",
                                newAngkatanMulai, newAngkatanSelesai,
                                getDetailDescription(newDetail),
                                conflictDescription));
            }
        }
    }

    /**
     * Helper method to build conflict description
     */
    private String buildConflictDescription(DetailTahunAkademik newDetail, DetailTahunAkademik existing) {
        StringBuilder desc = new StringBuilder();
        desc.append("angkatan ").append(existing.getAngkatanMulai()).append("-").append(existing.getAngkatanSelesai());

        if (existing.getProgram() == null) {
            desc.append(" untuk semua program");
        } else {
            desc.append(" untuk program ").append(existing.getProgram().getNamaProgram());
        }

        desc.append(" di prodi ").append(existing.getProdi().getNamaProdi());

        return desc.toString();
    }

    /**
     * Helper method to get detail description
     */
    private String getDetailDescription(DetailTahunAkademik detail) {
        StringBuilder desc = new StringBuilder();

        if (detail.getProgram() == null) {
            desc.append("semua program");
        } else {
            desc.append("program '").append(detail.getProgram().getNamaProgram()).append("'");
        }

        desc.append(" di prodi '").append(detail.getProdi().getNamaProdi()).append("'");

        return desc.toString();
    }

    /**
     * Mendapatkan tahun akademik sebelumnya untuk referensi
     * Logic:
     * - Jika sekarang GANJIL, cari GENAP tahun sebelumnya (contoh: 2024/2025 Ganjil -> 2023/2024 Genap)
     * - Jika sekarang GENAP, cari GANJIL tahun yang sama (contoh: 2023/2024 Genap -> 2023/2024 Ganjil)
     * - Skip tahun PENDEK
     */
    public TahunAkademik getPreviousTahunForReference(TahunAkademik currentTahun) {
        if (currentTahun == null) {
            return null;
        }

        // Fallback untuk kasus lain (termasuk PENDEK)
        return tahunAkademikDao.findFirstByKodeTahunAkademikLessThanAndStatusNotInOrderByKodeTahunAkademikDesc(
            currentTahun.getKodeTahunAkademik(), Arrays.asList(StatusRecord.HAPUS));
    }
}
