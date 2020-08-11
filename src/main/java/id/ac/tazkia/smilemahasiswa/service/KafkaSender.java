package id.ac.tazkia.smilemahasiswa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

}
