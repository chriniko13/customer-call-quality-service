package com.chriniko.customer_call.quality_service.init;

import com.chriniko.customer_call.quality_service.configuration.HazelcastConfiguration;
import com.chriniko.customer_call.quality_service.domain.CustomerCall;
import com.chriniko.customer_call.quality_service.domain.CustomerCallsByDate;
import com.chriniko.customer_call.quality_service.repository.CustomerCallRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2

@Component
public class DbInjector {

    private final CustomerCallRepository customerCallRepository;
    private final ObjectMapper objectMapper;
    private final HazelcastInstance hazelcastInstance;

    @Autowired
    public DbInjector(CustomerCallRepository customerCallRepository,
                      ObjectMapper objectMapper,
                      HazelcastInstance hazelcastInstance) {
        this.customerCallRepository = customerCallRepository;
        this.objectMapper = objectMapper;
        this.hazelcastInstance = hazelcastInstance;
    }


    @PostConstruct
    void init() throws Exception {
        List<CustomerCall> storedCustomerCalls = prepareDB();

        IMap<Long, CustomerCall> customerCallsById = hazelcastInstance.getMap(HazelcastConfiguration.CUSTOMER_CALLS_BY_DOC_ID);
        for (CustomerCall customerCall : storedCustomerCalls) {
            customerCallsById.put(customerCall.getDocumentId(), customerCall);
        }

        mapReduceExample(customerCallsById);
    }

    private List<CustomerCall> prepareDB() throws URISyntaxException, IOException {
        customerCallRepository.deleteAll();

        this.getClass().getClassLoader();

        URI uri = this.getClass().getClassLoader().getResource("data/customer_calls.json").toURI();
        Path path = Paths.get(uri);
        String dataAsString = Files.lines(path).collect(Collectors.joining());


        List<CustomerCall> customerCalls = objectMapper.readValue(dataAsString, new TypeReference<List<CustomerCall>>() {
        });

        log.debug("total customer calls to store: {}", customerCalls.size());

        return customerCallRepository.saveAll(customerCalls);
    }

    private void mapReduceExample(IMap<Long, CustomerCall> customerCallsById) throws InterruptedException, java.util.concurrent.ExecutionException {

        System.out.println("~~~ MAP REDUCE EXAMPLE ~~~");
        JobTracker jobTracker = hazelcastInstance.getJobTracker(HazelcastConfiguration.DEFAULT_JOB_TRACKER);

        KeyValueSource<Long, CustomerCall> source = KeyValueSource.fromMap(customerCallsById);
        Job<Long, CustomerCall> job = jobTracker.newJob(source);

        Map<String, List<CustomerCallsByDate>> mapReduceResult = job
                .mapper(new Mapper<Long, CustomerCall, String, CustomerCall>() {

                    @Override
                    public void map(Long documentId, CustomerCall customerCall, Context<String, CustomerCall> context) {
                        String agent = customerCall.getAgent();
                        context.emit(agent, customerCall);
                    }

                })
                .reducer(new ReducerFactory<String, CustomerCall, List<CustomerCallsByDate>>() {

                    @Override
                    public Reducer<CustomerCall, List<CustomerCallsByDate>> newReducer(String agentName) {

                        return new Reducer<CustomerCall, List<CustomerCallsByDate>>() {

                            Map<String /*agent*/, Map<String/*justDate*/, List<CustomerCall>>> callsByAgentByDate = new LinkedHashMap<>();

                            @Override
                            public void reduce(CustomerCall customerCall) {
                                String agent = customerCall.getAgent();
                                String justDate = customerCall.getDate().split(" ")[0];

                                Map<String, List<CustomerCall>> agentAllCalls = callsByAgentByDate.get(agent);
                                if (agentAllCalls == null) {
                                    agentAllCalls = new LinkedHashMap<>();

                                    callsByAgentByDate.put(agent, agentAllCalls);
                                }

                                List<CustomerCall> agentCallsByDate = agentAllCalls.get(justDate);
                                if (agentCallsByDate == null) {
                                    agentCallsByDate = new LinkedList<>();

                                    agentAllCalls.put(justDate, agentCallsByDate);
                                }

                                agentCallsByDate.add(customerCall);
                            }

                            @Override
                            public List<CustomerCallsByDate> finalizeReduce() {

                                Map<String, List<CustomerCall>> agentCallsByDate = callsByAgentByDate.get(agentName);

                                return agentCallsByDate
                                        .entrySet()
                                        .stream()
                                        .map(r -> new CustomerCallsByDate(LocalDate.parse(r.getKey()), r.getValue()))
                                        .collect(Collectors.toList());
                            }
                        };

                    }
                })
                .submit()
                .get();

        System.out.println();
        mapReduceResult.forEach((agentName, callsByDates) -> {

            long totalRecords = callsByDates
                    .stream()
                    .map(r -> r.getCustomerCalls().size())
                    .reduce(0, Integer::sum);

            System.out.println(agentName + " --- " + callsByDates.size() + " --- " + totalRecords);
        });
        System.out.println();
    }

}
