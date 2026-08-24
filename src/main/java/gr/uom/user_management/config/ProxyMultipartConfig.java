package gr.uom.user_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * Multipart handling for the API gateway.
 *
 * <p>{@link gr.uom.user_management.controllers.GatewayController} proxies raw
 * request bodies to downstream services by reading them as {@code byte[]}.
 * With Spring's default (eager) multipart resolution, a
 * {@code multipart/form-data} request is parsed — and its input stream
 * consumed — before the controller method runs. The proxy then reads an empty
 * body and forwards it downstream, so file uploads (e.g. PDF analysis on the
 * future-technology-trends-identifier backend) fail there with
 * {@code "file" field required} / HTTP 422.
 *
 * <p>Resolving multipart <b>lazily</b> defers parsing until the parts are
 * actually accessed. The gateway never touches the parts (it streams the raw
 * body straight through), so multipart uploads are forwarded intact. Endpoints
 * that genuinely consume a {@link org.springframework.web.multipart.MultipartFile}
 * (e.g. the CV upload) are unaffected — accessing the file triggers parsing on
 * demand.
 *
 * <p>The bean must be named {@code multipartResolver} so that the
 * {@code DispatcherServlet} picks it up in place of Spring Boot's
 * auto-configured (eager) resolver.
 */
@Configuration
public class ProxyMultipartConfig {

    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        resolver.setResolveLazily(true);
        return resolver;
    }
}
