package uk.gov.hmcts.payment.acceptancetests;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.payment.acceptancetests.dsl.PaymentTestDsl;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class IntegrationTestBase {
    @Autowired
    private ApplicationContext context;

    @Value("${base-urls.gov-pay-stub}")
    private String govPayStubUrl;

    private static boolean stubsLoaded = false;

    @Before
    public void loadGovPayStubs() throws FileNotFoundException, MalformedURLException {
        if (!stubsLoaded) {
            URL url = new URL(govPayStubUrl);
            WireMock wireMock = new WireMock(url.getHost(), url.getPort());
            wireMock.resetMappings();
            wireMock.loadMappingsFrom(ResourceUtils.getFile("classpath:gov-pay-stub"));
        }
    }

    PaymentTestDsl newScenario() {
        return (PaymentTestDsl) context.getBean("paymentTestDsl");
    }

    @Configuration
    @Profile("tunnel")
    @ComponentScan("uk.gov.hmcts.payment.acceptancetests")
    @PropertySource("classpath:application-tunnel.properties")
    public static class IntegrationTestTunnelConfiguration {
    }

    @Configuration
    @ComponentScan("uk.gov.hmcts.payment.acceptancetests")
    @PropertySource("classpath:application.properties")
    public static class IntegrationTestConfiguration {
    }
}
