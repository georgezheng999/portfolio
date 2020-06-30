package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.data.Comment;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that will delete all comments. */
@WebServlet("/delete-comments")
public class DeleterServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    List<Key> keysToDelete = new ArrayList<>();
    for (final Entity entity : results.asIterable()) {
      final long id = entity.getKey().getId();
      keysToDelete.add(KeyFactory.createKey("Comment", id));
    }
    for (final Key keyToDelete : keysToDelete) {
      datastore.delete(keyToDelete);
    }
    System.out.println("all comments delted");
    response.sendRedirect("/index.html");
  }

}