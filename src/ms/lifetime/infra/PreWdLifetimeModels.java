package ms.lifetime.infra;

import ms.lifetime.algo.PreWdLifetime;
import ms.lifetime.algoimpl.PreWdLifetime_LPCODE;
import ms.lifetime.algoimpl.PreWdLifetime_Padova;

/**
 * 
 *
 *
 * @author nrowell
 * @version $Id$
 */
public enum PreWdLifetimeModels {
	
	PADOVA(new PreWdLifetime_Padova()),
	LPCODE(new PreWdLifetime_LPCODE());
	
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
    	return getPreWdLifetimeModels().toString();
    }
	
}