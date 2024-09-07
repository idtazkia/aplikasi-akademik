package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Event;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventDao extends PagingAndSortingRepository<Event, String>, CrudRepository<Event, String> {

    Page<Event> findByStatusOrderByScheduleStartDesc(StatusRecord statusRecord, Pageable pageable);

    List<Event> findByStatusAndSchedulePublishBeforeAndScheduleEndAfterOrderByScheduleStart(StatusRecord statusRecord, LocalDateTime mulaiPublish, LocalDateTime selesaiAcaca);
}
