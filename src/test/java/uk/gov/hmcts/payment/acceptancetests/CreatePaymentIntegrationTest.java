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
        scenario.given().userId("1").serviceId("divorce")
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

}
