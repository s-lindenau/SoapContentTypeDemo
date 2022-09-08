package nl.slindenau.soap.demo;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class ContentTypeInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final String CONTENT_TYPE_TEXT_XML = "text/xml";

    public ContentTypeInterceptor() {
        super(Phase.RECEIVE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        // this is just for logging, may be removed
        Object originalContentType = message.get(Message.CONTENT_TYPE);
        System.out.println("------------------------");
        System.out.println("[SOAP CONTENT TYPE DEMO] Response content type: " + originalContentType);

        message.put(Message.CONTENT_TYPE, CONTENT_TYPE_TEXT_XML);

        // this is just for logging, may be removed
        System.out.println("[SOAP CONTENT TYPE DEMO] Updated to content type: " + CONTENT_TYPE_TEXT_XML);
        System.out.println("------------------------");
        System.out.println();
    }
}
