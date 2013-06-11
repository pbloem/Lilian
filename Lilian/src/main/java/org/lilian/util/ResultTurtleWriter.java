package org.lilian.util;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import org.lilian.Global;
import org.lilian.experiment.AbstractExperiment;
import org.lilian.experiment.Result;
import org.lilian.experiment.Results;
import org.lilian.experiment.Tools;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;

public class ResultTurtleWriter {

	Writer writer = null;
	AbstractExperiment exp = null;
	Repository tempRepository;
	RepositoryConnection conn;
	String experimentID;

	public ResultTurtleWriter(AbstractExperiment e, Writer w) {
		writer = w;
		exp = e;
		tempRepository = new SailRepository(new MemoryStore());
		experimentID = UUID.randomUUID().toString();
		try {
			tempRepository.initialize();
			conn = tempRepository.getConnection();
		} catch (RepositoryException e1) {
			e1.printStackTrace();
		}
	}

	public void writeExperiment() {

		for (Method method : Tools.allMethods(exp.getClass()))
			for (Annotation anno : method.getAnnotations()) {
				if (anno instanceof Result) {
					Object value = invoke(method);
					Result curAnnotation = (Result) anno;
					if (value instanceof Results) 
					{
						Results results = (Results) value;

						for (int i : Series.series(results.size()))
							addResult(experimentID, results.value(i),
									results.annotation(i));

					} else
						addResult(experimentID, value, curAnnotation);

				}

			}

		RDFWriter rdfWriter = Rio.createWriter(RDFFormat.N3, writer);
		try {
			conn.export(rdfWriter);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addResult(String experimentID, Object value, Result anno) {
		ValueFactory f = conn.getValueFactory();
		Resource exp = f.createURI("http://data2semantics.org/experiments/"
				+ experimentID);
		URI pred = f
				.createURI("http://data2semantics.org/experiments/resultType/"
						+ anno.name());
		Literal val = null;
		if (value instanceof Double)
			val = f.createLiteral((Double) value);
		else if (value instanceof Float)
			val = f.createLiteral((Float) value);
		else if (value instanceof Integer)
			val = f.createLiteral((Integer) value);
		else {
			if (value == null)
				val = f.createLiteral("");
			else
				val = f.createLiteral(value.toString());
		}
		try {
			conn.add(exp, pred, val);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

	}

	private Object invoke(Method method) {
		try {
			return method.invoke(exp);
		} catch (InvocationTargetException e) {
			Global.log().warning(
					"Failed to invoke result method " + method
							+ " on experiment " + this + ". Exception: "
							+ e.getMessage() + " -  "
							+ Arrays.toString(e.getStackTrace()));
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			Global.log().warning(
					"Failed to invoke result method " + method
							+ " on experiment " + this + ". Exception: "
							+ e.getMessage() + " -  "
							+ Arrays.toString(e.getStackTrace()));
			throw new RuntimeException(e);

		} catch (IllegalAccessException e) {
			Global.log().warning(
					"Failed to invoke result method " + method
							+ " on experiment " + this + ". Exception: "
							+ e.getMessage() + " -  "
							+ Arrays.toString(e.getStackTrace()));
			throw new RuntimeException(e);
		}
	}

}
