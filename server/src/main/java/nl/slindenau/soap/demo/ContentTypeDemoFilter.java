package nl.slindenau.soap.demo;

import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ContentTypeDemoFilter implements Filter {

    private static final String ACCEPT_HEADER = "Accept";
    private static final String ORIGINAL_RESPONSE_CONTENT_TYPE = "text/xml";
    private static final String UPDATED_RESPONSE_CONTENT_TYPE = "application/soap+xml; charset=utf-8";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String accept = request.getHeader(ACCEPT_HEADER);
        if (accept.contains(ORIGINAL_RESPONSE_CONTENT_TYPE)) {
            ContentTypeResponseWrapper wrapper = new ContentTypeResponseWrapper(response);
            // todo: for this Demo we're changing the correct content type for SOAP1.1 (text/xml)
            //       to the incorrect value (application/soap+xml) which should only be used for SOAP1.2
            wrapper.overrideContentType(UPDATED_RESPONSE_CONTENT_TYPE);
            chain.doFilter(req, wrapper);
        } else {
            chain.doFilter(req, response);
        }
    }
}