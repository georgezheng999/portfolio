package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a comment node. This class is a recursive datatype, where
 * CommentNode(comment cmt, children ch) represents a comment subtree rooted at cmt, with
 * edges to the children trees represented by the elements of children.
 *
 * <p>Note: The private variables in this class are converted into JSON.
 */
class CommentNode {

  private final Comment comment;
  private final List<CommentNode> children;

  public CommentNode(Comment comment) {
    this.comment = comment;
    this.children = new ArrayList<>();
  }

  // this class is package private, which prevents representation exposure
  // defense copying is not used here for performance considerations, even though
  // CommentNode is mutable and should not be directly added to children
  public void addChildNode(CommentNode cmtNode) {
    children.add(cmtNode);
  }

}
