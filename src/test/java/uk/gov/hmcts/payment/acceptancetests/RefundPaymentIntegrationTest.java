package uk.gov.hmcts.payment.acceptancetests;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.payment.acceptancetests.dsl.PaymentTestDsl;
import uk.gov.hmcts.payment.api.contract.CreatePaymentRequestDto;
import uk.gov.hmcts.payment.api.contract.PaymentDto;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.payment.api.contract.CreatePaymentRequestDto.createPaymentRequestDtoWith;

/**
 * Created by kknuthalapati on 15/03/2017.
 */
public class GetPaymentIntegrationTest extends IntegrationTestBase {

    private CreatePaymentRequestDto.CreatePaymentRequestDtoBuilder validRequest = createPaymentRequestDtoWith()
            .amount(100)
            .description("Description")
            .email("Email@email.com")
            .reference("Reference")
            .returnUrl("https://return-url");

    @Autowired
    private PaymentTestDsl scenario;

    @Test
    public void validGetPaymentRequestShouldResultIn200() throws IOException {
        AtomicReference<PaymentDto> paymentHolder = new AtomicReference<>();

        scenario.given().userId("1").serviceId("divorce")
                .when()
                .createPayment("1", validRequest, paymentHolder)
                .getPayment("1", paymentHolder.get().getId())
                .then().get((paymentDto -> {
                    assertThat(paymentDto.getAmount()).isEqualTo(100);
                    assertThat(paymentDto.getState()).isEqualTo(new PaymentDto.StateDto("created", false));
                    assertThat(paymentDto.getDescription()).isEqualTo("Description");
                    assertThat(paymentDto.getReference()).isEqualTo("Reference");
                    assertThat(paymentDto.getLinks().getCancel().getHref()).endsWith(paymentHolder.get().getId() + "/cancel");
                    assertThat(paymentDto.getLinks().getNextUrl()).isNotNull();
                })
        );
    }

    @Test
    public void createAndCancelApproachC() throws IOException {
        AtomicReference<PaymentDto> paymentHolder = new AtomicReference<>();

        scenario.given().userId("1").serviceId("divorce")
                .when()
                .createPayment("1", validRequest, paymentHolder)
                .cancelPayment("1", paymentHolder.get().getId())
                .then().cancelled();
    }


    @Test
    public void getPaymentWithOutServiceIdRequestShouldResultIn500() throws IOException {
        scenario.given().userId("1").serviceId("divorce")
                .when().getPayment("1", "")
                .then().validationErrorfor500("");
    }

    @Test
    public void getPaymentWithOutUserIdRequestShouldResultIn404() throws IOException {
        scenario.given().userId("1").serviceId("divorce")
                .when().getPayment("", "9")
                .then().notFound();
    }

    @Test
    public void getPaymentWithOutUserIdAndPaymentIdRequestShouldResultIn400() throws IOException {
        scenario.given().userId("1").serviceId("divorce")
                .when().getPayment("", "")
                .then().notFound();
    }

}
