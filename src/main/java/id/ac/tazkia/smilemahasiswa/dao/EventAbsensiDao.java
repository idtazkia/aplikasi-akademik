package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Event;
import id.ac.tazkia.smilemahasiswa.entity.EventAbsensi;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EventAbsensiDao extends PagingAndSortingRepository<EventAbsensi, String>, CrudRepository<EventAbsensi, String> {

    EventAbsensi findByStatusAndEventAndMahasiswa(StatusRecord statusRecord, Event event, Mahasiswa mahasiswa);

    Page<EventAbsensi> findByStatusAndEventOrderByWaktuAbsenDesc(StatusRecord statusRecord, Event event, Pageable pageable);

}
