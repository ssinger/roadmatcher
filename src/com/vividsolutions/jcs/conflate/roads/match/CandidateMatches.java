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

package com.vividsolutions.jcs.conflate.roads.match;

import java.io.Serializable;
import java.util.*;

/**
 * Maintains a set of matches of one object to other ones.
 * Matches are ordered by a real-valued value.  The ordering
 * can either be increasing or decreasing.
 * Matches can be accessed either by the matched object
 * or by the sorted values of the matches.
 */
public class CandidateMatches implements Serializable {


  private TreeMap matchMap;
  private List matchList;
  private boolean sorted = false;
  private boolean isPositiveValueFunction = true;

  public CandidateMatches(Comparator objComparator)
  {
    this(objComparator, true);
  }

  public CandidateMatches(Comparator objComparator, boolean isPositiveValueFunction)
  {
    matchMap = new TreeMap(objComparator);
    matchList = new ArrayList();
    this.isPositiveValueFunction = isPositiveValueFunction;
  }

  private MatchValue addMatch(Object obj, double value)
  {
    MatchValue mv = new MatchValue(obj, value);
    matchMap.put(obj, mv);
    matchList.add(mv);
    return mv;
  }
  public void setValue(Object obj, double value)
  {
    MatchValue mv = (MatchValue) matchMap.get(obj);
    if (mv == null) {
      mv = addMatch(obj, value);
    }
    mv.setValue(value);
    sorted = false;
  }

  public double getValue(Object obj)
  {
    MatchValue mv =  (MatchValue) matchMap.get(obj);
    return mv.getValue();
  }

  public List getMatches()
  {
    sort();
    return matchList;
  }

  public MatchValue getBestMatch()
  {
    sort();
    if (matchList.size() == 0) return null;
    return (MatchValue) matchList.get(0);
  }

  public static int findLargestIntervalIndex(List matches)
  {
    double maxInterval = -1;
    int index = 0;
    for (int i = 0; i < matches.size() - 1; i++) {
      double val0 = ((MatchValue) matches.get(i)).getValue();
      double val1 = ((MatchValue) matches.get(i + 1)).getValue();
      double interval = val0 - val1;
      if (interval > maxInterval) {
        maxInterval = interval;
        index = i;
      }
    }
    return index;
  }

  public List getBestMatches()
  {
    List matches = getMatches();
    if (matches.size() <= 1) return matches;

    int maxIntervalIndex = findLargestIntervalIndex(matches);
    return matches.subList(0, maxIntervalIndex + 1);

  }

  private void sort()
  {
    if (sorted) return;
    Collections.sort(matchList);
    if (isPositiveValueFunction) Collections.reverse(matchList);
    sorted = true;
  }
}
