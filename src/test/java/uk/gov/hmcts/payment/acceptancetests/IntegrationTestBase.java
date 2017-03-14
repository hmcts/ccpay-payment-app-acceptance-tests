package uk.gov.hmcts.payment.acceptancetests;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.payment.acceptancetests.dsl.PaymentTestDsl;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class IntegrationTestBase {
    @Autowired
    private ApplicationContext context;

    PaymentTestDsl newScenario() {
        return (PaymentTestDsl) context.getBean("paymentTestDsl");
    }

    @Configuration
    @ComponentScan("uk.gov.hmcts.payment.acceptancetests")
    @PropertySource("classpath:application.properties")
    public static class IntegrationTestConfiguration {
    }
}
