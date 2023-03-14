package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.ScheduleEvent;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;

public interface ScheduleEventDao extends PagingAndSortingRepository<ScheduleEvent, String> {
    ScheduleEvent findByStatusAndScheduleDate(StatusRecord status, LocalDate tanggal);
}
