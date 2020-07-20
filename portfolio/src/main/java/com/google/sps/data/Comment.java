package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;
import com.google.appengine.api.datastore.Entity;

/**
 * Class representing a comment. This class is immutable and threadsafe.
 *
 * <p>Note: The private variables in this class are converted into JSON.
 */
public class Comment {

  private final long DUMMY_COMMENT_ID = 0;
  private final long id;
  private final String text;
  private final long createdAt;
  private final String email;
  private final long parent;
  private final long root;

  public Comment(long id, String text, long createdAt, String email, long parent, long root) {
    this.id = id;
    this.text = text;
    this.createdAt = createdAt;
    this.email = email;
    this.parent = parent;
    this.root = root;
  }

  //This constructor is associated with the creation of a dummy, root level comment (a supernode for the comment tree)
  public Comment(Entity entity) {
    long id = entity.getKey().getId();
    String text = (String) entity.getProperty("text");
    String email = (String) entity.getProperty("creatorEmail");
    long createdAt = (long) entity.getProperty("createdAt");
    long parent = (long) entity.getProperty("parent");
    long root = (long) entity.getProperty("root");
    this(id, text, createdAt, email, parent, root);
  }

  public long getParent() {
    return this.parent;
  }  

  public long getId() {
    return this.id;
  }  

}
