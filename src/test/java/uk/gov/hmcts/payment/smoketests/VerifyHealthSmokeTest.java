package uk.gov.hmcts.payment.smoketests;

import io.restassured.RestAssured;
import java.io.IOException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class VerifyHealthSmokeTest extends SmokeTestBase {

    @Autowired
    @Value("${base-urls.payment}")
    private String baseUri;

    @Test
    public void validCreatePaymentRequestShouldResultIn201() throws IOException {
        RestAssured
                .given().baseUri(baseUri)
                .get("/health")
                .then().statusCode(200);
    }
}
