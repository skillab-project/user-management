package gr.uom.user_management.services;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Map;

@Service
public class ProxyService {

    public ProxyService() {
        // 30 Seconds timeout
        Unirest.config()
                .connectTimeout(10000)
                .socketTimeout(30000)
                .automaticRetries(false);
    }

    public ResponseEntity<byte[]> processProxyRequest(
            byte[] body,
            HttpMethod method,
            HttpServletRequest request,
            String targetBaseUrl,
            Map<String, String> customHeaders,
            String prefixToRemove
    ) throws URISyntaxException {

        // --- 1. URL Construction ---
        String requestUrl = request.getRequestURI();
        String targetPath = requestUrl.replace(prefixToRemove, "");
        if (!targetBaseUrl.endsWith("/") && !targetPath.startsWith("/")) {
            targetBaseUrl += "/";
        }
        String finalUrl = targetBaseUrl + targetPath +
                (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        System.out.println(">>> [PROXY START] Method: " + method + " | URL: " + finalUrl);

        // --- 2. Request Preparation ---
        var unirestReq = Unirest.request(method.name(), finalUrl);

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // FILTER HEADERS
            if (!headerName.equalsIgnoreCase("Host") &&
                    !headerName.equalsIgnoreCase("Accept-Encoding") &&
                    !headerName.equalsIgnoreCase("Content-Length") &&
                    !headerName.equalsIgnoreCase("Transfer-Encoding") &&
                    !headerName.equalsIgnoreCase("Connection")) {

                Enumeration<String> values = request.getHeaders(headerName);
                while(values.hasMoreElements()){
                    String val = values.nextElement();
                    unirestReq.header(headerName, val);
                }
            }
        }

        if (customHeaders != null) {
            customHeaders.forEach(unirestReq::header);
        }

        if (body != null && body.length > 0) {
            unirestReq.body(body);
        }

        try {
            System.out.println("... [PROXY SENDING] Waiting for downstream response ...");
            long startTime = System.currentTimeMillis();

            // --- 3. Execute Request ---
            // If the code hangs here, it is a Docker Network / Firewall issue
            HttpResponse<byte[]> response = unirestReq.asBytes();

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("<<< [PROXY RECEIVED] Status: " + response.getStatus() + " | Time: " + duration + "ms");

            // --- 4. Process Body ---
            byte[] responseBody = response.getBody();
            int bodySize = (responseBody != null) ? responseBody.length : 0;
            System.out.println("    [PROXY BODY] Size: " + bodySize + " bytes");

            // --- 5. Map Headers ---
            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().all().forEach(header -> {
                // Strip chunking/encoding headers to avoid confusing Nginx
                if (!header.getName().equalsIgnoreCase("Transfer-Encoding") &&
                        !header.getName().equalsIgnoreCase("Content-Encoding")) {
                    responseHeaders.add(header.getName(), header.getValue());
                }
            });

            System.out.println("=== [PROXY FINISHED] Returning ResponseEntity to Controller ===");

            return ResponseEntity.status(response.getStatus())
                    .headers(responseHeaders)
                    .body(responseBody);

        } catch (UnirestException e) {
            System.err.println("!!! [PROXY ERROR] Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(502).build();
        }
    }
}