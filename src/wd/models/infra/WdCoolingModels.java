package wd.models.infra;

import wd.models.algo.WdCoolingModelSet;
import wd.models.algoimpl.WdCoolingModelSet_BaSTI;
import wd.models.algoimpl.WdCoolingModelSet_LPCODE;
import wd.models.algoimpl.WdCoolingModelSet_Montreal;
import wd.models.algoimpl.WdCoolingModelSet_Montreal_2020;
import wd.models.algoimpl.WdCoolingModelSet_Montreal_2020_Evolutionary;

/**
 * Enumerated type to represent the available implementations of {@link WdCoolingModelSet}.
 *
 * @author nrowell
 * @version $Id$
 */
public enum WdCoolingModels {
	
	MONTREAL(new WdCoolingModelSet_Montreal()),
	MONTREAL_NEW_2020(new WdCoolingModelSet_Montreal_2020()),
	MONTREAL_EVOL(new WdCoolingModelSet_Montreal_2020_Evolutionary()),
    BASTI_PS(new WdCoolingModelSet_BaSTI(true)), 
    BASTI_NO_PS(new WdCoolingModelSet_BaSTI(false)),
    RENEDO(new WdCoolingModelSet_LPCODE());
    
	private WdCoolingModelSet wdCoolingModelSet;
	
	/**
	 * The main constructor.
	 * @param wdCoolingModelsBase
	 * 	The cooling models supplied by this source.
	 */
	WdCoolingModels(WdCoolingModelSet wdCoolingModelsBase) {
		this.wdCoolingModelSet = wdCoolingModelsBase;
	}
	
	/**
	 * Resolve the {@link WdCoolingModels} to the corresponding {@link BaseWdCoolingModels}.
	 * @return
	 * 	The {@link BaseWdCoolingModels} implementation corresponding to this {@link WdCoolingModels}.
	 */
    public WdCoolingModelSet getWdCoolingModels() {
    	return wdCoolingModelSet;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
    	return getWdCoolingModels().toString();
    }
}
