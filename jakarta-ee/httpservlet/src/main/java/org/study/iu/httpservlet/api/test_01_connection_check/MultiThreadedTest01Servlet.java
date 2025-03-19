package org.study.iu.httpservlet.api.test_01_connection_check;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/01_multi", asyncSupported = true)
public class MultiThreadedTest01Servlet extends SingleThreadedTest01Servlet implements MultiThreadingTestable {
}