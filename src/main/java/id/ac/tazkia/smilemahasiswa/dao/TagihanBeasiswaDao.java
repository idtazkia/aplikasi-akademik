package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Beasiswa;
import id.ac.tazkia.smilemahasiswa.entity.JenisTagihan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TagihanBeasiswa;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TagihanBeasiswaDao extends PagingAndSortingRepository<TagihanBeasiswa, String> {
    List<TagihanBeasiswa> findByBeasiswaAndStatus(Beasiswa beasiswa, StatusRecord statusRecord);

    List<TagihanBeasiswa> findByStatus(StatusRecord statusRecord);

    TagihanBeasiswa findByBeasiswaAndJenisTagihanAndStatus(Beasiswa beasiswa, JenisTagihan tagihan, StatusRecord status);

}
