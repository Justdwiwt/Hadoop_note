package cn.tedu;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

public class LogServlet extends HttpServlet {

    private Logger logger = Logger.getLogger(LogServlet.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String info = URLDecoder.decode(request.getQueryString(), "utf-8");
        String[] kvs = info.split("&");
        StringBuffer buffer = new StringBuffer();
        for (String kv : kvs) {
            String value = kv.split("=").length == 2 ? kv.split("=")[1] : "";
            buffer.append(value + "|");
        }
        buffer.append(request.getRemoteAddr());
        logger.info(buffer.toString());
        System.out.println(buffer.toString());
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

}
