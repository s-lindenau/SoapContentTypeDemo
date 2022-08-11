package nl.slindenau.soap.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.guides.gs_producing_web_service.GetCountryResponse;

import java.io.IOException;
import java.io.StringWriter;

public class CountryDecorator {

    private final GetCountryResponse country;

    public CountryDecorator(GetCountryResponse country) {
        this.country = country;
    }

    @Override
    public String toString() {
        // to make the SOAP-XML response parsed JAXB Object readable in the console log
        try {
            StringWriter jsonString = new StringWriter();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(jsonString, this.country);
            return "Country: " + jsonString;
        } catch (IOException e) {
            return country.toString();
        }
    }
}
