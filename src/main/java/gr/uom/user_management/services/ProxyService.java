package gr.uom.user_management.services;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;


@Service
public class ProxyService {

    public ProxyService() {
        // Configure Unirest Timeouts (Connect, Socket)
        Unirest.config()
                .connectTimeout(5000) // 5 seconds
                .socketTimeout(20000) // 20 seconds (wait for data)
                .automaticRetries(false); // Don't retry automatically for proxies
    }


    public ResponseEntity<byte[]> processProxyRequest(
            byte[] body,
            HttpMethod method,
            HttpServletRequest request,
            String targetBaseUrl,
            Map<String, String> customHeaders,
            String prefixToRemove
    ) throws URISyntaxException {
        String requestUrl = request.getRequestURI();
        String targetPath = requestUrl.replace(prefixToRemove, "");

        // Ensure we don't end up with double slashes if prefix match wasn't perfect
        if (!targetBaseUrl.endsWith("/") && !targetPath.startsWith("/")) {
            targetBaseUrl += "/";
        }

        String finalUrl = targetBaseUrl + targetPath +
                (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        System.out.println("GATEWAY PROXYING TO: " + finalUrl);

        // 2. Prepare Unirest Request
        // Unirest.request handles GET, POST, PUT, DELETE, etc. dynamically
        var unirestReq = Unirest.request(method.name(), finalUrl);

        // 3. Copy Incoming Headers
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();

            // FILTER SENSITIVE HEADERS
            if (!headerName.equalsIgnoreCase("Content-Length") &&
                    !headerName.equalsIgnoreCase("Host") &&
                    !headerName.equalsIgnoreCase("Connection") &&
                    !headerName.equalsIgnoreCase("Transfer-Encoding")) {

                Enumeration<String> values = request.getHeaders(headerName);
                while(values.hasMoreElements()){
                    unirestReq.header(headerName, values.nextElement());
                }
            }
        }

        // 4. Inject Custom Headers (User Info)
        if (customHeaders != null) {
            customHeaders.forEach(unirestReq::header);
        }

        // 5. Set Body (if exists)
        if (body != null && body.length > 0) {
            unirestReq.body(body);
        }

        try {
            // 6. Execute Request
            HttpResponse<byte[]> response = unirestReq.asBytes();

            // 7. Map Unirest Response -> Spring ResponseEntity
            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().all().forEach(header ->
                    responseHeaders.add(header.getName(), header.getValue())
            );

            return ResponseEntity.status(response.getStatus())
                    .headers(responseHeaders)
                    .body(response.getBody());

        } catch (UnirestException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}