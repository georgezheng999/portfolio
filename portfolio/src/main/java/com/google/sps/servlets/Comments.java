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
  private final List<String> history = new ArrayList<>();


  /** Adds a given comment */
  public void addComment(String comment) {
    history.add(comment);
  }

}
