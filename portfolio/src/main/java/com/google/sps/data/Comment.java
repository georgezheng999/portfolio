package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a comment. This class is immutable and threadsafe.
 *
 * <p>Note: The private variables in this class are converted into JSON.
 */
public class Comment {

  private final ROOT_COMMENT_ID = 0;
  private final long id;
  private final String text;
  private final long createdAt;
  private final String email;
  private final long parent;

  public Comment(long id, String text, long createdAt, String email, long parent, long root) {
    this.id = id;
    this.text = text;
    this.createdAt = createdAt;
    this.email = email;
    this.parent = parent;
    this.root = root;
  }

  public Comment(Entity entity) {
    final long this.id = entity.getKey().getId();
    final String this.text = (String) entity.getProperty("text");
    final String this.email = (String) entity.getProperty("creatorEmail");
    final long this.createdAt = (long) entity.getProperty("createdAt");
    final long this.parent = (long) entity.getProperty("parent");
    final long this.root = (long) entity.getProperty("root");
  }

  //This constructor is associated with the creation of a dummy, root level comment (a supernode for the comment tree)
  public Comment() {
    this.id = ROOT_COMMENT_ID;
    this.text = "Welcome to commenting! ";
    this.createdAt = 0;
    this.email = "";
    this.parent = 0;
    this.root = 0;
  }

  public long getParent() {
    return this.parent;
  }  

  public long getId() {
    return this.id;
  }  

}
