package edu.itc.se.shop.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ReportBuilder {
    public String buildReport() {
        return "Report: " + System.identityHashCode(this);
    }
}
