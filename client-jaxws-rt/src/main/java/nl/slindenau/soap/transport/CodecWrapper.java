package nl.slindenau.soap.transport;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class CodecWrapper implements Codec {

    // todo: disable to showcase the UnsupportedMediaException handling in HttpTransportPipeImpl
    private static final boolean ENABLE_CONTENT_TYPE_REWRITE = true;

    private static final String DEFAULT_SOAP11_CONTENT_TYPE = "text/xml; charset=utf-8";
    private static final String DEFAULT_SOAP12_CONTENT_TYPE = "application/soap+xml; charset=utf-8";

    private final Codec wrapped;

    private InputStream responseStream;
    private ReadableByteChannel responseChannel;

    public CodecWrapper(Codec wrapped) {
        this.wrapped = wrapped;
    }

    public InputStream getResponseStream() {
        return responseStream;
    }

    public ReadableByteChannel getResponseChannel() {
        return responseChannel;
    }

    @Override
    public String getMimeType() {
        return wrapped.getMimeType();
    }

    @Override
    public ContentType getStaticContentType(Packet packet) {
        return wrapped.getStaticContentType(packet);
    }

    @Override
    public ContentType encode(Packet packet, OutputStream out) throws IOException {
        return wrapped.encode(packet, out);
    }

    // todo: encode is the outgoing request, might also be interesting for other use cases?

    @Override
    public ContentType encode(Packet packet, WritableByteChannel buffer) {
        return wrapped.encode(packet, buffer);
    }

    @Override
    public Codec copy() {
        return new CodecWrapper(wrapped.copy());
    }

    @Override
    public void decode(InputStream in, String contentType, Packet response) throws IOException {
        this.responseStream = in;
        String updatedContentType = processContentType(contentType, response);
        wrapped.decode(in, updatedContentType, response);
    }

    @Override
    public void decode(ReadableByteChannel in, String contentType, Packet response) {
        this.responseChannel = in;
        String updatedContentType = processContentType(contentType, response);
        wrapped.decode(in, updatedContentType, response);
    }

    private String processContentType(String contentType, Packet response) {
        System.out.println("[SOAP CONTENT TYPE DEMO] Response content type: " + contentType);
        if (isSOAPCodec() && ENABLE_CONTENT_TYPE_REWRITE) {
            String expectedContentType = getExpectedContentType(response, DEFAULT_SOAP11_CONTENT_TYPE);
            System.out.println("[SOAP CONTENT TYPE DEMO] Updated to content type: " + expectedContentType);
            System.out.println();
            // todo: this is changed for every SOAP call that is executed on this Java runtime (in the same classloader)
            // todo: additional logic could be implemented here (perhaps based on endpoint URL via TransportTubeFactoryImpl,
            // todo: or check if we can access the SOAP version used, or the ACCEPT header in the HTTP request?)
            return expectedContentType;
        }
        return contentType;
    }

    private boolean isSOAPCodec() {
        return wrapped instanceof com.sun.xml.ws.encoding.SOAPBindingCodec;
    }

    private String getExpectedContentType(Packet packet, String defaultContentType) {
        // todo: this should return the expected content type for SOAP1.1 or SOAP1.2 based on com.sun.xml.ws.encoding.SOAPBindingCodec.getXMLCodec
        // todo: but since the specific version subclasses of that codec are package private, we can't simply do an instanceof check
        ContentType staticContentType = wrapped.getStaticContentType(packet);
        if(staticContentType != null) {
            return staticContentType.getContentType();
        }
        return defaultContentType;
    }

}