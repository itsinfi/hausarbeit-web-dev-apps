package org.study.iu.httpservlet.api.test_11_sort_whole_numbers;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/11_multi", asyncSupported = true)
public class MultiThreadedTest11Servlet extends SingleThreadedTest11Servlet implements MultiThreadingTestable {
}