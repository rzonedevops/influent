/**
 * Copyright (c) 2013 Oculus Info Inc.
 * http://www.oculusinfo.com/
 *
 * Released under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package influent.bitcoin.server.clustering;

import influent.entity.clustering.GeneralEntityInstanceFactory;
import influent.idl.FL_Entity;
import influent.idl.FL_Geocoding;
import influent.idl.FL_PropertyTag;
import influent.idlhelper.EntityHelper;
import influent.idlhelper.PropertyHelper;
import influent.midtier.IdGenerator;
import java.util.LinkedList;
import java.util.List;

import com.oculusinfo.ml.Instance;
import com.oculusinfo.ml.feature.BagOfWordsFeature;
import com.oculusinfo.ml.feature.NumericVectorFeature;

public class BitCoinEntityInstanceFactory extends GeneralEntityInstanceFactory {

	public BitCoinEntityInstanceFactory(IdGenerator idGen, FL_Geocoding geocoding) {
		super(idGen, geocoding);
	}
	
	@Override
	public Instance toInstance(FL_Entity entity) {
		Instance inst = new Instance( entity.getUid() );
		
		PropertyHelper labelProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.LABEL);
		PropertyHelper userTagProp = EntityHelper.getFirstProperty(entity, "UserTag");
		
		String label = userTagProp != null ? (String)userTagProp.getValue() : (String)labelProp.getValue();
				
		// normalized tokenized string feature
		BagOfWordsFeature tokens = new BagOfWordsFeature("tokens", "tokens");
		List<String> tokenStrs;
		try {
			Integer.parseInt(label);
			tokenStrs = new LinkedList<String>();
			tokenStrs.add(label);
		}
		catch (Exception e) {
			tokenStrs = tokenizeString(label);
		}
		for (String token : tokenStrs) {
			tokens.incrementValue(token);
		}
		inst.addFeature(tokens);
		
		PropertyHelper avgTrnAmntProp = EntityHelper.getFirstProperty(entity, "AvgTrasactionAmount");
		
		if (avgTrnAmntProp != null) {
			long val = (Long)avgTrnAmntProp.getValue();
			
			NumericVectorFeature amount = new NumericVectorFeature("NormAvgTrasactionAmount", "NormAvgTrasactionAmount");
			amount.setValue(new double[]{val});
			inst.addFeature(amount);
			
			amount = new NumericVectorFeature("AvgTrasactionAmount", "AvgTrasactionAmount");
			amount.setValue(new double[]{val});
			inst.addFeature(amount);
		}
		
		PropertyHelper cntTrnProp = EntityHelper.getFirstProperty(entity, "CountOfTransactions");
		
		if (cntTrnProp != null) {
			long val = (Long)cntTrnProp.getValue();
			
			NumericVectorFeature count = new NumericVectorFeature("NormCountOfTransactions", "NormCountOfTransactions");
			count.setValue(new double[]{val});
			inst.addFeature(count);
			
			count = new NumericVectorFeature("CountOfTransactions", "CountOfTransactions");
			count.setValue(new double[]{val});
			inst.addFeature(count);
		}
		
		PropertyHelper degreeProp = EntityHelper.getFirstProperty(entity, "Degree");
		
		if (degreeProp != null) {
			long val = (Long)degreeProp.getValue();
			
			NumericVectorFeature degree = new NumericVectorFeature("NormDegree", "NormDegree");
			degree.setValue(new double[]{val});
			inst.addFeature(degree);
			
			degree = new NumericVectorFeature("Degree", "Degree");
			degree.setValue(new double[]{val});
			inst.addFeature(degree);
		}
		
		PropertyHelper typeProp = EntityHelper.getFirstPropertyByTag(entity, FL_PropertyTag.TYPE);
		
		if (typeProp != null) {
			String type = (String)typeProp.getValue();
			// entity type feature
			BagOfWordsFeature entityType = new BagOfWordsFeature("entityType", "entityType");
			entityType.incrementValue( type );
			inst.addFeature(entityType);
		}
		
		return inst;
	}

}
