package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Event;
import id.ac.tazkia.smilemahasiswa.entity.EventAbsensi;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface EventAbsensiDao extends PagingAndSortingRepository<EventAbsensi, String>, CrudRepository<EventAbsensi, String> {

    EventAbsensi findByStatusAndEventAndMahasiswa(StatusRecord statusRecord, Event event, Mahasiswa mahasiswa);

    Page<EventAbsensi> findByStatusAndEventOrderByWaktuAbsenAsc(StatusRecord statusRecord, Event event, Pageable pageable);

    @Query(value = "select ea.id, m.nim, m.nama, e.event_name, case when ea.id IS NOT NULL THEN 'HADIR' ELSE 'Tidak Hadir' END AS status_kehadiran, ea.waktu_absen from mahasiswa m inner join dosen d on m.id_dosen_wali=d.id cross join event e left join event_absensi ea on m.id=ea.id_mahasiswa and e.id=ea.id_event where m.status='AKTIF' and m.status_aktif='AKTIF' and e.id = ?1 and d.id=?2 order by m.nim", nativeQuery = true,
                    countQuery = "select count(m.id) from mahasiswa m inner join dosen d on m.id_dosen_wali=d.id cross join event e left join event_absensi ea on m.id=ea.id_mahasiswa and e.id=ea.id_event where m.status='AKTIF' and m.status_aktif='AKTIF' and e.id = ?1 and d.id=?2 order by m.nim")
    Page<Object[]> listAbsensiEventDosenWali(String idEvent, String idDosen, Pageable page);

}
