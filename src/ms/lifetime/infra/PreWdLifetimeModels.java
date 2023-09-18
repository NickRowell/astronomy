package ms.lifetime.infra;

import ms.lifetime.algo.PreWdLifetime;
import ms.lifetime.algoimpl.PreWdLifetime_Hurley2000;
import ms.lifetime.algoimpl.PreWdLifetime_LPCODE;
import ms.lifetime.algoimpl.PreWdLifetime_PARSECv1p2;
import ms.lifetime.algoimpl.PreWdLifetime_PARSECv2p0;
import ms.lifetime.algoimpl.PreWdLifetime_Padova;

/**
 * Enumerated type representing the different implementations of {@link PreWdLifetime} that are available.
 *
 * @author nrowell
 * @version $Id$
 */
public enum PreWdLifetimeModels {
	
	PADOVA(new PreWdLifetime_Padova()),
	LPCODE(new PreWdLifetime_LPCODE()),
	PARSECV1p2s(new PreWdLifetime_PARSECv1p2()),
	PARSECV2p0(new PreWdLifetime_PARSECv2p0()),
	HURLEY(new PreWdLifetime_Hurley2000());
	
	private PreWdLifetime preWdLifetimeModels;
	
	/**
	 * The main constructor.
	 * @param preWdLifetimeModels
	 * 	The {@link PreWdLifetime} supplied by this source.
	 */
	PreWdLifetimeModels(PreWdLifetime preWdLifetimeModels) {
		this.preWdLifetimeModels = preWdLifetimeModels;
	}
	
	/**
	 * Resolve the {@link PreWdLifetimeModels} to the corresponding {@link PreWdLifetime}.
	 * @return
	 * 	The {@link PreWdLifetime} implementation corresponding to this {@link PreWdLifetimeModels}.
	 */
    public PreWdLifetime getPreWdLifetimeModels() {
    	return preWdLifetimeModels;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
    	return getPreWdLifetimeModels().getName();
    }
	
}