package de.vonloesch.pdf4eclipse.model.jpedal;

import org.jpedal.PdfDecoder;
import org.jpedal.objects.raw.PdfObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.vonloesch.pdf4eclipse.model.IOutlineNode;
import de.vonloesch.pdf4eclipse.model.IPDFDestination;

public class JPedalOutlineNode implements IOutlineNode {

	Node node;
	PdfDecoder decoder;
	
	public JPedalOutlineNode(Node n, PdfDecoder dec) {
		this.node = n;
		this.decoder = dec;
	}
	
	@Override
	public String getLabel() {
		Node namedItem = node.getAttributes().getNamedItem("title");
		return namedItem.getTextContent();
	}

	@Override
	public IOutlineNode getParent() {
		if (node.getParentNode() == null) return null;
		
		return new JPedalOutlineNode(node.getParentNode(), decoder);
	}

	@Override
	public IOutlineNode[] getChildren() {
		NodeList l = node.getChildNodes();
		IOutlineNode[] childs = new JPedalOutlineNode[l.getLength()];
		for (int i=0; i<l.getLength(); i++) {
			childs[i] = new JPedalOutlineNode(l.item(i), decoder);
		}
		return childs;
	}

	@Override
	public boolean hasChildren() {
		return node.hasChildNodes();
	}

	@Override
	public IPDFDestination getDestination() {
		String id = node.getAttributes().getNamedItem("objectRef").getTextContent();
		PdfObject o = decoder.getOutlineData().getAobj(id);
		if (o == null) return null;
		return new JPedalPDFDestination(o);
	}

}
