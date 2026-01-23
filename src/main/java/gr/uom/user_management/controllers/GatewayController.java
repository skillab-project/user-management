package gr.uom.user_management.controllers;

import gr.uom.user_management.models.User;
import gr.uom.user_management.repositories.UserRepository;
import gr.uom.user_management.services.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class GatewayController {

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private UserRepository userRepository;

    @Value("${hiring.service.url}")
    private String hiringServiceUrl;

    @Value("${policy.service.url}")
    private String policyServiceUrl;

    @Value("${employeemanagement.service.url}")
    private String employeeManagementUrl;

    @Value("${kudetection.service.url}")
    private String kUDetectionUrl;

    @Value("${policysuccessevaluator.service.url}")
    private String policySuccessEvaluatorUrl;

    // --- Hiring Proxy ---
    @RequestMapping("/hiring-management-backend/**")
    public ResponseEntity<byte[]> proxyHiring(
            @RequestBody(required = false) byte[] body,
            HttpMethod method,
            HttpServletRequest request
    ) throws URISyntaxException {
        return handleProxy(body, method, request, hiringServiceUrl, "/hiring-management-backend");
    }

    // --- Policy Proxy ---
    @RequestMapping("/backend-policy/**")
    public ResponseEntity<byte[]> proxyPolicy(
            @RequestBody(required = false) byte[] body,
            HttpMethod method,
            HttpServletRequest request
    ) throws URISyntaxException {
        return handleProxy(body, method, request, policyServiceUrl, "/backend-policy");
    }

    // --- Employee Management Proxy ---
    @RequestMapping("/employee-management-backend/**")
    public ResponseEntity<byte[]> proxyEmployeeManagement(
            @RequestBody(required = false) byte[] body,
            HttpMethod method,
            HttpServletRequest request
    ) throws URISyntaxException {
        return handleProxy(body, method, request, employeeManagementUrl, "/employee-management-backend");
    }

    // --- KU Detection Proxy ---
    @RequestMapping("/ku-detection-backend/**")
    public ResponseEntity<byte[]> proxyKUDetection(
            @RequestBody(required = false) byte[] body,
            HttpMethod method,
            HttpServletRequest request
    ) throws URISyntaxException {
        return handleProxy(body, method, request, kUDetectionUrl, "/ku-detection-backend");
    }

    // --- Policy Success Evaluator Proxy ---
    @RequestMapping("/policy-success-evaluator-backend/**")
    public ResponseEntity<byte[]> proxyPolicySuccessEvaluator(
            @RequestBody(required = false) byte[] body,
            HttpMethod method,
            HttpServletRequest request
    ) throws URISyntaxException {
        return handleProxy(body, method, request, policySuccessEvaluatorUrl, "/policy-success-evaluator-backend");
    }


    private ResponseEntity<byte[]> handleProxy(byte[] body, HttpMethod method, HttpServletRequest request, String targetUrl, String prefixToRemove) throws URISyntaxException {
        // Auth & Get User info
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findByEmail(email);

        Map<String, String> customHeaders = new HashMap<>();
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            customHeaders.put("X-User-Id", user.getId().toString());
            customHeaders.put("X-User-Email", user.getEmail());
            customHeaders.put("X-User-Organization", user.getOrganization());
        }

        // Adjust the URI in the ProxyService call
        // We need to make sure ProxyService knows to strip the specific prefix ("/policy" or "/hiring-management")
        return proxyService.processProxyRequest(body, method, request, targetUrl, customHeaders, prefixToRemove);
    }
}