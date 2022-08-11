package nl.slindenau.soap.client;

import io.spring.guides.gs_producing_web_service.CountriesPort;
import io.spring.guides.gs_producing_web_service.CountriesPortService;
import io.spring.guides.gs_producing_web_service.GetCountryRequest;
import io.spring.guides.gs_producing_web_service.GetCountryResponse;
import nl.slindenau.soap.model.CountryDecorator;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class CountriesSoapClient {

    private static final String CONTENT_TYPE_TEXT_XML = "text/xml";

    public void getCountries() {
        CountriesPortService portService = new CountriesPortService(getLoggingFeature());
        CountriesPort port = portService.getCountriesPortSoap11();
        configureClientInterceptor(port);
        GetCountryResponse country = port.getCountry(createRequest());
        System.out.println("Response");
        System.out.println("--------");
        System.out.println(new CountryDecorator(country));
    }

    private LoggingFeature getLoggingFeature() {
        LoggingFeature loggingFeature = new LoggingFeature();
        loggingFeature.setPrettyLogging(true);
        return loggingFeature;
    }

    private void configureClientInterceptor(CountriesPort port) {
        Client client = ClientProxy.getClient(port);
        client.getInInterceptors().add(new AbstractPhaseInterceptor<Message>(Phase.RECEIVE) {
            public void handleMessage(Message message) {
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
        });
    }

    private GetCountryRequest createRequest() {
        GetCountryRequest request = new GetCountryRequest();
        request.setName("United Kingdom");
        return request;
    }
}

