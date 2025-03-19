package org.study.iu.httpservlet.api.test_08_prime_numbers;

import org.study.iu.httpservlet.interfaces.MultiThreadingTestable;

import jakarta.servlet.annotation.WebServlet;

@WebServlet(value = "/api/08_multi", asyncSupported = true)
public class MultiThreadedTest08Servlet extends SingleThreadedTest08Servlet implements MultiThreadingTestable {
}