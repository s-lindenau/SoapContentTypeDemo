package nl.slindenau.soap.demo;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ContentTypeResponseWrapper extends HttpServletResponseWrapper {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    public ContentTypeResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    public void overrideContentType(String type) {
        super.setContentType(type);
        System.out.println("[SOAP CONTENT TYPE DEMO] Changed response content type to: " + type);
    }

    public void setContentType(String type) {
        // no-op; don't allow any further changes to the content type
        logOriginalContentType(type);
    }

    public void setHeader(String name, String value) {
        if (!name.equalsIgnoreCase(CONTENT_TYPE_HEADER)) {
            super.setHeader(name, value);
        } else {
            logOriginalContentType(value);
        }
    }

    public void addHeader(String name, String value) {
        if (!name.equalsIgnoreCase(CONTENT_TYPE_HEADER)) {
            super.addHeader(name, value);
        } else {
            logOriginalContentType(value);
        }
    }

    private void logOriginalContentType(String value) {
        System.out.println("[SOAP CONTENT TYPE DEMO] Original response content type was: " + value);
    }

    public String getContentType() {
        return super.getContentType();
    }
}
