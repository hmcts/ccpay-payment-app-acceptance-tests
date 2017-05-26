package uk.gov.hmcts.payment.smoketests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.io.IOException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.payment.acceptancetests.IntegrationTestBase;
import uk.gov.hmcts.payment.api.contract.CreatePaymentRequestDto;

import static uk.gov.hmcts.payment.api.contract.CreatePaymentRequestDto.createPaymentRequestDtoWith;

public class CreatePaymentSmokeTest extends SmokeTestBase {

    private final static CreatePaymentRequestDto VALID_REQUEST = createPaymentRequestDtoWith()
            .amount(100)
            .description("Description")
            .reference("Reference")
            .returnUrl("https://return-url").build();

    @Autowired
    @Value("${base-urls.payment}")
    private String baseUri;

    @Autowired
    @Value("${smoke-test-headers.authorization}")
    private String authorization;

    @Autowired
    @Value("${smoke-test-headers.service-authorization}")
    private String serviceAuthorization;

    @Test
    public void validCreatePaymentRequestShouldResultIn201() throws IOException {
        RestAssured
                .given().baseUri(baseUri)
                .contentType(ContentType.JSON)
                .header("Authorization", authorization)
                .header("ServiceAuthorization", serviceAuthorization)
                .body(VALID_REQUEST)
                .post("/users/999999999/payments/")
                .then().statusCode(201);
    }
}
