package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.BiayaSksSp;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BiayaSksSpDao extends PagingAndSortingRepository<BiayaSksSp, String> {
    BiayaSksSp findByStatus(StatusRecord statusRecord);
}
