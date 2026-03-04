package gr.uom.user_management.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Map;

@Service
public class ProxyService {

    private final RestTemplate restTemplate;

    public ProxyService() {
        this.restTemplate = new RestTemplate();
        // Increase timeouts if needed via SimpleClientHttpRequestFactory
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(300000);
        this.restTemplate.setRequestFactory(factory);
    }

    public ResponseEntity<byte[]> processProxyRequest(
            byte[] body,
            HttpMethod method,
            HttpServletRequest request,
            String targetBaseUrl,
            Map<String, String> customHeaders,
            String prefixToRemove
    ) throws URISyntaxException {

        // 1. Build target URL
        String requestUri = request.getRequestURI();
        String targetPath = requestUri.replace(prefixToRemove, "");
        String queryString = request.getQueryString();
        String finalUrl = targetBaseUrl.replaceAll("/+$", "")
                + targetPath
                + (queryString != null ? "?" + queryString : "");

        System.out.println(">>> [PROXY START] Method: " + method + " | URL: " + finalUrl);

        // 2. Copy headers from incoming request
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (isExcludedHeader(name)) continue;
            headers.set(name, request.getHeader(name));
        }

        // 3. Add custom headers (user info etc.)
        if (customHeaders != null) {
            customHeaders.forEach(headers::set);
        }

        // 4. Log body
        System.out.println(">>> [PROXY] Body is null? " + (body == null));
        if (body != null) {
            System.out.println(">>> [PROXY] Body length: " + body.length);
            System.out.println(">>> [PROXY] Body: " + new String(body, java.nio.charset.StandardCharsets.UTF_8));
        }

        // 5. Build request entity
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(
                (body != null && body.length > 0) ? body : null,
                headers
        );

        // 6. Execute
        try {
            System.out.println("... [PROXY SENDING]");
            long start = System.currentTimeMillis();

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    new URI(finalUrl),
                    method,
                    requestEntity,
                    byte[].class
            );

            System.out.println("<<< [PROXY RECEIVED] Status: " + response.getStatusCode()
                    + " | Time: " + (System.currentTimeMillis() - start) + "ms");

            // Strip problematic headers
            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().forEach((name, values) -> {
                if (!name.equalsIgnoreCase("Transfer-Encoding")
                        && !name.equalsIgnoreCase("Content-Encoding")) {
                    responseHeaders.put(name, values);
                }
            });

            return ResponseEntity.status(response.getStatusCode())
                    .headers(responseHeaders)
                    .body(response.getBody());

        } catch (HttpStatusCodeException e) {
            // Downstream returned 4xx/5xx — pass it through
            System.err.println("!!! [PROXY] Downstream error: " + e.getStatusCode());
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            System.err.println("!!! [PROXY ERROR] " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(502).build();
        }
    }

    private boolean isExcludedHeader(String name) {
        return name.equalsIgnoreCase("Host") ||
                name.equalsIgnoreCase("Content-Length") ||
                name.equalsIgnoreCase("Transfer-Encoding") ||
                name.equalsIgnoreCase("Connection") ||
                name.equalsIgnoreCase("Accept-Encoding");
    }
}