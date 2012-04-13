package org.lilian.experiment;

import freemarker.template.Template;

/** 
 * An object that can output its own complex report, in the form of a data model
 * and a template
 * 
 * @author Peter
 *
 */
public interface Reporting
{
	public String name();
	public String description();
	
	public Template template();
	public Object data();
}
