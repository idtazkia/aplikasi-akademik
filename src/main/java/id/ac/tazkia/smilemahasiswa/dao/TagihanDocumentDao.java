package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.StatusDocument;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.Tagihan;
import id.ac.tazkia.smilemahasiswa.entity.TagihanDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TagihanDocumentDao extends PagingAndSortingRepository<TagihanDocument, String> {

    Page<TagihanDocument> findByStatusNotInAndTagihanAndStatusDocument(List<StatusRecord> asList, Tagihan tagihan, StatusDocument statusDocument, Pageable page);

    Long countAllByTagihanAndStatusAndStatusDocument(Tagihan tagihan, StatusRecord statusRecord, StatusDocument statusDocument);

}
