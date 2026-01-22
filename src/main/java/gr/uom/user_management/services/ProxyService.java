package gr.uom.user_management.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

@Service
public class ProxyService {

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<byte[]> processProxyRequest(
            byte[] body,
            HttpMethod method,
            HttpServletRequest request,
            String targetBaseUrl,
            Map<String, String> customHeaders,
            String prefixToRemove
    ) throws URISyntaxException {
        String requestUrl = request.getRequestURI();

        // Use the dynamic prefix passed from controller
        String targetPath = requestUrl.replace(prefixToRemove, "");

        URI uri = new URI(targetBaseUrl + targetPath +
                (request.getQueryString() != null ? "?" + request.getQueryString() : ""));

        // Copy Incoming Headers
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Skip content-length to let RestTemplate calculate it
            if (!headerName.equalsIgnoreCase("Content-Length")) {
                headers.addAll(headerName, Collections.list(request.getHeaders(headerName)));
            }
        }

        // Inject Custom Headers
        if (customHeaders != null) {
            customHeaders.forEach(headers::add);
        }

        // Create Entity and Forward
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(uri, method, httpEntity, byte[].class);
        } catch (HttpStatusCodeException e) {
            // Handle errors from downstream service (4xx, 5xx)
            return ResponseEntity.status(e.getRawStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}