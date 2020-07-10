// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.data.Comment;
import com.google.sps.data.CommentNode;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/comments")
public class DataServlet extends HttpServlet {

  private final Gson GSON_OBJECT = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String userCommentLimit = request.getParameter("comment-limit");
    final int commentLimit = Math.max(0, Integer.parseInt(userCommentLimit)); //handle negative input
    final CommentNode comments = getComments(commentLimit);
    response.setContentType("application/json");
    final String json = GSON_OBJECT.toJson(comments);
    response.getWriter().println(json);
  }

  // Retrieves all desired comments in datastore
  // assumes acyclic invariant of comments: assumes that an ancestor path exists from A to B if and only if A was B was created before A. 
  // furthermore, assumes that the graph of vertices being comments and edges being a parent-child relation is a directed acyclic graph. 
  // does not rely on comments having a tree structure (connected graph with exactly n vertices and n-1 edges), but that should be the case!
  private CommentNode getComments(int commentLimit) {
    Query query = new Query("Comment")
                    .setFilter(FilterOperator.EQUAL.of("parent",0))
                      .addSort("createdAt", SortDirection.ASCENDING); 
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    final List<Long> rootCommentIds = new ArrayList<>();
    final List<Comment> allComments = new ArrayList<>();
    final Map<Long, Comment> idToComment = new HashMap<>();
    final Comment dummy = new Comment();//supernode dummy comment
    idToComment.put(dummy.getId(), dummy);
    for (final Entity entity : results.asIterable(FetchOptions.Builder.withLimit(commentLimit))) {
      final long id = entity.getKey().getId();
      final Comment comment = new Comment(entity);
      idToComment.put(id, comment);
      rootCommentIds.add(id);
      allComments.add(comment);
    }
    final Set<Long> hashedRootCommentIds = new HashSet<>(rootCommentIds);
    if (!rootCommentIds.isEmpty()) {
      query = new Query("Comment")
                .setFilter(new FilterPredicate("root", FilterOperator.IN, hashedRootCommentIds))
                  .addSort("createdAt", SortDirection.ASCENDING);
      results = datastore.prepare(query);
      for (final Entity entity : results.asIterable()) {
        final long id = entity.getKey().getId();
        final Comment comment = new Comment(entity);
        idToComment.put(id, comment);
        allComments.add(comment);
      }
    }
    final Map<Comment, List<Comment>> childrenMapping = getChildrenMapping(allComments, idToComment);
    childrenMapping.putIfAbsent(dummy, new ArrayList<>());
    final CommentNode dummyRoot = createTree(dummy, childrenMapping);
    return dummyRoot;
  }

  // canonical DFS tree traversal, done in linear time and space 
  private CommentNode createTree(Comment cmt, Map<Comment, List<Comment>> childrenMapping) {
    final CommentNode root = new CommentNode(cmt);
    System.out.println(childrenMapping);
    for (final Comment childComment : childrenMapping.get(cmt)) { 
      root.addChildNode(createTree(childComment, childrenMapping));
    }
    return root;
  }

  // precondition: linearComments has exactly every single desired comment once, and there exists a bijection
  // to the key value pairs of idToComment
  // returns a map whose keys are exactly the set of all comments passed in linearComments, and where
  // map[cmt] is a list of comments whose parent is cmt.
  private Map<Comment, List<Comment>> getChildrenMapping(List<Comment> linearComments, Map<Long, Comment> idToComment) {
    final Map<Comment, List<Comment>> associations = new HashMap<>();
    for (final Comment cmt : linearComments) {
      final Comment parentComment = idToComment.get(cmt.getParent());
      associations.putIfAbsent(parentComment, new ArrayList<>());
      associations.get(parentComment).add(cmt);
    }
    return associations;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html"); 
      return;
    }
    final String email = userService.getCurrentUser().getEmail();
    final String comment = request.getParameter("comment");
    final long createdAt = System.currentTimeMillis();
    final long parent = Long.parseLong(request.getParameter("parent"));
    final long root = Long.parseLong(request.getParameter("root"));
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("text", comment);
    commentEntity.setProperty("createdAt", createdAt);
    commentEntity.setProperty("creatorEmail", email);
    commentEntity.setProperty("parent", parent);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    response.sendRedirect("/index.html");
  }

}
