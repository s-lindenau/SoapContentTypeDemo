package nl.slindenau.soap.client;

import io.spring.guides.gs_producing_web_service.CountriesPort;
import io.spring.guides.gs_producing_web_service.CountriesPortService;
import io.spring.guides.gs_producing_web_service.GetCountryRequest;
import io.spring.guides.gs_producing_web_service.GetCountryResponse;
import nl.slindenau.soap.demo.ContentTypeInterceptor;
import nl.slindenau.soap.model.CountryDecorator;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;

public class CountriesSoapClient {

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
        client.getInInterceptors().add(new ContentTypeInterceptor());
    }

    private GetCountryRequest createRequest() {
        GetCountryRequest request = new GetCountryRequest();
        request.setName("United Kingdom");
        return request;
    }
}

