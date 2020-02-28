package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Note;
import id.ac.tazkia.smilemahasiswa.entity.Seminar;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SeminarDao extends PagingAndSortingRepository<Seminar, String> {
    Seminar findByNoteAndStatusNotIn(Note note, List<StatusApprove> statusRecords);
}
