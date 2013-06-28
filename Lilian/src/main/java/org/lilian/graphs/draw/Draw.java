package org.lilian.graphs.draw;

import java.awt.image.BufferedImage;

import org.lilian.data.real.Point;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Link;
import org.lilian.graphs.Node;
import org.lilian.util.BufferedImageTranscoder;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;


public class Draw
{
	private static final String ns = SVGDOMImplementation.SVG_NAMESPACE_URI;
	
	public static <L> BufferedImage draw(Graph<L> graph, int width, int height)
	{
		return draw(graph, new CircleLayout<L>(graph), width, height);
	
	}
	
	public static <L> BufferedImage draw(Graph<L> graph, Layout<L> layout, int width, int height)
	{
		Document svg = svg(graph, layout);
		
		BufferedImageTranscoder t = new BufferedImageTranscoder();
		
	    t.addTranscodingHint(PNGTranscoder.KEY_WIDTH,  (float) width);
	    t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
	    
	   // t.addTranscodingHint(PNGTranscoder.KEY_,  (float) width);

	
	    TranscoderInput input = new TranscoderInput(svg);
	    try
		{
			t.transcode(input, null);
		} catch (TranscoderException e)
		{
			throw new RuntimeException(e);
		}
	 
	    return t.getBufferedImage();
	}
	
	public static <L> Document svg(Graph<L> graph)
	{
		return svg(graph, new CircleLayout<L>(graph));
	}	
	
	public static <L> Document svg(Graph<L> graph, Layout<L> layout)
	{
		// * Set up an SVG generator
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		Document doc = impl.createDocument(ns, "svg", null);

		Element canvas = doc.getDocumentElement();
		canvas.setAttributeNS(null, "width", "2.0");
		canvas.setAttributeNS(null, "height", "2.0");
		
		Element g = doc.createElementNS(ns, "g");
		g.setAttributeNS(null, "transform", "translate(1,1)");
		canvas.appendChild(g);
				
		// * Draw the edges
		for(Link<L> link : graph.links())
		{
			Point a = layout.point(link.first()),
			      b = layout.point(link.second());
			
			
			Element line = doc.createElementNS(ns, "line");
			line.setAttributeNS(null, "x1", "" + a.get(0));
			line.setAttributeNS(null, "y1", "" + a.get(1));
			line.setAttributeNS(null, "x2", "" + b.get(0));
			line.setAttributeNS(null, "y2", "" + b.get(1));

			line.setAttributeNS(null, "stroke", "black");
			line.setAttributeNS(null, "stroke-opacity", "0.1");
			line.setAttributeNS(null, "stroke-width", "0.005");

			g.appendChild(line);
		}	
		
		// * Draw the nodes
		for(Node<L>  node : graph.nodes())
		{
			Point p = layout.point(node);
			
			Element circle = doc.createElementNS(ns, "circle");
			circle.setAttributeNS(null, "cx", "" + p.get(0));
			circle.setAttributeNS(null, "cy", "" + p.get(1));
			circle.setAttributeNS(null, "r", "0.01");
			circle.setAttributeNS(null, "fill", "red");

			g.appendChild(circle);
		}
		
	
		
		return doc;
	}
	
	public static <L> void show(Graph<L> graph)
	{
		
	}

}
