package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KrsApprovalDao extends PagingAndSortingRepository<KrsApproval, String> {

    KrsApproval findByKrsAndMahasiswaAndStatus(Krs krs, Mahasiswa mahasiswa, StatusApprove status);
    KrsApproval findByKrsAndMahasiswa(Krs krs, Mahasiswa mahasiswa);

    Page<KrsApproval> findByMahasiswaDosen(Dosen dosen, Pageable page);

}
