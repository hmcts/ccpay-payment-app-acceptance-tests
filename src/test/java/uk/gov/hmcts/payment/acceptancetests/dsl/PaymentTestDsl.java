package uk.gov.hmcts.payment.acceptancetests.dsl;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.payment.acceptancetests.tokens.ServiceTokenFactory;
import uk.gov.hmcts.payment.api.contract.CreatePaymentRequestDto.CreatePaymentRequestDtoBuilder;
import uk.gov.hmcts.payment.api.contract.PaymentDto;

@Component
@Scope("prototype")
public class PaymentTestDsl {
    private final ServiceTokenFactory serviceTokenFactory;
    private final RequestSpecification requestSpecification;
    private Response response;

    @Autowired
    public PaymentTestDsl(@Value("${base-urls.payment}") String baseUri, ServiceTokenFactory serviceTokenFactory) {
        this.serviceTokenFactory = serviceTokenFactory;
        this.requestSpecification = RestAssured.given().baseUri(baseUri).contentType(ContentType.JSON);
    }

    public PaymentGivenDsl given() {
        return new PaymentGivenDsl();
    }


    public class PaymentGivenDsl {
        public PaymentGivenDsl userId(String id) {
            requestSpecification.header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwic3ViIjoiMSIsImlhdCI6MTQ4OTA1MTg1NSwicm9sZXMiOiJjaXRpemVuIiwidHlwZSI6IkFDQ0VTUyIsImlkIjoiMSIsImZvcmVuYW1lIjoiTmV3Iiwic3VybmFtZSI6IlVzZXIiLCJlbWFpbCI6Im5ldy51c2VyQHRlc3QubmV0IiwibG9hIjoxfQ.ylr8Pwl7yxQs_J5ZINcVzZ9DfTZ9QL598NlTGYyzUHA");
            return this;
        }

        public PaymentGivenDsl serviceId(String id) {
            requestSpecification.header("ServiceAuthorization", serviceTokenFactory.validTokenForService(id));
            return this;
        }

        public PaymentWhenDsl when() {
            return new PaymentWhenDsl();
        }
    }

    public class PaymentWhenDsl {
        public PaymentWhenDsl getPayment(String userId, String paymentId) {
            response = requestSpecification.get("/users/" + userId + "/payments/" + paymentId);
            return this;
        }

        public PaymentWhenDsl createPayment(String userId, CreatePaymentRequestDtoBuilder requestDto) {
            response = requestSpecification.body(requestDto.build()).post("/users/" + userId + "/payments/");
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

        public PaymentThenDsl validationError(String message) {
            String validationError = response.then().statusCode(422).extract().body().asString();
            Assertions.assertThat(validationError).isEqualTo(message);
            return this;
        }

    }
}
