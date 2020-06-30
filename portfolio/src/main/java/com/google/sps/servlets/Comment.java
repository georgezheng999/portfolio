package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a comment
 *
 * <p>Note: The private variables in this class are converted into JSON.
 */
public class Comment {

  private final long id;
  private final String body;
  private final long timestamp;

  public Comment(long id, String body, long timestamp) {
    this.id = id;
    this.body = body;
    this.timestamp = timestamp;
  }

}
