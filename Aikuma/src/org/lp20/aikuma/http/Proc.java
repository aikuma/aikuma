package org.lp20.aikuma.http;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lp20.aikuma.http.NanoHTTPD.IHTTPSession;
import org.lp20.aikuma.http.NanoHTTPD.Response;

/**
 * A chain of HTTP request processors.
 * 
 * @author haejoong
 */
abstract public class Proc {
	private List<Proc> procs;
	
	/**
	 * Add a new processor to the processor chain and returns itself.
	 * @param p
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
	 * @param session
	 */
	abstract public Response run(IHTTPSession session);
	
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
