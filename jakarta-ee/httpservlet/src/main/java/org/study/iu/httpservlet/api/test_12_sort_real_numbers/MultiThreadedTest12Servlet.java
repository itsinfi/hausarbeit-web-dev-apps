package org.study.iu.httpservlet.api.test_12_sort_real_numbers;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/12_multi", asyncSupported = true)
public class MultiThreadedTest12Servlet extends SingleThreadedTest12Servlet implements MultiThreadingTestable {
}