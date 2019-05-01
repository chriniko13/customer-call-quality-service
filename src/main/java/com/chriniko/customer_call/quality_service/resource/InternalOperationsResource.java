package com.chriniko.customer_call.quality_service.resource;

import com.chriniko.customer_call.quality_service.service.CustomerCallQualityHeapService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Log4j2

@RestController
@RequestMapping("/api/internal-operations")
public class InternalOperationsResource {

    private final CustomerCallQualityHeapService customerCallQualityHeapService;

    @Autowired
    public InternalOperationsResource(CustomerCallQualityHeapService customerCallQualityHeapService) {
        this.customerCallQualityHeapService = customerCallQualityHeapService;
    }

    @PutMapping(path = "reindex-heap-service")
    public @ResponseBody
    HttpEntity<Void> reindexHeapService() {
        customerCallQualityHeapService.index();
        log.debug("heap service re-indexed successfully!");
        return ResponseEntity.ok().build();
    }

}
