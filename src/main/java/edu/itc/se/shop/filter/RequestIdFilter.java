package edu.itc.se.shop.filter;

import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import edu.itc.se.shop.audit.CurrentRequest;

@Component
public class RequestIdFilter extends OncePerRequestFilter {
    private final ObjectProvider<CurrentRequest> currentRequestProvider;

    public RequestIdFilter(ObjectProvider<CurrentRequest> currentRequestProvider) {
        this.currentRequestProvider = currentRequestProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String reqId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", reqId);
        CurrentRequest currentRequest = currentRequestProvider.getIfAvailable();
        if (currentRequest != null) {
            currentRequest.setRequestId(reqId);
        }
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove("requestId");
        }
    }
}
