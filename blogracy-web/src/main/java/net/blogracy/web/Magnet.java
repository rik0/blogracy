package net.blogracy.web;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.blogracy.config.Configurations;
import net.blogracy.controller.FileSharing;

public class Magnet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String hash = request.getParameter("hash");
        final String CACHE_FOLDER = Configurations.getPathConfig()
                .getCachedFilesDirectoryPath();
        File file = new File(CACHE_FOLDER + File.separator + hash);
        if (file.exists()) {
            // response.sendRedirect("/cache/" + hash);
            response.setContentType("video/mp4"); // TODO! ...
            request.getRequestDispatcher("/cache/" + hash).forward(request,
                    response);
        } else {
            int dot = hash.lastIndexOf(".");
            if (dot >= 0) {
                hash = hash.substring(0, dot);
            }
            FileSharing.getSingleton().downloadByHash(hash);
            response.sendError(503);
        }
    }
}
