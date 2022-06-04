package nl.slindenau.soap.client;

import nl.slindenau.soap.model.CountryDecorator;

import java.net.MalformedURLException;
import java.net.URL;

public class CountriesSoapClient {

    public static final String WSDL_LOCATION = "http://localhost:9097/ws/countries.wsdl";

    public void getCountries() {
        CountriesPortService portService = new CountriesPortService(getWsdlLocation());
        CountriesPort port = portService.getCountriesPortSoap11();
        GetCountryRequest request = createRequest();
        GetCountryResponse response = port.getCountry(request);
        Country country = response.getCountry();
        System.out.println("Response");
        System.out.println("--------");
        System.out.println(new CountryDecorator(country));
    }

    private GetCountryRequest createRequest() {
        GetCountryRequest request = new GetCountryRequest();
        request.setName("United Kingdom");
        return request;
    }

    public void setDebugProperties(Boolean enableDebug) {
        setDebugProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", enableDebug.toString());
        setDebugProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", enableDebug.toString());
        setDebugProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump",enableDebug.toString());
        setDebugProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", enableDebug.toString());
        setDebugProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dumpTreshold", "999999");
    }

    private void setDebugProperty(String key, String value) {
        System.setProperty(key, value);
    }

    private URL getWsdlLocation() {
        try {
            return new URL(WSDL_LOCATION);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid WSDL url: " + WSDL_LOCATION, e);
        }
    }
}

