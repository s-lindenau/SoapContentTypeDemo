## SOAP Content Type Demo
A SOAP `Content-Type` demonstration by [slindenau](https://github.com/s-lindenau)

### Introduction
In SOAP web services the `Content-Type` header can cause some annoying problems.

The expected content types for SOAP messages are as follows:
- `text/xml` for SOAP v1.1
- `application/soap+xml` for SOAP v1.2

If there is a mismatch on this header, your Java client may respond with any of the following exceptions: 
- `UnsupportedMediaException: Unsupported Content-Type: text/xml; charset=utf-8 Supported ones are: [application/soap+xml]`
- `UnsupportedMediaException: Unsupported Content-Type: text/plain;charset=ISO-8859-1 Supported ones are: [text/xml]`
- `UnsupportedMediaException: Unsupported Content-Type: text/html Supported ones are: [text/xml]`
- `etc`

In this demonstration I have set up a SOAP v1.1 server that is responding with the content type of SOAP v1.2, 
and several clients to showcase the `UnsupportedMediaException` and possible ways to deal with this exception.

If you're looking for a solution without changing your code, a proxy server would be a good alternative.  
See for reference: https://stackoverflow.com/a/65065756/18699445

The code in this project was created & tested with the following tools (unless stated otherwise):
- IntelliJ IDEA 2021.3.2 (Community Edition)
- Java jdk8u332-b09
- Maven 3.8.4

### The Server
The server is set up with the following Spring guide:
- https://spring.io/guides/gs/producing-web-service/ 
- https://github.com/spring-guides/gs-producing-web-service

With the following modifications:
- A Servlet Filter was added in [WebServiceConfig](https://github.com/s-lindenau/SoapContentTypeDemo/blob/master/server/src/main/java/nl/slindenau/producingwebservice/WebServiceConfig.java)
- The content type is rewritten in [ContentTypeDemoFilter](https://github.com/s-lindenau/SoapContentTypeDemo/blob/master/server/src/main/java/nl/slindenau/soap/demo/ContentTypeDemoFilter.java)

You can run the server with the following command in the `server` project:
- `mvn spring-boot:run`

The  SOAP service should then be available on `http://localhost:9097/ws/countries.wsdl`  
The code will loop through a set of different content types for the response in order.  
On processing a request the original & rewritten content type of the response are logged to the console, see the following screenshot:

<a href="https://raw.githubusercontent.com/s-lindenau/SoapContentTypeDemo/master/blob/soap-server.PNG" target="_blank"><img src="https://raw.githubusercontent.com/s-lindenau/SoapContentTypeDemo/master/blob/soap-server-thumb.PNG"/></a>

### The Client - JRE
This is a basic client that uses the `jaxws-maven-plugin` to generate a model with the `wsimport` goal.  
It uses the JRE internal SOAP processing code located in package `com.sun.xml.internal.ws` (RT.jar).

You can run the client as follows: 
- Execute `mvn clean package` and run main class `nl.slindenau.Application` in project `client-jre`

This will result in the following exception:
```
Exception in thread "main" com.sun.xml.internal.ws.server.UnsupportedMediaException: Unsupported Content-Type: application/soap+xml;charset=utf-8 Supported ones are: [text/xml]
	at com.sun.xml.internal.ws.encoding.StreamSOAPCodec.decode(StreamSOAPCodec.java:220)
	at com.sun.xml.internal.ws.encoding.StreamSOAPCodec.decode(StreamSOAPCodec.java:151)
	at com.sun.xml.internal.ws.encoding.SOAPBindingCodec.decode(SOAPBindingCodec.java:299)
	at com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.createResponsePacket(HttpTransportPipe.java:268)
	at com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.process(HttpTransportPipe.java:217)
	at com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.processRequest(HttpTransportPipe.java:130)
	at com.sun.xml.internal.ws.transport.DeferredTransportPipe.processRequest(DeferredTransportPipe.java:95)
```
In the console the full HTTP request + response are logged, 
this can be disabled in [CountriesSoapClient#setDebugProperties](https://github.com/s-lindenau/SoapContentTypeDemo/blob/master/client-jre/src/main/java/nl/slindenau/soap/client/CountriesSoapClient.java).
See the following screenshot:

<a href="https://raw.githubusercontent.com/s-lindenau/SoapContentTypeDemo/master/blob/soap-client-jre.PNG" target="_blank"><img src="https://raw.githubusercontent.com/s-lindenau/SoapContentTypeDemo/master/blob/soap-client-jre-thumb.PNG"/></a>

*Note that there currently is no way to prevent this without changing the SOAP runtime, which will be shown in the next client.
In theory the proposed solution in that client should be possible here as well, but that would require us to depend on- and extend internal JRE code. 
This is a bad practice, and maven support for [system scope dependencies](https://stackoverflow.com/questions/13121954/maven-cant-compile-class-which-depends-on-rt-jar) 
did not work for me.*

### The Client - JAXWS-RT
This client uses the exact same generated SOAP code as the `client-jre` (so no modifications required), but with a different JAX-WS runtime.  
By depending on the following libraries we can exchange the `internal` runtime for `jaxws-rt`.  
Note that this runtime should be nearly identical and fully compatible with the internal code of the JRE, 
but this now gives us the option to expand it.

Dependencies required to switch from internal SOAP processing to JAX-WS runtime:

```
<dependency>
    <groupId>javax.activation</groupId>
    <artifactId>activation</artifactId>
    <version>1.1.1</version>
</dependency>
<dependency>
    <groupId>com.sun.xml.bind</groupId>
    <artifactId>jaxb-impl</artifactId>
    <version>2.3.5</version>
</dependency>
<dependency>
    <groupId>com.sun.xml.ws</groupId>
    <artifactId>jaxws-rt</artifactId>
    <version>2.3.5</version>
</dependency>
```

The part that rewrites the content type on this client is implemented via a custom `TransportTubeFactory` 
that is loaded with [service discovery](https://www.javadoc.io/doc/com.sun.xml.ws/jaxws-rt/latest/com.sun.xml.ws/com/sun/xml/ws/api/pipe/TransportTubeFactory.html).
Keep in mind that this factory is used for **every SOAP call** executed by clients in the current `ClassLoader` 
(by default that is the entire application you're running in a single JRE process, unless you're using some form of container/isolation).

By using a custom `HttpTransportPipe` and wrapping the `Codec` we can rewrite the content type just before the HTTP response is passed to the SOAP codec. 
This works for most cases, except for `text/html`, which is [rejected](https://github.com/eclipse-ee4j/metro-jax-ws/blob/2.3.5/jaxws-ri/runtime/rt/src/main/java/com/sun/xml/ws/transport/http/client/HttpTransportPipe.java#L262) 
earlier in the process by JAX-WS in more recent versions of the runtime library. If you need to process HTML consider using an older version of `jaxws-rt`.
`jaxws-rt-2.1.7` is the last version that does not reject HTML right away, but beware this library is from 2009! You could also duplicate more code in the custom Transport tube or -pipe classes.

You can run the client as follows:
- Execute `mvn clean package` and run main class `nl.slindenau.Application` in project `client-jaxws-rt`

If we do not rewrite the content type in the [CodecWrapper](https://github.com/s-lindenau/SoapContentTypeDemo/blob/master/client-jaxws-rt/src/main/java/nl/slindenau/soap/transport/CodecWrapper.java) 
we get a similar exception as before, but note that the `internal` package is missing:

```
Exception in thread "main" com.sun.xml.ws.server.UnsupportedMediaException: Unsupported Content-Type: application/soap+xml;charset=utf-8 Supported ones are: [text/xml]
at com.sun.xml.ws.encoding.StreamSOAPCodec.decode(StreamSOAPCodec.java:205)
at com.sun.xml.ws.encoding.StreamSOAPCodec.decode(StreamSOAPCodec.java:136)
at com.sun.xml.ws.encoding.SOAPBindingCodec.decode(SOAPBindingCodec.java:289)
```

But with our custom [HttpTransportPipe](https://github.com/s-lindenau/SoapContentTypeDemo/blob/master/client-jaxws-rt/src/main/java/nl/slindenau/soap/transport/HttpTransportPipeImpl.java)
we can now access the full HTTP response body, which would otherwise be lost (or only logged to the console):  
<a href="https://raw.githubusercontent.com/s-lindenau/SoapContentTypeDemo/master/blob/soap-client-jaxws-rt-1.PNG" target="_blank"><img src="https://raw.githubusercontent.com/s-lindenau/SoapContentTypeDemo/master/blob/soap-client-jaxws-rt-1-thumb.PNG"/></a>

And if we enable client-side content type rewrite in the [CodecWrapper](https://github.com/s-lindenau/SoapContentTypeDemo/blob/master/client-jaxws-rt/src/main/java/nl/slindenau/soap/transport/CodecWrapper.java),
we can process the request like nothing was wrong:  
<a href="https://raw.githubusercontent.com/s-lindenau/SoapContentTypeDemo/master/blob/soap-client-jaxws-rt-2.PNG" target="_blank"><img src="https://raw.githubusercontent.com/s-lindenau/SoapContentTypeDemo/master/blob/soap-client-jaxws-rt-2-thumb.PNG"/></a>

### The Client - JAXWS-RT-JRE17

- [ ] todo: client with most recent version of each library, java 17

### The Client - Apache-CXF

This is an [Apache CXF](https://cxf.apache.org/) client that uses the `cxf-codegen-plugin` to generate a model with the `wsdl2java` goal.  
It uses the Apache CXF custom SOAP processing code. The content-type filter of this code was inspired by https://stackoverflow.com/a/50863434/18699445

You can run the client as follows: 
- Execute `mvn clean package` and run main class `nl.slindenau.Application` in project `client-cxf`

### Support, Disclaimer and Contributing

Be aware that all code in this project is a demonstration. It is not production ready.  
Feel free to use it as the license permits, but you are fully responsible for the final product you're developing.  
I am open to contributions that improve the quality of this demonstration & different client implementations that achieve the same goal.

If this helped you in any way to solve a problem, please show your support on https://stackoverflow.com/a/71734557/18699445