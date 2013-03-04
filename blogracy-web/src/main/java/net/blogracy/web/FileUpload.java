package net.blogracy.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.blogracy.config.Configurations;
import net.blogracy.controller.ActivitiesController;
import net.blogracy.controller.FileSharing;

public class FileUpload extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        File attachment = (File) req.getAttribute("userfile");
        String text = req.getParameter("usertext").trim();

        FileSharing sharing = FileSharing.getSingleton();
        // String id = sharing.hash("mic");
        String id = Configurations.getUserConfig().getUser().getHash()
                .toString();

        // TODO recipient of message/files? the publishing user or the profile's
        // user?
        // String dest = req.getParameter("user");
        ActivitiesController activities = ActivitiesController.getSingleton();
        activities.addFeedEntry(id, text, attachment);

        PrintWriter outp = resp.getWriter();
        outp.write("<html>");
        outp.write("<head><title>FileUpload page</title></head>");
        outp.write("<body>");
        outp.write("<h2>" + text + "</h2>");
        outp.write("</body>");
        outp.write("</html>");
    }
}
