package org.lilian;

public class Test
{

	
	public interface Container 
	{
		public Element element();
	}

	public interface BottomContainer1 extends Container 
	{
		public BottomElement1 element();
	}
	public interface BottomContainer2 extends Container 
	{
		public BottomElement2 element();
	}

	public interface SubContainer extends BottomContainer1, BottomContainer2
	{
		public SubElement element();
	}
	
	public interface Element
	{
		public Container container();
	}

	public interface BottomElement1 extends Element 
	{
		public BottomContainer1 container();
	}
	public interface BottomElement2 extends Element
	{
		public BottomContainer2 container();
	}
	
	public interface SubElement extends BottomElement1, BottomElement2
	{
		public SubContainer container();
	}
}
