package org.hara.cwseg.tree;

import org.hara.cwseg.constants.WordSegConstants;

public class WordTree {

	private Node rootTopDown;
	private Node rootBottomUp;

	public WordTree() {
		rootTopDown = null;
		rootBottomUp = null;
	}

	public Node getRootTopDown() {
		return rootTopDown;
	}

	public Node getRootBottomUp() {
		return rootBottomUp;
	}

	public void insertTopDown(String word) {
		String sylls[] = word.split(WordSegConstants.splitDelimeter);
		if (rootTopDown == null) {
			rootTopDown = new Node();
			rootTopDown.value = "ROOT_TD";
		}

		Node curr;
		String dataToRoot = "";
		for (int i = 0; i < sylls.length; i++) {
			dataToRoot = getValueToRootTopDown(sylls, i);
			curr = getNodeToInsertTopDown(dataToRoot, i);
			if (curr != null && curr.value == rootTopDown.value) {
				if (!checkContains(curr, sylls[i])) {
					Node newNode = new Node();
					newNode.value = sylls[i];
					newNode.valueToRoot = dataToRoot;
					newNode.parent = curr;
					curr.children.add(newNode);
				}
			} else if (curr != null) {
				Node newNode = new Node();
				newNode.value = sylls[i];
				newNode.valueToRoot = dataToRoot;
				newNode.parent = curr;
				curr.children.add(newNode);
			}
		}
	}

	private Node getNodeToInsertTopDown(String dataToRoot, int level) {
		if (level == 0) {
			return rootTopDown;
		}
		int checkLevel = 0;
		Node insertPosNode = rootTopDown;
		String parts[] = dataToRoot.split(WordSegConstants.splitDelimeter);
		while (checkLevel != level) {
			int noChild = insertPosNode.children.size();
			for (int i = 0; i < noChild; i++) {
				Node childNode = insertPosNode.children.get(i);
				if (childNode.value.compareTo(parts[checkLevel]) == 0) {
					if (checkLevel == level - 1) {
						boolean checkPresent = checkContains(childNode,
								parts[checkLevel + 1]);
						if (!checkPresent) {
							return childNode;
						}
					} else {
						insertPosNode = childNode;
						break;
					}
				}
			}
			checkLevel++;
		}
		return null;
	}

	public void insertBottomUp(String word) {
		String sylls[] = word.split(WordSegConstants.splitDelimeter);
		if (rootBottomUp == null) {
			rootBottomUp = new Node();
			rootBottomUp.value = "ROOT_BU";
		}

		Node curr;
		String dataToRoot = "";
		for (int i = sylls.length - 1; i >= 0; i--) {
			dataToRoot = getValueToRootBottomUp(sylls, i);
			curr = getNodeToInsertBottomUp(dataToRoot, sylls.length -1 - i);
			if (curr != null && curr.value == rootBottomUp.value) {
				if (!checkContains(curr, sylls[i])) {
					Node newNode = new Node();
					newNode.value = sylls[i];
					newNode.valueToRoot = dataToRoot;
					newNode.parent = curr;
					curr.children.add(newNode);
				}
			} else if (curr != null) {
				Node newNode = new Node();
				newNode.value = sylls[i];
				newNode.valueToRoot = dataToRoot;
				newNode.parent = curr;
				curr.children.add(newNode);
			}
		}
	}

	private Node getNodeToInsertBottomUp(String dataToRoot, int level) {
		if (level == 0) {
			return rootBottomUp;
		}
		int checkLevel = 0;
		Node insertPosNode = rootBottomUp;
		String parts[] = dataToRoot.split(WordSegConstants.splitDelimeter);
		while (checkLevel != level) {
			int noChild = insertPosNode.children.size();
			for (int i = 0; i < noChild; i++) {
				Node childNode = insertPosNode.children.get(i);
				if (childNode.value.compareTo(parts[checkLevel]) == 0) {
					if (checkLevel == level - 1) {
						boolean checkPresent = checkContains(childNode,
								parts[checkLevel + 1]);
						if (!checkPresent) {
							return childNode;
						}
					} else {
						insertPosNode = childNode;
						break;
					}
				}
			}
			checkLevel++;
		}
		return null;
	}

	private String getValueToRootBottomUp(String[] sylls, int prevIndex) {
		String dataToRoot = "";
		for (int i = sylls.length - 1; i >= prevIndex; i--) {
			dataToRoot += sylls[i];
			if (i > prevIndex) {
				dataToRoot += WordSegConstants.addDelimeter;
			}
		}
		return dataToRoot;
	}

	private String getValueToRootTopDown(String[] sylls, int lastIndex) {
		String dataToRoot = "";
		for (int i = 0; i <= lastIndex; i++) {
			dataToRoot += sylls[i];
			if (i < lastIndex) {
				dataToRoot += WordSegConstants.addDelimeter;
			}
		}
		return dataToRoot;
	}

	private boolean checkContains(Node node, String data) {
		for (int i = 0; i < node.children.size(); i++) {
			if (node.children.get(i).value.compareTo(data) == 0) {
				return true;
			}
		}
		return false;
	}

	public void printTreeRec(Node printNode) {
		for (int i = 0; i < printNode.children.size(); i++) {
			Node childNode = printNode.children.get(i);
			printTreeRec(childNode);
			System.out.println("node value=" + childNode.value);
			System.out.println("node value to root=" + childNode.valueToRoot);
		}
	}
}
