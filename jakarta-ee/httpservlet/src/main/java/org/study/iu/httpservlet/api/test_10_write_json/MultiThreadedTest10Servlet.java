package org.study.iu.httpservlet.api.test_10_write_json;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/10_multi", asyncSupported = true)
public class MultiThreadedTest10Servlet extends SingleThreadedTest10Servlet implements MultiThreadingTestable {
}