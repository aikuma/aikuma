package org.lp20.aikuma.http;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

/**
 * Allows to create a chain of HTTP request processors.
 * 
 * @author	Haejoong Lee	<haejoong@ldc.upenn.edu>
 */
abstract public class Proc {
	private List<Proc> procs;
	
	/**
	 * Add a new processor to the processor chain and returns itself.
	 * @param p Proc object
	 * @return this object.
	 */
	public Proc add(Proc p) {
		if (procs == null) {
			procs = new ArrayList<Proc>();
			procs.add(this);
		}
		procs.add(p);
		return this;
	}
	
	/**
	 * The actual behavior of the object should be implemented here.
	 * @param session An HTTP session object.
	 * @return a Response object, or null if the HTTP request, described by
	 *     the session object, was not handled.
	 */
	abstract public Response run(IHTTPSession session);

	/**
	 * Execute the processor chain.
	 * @param session HTTP session object given to the serve() method of
	 *     NanoHTTPD object.
	 * @return a Response object, or null if the HTTP request was not
	 *     handled.
	 */
	public Response exec(IHTTPSession session) {
		Iterator<Proc> it = procs.iterator();
		while (it.hasNext()) {
			Response r = it.next().run(session);
			if (r != null) {
				return r;
			}
		}
		return null;
	}
}
