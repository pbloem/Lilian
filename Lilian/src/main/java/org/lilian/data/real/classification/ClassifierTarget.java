package org.lilian.data.real.classification;

import org.lilian.data.real.Point;
import org.lilian.search.evo.Target;

public class ClassifierTarget implements Target<Classifier>
{

	private static final long serialVersionUID = -3873330452952466860L;
	Classified<Point> testData;
	
	public ClassifierTarget(Classified<Point> testData)
	{
		this.testData = testData;
	}

	@Override
	public double score(Classifier clsr)
	{
		return - Classification.symmetricError(clsr, testData);
	}

}
