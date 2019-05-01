## Customer Call Quality Service

###### Assignee: Nikolaos Christidis (nick.christidis@yahoo.com)

### Description
* See [assignment_description](assignment.pdf)


### Build
* Execute: `mvn clean install -DskipUTs=true -DskipITs=true`


### Run
* (1st option, Build step is necessary) Execute: `java -jar -Dspring.profiles.active=dev -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector target/customer-call-quality-service-1.0-SNAPSHOT.jar`

* (2nd option, Build step is not necessary) Execute: `mvn spring-boot:run -Dspring.profiles.active=dev -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector`

### Configurations

* You can choose between the following quality calculation strategies:
    * Database(MongoDB) access with the following setting: `quality-service.calculation-strategy=quality-db-service`
    
    * IMDG (In-Memory Data Grid, Hazelcast) access with the following setting: `quality-service.calculation-strategy=quality-heap-service`

* You can choose to make the agent score calculator concurrently or not via property: `agent-score-calculator.concurrent-approach=true`

* You can choose if you want to cache the results or not, of the `quality-db-service` via the property: `quality-db-service.cache-enabled`


### Integration tests (you need to run docker-compose up first)
* Execute: `docker-compose up`

* Execute: `mvn integration-test -DskipUTs=true`

* (Optional) execute: `docker-compose down`


### Unit tests
* Execute: `mvn clean test`


### Test coverage reports (unit & integration)
* Execute: `mvn clean verify`

* Unit Test coverage reports, go to: `target/site/jacoco-ut/index.html`

* Integration Test coverage reports, go to: `target/site/jacoco-it/index.html`


### Code Quality via SonarQube (you should run docker-compose up first)
* Uncomment commented sections in `docker-compose.yml`

* Execute: `docker-compose up`

* First login to web portal: `http://localhost:9000/sessions/new`
    * Credentials
        * Username: `admin`
        * Password: `admin`
        
* Then execute: `mvn sonar:sonar` and browse to: `http://localhost:9000/projects` and select the correct one
  in order to see more information for code quality.
  
  
* (Optional) execute: `docker-compose down`

##############################################
### Example Request

* Http Post at: `localhost:8181/api/search-agent-score` with payload:

```json

{
	"agent":"Natasha",
	"start_date":"2017-04-01",
	"end_date":"2017-04-10"
}

```


### Example Response

* With `quality-service.fetch-strategy=quality-db-service`:

```json

{
    "calculationStrategy": "quality-db-service",
    "totalTimeInMS": 439,
    "results": [
        {
            "date": "2017-04-01",
            "agent_score": "-5.21"
        },
        {
            "date": "2017-04-02",
            "agent_score": "-7.2"
        },
        {
            "date": "2017-04-03",
            "agent_score": "-4.05"
        },
        {
            "date": "2017-04-04",
            "agent_score": "-4.5"
        },
        {
            "date": "2017-04-05",
            "agent_score": "-5.04"
        },
        {
            "date": "2017-04-06",
            "agent_score": "-5.36"
        },
        {
            "date": "2017-04-07",
            "agent_score": "-6.79"
        },
        {
            "date": "2017-04-08",
            "agent_score": "-3.37"
        },
        {
            "date": "2017-04-09",
            "agent_score": "-4.34"
        },
        {
            "date": "2017-04-10",
            "agent_score": "-5.89"
        }
    ]
}

```



* With `quality-service.fetch-strategy=quality-heap-service`:

```json

{
    "calculationStrategy": "quality-heap-service",
    "totalTimeInMS": 13,
    "results": [
        {
            "date": "2017-04-01",
            "agent_score": "-5.21"
        },
        {
            "date": "2017-04-02",
            "agent_score": "-7.2"
        },
        {
            "date": "2017-04-03",
            "agent_score": "-4.05"
        },
        {
            "date": "2017-04-04",
            "agent_score": "-4.5"
        },
        {
            "date": "2017-04-05",
            "agent_score": "-5.04"
        },
        {
            "date": "2017-04-06",
            "agent_score": "-5.36"
        },
        {
            "date": "2017-04-07",
            "agent_score": "-6.79"
        },
        {
            "date": "2017-04-08",
            "agent_score": "-3.37"
        },
        {
            "date": "2017-04-09",
            "agent_score": "-4.34"
        },
        {
            "date": "2017-04-10",
            "agent_score": "-5.89"
        }
    ]
}


```
##############################################
### Example Request

* Http Post at: `localhost:8181/api/search-agent-score` with payload:

```json

{
	"agent":"Natasha",
	"start_date":"2017-04-67",
	"end_date":"2017-04-10"
}
```

### Example Response

```json
{
    "timestamp": "2019-05-01 02:31:09",
    "status": 400,
    "error": "not valid date provided, message: Text '2017-04-67' could not be parsed: Invalid value for DayOfMonth (valid values 1 - 28/31): 67"
}

```

##############################################
### Example Request

* Http Post at: `localhost:8181/api/search-agent-score` with payload:

```json
{
	"agent":"Natasha",
	"start_date":"2017-04-22",
	"end_date":"2017-04-10"
}

```

### Example Response

```json
{
    "timestamp": "2019-04-30 11:30:22",
    "status": 400,
    "error": "provided startDate is after provided endDate"
}
```

##############################################
### Example Request

* Http Post at: `localhost:8181/api/search-agent-score` with payload:

```json
{
	"agent":"",
	"start_date":"2017-04-22",
	"end_date":"2017-04-10"
}

```

### Example Response

```json
{
    "timestamp": "2019-04-30 11:57:54",
    "status": 400,
    "error": " [Field error in object 'agentScoreSearchInfo' on field 'agent',  default message [must not be blank]] "
}
```


##############################################
### Example Request

* Http Post at: `localhost:8181/api/search-agent-score` with payload:

```json
{
	"agent":"Natasha",
	"start_date":"erw-04-67",
	"end_date":"2017-04-10"
}

```

### Example Response

```json
{
    "timestamp": "2019-04-30 11:58:13",
    "status": 400,
    "error": " [Field error in object 'agentScoreSearchInfo' on field 'startDate',  default message [must match \"\\d{4}-\\d{1,2}-\\d{1,2}\"]] "
}
```