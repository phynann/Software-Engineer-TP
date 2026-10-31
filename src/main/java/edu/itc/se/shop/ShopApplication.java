package edu.itc.se.shop;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import edu.itc.se.shop.service.Notifier;
import edu.itc.se.shop.service.ReportBuilder;
import edu.itc.se.shop.service.WelcomeService;

@SpringBootApplication
public class ShopApplication {
    private static final Logger log = LoggerFactory.getLogger(ShopApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(ApplicationContext ctx,
                                     ObjectProvider<ReportBuilder> reportBuilderProvider,
                                     ObjectProvider<WelcomeService> welcomeServiceProvider) {
        return args -> {
            welcomeServiceProvider.ifAvailable(welcomeService -> {
                String[] notifiers = ctx.getBeanNamesForType(Notifier.class);
                log.info("{} beans; notifiers = {}", ctx.getBeanDefinitionCount(), Arrays.toString(notifiers));
                welcomeService.welcome("ann", false);
                welcomeService.welcome("bob", true);
                ReportBuilder r1 = reportBuilderProvider.getObject();
                ReportBuilder r2 = reportBuilderProvider.getObject();
                log.info("prototype instances differ: {}", System.identityHashCode(r1) != System.identityHashCode(r2));
            });
        };
    }
}
