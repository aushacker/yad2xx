/*
 * Copyright 2016-2018 Stephen Davies
 * 
 * This file is part of yad2xx.
 * 
 * yad2xx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * yad2xx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with yad2xx.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.yad2xx.jtag;

import java.util.HashMap;
import java.util.Map;

/**
 * IEEE 1149.1 Test Access Port (TAP) states. Creates a bunch of hardcoded
 * data to allow the TAP statemachine to be navigated under software control.
 * <p>
 * Each TapState can transition to one of two next states depending on the
 * value of the TMS pin. This creates a small, directed graph of TapStates.
 *
 * @author		Stephen Davies
 * @since		14 April 2016
 * @since		0.4
 */
public enum TapState {

	RESET(true),
	IDLE(true),
	DRSELECT(false),
	DRCAPTURE(false),
	DRSHIFT(false),
	DREXIT1(false),
	DRPAUSE(true),
	DREXIT2(false),
	DRUPDATE(false),
	IRSELECT(false),
	IRCAPTURE(false),
	IRSHIFT(false),
	IREXIT1(false),
	IRPAUSE(true),
	IREXIT2(false),
	IRUPDATE(false);

	/*
	 * ===== SVF Identifies a subset of TAP states as being stable ============
	 */
	
	private TapState(boolean stable) {
		this.stable = stable;
	}
	
	private final boolean stable;
	
	public boolean isStable() {
		return stable;
	}

	/*
	 * ================= IEEE 1149.1 State Machine ============================
	 */
	
	/**
	 * Next state when TMS = 0.
	 */
	private TapState zero;

	/**
	 * Next state when TMS = 1.
	 */
	private TapState one;
	
	private void setNextStates(TapState zero, TapState one) {
		this.zero = zero;
		this.one = one;
	}
	
	/**
	 * Encode state transition graph from IEEE 1194.1 in individual states.
	 */
	static {
		RESET.setNextStates(IDLE, RESET);
		IDLE.setNextStates(IDLE, DRSELECT);
		DRSELECT.setNextStates(DRCAPTURE, IRSELECT);
		DRCAPTURE.setNextStates(DRSHIFT, DREXIT1);
		DRSHIFT.setNextStates(DRSHIFT, DREXIT1);
		DREXIT1.setNextStates(DRPAUSE, DRUPDATE);
		DRPAUSE.setNextStates(DRPAUSE, DREXIT2);
		DREXIT2.setNextStates(DRSHIFT, DRUPDATE);
		DRUPDATE.setNextStates(IDLE, DRSELECT);
		IRSELECT.setNextStates(IRCAPTURE, RESET);
		IRCAPTURE.setNextStates(IRSHIFT, IREXIT1);
		IRSHIFT.setNextStates(IRSHIFT, IREXIT1);
		IREXIT1.setNextStates(IRPAUSE, IRUPDATE);
		IRPAUSE.setNextStates(IRPAUSE, IREXIT2);
		IREXIT2.setNextStates(IRSHIFT, IRUPDATE);
		IRUPDATE.setNextStates(IDLE, DRSELECT);
	}
	
	/**
	 * Tests if this state has next state as one of its two possible
	 * transitions.
	 * 
	 * @param	next			desired next state
	 * @return					true if next state is this states immediate
	 * 							successor
	 */
	public boolean hasTransitionTo(TapState next) {
		return zero == next || one == next;
	}

	/**
	 * Returns next state based on tms value.
	 * 
	 * @param	tms				boolean value
	 * @return					next state based on tms value
	 */
	public TapState getNext(boolean tms) {
		return tms ? one : zero;
	}
	
	/**
	 * Returns the TMS value to transition to the next state.
	 * 
	 * @param	next			desired next state
	 * @return					TMS value, 0 or 1, to get to the next TAP state.
	 */
	public int getTransition(TapState next) {
		if (zero == next) {
			return 0;
		} else if (one == next) {
			return 1;
		} else {
			throw new IllegalStateException("Current state: " + this.name() +
					" cannot navigate to: " + next.name());
		}
	}
	
	/*
	 * ===== SVF defines navigation paths between stable states ===============
	 */

	/**
	 * Nested Maps, top level is keyed by the from TapState. The inner Map is
	 * keyed by the to TapState. The TapState[] specifies the path between the
	 * from and to states as required by SVF.
	 */
	private static Map<TapState, Map<TapState, TapState[]>> fromMap
		= new HashMap<TapState, Map<TapState, TapState[]>>();
	
	/**
	 * Encode SVF navigation paths. Taken from page 22 of the SVF specification.
	 */
	static {
		RESET.setPathTo(RESET, new TapState[] {});
		RESET.setPathTo(IDLE, new TapState[] { IDLE });
		RESET.setPathTo(DRSHIFT, new TapState[] { IDLE, DRSELECT, DRCAPTURE, DRSHIFT });
		RESET.setPathTo(DRPAUSE, new TapState[] { IDLE, DRSELECT, DRCAPTURE, DREXIT1, DRPAUSE });
		RESET.setPathTo(IRSHIFT, new TapState[] { IDLE, DRSELECT, IRSELECT, IRCAPTURE, IRSHIFT });
		RESET.setPathTo(IRPAUSE, new TapState[] { IDLE, DRSELECT, IRSELECT, IRCAPTURE, IREXIT1, IRPAUSE });

		IDLE.setPathTo(RESET, new TapState[] { DRSELECT, IRSELECT, RESET });
		IDLE.setPathTo(IDLE, new TapState[] {});
		IDLE.setPathTo(DRSHIFT, new TapState[] { DRSELECT, DRCAPTURE, DRSHIFT });
		IDLE.setPathTo(DRPAUSE, new TapState[] { DRSELECT, DRCAPTURE, DREXIT1, DRPAUSE });
		IDLE.setPathTo(IRSHIFT, new TapState[] { DRSELECT, IRSELECT, IRCAPTURE, IRSHIFT });
		IDLE.setPathTo(IRPAUSE, new TapState[] { DRSELECT, IRSELECT, IRCAPTURE, IREXIT1, IRPAUSE });

		DRSHIFT.setPathTo(RESET, new TapState[] { DREXIT1, DRUPDATE, DRSELECT, IRSELECT, RESET });
		DRSHIFT.setPathTo(IDLE, new TapState[] { DREXIT1, DRUPDATE, IDLE });
		DRSHIFT.setPathTo(DRPAUSE, new TapState[] { DREXIT1, DRUPDATE, DRSELECT, DRCAPTURE, DREXIT1, DRPAUSE });
		DRSHIFT.setPathTo(IRPAUSE, new TapState[] { DREXIT1, DRUPDATE, DRSELECT, IRSELECT, IRCAPTURE, IREXIT1, IRPAUSE });

		DREXIT1.setPathTo(RESET, new TapState[] { DRUPDATE, DRSELECT, IRSELECT, RESET });
		DREXIT1.setPathTo(IDLE, new TapState[] { DRUPDATE, IDLE });
		DREXIT1.setPathTo(DRPAUSE, new TapState[] { DRUPDATE, DRSELECT, DRCAPTURE, DREXIT1, DRPAUSE });
		DREXIT1.setPathTo(IRPAUSE, new TapState[] { DRUPDATE, DRSELECT, IRSELECT, IRCAPTURE, IREXIT1, IRPAUSE });

		DRPAUSE.setPathTo(RESET, new TapState[] { DREXIT2, DRUPDATE, DRSELECT, IRSELECT, RESET });
		DRPAUSE.setPathTo(IDLE, new TapState[] { DREXIT2, DRUPDATE, IDLE });
		DRPAUSE.setPathTo(DRSHIFT, new TapState[] { DREXIT2, DRUPDATE, DRSELECT, DRCAPTURE, DRSHIFT });
		DRPAUSE.setPathTo(DRPAUSE, new TapState[] { DREXIT2, DRUPDATE, DRSELECT, DRCAPTURE, DREXIT1, DRPAUSE });
		DRPAUSE.setPathTo(IRSHIFT, new TapState[] { DREXIT2, DRUPDATE, DRSELECT, IRSELECT, IRCAPTURE, IRSHIFT });
		DRPAUSE.setPathTo(IRPAUSE, new TapState[] { DREXIT2, DRUPDATE, DRSELECT, IRSELECT, IRCAPTURE, IREXIT1, IRPAUSE });

		IRSHIFT.setPathTo(RESET, new TapState[] { IREXIT1, IRUPDATE, DRSELECT, IRSELECT, RESET });
		IRSHIFT.setPathTo(IDLE, new TapState[] { IREXIT1, IRUPDATE, IDLE });
		IRSHIFT.setPathTo(DRPAUSE, new TapState[] { IREXIT1, IRUPDATE, DRSELECT, DRCAPTURE, DREXIT1, DRPAUSE });
		IRSHIFT.setPathTo(IRPAUSE, new TapState[] { IREXIT1, IRUPDATE, DRSELECT, IRSELECT, IRCAPTURE, IREXIT1, IRPAUSE });

		IREXIT1.setPathTo(RESET, new TapState[] { IRUPDATE, DRSELECT, IRSELECT, RESET });
		IREXIT1.setPathTo(IDLE, new TapState[] { IRUPDATE, IDLE });
		IREXIT1.setPathTo(DRPAUSE, new TapState[] { IRUPDATE, DRSELECT, DRCAPTURE, DREXIT1, DRPAUSE });
		IREXIT1.setPathTo(IRPAUSE, new TapState[] { IRUPDATE, DRSELECT, IRSELECT, IRCAPTURE, IREXIT1, IRPAUSE });

		IRPAUSE.setPathTo(RESET, new TapState[] { IREXIT2, IRUPDATE, DRSELECT, IRSELECT, RESET });
		IRPAUSE.setPathTo(IDLE, new TapState[] { IREXIT2, IRUPDATE, IDLE });
		IRPAUSE.setPathTo(DRSHIFT, new TapState[] { IREXIT2, IRUPDATE, DRSELECT, DRCAPTURE, DRSHIFT });
		IRPAUSE.setPathTo(DRPAUSE, new TapState[] { IREXIT2, IRUPDATE, DRSELECT, DRCAPTURE, DREXIT1, DRPAUSE });
		IRPAUSE.setPathTo(IRSHIFT, new TapState[] { IREXIT2, IRUPDATE, DRSELECT, IRSELECT, IRCAPTURE, IRSHIFT });
		IRPAUSE.setPathTo(IRPAUSE, new TapState[] { IREXIT2, IRUPDATE, DRSELECT, IRSELECT, IRCAPTURE, IREXIT1, IRPAUSE });
	}
	
	private void setPathTo(TapState to, TapState[] path) {
		Map<TapState, TapState[]> toMap = fromMap.get(this);
		if (toMap == null) {
			toMap = new HashMap<TapState, TapState[]>();
			fromMap.put(this, toMap);
		}
		toMap.put(to, path);
	}
	
	/**
	 * Returns the path between SVF states. This is encoded as an array of 0's
	 * and 1's, suitable for setting the value of TMS.
	 * 
	 * @param to				target TapState
	 * @return					an array of zero or more integers representing
	 * 							the TMS values to apply
	 */
	public int[] svfPathTo(TapState to) {
		
		// Initialise for result
		TapState[] path = fromMap.get(this).get(to);
		int[] result = new int[path.length];
		TapState from = this;
		
		// determine TMS values for each transition in the path
		for (int i = 0; i < result.length; i++) {
			result[i] = from.getTransition(path[i]);
			from = path[i];
		}
		
		return result;
	}
}
