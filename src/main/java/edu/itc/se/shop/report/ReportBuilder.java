package edu.itc.se.shop.report;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ReportBuilder {

    public ReportBuilder() {
        System.out.println(
                "ReportBuilder created: "
                + System.identityHashCode(this)
        );
    }

    public void build() {
        System.out.println("Building report...");
    }
}
