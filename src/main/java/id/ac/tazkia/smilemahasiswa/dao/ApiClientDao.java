package id.ac.tazkia.smilemahasiswa.dao;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.entity.ApiClient;

public interface ApiClientDao extends PagingAndSortingRepository<ApiClient, String> {

    ApiClient findByApiKeyHash(String apiKeyHash);

    List<ApiClient> findByIsActiveTrue();

    ApiClient findByClientNameAndIsActiveTrue(String clientName);
}
