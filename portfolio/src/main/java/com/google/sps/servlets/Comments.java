package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a collection of comments
 *
 * <p>Note: The private variables in this class are converted into JSON.
 */
public class Comments {

  /** List of comments */
  private final List<String> comments = new ArrayList<>();


  /** Adds the player's turn */
  public void addComment(String comment) {
    comments.add(comment);
  }

}
