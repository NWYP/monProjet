package datastructures;

public class AVL {

	  private Node root;

	  private class Node {

	    private int key;
	    private double rank;
	    
	    private int c;
	    private int CC;
	    
	    private int balance;
	    private int height;
	    private Node left, right;

	    Node(int k) {
	      key = k;
	    }
	    Node(int k,  int _c, int lCC, double r) {
		      key = k;
		      rank = r;
		      c = _c;
		      CC = lCC;
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
		  root = insert(root,  key, w, 0);
	  }
	  public void insertWeighted(int key, int w, double rank) {
		  root = insert(root, key, w, rank);
	  }
	  public Node insert(Node r, int key, int w, double rank) {
		  if (r == null) return new Node(key, w, 0, rank);  

		  if(r.rank > rank || (r.rank == rank && r.key > key)) { 	// ajouter key à gauche
			  
			  r.CC = r.CC + w;										// mettre à jour GC

			  r.left = insert(r.left,  key, w, rank);
		  }
		  else if(r.rank < rank || (r.rank == rank && r.key < key)){ // ajouter key à droite
			  r.right = insert(r.right,  key, w, rank);			 
		  }
		  else return r;
		  
		  return rebalance(r);
	  }


	  public boolean delete(int key, double rank) {
		  Node delNode = searchHelper(root, key, rank);
		  if(delNode != null) {	
			  //System.out.println(" to remove "+  delNode.getKey());
			  root = delete(root, delNode);
			  return true;
		  }
		  return false;
	  }
	  
	  public Node delete(Node r, Node node_del) {
		  if (r == null)  return null; // Element not found
	      if (r.rank < node_del.rank || (r.rank == node_del.rank && r.key < node_del.key)) {	    	  
	    	  r.right = delete(r.right, node_del);
	    	  
	      }
	      else if (r.rank > node_del.rank || (r.rank == node_del.rank && r.key > node_del.key)) {
	    	  r.left = delete(r.left, node_del);
	    	  r.CC = r.CC - node_del.c;
	      }
	      else {
	    	  if (r.left == null && r.right == null)	r = null;
	    	  else if(r.left == null) r = r.right;
	    	  else if(r.right == null) {
	    		  //Node temp = r;
	              r = r.left;
	              //temp = null;
	    	  }
	    	  else {
	              Node child = findMin(r.right);
	    	      r.key = child.key;	r.c = child.c;	r.rank = child.rank;
	              r.right = delete(r.right, child);
	    	  }
	      }

	      if (r == null)  return null;
		  return rebalance(r);
	  }
	  

	  private Node rebalance(Node n) {
	    setBalance(n);

	    if (height(n.right) - height(n.left)  == -2) {
	      if (height(n.left.left) >= height(n.left.right)) 
	    	  n = rotateRight(n);
	      else 
	    	  n = rotateLeftThenRight(n);

	    } else if (n.balance == 2) {
	      if (height(n.right.right) >= height(n.right.left)) 
	    	  n = rotateLeft(n);
	      else 
	    	  n = rotateRightThenLeft(n);
	    }
	    return n;
	  }

	  private Node rotateLeft(Node a) {

	    Node b = a.right;  // b - x   || a -- y

	    // update CC
	    b.CC += a.c + a.CC;
	    a.right = b.left;
	    b.left = a;

	    setBalance(a, b);

	    return b;
	  }

	  private Node rotateRight(Node a) {

	    Node b = a.left; // a -- Y, b -- x
	    
	    // update CC
	    a.CC -= (b.CC + b.c);

	    a.left = b.right;


	    b.right = a;


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
			  System.out.print("key : "+ root.key+ " \t poids : ["+ root.CC+","+(root.CC+root.c)+"] \t rank : "+ root.rank+" key "+ root.key);
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
	  
	  public Node findMin(Node t) {
		    if (t == null)              return null;
		    else if (t.left == null)    return t;
		    else                        return findMin(t.left);
		}
}