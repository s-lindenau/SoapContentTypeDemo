package nl.slindenau.soap.transport;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.server.UnsupportedMediaException;
import com.sun.xml.ws.transport.http.client.HttpClientTransport;
import com.sun.xml.ws.transport.http.client.HttpTransportPipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpTransportPipeImpl extends HttpTransportPipe {

    private final CodecWrapper codec;

    public HttpTransportPipeImpl(CodecWrapper codec, WSBinding binding) {
        super(codec, binding);
        this.codec = codec;
    }

    @Override
    public NextAction processRequest(Packet request) {
        try {
            return super.processRequest(request);
        } catch (UnsupportedMediaException ex) {
            // todo: here you can access the stored data from the codec wrapper
            // todo:   this input stream is the (unread) stream of the HTTP response body when the codec rejected the content-type header
            // todo: note that for the content type text/html a different exception is thrown (earlier), and this part of the code will not be reached
            System.out.println("[SOAP CONTENT TYPE DEMO] Response content on caught UnsupportedMediaException: " + getResponseAsString());
            System.out.println();
            // todo: note that every SOAP client in the current Java runtime process in this classloader will go through this custom transport pipe
            // todo: so in case of multi threading this can be from multiple different client instances
            // todo: so the easiest way to relay this content back to the calling client would be to attach it to the following exception;
            throw ex;
        } catch (ClientTransportException ex) {
            // todo: HTML is currently not supported, the codec.decode method has not been called at this point, so the response stream is null.
            System.out.println("[SOAP CONTENT TYPE DEMO] Unsupported content type: text/html");
            System.out.println();
            throw ex;
        }
    }

    private String getResponseAsString() {
        try (InputStreamReader in = new InputStreamReader(codec.getResponseStream(), StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(in)) {
            return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected HttpClientTransport getTransport(Packet request, Map<String, List<String>> requestHeaders) {
        // todo: this is the Connection doing the actual HTTP work. Might be interesting to extend for other use cases?
        return new HttpClientTransport(request, requestHeaders);
    }
}