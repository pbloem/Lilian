package org.lilian.data.real;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.QRDecomposition;
import org.apache.commons.math.linear.QRDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.Global;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;

/**
 * Implementation of the EM method for PCA described in the paper 
 * "EM Algorithms for PCA" (Roweis, 1998)
 * 
 * It calculates a PCA patrix for a given datamatrix in a fast and efficient 
 * way.
 *              
 * </pre></code>
 * 
 * Based on MATLAB code available here: 
 *   {@link http://cs.nyu.edu/~roweis/code.html}
 * and here 
 *   {@link http://www.mathworks.com/matlabcentral/fileexchange/28904-em-pca}
 *   
 * 
 * @author peter
 *
 */
public class PCAIterative
{
        // * The data as N column vectors, with zero mean        
        private RealMatrix d; // * the data
        private RealMatrix x; // * result of the E step (will be the pc's)
        private RealMatrix c; // * result of the M step (y = Cx
        private RealVector mean;
        
        private int dim;
        private int num;
        private int k; 

        public PCAIterative(List<Point> data, int numComponents, int iterations)
        {
                dim = data.get(0).size(); // dimensionality of the data
                num = data.size(); // number of data points
                k = numComponents;
                
               
                mean = new ArrayRealVector(dim);
        		
        		// * Calculate the mean
        		for(Point datum : data)
        			mean = mean.add(datum.getBackingData());
        		mean.mapMultiplyToSelf(1.0/num);
                
                // * Get the matrix into a mean subtracted matrix
        		d = new Array2DRowRealMatrix(dim, num);
        		for(int i : series(num))
        		{
        			Point datum = data.get(i);
        			
        			RealVector norm = datum.getVector().subtract(mean);
        			d.setColumnVector(i, norm);
        		}
                    
                // this.x = DenseDoubleMatrix2D.factory.zeros(k, num);
                x = new Array2DRowRealMatrix(k, num);
                
                // this.c = DenseDoubleMatrix2D.factory.zeros(dim, k);
                c = new Array2DRowRealMatrix(dim, k);
                for(int row : Series.series((int)dim))
                        for(int col : Series.series((int)k))
                                c.setEntry(row, col, Global.random.nextDouble());
                
                
                for(int i : series(iterations))
                {
                	estep();
                	mstep();
                }
                finish();
        }
        
        public List<Point> simplify(int k )
        {
        	List<Point> simplified = new ArrayList<Point>(num);
        	for(int i : series(num))
        	{
        		Point p = new Point(x.getColumnVector(i));
        		if(k < p.size())
        			p = new Point(p.subList(0, k));
        		simplified.add(p);
        	}
        		
        	return simplified;
        }
        
        protected void estep()
        {
                // * Calculate cc = (c'c)^-1 * c'
                RealMatrix ct    = c.transpose(),        // ct   = c'
                       ctc   = ct.multiply(c),           // ctc  = c'c 
                       ctci  = MatrixTools.inverse(ctc), // ctci = (c'c)^-1
                       cc    = ctci.multiply(ct);        // cc = ctci * c' 
                       
                // * x = cc * data;
                x = cc.multiply(d);
        }
        
        protected void mstep()
        {
                // * Calculate xxx = x' * (xx')^-1
                RealMatrix xt    = x.transpose(),       // xt = x' 
                       xx    = x.multiply(xt),          // xx = x * x'
                       xxinv = MatrixTools.inverse(xx),  // xxinv = xx ^-1   
                       xxx   = xt.multiply(xxinv);      // xxx = xt * xxinv
                
                c = d.multiply(xxx);
        }
        
        /**
         */
        protected void finish()
        {
                // RealMatrix[] qr = c.qr();
        	
        	    QRDecomposition qr = new QRDecompositionImpl(c);
                
                c = qr.getQ();
                x = c.transpose().multiply(d);
                
//              Matrix[] svd = x.svd();
//              System.out.println(c);
//              
//              System.out.println(svd[0]);
//              System.out.println(svd[1]);             
//              System.out.println(svd[2]);     
        }
        
        
        
        public RealMatrix x()
        {
                return x;
        }
        
        public RealMatrix c()
        {
        		return c;
        }
        
        public RealMatrix data()
        {
                return d;
        }
}