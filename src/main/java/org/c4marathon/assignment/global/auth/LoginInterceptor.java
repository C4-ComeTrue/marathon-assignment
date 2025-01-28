package org.c4marathon.assignment.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.c4marathon.assignment.global.session.SessionConst;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        HttpSession session = request.getSession();
        SessionMemberInfo memberInfo = (SessionMemberInfo) session.getAttribute(SessionConst.LOGIN_MEMBER);

        // 적당한 에러 status와 에러메시지 반환하도록 변경해야함
        if (memberInfo == null) {
            response.sendRedirect("/login?redirectURL=" + requestURI);
            return false;
        }
        return true;
    }

}
