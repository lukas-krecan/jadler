package net.jadler.stubbing.server.jetty;

import net.jadler.stubbing.RequestRecorder;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Records request using RequestRecorder.
 */
public class RequestRecordingHandler extends AbstractHandler {
    private final RequestRecorder requestRecorder;

    public RequestRecordingHandler(RequestRecorder requestRecorder) {
        this.requestRecorder = requestRecorder;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        requestRecorder.recordRequest(request);
    }
}
