package org.lilian;

public class Test
{
	// * First level
	
	public interface Container 
	{
		public Container container();
	}	
	
//	public interface Element
//	{
//		public Container container();
//	}

	// * Second level

	public interface AContainer extends Container 
	{
		public AContainer container();
	}
	public interface BContainer extends Container 
	{
		public BContainer container();
	}
//	
//	public interface AElement extends Element 
//	{
//		public AContainer container();
//	}
//	
//	public interface BElement extends Element
//	{
//		public BContainer container();
//	}
	
	// * Third level
	
	public interface SubContainer extends AContainer, BContainer
	{
		public SubContainer container();
	}
	
//	public interface SubElement extends AElement, BElement
//	{
//		public SubContainer container();
//	}
}
