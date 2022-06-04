package nl.slindenau.soap.transport;

import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.TransportTubeFactory;
import com.sun.xml.ws.api.pipe.Tube;

import javax.xml.ws.WebServiceException;

public class TransportTubeFactoryImpl extends TransportTubeFactory {

    private static final String URI_SCHEME_HTTP = "http";
    private static final String URI_SCHEME_HTTPS = "https";

    @Override
    public Tube doCreate(ClientTubeAssemblerContext context) {
        String scheme = context.getAddress().getURI().getScheme();
        if (scheme != null) {
            if (URI_SCHEME_HTTP.equalsIgnoreCase(scheme) || URI_SCHEME_HTTPS.equalsIgnoreCase(scheme)) {
                CodecWrapper codec = new CodecWrapper(context.getCodec());
                return new HttpTransportPipeImpl(codec, context.getBinding());
            }
        }

        throw new WebServiceException("Unsupported endpoint address: " + context.getAddress());
    }
}