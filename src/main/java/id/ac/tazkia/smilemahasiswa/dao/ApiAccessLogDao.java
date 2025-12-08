package id.ac.tazkia.smilemahasiswa.dao;

import java.time.LocalDateTime;

import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.entity.ApiAccessLog;
import id.ac.tazkia.smilemahasiswa.entity.ApiClient;

public interface ApiAccessLogDao extends PagingAndSortingRepository<ApiAccessLog, String> {

    long countByClientAndTimestampAfter(ApiClient client, LocalDateTime timestamp);
}
