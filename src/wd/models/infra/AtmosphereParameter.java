package wd.models.infra;

import infra.Quantity;

/**
 * This enum enumerates quantities that define stellar atmosphere parameters.
 * 
 * @author nrowell
 */
public enum AtmosphereParameter implements Quantity<AtmosphereParameter> {
	TEFF, LOGG;
}
