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
  private final String text;
  private final long createdAt;

  public Comment(long id, String text, long createdAt) {
    this.id = id;
    this.text = text;
    this.createdAt = createdAt;
  }

}
