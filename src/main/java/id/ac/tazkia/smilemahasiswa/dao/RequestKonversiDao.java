package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RequestKonversiDao extends PagingAndSortingRepository<RequestKonversi,String> {

    RequestKonversi findByKrsDetailAndStatusAndApproval(KrsDetail krsDetail, StatusRecord statusRecord, StatusRecord approval);

    RequestKonversi findByKrsDetailAndStatus(KrsDetail krsDetail, StatusRecord statusRecord);

    List <RequestKonversi> findByKrsDetailAndKrsDetailJadwalAndStatus(KrsDetail krsDetail, Jadwal jadwal, StatusRecord statusRecord);

    Page <RequestKonversi> findByApprovalAndStatusOrderByCreatedDateAsc(StatusRecord statusRecord1, StatusRecord statusRecord2, Pageable pageable);

    Page <RequestKonversi> findByApprovalAndStatusAndKrsDetailMahasiswaNamaContainingIgnoreCaseOrderByCreatedDateAsc(StatusRecord statusRecord1, StatusRecord statusRecord2,String search, Pageable page);

    Page <RequestKonversi> findByKrsDetailJadwalProdiAndApprovalAndStatusAndFinalization(Prodi prodi, StatusRecord statusRecord1, StatusRecord statusRecord2, StatusRecord statusRecord3, Pageable page);

    Page <RequestKonversi> findByKrsDetailJadwalProdiAndApprovalAndStatus(Prodi prodi, StatusRecord statusRecord1, StatusRecord statusRecord2, Pageable page);
    Page <RequestKonversi> findByKrsDetailJadwalProdiAndApprovalAndStatusAndKrsDetailMahasiswaNamaContainingIgnoreCaseOrderByCreatedDateAsc(Prodi prodi,StatusRecord statusRecord1, StatusRecord statusRecord2, String search, Pageable page);

    Page <RequestKonversi> findByKrsDetailJadwalProdiAndApprovalAndStatusAndFinalizationAndKrsDetailMahasiswaNamaContainingIgnoreCaseOrderByCreatedDateAsc(Prodi prodi, StatusRecord statusRecord1, StatusRecord statusRecord2, StatusRecord statusRecord, String search, Pageable page);

    Page <RequestKonversi> findByApprovalAndStatusAndFinalizationAndKrsDetailMahasiswaNamaContainingIgnoreCaseOrderByCreatedDateAsc(StatusRecord statusRecord1, StatusRecord statusRecord2, StatusRecord statusRecord, String search, Pageable page);


    Page <RequestKonversi> findByKrsDetailJadwalProdiAndStatusAndFinalization(Prodi prodi, StatusRecord statusRecord, StatusRecord finalization, Pageable page);

    Page <RequestKonversi> findByStatusAndFinalizationAndKrsDetailMahasiswaNamaContainingIgnoreCaseOrderByCreatedDateAsc(StatusRecord statusRecord, StatusRecord finalization,String search, Pageable page);

    Page <RequestKonversi> findByKrsDetailJadwalProdiAndStatusAndFinalizationAndKrsDetailMahasiswaNamaContainingIgnoreCaseOrderByCreatedDateAsc(Prodi prodi, StatusRecord statusRecord, StatusRecord finalization,String search, Pageable page);

}
