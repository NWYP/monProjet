package datastructures;

public class AVL2 {

	  private Node root;

	  private class Node {

	    private int key;
	    private double rank;
	    
	    private int c;
	    private int CC;
	    
	    private int balance;
	    private int height;
	    private Node left, right, parent;

	    Node(int k, Node p) {
	      key = k;
	      parent = p;
	    }
	    Node(int k, Node p, int _c, int lCC, double r) {
		      key = k;
		      rank = r;
		      c = _c;
		      CC = lCC;
		      parent = p;
		      left = null;
		      right = null;
		}
	    
	    public int getKey() {
	    	return key;
	    }
	    public int getValue() {
	    	return key;
	    }
	  }
	  
	  public void insertWeighted(int key, int w) {
		  insert(root, null, key, w, 0);
	  }
	  public void insertWeighted(int key, int w, double rank) {
		  insert(root, null, key, w, rank);
	  }
	  public Node insert(Node r, Node p, int key, int w, double rank) {
		  if (root == null) {
			  root = new Node(key, p, w, 0, rank);
			  return root;
		  }

		  if(r.rank > rank || (r.rank == rank && r.key > key)) { // ajouter key � gauche
			  // mettre � jour GC
			  r.CC = r.CC + w;
			  // inserer key � gauche
			  if (r.left == null) {
				  r.left = new Node(key, r, w, 0, rank);
				  rebalance(r);
			  }
			  else
				  insert(r.left, r, key, w, rank);
		  }
		  else { 				// ajouter key � droite
			  if (r.right == null) {
				  r.right = new Node(key, r, w, 0, rank);
				  rebalance(r);
			  }
			  else {
				  insert(r.right, r, key, w, rank);
			  }
		  }
		  return r;
	  }


	  private void delete(Node node) {
	    if (node.left == null && node.right == null) {
	      if (node.parent == null) root = null;
	      else {
	        Node parent = node.parent;
	        if (parent.left == node) parent.left = null;
	        else parent.right = null;
	        rebalance(parent);
	      }
	      return;
	    }
	    if (node.left != null) {
	      Node child = node.left;
	      while (child.right != null) child = child.right;
	      node.key = child.key;	node.c = child.c;
	      node.CC = child.CC;	node.rank = child.rank;
	      delete(child);
	    } else {
	      Node child = node.right;
	      while (child.left != null) child = child.left;
	      node.key = child.key;	node.c = child.c;
	      node.CC = child.CC; 	node.rank = child.rank;
	      delete(child);
	    }
	  }

	  public boolean delete(int delKey, double rank) {
		Node delNode = searchHelper(root, delKey, rank);
	    if (delNode == null) return false;
	    Node node = root;
	    Node child = root;
	    
	    while (child != null) {
	      node = child;
	      if(node.rank > rank || (node.rank == rank && node.key > delKey)) {
	    	  child = node.left;
	    	  node.CC = node.CC - delNode.c;
	      }
	      else{
	    	  child = node.right;
	      }
	    
	      if (node.rank == rank && delKey == node.key) {
	        delete(node);
	        return true;
	      }
	    }
	    return false;
	  }

	  private void rebalance(Node n) {
	    setBalance(n);

	    if (n.balance == -2) {
	      if (height(n.left.left) >= height(n.left.right)) n = rotateRight(n);
	      else n = rotateLeftThenRight(n);

	    } else if (n.balance == 2) {
	      if (height(n.right.right) >= height(n.right.left)) n = rotateLeft(n);
	      else n = rotateRightThenLeft(n);
	    }

	    if (n.parent != null) rebalance(n.parent);
	    else				  root = n;
	    
	  }

	  private Node rotateLeft(Node a) {

	    Node b = a.right;  // b - x   || a -- y

	    // update CC
	    b.CC += a.c + a.CC;
	    
	    b.parent = a.parent; 
	    a.right = b.left;
	    
	    
	    if (a.right != null) a.right.parent = a;

	    b.left = a;
	    a.parent = b;

	    if (b.parent != null) {
	      if (b.parent.right == a) {
	        b.parent.right = b;
	      } else {
	        b.parent.left = b;
	      }
	    }

	    setBalance(a, b);

	    return b;
	  }

	  private Node rotateRight(Node a) {

	    Node b = a.left; // a -- Y, b -- x
	    
	    // update CC
	    a.CC -= (b.CC + b.c);
	    
	    b.parent = a.parent;
	    a.left = b.right;

	    if (a.left != null) a.left.parent = a;

	    b.right = a;
	    a.parent = b;

	    if (b.parent != null) {
	      if (b.parent.right == a) {
	        b.parent.right = b;
	      } else {
	        b.parent.left = b;
	      }
	    }

	    setBalance(a, b);

	    return b;
	  }

	  private Node rotateLeftThenRight(Node n) {
	    n.left = rotateLeft(n.left);
	    return rotateRight(n);
	  }

	  private Node rotateRightThenLeft(Node n) {
	    n.right = rotateRight(n.right);
	    return rotateLeft(n);
	  }

	  private int height(Node n) {
	    if (n == null) return -1;
	    return n.height;
	  }

	  private void setBalance(Node... nodes) {
	    for (Node n : nodes) {
	      reheight(n);
	      n.balance = height(n.right) - height(n.left);
	    }
	  }

	  private void reheight(Node node) {
		  if (node != null) {
			  node.height = 1 + Math.max(height(node.left), height(node.right));
		  }
	  }	  
	  public void printBalance() {
		  printBalance(root);
	  }

	  private void printBalance(Node n) {
	    if (n != null) {
	      printBalance(n.left);
	      System.out.printf("%s ", n.balance);
	      printBalance(n.right);
	    }
	  }


	  
	  public void printPreOrder() {
		  preOrder(root);
	  }
	  public void preOrder(Node root) {
		  if (root != null) {
			  preOrder(root.left);
			  System.out.print("key : "+ root.key+ " poids : "+ root.CC+" rank : "+ root.rank+" parent : " );
			  if (root.parent != null) System.out.print(root.parent.key);
			  System.out.println();
			  preOrder(root.right);
		  }
	  }
		
	  /*
	   * Find the mth element of the tree
	   */
	  public int searchMth(int m) {
		    Node result = searchHelperMth(this.root, m);
		    if (result != null) return result.key;

		    return -1;
	  }
	  private Node searchHelperMth(Node root, int m) {
		    // root is null or key is present at root
		    if (root == null) return root;
		    
		    if (root.CC < m && m  <= root.CC + root.c) return root;
		    
		    // Mth element is in the right subtree
		    if (root.CC + root.c < m)
		    	return searchHelperMth(root.right, m - root.CC - root.c); // call the function on the node's left child

		    // Mth element is in the right subtree
		    return searchHelperMth(root.left, m);
	  }
	  
	  public boolean search(int key, double rank) {
	    Node result = searchHelper(this.root, key, rank);
	    if (result != null) return true;

	    return false;
	  }

	  private Node searchHelper(Node root, int key, double rank) {
	    if (root == null || (root.key == key && root.rank == rank)) return root;
	    if ((root.rank > rank) || (root.rank == rank && root.key > key)) return searchHelper(root.left, key, rank); 
	    return searchHelper(root.right, key, rank);
	  }
	  
	  public boolean isEmpty() {
		  return root == null;
	  }
	  
}