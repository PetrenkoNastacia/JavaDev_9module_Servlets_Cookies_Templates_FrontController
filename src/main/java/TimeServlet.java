import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    @Override
    public void init() {
        templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(templateEngine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        templateEngine.addTemplateResolver(resolver);
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String currentTime = getCurrentTime(req, resp);

        Context context = new Context(req.getLocale(), Map.of("time", currentTime));
        templateEngine.process("time", context, resp.getWriter());

        resp.getWriter().close();
    }

    private static String getCurrentTime(HttpServletRequest req, HttpServletResponse resp) {
        String timezone = req.getParameter("timezone");

        if (timezone == null) {
            timezone = getTimezoneFromCookies(req);
        } else {
            timezone = timezone.replace(' ', '+');
            resp.addCookie(new Cookie("lastTimezone", timezone));
        }
        return ZonedDateTime
                .now(ZoneId.of(timezone))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ")) + timezone;
    }

    private static String getTimezoneFromCookies(HttpServletRequest req) {
        String cookies = req.getHeader("Cookie");
        Map<String, String> result = new HashMap<>();

        if (cookies != null) {
            String[] ArrOfCookies = cookies.split(";");
            for (String pair : ArrOfCookies) {
                String[] ArrOfKeyValue = pair.split("=");
                result.put(ArrOfKeyValue[0], ArrOfKeyValue[1]);
            }
        }
        return result.getOrDefault("lastTimezone", "UTC");
    }
}