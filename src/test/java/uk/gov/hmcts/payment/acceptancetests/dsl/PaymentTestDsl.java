package uk.gov.hmcts.payment.acceptancetests.dsl;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.payment.acceptancetests.tokens.ServiceTokenFactory;
import uk.gov.hmcts.payment.acceptancetests.tokens.UserTokenFactory;
import uk.gov.hmcts.payment.api.contract.CreatePaymentRequestDto.CreatePaymentRequestDtoBuilder;
import uk.gov.hmcts.payment.api.contract.RefundPaymentRequestDto.RefundPaymentRequestDtoBuilder;
import uk.gov.hmcts.payment.api.contract.PaymentDto;
import uk.gov.hmcts.payment.api.contract.RefundPaymentRequestDto;

@Component
@Scope("prototype")
public class PaymentTestDsl {
    private final Map<String, String> headers = new HashMap<>();
    private final String baseUri;
    private final ServiceTokenFactory serviceTokenFactory;
    private final UserTokenFactory userTokenFactory;
    private Response response;

    @Autowired
    public PaymentTestDsl(@Value("${base-urls.payment}") String baseUri, ServiceTokenFactory serviceTokenFactory, UserTokenFactory userTokenFactory) {
        this.baseUri = baseUri;
        this.serviceTokenFactory = serviceTokenFactory;
        this.userTokenFactory = userTokenFactory;
    }

    public PaymentGivenDsl given() {
        return new PaymentGivenDsl();
    }

    public class PaymentGivenDsl {
        public PaymentGivenDsl userId(String id) {
            headers.put("Authorization", userTokenFactory.validTokenForUser(id));
            return this;
        }

        public PaymentGivenDsl serviceId(String id) {
            headers.put("ServiceAuthorization", serviceTokenFactory.validTokenForService(id));
            return this;
        }

        public PaymentWhenDsl when() {
            return new PaymentWhenDsl();
        }
    }

    public class PaymentWhenDsl {
        private RequestSpecification newRequest() {
            return RestAssured.given().baseUri(baseUri).contentType(ContentType.JSON).headers(headers);
        }

        public PaymentWhenDsl getPayment(String userId, String paymentId) {
            response = newRequest().get("/users/{userId}/payments/{paymentId}", userId, paymentId);
            return this;
        }

        public PaymentWhenDsl createPayment(String userId, CreatePaymentRequestDtoBuilder requestDto, AtomicReference<PaymentDto> paymentHolder) {
            createPayment(userId, requestDto);
            paymentHolder.set(response.then().statusCode(201).extract().as(PaymentDto.class));
            return this;
        }

        public PaymentWhenDsl createPayment(String userId, CreatePaymentRequestDtoBuilder requestDto) {
            response = newRequest().body(requestDto.build()).post("/users/{userId}/payments/", userId);
            return this;
        }

        public PaymentWhenDsl cancelPayment(String userId, String paymentId) {
            response = newRequest().post("/users/{userId}/payments/{paymentId}/cancel", userId, paymentId);
            return this;
        }

        public PaymentWhenDsl refundPayment(String userId, RefundPaymentRequestDtoBuilder requestDto,String paymentId) {
            response = newRequest().body(requestDto.build()).post("/users/{userId}/payments/{paymentId}/refunds", userId, paymentId);
            return this;
        }

        public PaymentThenDsl then() {
            return new PaymentThenDsl();
        }
    }

    public class PaymentThenDsl {
        public PaymentThenDsl forbidden() {
            response.then().statusCode(403);
            return this;
        }

        public PaymentThenDsl notFound() {
            response.then().statusCode(404);
            return this;
        }

        public PaymentThenDsl created(Consumer<PaymentDto> paymentAssertions) {
            PaymentDto paymentDto = response.then().statusCode(201).extract().as(PaymentDto.class);
            paymentAssertions.accept(paymentDto);
            return this;
        }

        public PaymentThenDsl cancelled() {
            response.then().statusCode(204);
            return this;
        }

        public PaymentThenDsl get(Consumer<PaymentDto> paymentAssertions) {
            PaymentDto paymentDto = response.then().statusCode(200).extract().as(PaymentDto.class);
            paymentAssertions.accept(paymentDto);
            return this;
        }

        public PaymentDto get() {
            return response.then().statusCode(200).extract().as(PaymentDto.class);
        }

        public PaymentThenDsl validationError(String message) {
            String validationError = response.then().statusCode(422).extract().body().asString();
            Assertions.assertThat(validationError).isEqualTo(message);
            return this;
        }

        public PaymentThenDsl validationErrorfor500(String message) {
            String validationError = response.then().statusCode(500).extract().body().asString();
            Assertions.assertThat(validationError).isEqualTo(message);
            return this;
        }

        public PaymentThenDsl refundPayment() {
            response.then().statusCode(201);
            return this;
        }

        public PaymentThenDsl refundAvailableAmountInvalid412() {
            response.then().statusCode(412);
            return this;
        }


    }
}
