package org.hara.cwseg.tree;

import java.util.ArrayList;
import java.util.List;

public class Node {
	
	public String value;
	public String valueToRoot;
	public Node parent;
	public List<Node> children;
	
	public Node() {
		value = null;
		valueToRoot = null;
		parent = null;
		children = new ArrayList<Node>();
	}
}
