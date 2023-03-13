package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.AttendanceEventParent;
import id.ac.tazkia.smilemahasiswa.entity.Ayah;
import id.ac.tazkia.smilemahasiswa.entity.ScheduleEvent;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AttendanceEventParentDao extends PagingAndSortingRepository<AttendanceEventParent, String> {
    AttendanceEventParent findByAyahAndScheduleEvent(Ayah ayah, ScheduleEvent scheduleEvent);
}
