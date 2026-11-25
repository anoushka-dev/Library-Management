package com.library.filter;

import com.library.model.User;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getServletPath();

        if (path == null || path.isEmpty() || "/index.jsp".equals(path) ||
                "/jsp/auth/login.jsp".equals(path) || "/jsp/auth/register.jsp".equals(path) ||
                "/login.do".equals(path) || "/register.do".equals(path) ||
                path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") ||
                path.startsWith("/WEB-INF/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        Object sessionUser = session == null ? null : session.getAttribute("currentUser");
        if (!(sessionUser instanceof User)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/index.jsp");
            return;
        }

        User currentUser = (User) sessionUser;
        if (isAdminOnlyPath(path) && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/member/dashboard.do");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isAdminOnlyPath(String path) {
        return path.startsWith("/admin/") || path.startsWith("/jsp/admin/") ||
                "/books/add.do".equals(path) || "/books/update.do".equals(path) ||
                "/books/delete.do".equals(path) || "/rentals/overdue.do".equals(path);
    }

    @Override
    public void destroy() {
    }
}
