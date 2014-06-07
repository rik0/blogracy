package net.blogracy.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.blogracy.config.Configurations;
import net.blogracy.controller.CommentsControllerImpl;
import net.blogracy.errors.BlogracyItemNotFound;

public class LikeSender extends HttpServlet {

	@SuppressWarnings("unused")
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String destinationUserId = request.getParameter("userId");
		String mediaId = request.getParameter("mediaId");
		
		try {
			CommentsControllerImpl.getInstance().addLike(destinationUserId, Configurations.getUserConfig().getUser().getHash().toString(), mediaId);
		} catch (BlogracyItemNotFound e) {
			e.printStackTrace();
		}
		
	}
}
