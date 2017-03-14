package uk.gov.hmcts.payment.acceptancetests.tokens;


import org.springframework.boot.test.context.TestComponent;

import static io.restassured.RestAssured.post;

@TestComponent
public class ServiceTokenFactory {

    public String validTokenForService(String microservice) {
        return post("http://localhost:8086/testing-support/lease?microservice={microservice}", microservice).body().asString();
    }
}
