package com.chriniko.customer_call.quality_service.init;

import com.chriniko.customer_call.quality_service.domain.CustomerCall;
import com.chriniko.customer_call.quality_service.repository.CustomerCallRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DbInjector {

    private final CustomerCallRepository customerCallRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public DbInjector(CustomerCallRepository customerCallRepository,
                      ObjectMapper objectMapper) {
        this.customerCallRepository = customerCallRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() throws Exception {

        customerCallRepository.deleteAll();

        this.getClass().getClassLoader();

        URI uri = this.getClass().getClassLoader().getResource("data/customer_calls.json").toURI();
        Path path = Paths.get(uri);
        String dataAsString = Files.lines(path).collect(Collectors.joining());


        List<CustomerCall> customerCalls = objectMapper.readValue(dataAsString, new TypeReference<List<CustomerCall>>() {
        });

        customerCallRepository.saveAll(customerCalls);
    }

}
