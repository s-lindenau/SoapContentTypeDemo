package nl.slindenau;

import nl.slindenau.soap.client.CountriesSoapClient;

public class Application {

    public static void main(String[] commandLineArguments) {
        CountriesSoapClient soapClient = new CountriesSoapClient();
        soapClient.getCountries();
    }
}
