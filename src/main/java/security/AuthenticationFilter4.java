package security;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import model.*;

/** Protects each group of JSP pages with its server-side session role. */
public class AuthenticationFilter4 implements Filter {
    public void init(FilterConfig config) { }
    public void destroy() { }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        res.setHeader("Cache-Control", "no-store"); res.setHeader("Pragma", "no-cache");
        res.setHeader("X-Content-Type-Options", "nosniff");
        String path = req.getServletPath();
        boolean readerPage = path.startsWith("/view/reader/");
        boolean staffPage = path.startsWith("/view/libraryStaff/");
        boolean managerPage = path.startsWith("/view/managementStaff/");
        if (!readerPage && !staffPage && !managerPage) { chain.doFilter(request,response); return; }
        HttpSession session = req.getSession(false);
        boolean reader = session != null && session.getAttribute("currentReader") instanceof Reader4;
        boolean staff = session != null && session.getAttribute("currentStaff") instanceof LibraryStaff4;
        boolean manager = session != null && session.getAttribute("currentManagementStaff") instanceof ManagementStaff4;
        if (!reader && !staff && !manager) {
            res.sendRedirect(req.getContextPath() + "/view/member/LoginView4.jsp"); return;
        }
        if (!(readerPage && reader || staffPage && staff || managerPage && manager)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN); return;
        }
        chain.doFilter(request,response);
    }
}
