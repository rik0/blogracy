package net.blogracy.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.blogracy.config.Configurations;
import net.blogracy.controller.CommentsController;
import net.blogracy.errors.BlogracyItemNotFound;

public class CommentSender extends HttpServlet {

	
	@SuppressWarnings("unused")
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String destinationUserId = request.getParameter("userId");
		String albumId = request.getParameter("albumId");
		String mediaId = request.getParameter("mediaId");
		String commentText = request.getParameter("userCommentText");
		

		try {
			CommentsController.getInstance().addComment(destinationUserId, Configurations.getUserConfig().getUser().getHash().toString(), commentText, mediaId);
		} catch (BlogracyItemNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
