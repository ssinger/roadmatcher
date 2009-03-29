package com.vividsolutions.jcs.conflate.linearpathmatch.split;

import com.vividsolutions.jts.util.Assert;

/**
 * A strategy class to place unmatched splitnodes
 * by a backwards pass through the split node array.
 * It is expected that all unmatched splitting nodes will
 * be at the end of the splitting path.
 * The algorithm works by creating "holes" for them to match to.
 * Holes are created by displacing previously matched splitnodes
 * backwards along the path.
 * <p>
 * Maintains the invariant:
 * <ul>
 * <li>Split nodes occur in the same order along the split path as the splitting points
 * occur along the splitting path
 * </ul>
 *
 * @version 1.0
 */
public class PlaceBackward {

  private SplitNode[] splitNodes;

  public PlaceBackward(SplitNode[] splitNodes)
  {
    this.splitNodes = splitNodes;
    match();
  }

  public void match()
  {
    // check if there's anything to do!
    if (splitNodes.length <= 0)
      return;

    Assert.isTrue(splitNodes[splitNodes.length - 1].isPlaced(),
                  "last splitNode should be matched");

    int[] unmatchedRange = findUnmatchedRange();
    if (unmatchedRange[0] < 0)
      return;   // no unmatched were found

    int lastMatchedIndex = unmatchedRange[0] - 1;
    SplitNode lastMatchedSplit = splitNodes[lastMatchedIndex];
    QuantumIndex targetIndex = splitNodes[splitNodes.length - 1].getPlace().prev();
    for (int i = unmatchedRange[1]; i >= unmatchedRange[0]; i--) {
      /**
       * If there is space between the last splitnode and the last matched one,
       * put the unmatched splitnode there
       */
      if (targetIndex.compareTo(lastMatchedSplit.getPlace()) > 0) {
        splitNodes[i].setPlace(targetIndex);
        targetIndex = targetIndex.prev();
      }
      else {
        /**
         * If there is no space between the last splitnode the last matched one,
         * try and move the earlier matched splitnodes back to make a hole.
         */
        if (! hasHole(0, lastMatchedIndex)) {
          System.out.println("unable to create hole");
          return;
        }
        QuantumIndex qi = lastMatchedSplit.getPlace();
        shiftBack(lastMatchedIndex);
        splitNodes[i].setPlace(qi);
      }
    }
  }

  private int[] findUnmatchedRange()
  {
    int[] unmatchedRange = { -1, -1 };
    for (int i = splitNodes.length - 1; i >= 0; i--) {
      if (! splitNodes[i].isPlaced()) {
        unmatchedRange[1] = i;
        break;
      }
    }
    // no unmatched were found
    if (unmatchedRange[1] < 0)
      return unmatchedRange;

    // search backwards from end of unmatched range
    // to find the beginning of it
    for (int i = unmatchedRange[1]; i >= 0; i--) {
      if (splitNodes[i].isPlaced()) {
        unmatchedRange[0] = i + 1;
        break;
      }
    }
    return unmatchedRange;
  }

  private boolean hasHole(int start, int end)
  {
    for (int i = start; i < end; i++) {
      if (! splitNodes[i].getPlace().isNext(splitNodes[i + 1].getPlace()))
        return true;
    }
    return false;
  }

  private void shiftBack(int startIndex)
  {
    boolean foundHole = false;
    int indexToShift = startIndex;
    while (! foundHole) {
      if (indexToShift == 0) {
        throw new IllegalStateException("unable to shift all nodes");
        // return;
      }
      QuantumIndex thisIndex = splitNodes[indexToShift].getPlace();
      QuantumIndex prevIndex = splitNodes[indexToShift - 1].getPlace();

      if (prevIndex.isNext(thisIndex)) {
        splitNodes[indexToShift].setPlace(prevIndex);
        indexToShift--;
        // no hole - keep shifting back
      }
      else {
        splitNodes[indexToShift].setPlace(thisIndex.prev());
        foundHole = true;
      }
    }
  }
}