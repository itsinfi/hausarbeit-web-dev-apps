package org.study.iu.jaxrs.api.test_01_connection_check;

import org.study.iu.jaxrs.interfaces.MultiThreadingTestable;

import jakarta.ws.rs.Path;

@Path("01_multi")
public class MultiThreaded01Controller extends SingleThreadedTest01Controller implements MultiThreadingTestable {
}
