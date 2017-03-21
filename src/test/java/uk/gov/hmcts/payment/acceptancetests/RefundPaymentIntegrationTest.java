package uk.gov.hmcts.payment.acceptancetests;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.payment.acceptancetests.dsl.PaymentTestDsl;
import uk.gov.hmcts.payment.api.contract.CreatePaymentRequestDto;
import uk.gov.hmcts.payment.api.contract.PaymentDto;
import uk.gov.hmcts.payment.api.contract.RefundPaymentRequestDto;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import static uk.gov.hmcts.payment.api.contract.CreatePaymentRequestDto.createPaymentRequestDtoWith;
import static uk.gov.hmcts.payment.api.contract.RefundPaymentRequestDto.refundPaymentRequestDtoWith;

/**
 * Created by Sivakumar Sirigiri on 20/03/2017.
 */
public class RefundPaymentIntegrationTest extends IntegrationTestBase {

    private CreatePaymentRequestDto.CreatePaymentRequestDtoBuilder validRequest = createPaymentRequestDtoWith()
            .amount(100)
            .description("Description")
            .email("Email@email.com")
            .reference("Reference")
            .returnUrl("https://return-url");

    private RefundPaymentRequestDto.RefundPaymentRequestDtoBuilder refundValidRequest = refundPaymentRequestDtoWith()
            .amount(5)
            .refundAmountAvailable(100);

    private RefundPaymentRequestDto.RefundPaymentRequestDtoBuilder refundAmountAvailalbeInvalidRequest = refundPaymentRequestDtoWith()
            .amount(5)
            .refundAmountAvailable(1000);

    private RefundPaymentRequestDto.RefundPaymentRequestDtoBuilder refundAmountInvalidRequest = refundPaymentRequestDtoWith()
            .amount(5000)
            .refundAmountAvailable(100);


    @Autowired
    private PaymentTestDsl scenario;

    @Test
    public void createAndRefundPayment() throws IOException {
        AtomicReference<PaymentDto> paymentHolder = new AtomicReference<>();
        scenario.given().userId("1").serviceId("divorce")
                .when()
                .createPayment("1", validRequest, paymentHolder)
                .refundPayment("1",refundValidRequest,paymentHolder.get().getId())
                .then().refundPayment();
    }

    @Test
    public void createAndRefundAvaialbleAmountInvalid() throws IOException {
        AtomicReference<PaymentDto> paymentHolder = new AtomicReference<>();
        scenario.given().userId("1").serviceId("divorce")
                .when()
                .createPayment("1", validRequest, paymentHolder)
                .refundPayment("1",refundAmountAvailalbeInvalidRequest,paymentHolder.get().getId())
                .then().refundAvailableAmountInvalid412();
    }

    @Test
    public void createAndRefundAmountInvalid() throws IOException {
        AtomicReference<PaymentDto> paymentHolder = new AtomicReference<>();
        scenario.given().userId("1").serviceId("divorce")
                .when()
                .createPayment("1", validRequest, paymentHolder)
                .refundPayment("1",refundAmountInvalidRequest,paymentHolder.get().getId())
                .then().validationErrorfor500("");
    }
}
