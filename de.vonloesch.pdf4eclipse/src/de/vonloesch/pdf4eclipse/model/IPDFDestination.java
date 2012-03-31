package de.vonloesch.pdf4eclipse.model;

import java.awt.geom.Rectangle2D;

public interface IPDFDestination {
	IPDFPage getPage();
	
	Rectangle2D getPosition();
}
