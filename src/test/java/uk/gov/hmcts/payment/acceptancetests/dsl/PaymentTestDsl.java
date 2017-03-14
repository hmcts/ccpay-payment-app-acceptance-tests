package uk.gov.hmcts.payment.acceptancetests.dsl;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.payment.acceptancetests.tokens.ServiceTokenFactory;

@Component
@Scope("prototype")
public class PaymentTestDsl {
    private final ServiceTokenFactory serviceTokenFactory;
    private final RequestSpecification requestSpecification;

    @Autowired
    public PaymentTestDsl(@Value("${base-urls.payment}") String baseUri, ServiceTokenFactory serviceTokenFactory) {
        this.serviceTokenFactory = serviceTokenFactory;
        this.requestSpecification = RestAssured.given().baseUri(baseUri);
    }

    public PaymentTestDsl given() {
        return this;
    }

    public PaymentTestDsl userId(String id) {
        requestSpecification.header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwic3ViIjoiMSIsImlhdCI6MTQ4OTA1MTg1NSwicm9sZXMiOiJjaXRpemVuIiwidHlwZSI6IkFDQ0VTUyIsImlkIjoiMSIsImZvcmVuYW1lIjoiTmV3Iiwic3VybmFtZSI6IlVzZXIiLCJlbWFpbCI6Im5ldy51c2VyQHRlc3QubmV0IiwibG9hIjoxfQ.ylr8Pwl7yxQs_J5ZINcVzZ9DfTZ9QL598NlTGYyzUHA");
        return this;
    }

    public PaymentTestDsl serviceId(String id) {
        requestSpecification.header("ServiceAuthorization", serviceTokenFactory.validTokenForService(id));
        return this;
    }

    public RequestSpecification when() {
        return requestSpecification.when();
    }
}
