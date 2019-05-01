package com.chriniko.customer_call.quality_service.it;


import com.chriniko.customer_call.quality_service.Application;
import com.chriniko.customer_call.quality_service.core.FileSupport;
import com.chriniko.customer_call.quality_service.domain.CustomerCall;
import com.chriniko.customer_call.quality_service.dto.AgentScoreSearchInfo;
import com.chriniko.customer_call.quality_service.repository.CustomerCallRepository;
import com.chriniko.customer_call.quality_service.resource.CustomerCallQualityResource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joor.Reflect;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = Application.class,
        properties = {"application.properties"}
)

@RunWith(SpringRunner.class)
public class SpecificationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerCallRepository customerCallRepository;

    @Autowired
    private CustomerCallQualityResource customerCallQualityResource;

    private static RestTemplate restTemplate;

    @BeforeClass
    public static void setUp() {
        restTemplate = new RestTemplate();
    }

    @Test
    public void agent_score_calculation_works_as_expected_heap_service_case() throws Exception {

        // given
        Reflect.on(customerCallQualityResource).set("calculationStrategy", "quality-heap-service");

        prepareDatabase("Natasha");

        restTemplate.put("http://localhost:" + port + "/api/internal-operations/reindex-heap-service", null);

        String firstCaseInput = FileSupport.getResource("test/first_case_input.json");
        AgentScoreSearchInfo agentScoreSearchInfo = objectMapper.readValue(firstCaseInput, AgentScoreSearchInfo.class);


        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        HttpEntity<AgentScoreSearchInfo> httpEntity = new HttpEntity<>(agentScoreSearchInfo, httpHeaders);


        // when
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/api/search-agent-score",
                HttpMethod.POST,
                httpEntity,
                String.class);


        // then
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertNotNull(responseEntity.getBody());

        String expected = FileSupport.getResource("test/first_case_output.json");

        JSONAssert.assertEquals(
                expected,
                responseEntity.getBody(),
                new CustomComparator(JSONCompareMode.STRICT,
                        new Customization("totalTimeInMS", (o1, o2) -> true),
                        new Customization("calculationStrategy", (o1, o2) -> true)
                )
        );


        // cleanup
        customerCallRepository.deleteAll();

    }

    @Test
    public void agent_score_calculation_works_as_expected_db_service_case() throws Exception {
        // given
        Reflect.on(customerCallQualityResource).set("calculationStrategy", "quality-db-service");

        prepareDatabase("Natasha");

        String firstCaseInput = FileSupport.getResource("test/first_case_input.json");
        AgentScoreSearchInfo agentScoreSearchInfo = objectMapper.readValue(firstCaseInput, AgentScoreSearchInfo.class);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        HttpEntity<AgentScoreSearchInfo> httpEntity = new HttpEntity<>(agentScoreSearchInfo, httpHeaders);


        // when
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/api/search-agent-score",
                HttpMethod.POST,
                httpEntity,
                String.class);


        // then
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertNotNull(responseEntity.getBody());

        String expected = FileSupport.getResource("test/first_case_output.json");

        JSONAssert.assertEquals(
                expected,
                responseEntity.getBody(),
                new CustomComparator(JSONCompareMode.STRICT,
                        new Customization("totalTimeInMS", (o1, o2) -> true),
                        new Customization("calculationStrategy", (o1, o2) -> true)
                )
        );


        // cleanup
        customerCallRepository.deleteAll();
    }

    @Test
    public void agent_name_not_exists_case() throws Exception {

        // given
        Reflect.on(customerCallQualityResource).set("calculationStrategy", "quality-db-service");

        prepareDatabase("Natasha");

        String firstCaseInput = FileSupport.getResource("test/second_case_input.json");
        AgentScoreSearchInfo agentScoreSearchInfo = objectMapper.readValue(firstCaseInput, AgentScoreSearchInfo.class);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        HttpEntity<AgentScoreSearchInfo> httpEntity = new HttpEntity<>(agentScoreSearchInfo, httpHeaders);


        // when
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/api/search-agent-score",
                HttpMethod.POST,
                httpEntity,
                String.class);


        // then
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertNotNull(responseEntity.getBody());

        String expected = FileSupport.getResource("test/second_case_output.json");

        JSONAssert.assertEquals(
                expected,
                responseEntity.getBody(),
                new CustomComparator(JSONCompareMode.STRICT,
                        new Customization("totalTimeInMS", (o1, o2) -> true),
                        new Customization("calculationStrategy", (o1, o2) -> true)
                )
        );


        // cleanup
        customerCallRepository.deleteAll();
    }

    @Test
    public void malformed_date_case() throws Exception {
        // given
        Reflect.on(customerCallQualityResource).set("calculationStrategy", "quality-db-service");

        String firstCaseInput = FileSupport.getResource("test/third_case_input.json");
        AgentScoreSearchInfo agentScoreSearchInfo = objectMapper.readValue(firstCaseInput, AgentScoreSearchInfo.class);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        HttpEntity<AgentScoreSearchInfo> httpEntity = new HttpEntity<>(agentScoreSearchInfo, httpHeaders);


        // when
        try {
            restTemplate.exchange(
                    "http://localhost:" + port + "/api/search-agent-score",
                    HttpMethod.POST,
                    httpEntity,
                    String.class);

            Assert.fail();

        } catch (HttpClientErrorException.BadRequest e) {

            // then
            Assert.assertEquals("400 Bad Request", e.getMessage());

            String responseBodyAsString = e.getResponseBodyAsString();

            String expected = FileSupport.getResource("test/third_case_output.json");

            JSONAssert.assertEquals(
                    expected,
                    responseBodyAsString,
                    new CustomComparator(JSONCompareMode.STRICT,
                            new Customization("timestamp", (o1, o2) -> true)
                    )
            );
        }
    }

    @Test
    public void start_date_bigger_than_end_date_case() throws Exception {

        // given
        Reflect.on(customerCallQualityResource).set("calculationStrategy", "quality-db-service");


        String firstCaseInput = FileSupport.getResource("test/fourth_case_input.json");
        AgentScoreSearchInfo agentScoreSearchInfo = objectMapper.readValue(firstCaseInput, AgentScoreSearchInfo.class);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        HttpEntity<AgentScoreSearchInfo> httpEntity = new HttpEntity<>(agentScoreSearchInfo, httpHeaders);


        // when
        try {
            restTemplate.exchange(
                    "http://localhost:" + port + "/api/search-agent-score",
                    HttpMethod.POST,
                    httpEntity,
                    String.class);

            Assert.fail();

        } catch (HttpClientErrorException.BadRequest e) {

            // then
            Assert.assertEquals("400 Bad Request", e.getMessage());

            String responseBodyAsString = e.getResponseBodyAsString();

            String expected = FileSupport.getResource("test/fourth_case_output.json");

            JSONAssert.assertEquals(
                    expected,
                    responseBodyAsString,
                    new CustomComparator(JSONCompareMode.STRICT,
                            new Customization("timestamp", (o1, o2) -> true)
                    )
            );
        }

    }

    @Test
    public void start_provided_date_not_logical_case() throws Exception {

        // given
        Reflect.on(customerCallQualityResource).set("calculationStrategy", "quality-db-service");


        String firstCaseInput = FileSupport.getResource("test/fifth_case_input.json");
        AgentScoreSearchInfo agentScoreSearchInfo = objectMapper.readValue(firstCaseInput, AgentScoreSearchInfo.class);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        HttpEntity<AgentScoreSearchInfo> httpEntity = new HttpEntity<>(agentScoreSearchInfo, httpHeaders);


        // when
        try {
            restTemplate.exchange(
                    "http://localhost:" + port + "/api/search-agent-score",
                    HttpMethod.POST,
                    httpEntity,
                    String.class);

            Assert.fail();

        } catch (HttpClientErrorException.BadRequest e) {

            // then
            Assert.assertEquals("400 Bad Request", e.getMessage());

            String responseBodyAsString = e.getResponseBodyAsString();

            String expected = FileSupport.getResource("test/fifth_case_output.json");

            JSONAssert.assertEquals(
                    expected,
                    responseBodyAsString,
                    new CustomComparator(JSONCompareMode.STRICT,
                            new Customization("timestamp", (o1, o2) -> true)
                    )
            );
        }


    }

    private void prepareDatabase(String agentName) throws Exception {

        customerCallRepository.deleteAll();

        String customerCallsAsString = FileSupport.getResource("mock_data/customer_calls.json");
        List<CustomerCall> customerCalls = objectMapper.readValue(customerCallsAsString, new TypeReference<List<CustomerCall>>() {
        });

        if (agentName != null) {
            List<CustomerCall> natashaCustomerCalls = customerCalls.stream().filter(r -> r.getAgent().equals(agentName)).collect(Collectors.toList());
            customerCallRepository.insert(natashaCustomerCalls);
        } else {
            customerCallRepository.insert(customerCalls);
        }

    }
}
