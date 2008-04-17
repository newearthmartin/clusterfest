package com.flaptor.clusterfest;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.flaptor.util.Config;

public class UserAuthorizationFilter extends com.flaptor.util.web.UserAuthorizationFilter{
    
    private boolean enabled = true;
    
    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
        enabled = Config.getConfig("clustering.properties").getBoolean("clustering.web.login.enabled");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (enabled) super.doFilter(request, response, chain);
        else chain.doFilter(request, response);
    }
}
