package uk.gov.hmcts.payment.acceptancetests;

import java.io.IOException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.payment.acceptancetests.dsl.PaymentTestDsl;

public class SecurityIntegrationTest extends IntegrationTestBase {

    @Autowired
    private PaymentTestDsl scenario;

    @Test
    public void noUserAndServiceTokenShouldResultIn403() throws IOException {
        scenario.given()
                .when().get("/users/1/payments/1")
                .then().statusCode(403);
    }

    @Test
    public void noUserTokenShouldResultIn403() throws IOException {
        scenario.given().serviceId("divorce")
                .when().get("/users/1/payments/1")
                .then().statusCode(403);
    }

    @Test
    public void noServiceTokenShouldResultIn403() throws IOException {
        scenario.given().userId("1")
                .when().get("/users/1/payments/1")
                .then().statusCode(403);
    }

    @Test
    public void validUserAndServiceTokenShouldResultIn200() throws IOException {
        scenario.given().serviceId("divorce").userId("1")
                .when().get("/users/1/payments/1")
                .then().statusCode(200);
    }

    @Test
    public void callFromUnknownServiceShouldResultIn403() throws IOException {
        scenario.given().serviceId("unknown-service").userId("1")
                .when().get("/users/1/payments/1")
                .then().statusCode(403);
    }

    @Test
    public void callToOtherUsersResourceShouldResultIn403() throws IOException {
        scenario.given().serviceId("unknown-service").userId("1")
                .when().get("/users/2/payments/1")
                .then().statusCode(403);
    }
}
