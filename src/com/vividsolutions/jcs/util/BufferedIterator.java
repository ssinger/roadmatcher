/*
 * The JCS Conflation Suite (JCS) is a library of Java classes that
 * can be used to build automated or semi-automated conflation solutions.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.vividsolutions.jcs.util;

import java.util.Iterator;

/**
 * Provides an iterator which can buffer iterated items, allowing
 * them to be "pushed back" onto the stream of items.
 * <p>
 * Currently able to buffer a single object, but could easily
 * be extended to handle an arbitrary number of objects
 */
public class BufferedIterator
    implements Iterator
{
  private Iterator it;
  private Object buffer = null;// could also be a stack?

  /**
   * Constructs a new buffered iterator for a given {@link Iterator}
   * @param it the iterator to buffer
   */
  public BufferedIterator(Iterator it)
  {
    this.it = it;
  }

  /**
   * Push an object obtained from the iteration stream back onto the stream
   * @param obj the object to put back on the iteration stream
   */
  public void putBack(Object obj)
  {
    if (buffer != null)
      throw new RuntimeException("buffer can only hold one node");
    buffer = obj;
  }

  /**
   * Tests whether any objects remain to be iterated over
   * @return <code>true</code> if there are items remaining in the iteration stream
   */
  public boolean hasNext()
  {
    if (buffer != null) return true;
    return it.hasNext();
  }

  /**
   * Fetches the next object from the iteration stream
   * @return the next object on the iteration stream, if any
   */
  public Object next()
  {
    if (buffer != null)
    {
      Object temp = buffer;
      buffer = null;
      return temp;
    }
    return it.next();
  }

  /**
   * This operation is not supported.
   */
  public void remove() { throw new UnsupportedOperationException(); }
}
