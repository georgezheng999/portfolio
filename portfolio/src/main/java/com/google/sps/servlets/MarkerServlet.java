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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.sps.data.Marker;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handles fetching and saving markers data. */
@WebServlet("/markers")
public class MarkerServlet extends HttpServlet {

  private final Gson GSON_OBJECT = new Gson();

  /** Responds with a JSON array containing marker data. */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    final List<Marker> markers = getMarkers();
    final String json = GSON_OBJECT.toJson(markers);
    response.getWriter().println(json);
  }

  /** Accepts a POST request containing a new marker. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    final double lat = Double.parseDouble(request.getParameter("lat"));
    final double lng = Double.parseDouble(request.getParameter("lng"));
    final UserService userService = UserServiceFactory.getUserService();
    final String content = userService.getCurrentUser().getEmail();
    final Marker marker = new Marker(lat, lng, content);
    storeMarker(marker);
  }

  /** Fetches markers from Datastore. */
  private List<Marker> getMarkers() {
    final List<Marker> markers = new ArrayList<>();
    final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    final Query query = new Query("Marker");
    final PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      final double lat = (double) entity.getProperty("lat");
      final double lng = (double) entity.getProperty("lng");
      final String content = (String) entity.getProperty("content");
      final Marker marker = new Marker(lat, lng, content);
      markers.add(marker);
    }
    return markers;
  }

  /** Stores a marker in Datastore. */
  public void storeMarker(Marker marker) {
    final Entity markerEntity = new Entity("Marker");
    markerEntity.setProperty("lat", marker.getLat());
    markerEntity.setProperty("lng", marker.getLng());
    markerEntity.setProperty("content", marker.getContent());
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(markerEntity);
  }
}
