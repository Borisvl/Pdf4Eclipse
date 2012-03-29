package de.vonloesch.pdf4eclipse.model;

public interface IOutlineNode {
	public String getLabel();
	public IOutlineNode getParent();
	public IOutlineNode[] getChildren();
	public boolean hasChildren();
	public Object getDestination();
}
