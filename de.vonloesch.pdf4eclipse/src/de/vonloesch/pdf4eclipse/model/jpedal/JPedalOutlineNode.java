package de.vonloesch.pdf4eclipse.model.jpedal;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.vonloesch.pdf4eclipse.model.IOutlineNode;

public class JPedalOutlineNode implements IOutlineNode {

	Node node;
	
	public JPedalOutlineNode(Node n) {
		this.node = n;
	}
	
	@Override
	public String getLabel() {
		Node namedItem = node.getAttributes().getNamedItem("title");
		return namedItem.getTextContent();
	}

	@Override
	public IOutlineNode getParent() {
		if (node.getParentNode() == null) return null;
		
		return new JPedalOutlineNode(node.getParentNode());
	}

	@Override
	public IOutlineNode[] getChildren() {
		NodeList l = node.getChildNodes();
		IOutlineNode[] childs = new JPedalOutlineNode[l.getLength()];
		for (int i=0; i<l.getLength(); i++) {
			childs[i] = new JPedalOutlineNode(l.item(i));
		}
		return childs;
	}

	@Override
	public boolean hasChildren() {
		return node.hasChildNodes();
	}

	@Override
	public Object getDestination() {
		// TODO Auto-generated method stub
		return null;
	}

}
