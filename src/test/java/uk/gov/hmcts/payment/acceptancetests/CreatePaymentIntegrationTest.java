package uk.gov.hmcts.payment.acceptancetests;

import java.io.IOException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.payment.acceptancetests.dsl.PaymentTestDsl;
import uk.gov.hmcts.payment.api.contract.CreatePaymentRequestDto.CreatePaymentRequestDtoBuilder;
import uk.gov.hmcts.payment.api.contract.PaymentDto;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.payment.api.contract.CreatePaymentRequestDto.createPaymentRequestDtoWith;

public class CreatePaymentIntegrationTest extends IntegrationTestBase {

    @Autowired
    private PaymentTestDsl scenario;

    private CreatePaymentRequestDtoBuilder validRequest = createPaymentRequestDtoWith()
            .amount(100)
            .description("Description")
            .email("Email@email.com")
            .reference("Reference")
            .returnUrl("https://return-url");

    @Test
    public void validCreatePaymentRequestShouldResultIn201() throws IOException {
        scenario.given().userId("1").serviceId("reference")
                .when().createPayment("1", validRequest)
                .then().created((paymentDto -> {
                    assertThat(paymentDto.getAmount()).isEqualTo(100);
                    assertThat(paymentDto.getState()).isEqualTo(new PaymentDto.StateDto("created", false));
                    assertThat(paymentDto.getDescription()).isEqualTo("Description");
                    assertThat(paymentDto.getReference()).isEqualTo("Reference");
                    assertThat(paymentDto.getLinks().getCancel()).isNotNull();
                    assertThat(paymentDto.getLinks().getNextUrl()).isNotNull();
                })
        );
    }

    @Test
    public void paymentWithoutAmountShouldNotBeCreated() throws IOException {
        scenario.given().userId("1").serviceId("reference")
                .when().createPayment("1", validRequest.amount(null))
                .then().validationError("amount: may not be null");
    }

    @Test
    public void paymentWithoutDescriptionShouldNotBeCreated() throws IOException {
        scenario.given().userId("1").serviceId("reference")
                .when().createPayment("1", validRequest.description(null))
                .then().validationError("description: may not be empty");
    }

    @Test
    public void paymentWithoutEmailShouldNotBeCreated() throws IOException {
        scenario.given().userId("1").serviceId("reference")
                .when().createPayment("1", validRequest.email(null))
                .then().validationError("email: may not be empty");
    }

    @Test
    public void paymentWithoutReferenceShouldNotBeCreated() throws IOException {
        scenario.given().userId("1").serviceId("reference")
                .when().createPayment("1", validRequest.reference(null))
                .then().validationError("reference: may not be empty");
    }

    @Test
    public void paymentWithoutReturnUrlShouldNotBeCreated() throws IOException {
        scenario.given().userId("1").serviceId("reference")
                .when().createPayment("1", validRequest.returnUrl(null))
                .then().validationError("returnUrl: may not be empty");
    }
}
