
package edu.itc.se.shop;

import java.util.Arrays;

import edu.itc.se.shop.report.ReportBuilder;
import edu.itc.se.shop.service.Notifier;
import edu.itc.se.shop.service.WelcomeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ShopApplication {

    private static final Logger log =
            LoggerFactory.getLogger(ShopApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(
            ApplicationContext ctx,
            WelcomeService welcomeService,
            ObjectProvider<ReportBuilder> reportProvider) {

        return args -> {

            // Display bean information
            String[] notifierNames =
                    ctx.getBeanNamesForType(Notifier.class);

            log.info("{} beans; notifiers = {}",
                    ctx.getBeanDefinitionCount(),
                    Arrays.toString(notifierNames));

            // Send welcome messages
            welcomeService.welcome("ann", false);
            welcomeService.welcome("bob", true);

            // Test prototype scope
            ReportBuilder report1 = reportProvider.getObject();
            ReportBuilder report2 = reportProvider.getObject();

            log.info("prototype instances differ: {}",
                    report1 != report2);
        };
    }
}
