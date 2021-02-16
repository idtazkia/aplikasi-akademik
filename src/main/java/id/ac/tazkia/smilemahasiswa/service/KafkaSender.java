package id.ac.tazkia.smilemahasiswa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.smilemahasiswa.dto.payment.HapusTagihanRequest;
import id.ac.tazkia.smilemahasiswa.dto.payment.TagihanRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class KafkaSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListener.class);

    @Value("${kafka.topic.tagihan.request}") private String kafkaTopicTagihanRequest;
    @Value("${kafka.topic.hapus.tagihan}") private String kafkaTopicHapusTagihan;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public void requestCreateTagihan(TagihanRequest request){
        try{
            String jsonRequest = objectMapper.writeValueAsString(request);
            LOGGER.info("Create Tagihan Request : {}", jsonRequest);
            kafkaTemplate.send(kafkaTopicTagihanRequest, jsonRequest);
        } catch (Exception err){
            LOGGER.warn(err.getMessage(), err);
        }
    }

    public void requsetHapusTagihan(HapusTagihanRequest hapusTagihanRequest){
        try{
            String jsonRequest = objectMapper.writeValueAsString(hapusTagihanRequest);
            LOGGER.info("Hapus Tagihan Request : {}", jsonRequest);
            kafkaTemplate.send(kafkaTopicHapusTagihan, jsonRequest);
        }catch (Exception err){
            LOGGER.warn(err.getMessage(), err);
        }
    }

}
