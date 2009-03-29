package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Iterator;
import com.vividsolutions.jts.util.Assert;

public class NetworkStatistics implements Serializable {
	private RoadNetwork network;

	public NetworkStatistics(RoadNetwork network) {
		this.network = network;
	}

	public static class ResultStatistics implements Serializable {

		private int inconsistentCount;

		private double inconsistentLength;

		private int integratedCount;

		private double integratedLength;

		private int pendingCount;

		private double pendingLength;

		public int getInconsistentCount() {
			return inconsistentCount;
		}

		public double getInconsistentLength() {
			return inconsistentLength;
		}

		public int getIntegratedCount() {
			return integratedCount;
		}

		public double getIntegratedLength() {
			return integratedLength;
		}

		public int getPendingCount() {
			return pendingCount;
		}

		public double getPendingLength() {
			return pendingLength;
		}
	}

	public void clear() {
		clear(this);
		clear(resultStatistics);
	}

	private void clear(Object object) {
		Field[] fields = object.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getType() != double.class
					&& fields[i].getType() != int.class) {
				continue;
			}
			fields[i].setAccessible(true);
			try {
				fields[i].setInt(object, 0);
			} catch (IllegalArgumentException e) {
				Assert.shouldNeverReachHere();
			} catch (IllegalAccessException e) {
				Assert.shouldNeverReachHere();
			}
		}
	}

	private void inc(Object object, String name, int count, double length) {
		try {
			Field countField = object.getClass().getDeclaredField(
					Statistics.normalize(name) + "Count");
			countField.setAccessible(true);
			countField.setInt(object, countField.getInt(object) + count);
			Field lengthField = object.getClass().getDeclaredField(
					Statistics.normalize(name) + "Length");
			lengthField.setAccessible(true);
			lengthField.setDouble(object, lengthField.getDouble(object)
					+ length);
		} catch (IllegalArgumentException e) {
			Assert.shouldNeverReachHere();
		} catch (IllegalAccessException e) {
			Assert.shouldNeverReachHere();
		} catch (SecurityException e) {
			Assert.shouldNeverReachHere();
		} catch (NoSuchFieldException e) {
			Assert.shouldNeverReachHere();
		}
	}

	void inc(SourceState sourceState, int count, double length) {
		inc(this, sourceState.getName(), count, length);
		inc(this, "total", count, length);
	}

	void inc(ResultState resultState, int count, double length) {
		inc(resultStatistics, resultState.getName(), count, length);
	}

	private int matchedNonReferenceCount;

	private double matchedNonReferenceLength;

	private int matchedReferenceCount;

	private double matchedReferenceLength;

	private ResultStatistics resultStatistics = new ResultStatistics();

	private int retiredCount;

	private double retiredLength;

	private int standaloneCount;

	private double standaloneLength;

	private int totalCount;

	private double totalLength;

	private int unknownCount;

	private double unknownLength;

	public int getMatchedNonReferenceCount() {
		return matchedNonReferenceCount;
	}

	public double getMatchedNonReferenceLength() {
		return matchedNonReferenceLength;
	}

	public int getMatchedReferenceCount() {
		return matchedReferenceCount;
	}

	public double getMatchedReferenceLength() {
		return matchedReferenceLength;
	}

	public ResultStatistics getResultStatistics() {
		return resultStatistics;
	}

	public int getRetiredCount() {
		return retiredCount;
	}

	public double getRetiredLength() {
		return retiredLength;
	}

	public int getStandaloneCount() {
		return standaloneCount;
	}

	public double getStandaloneLength() {
		return standaloneLength;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public double getTotalLength() {
		return totalLength;
	}

	public int getUnknownCount() {
		return unknownCount;
	}

	public double getUnknownLength() {
		return unknownLength;
	}

	public int getAutoMatchedCount() {
		return count(new Filter() {
			public boolean allows(SourceRoadSegment segment) {
				return segment.isMatched() && !segment.isManuallyMatched();
			}
		});
	}

	public int getAutoAdjustedCount() {
		return count(new Filter() {
			public boolean allows(SourceRoadSegment segment) {
				return segment.isAdjusted() && !segment.isManuallyAdjusted();
			}
		});
	}

	public int getManuallyMatchedCount() {
		return count(new Filter() {
			public boolean allows(SourceRoadSegment segment) {
				return segment.isMatched() && segment.isManuallyMatched();
			}
		});
	}

	public int getManuallyAdjustedCount() {
		return count(new Filter() {
			public boolean allows(SourceRoadSegment segment) {
				return segment.isAdjusted() && segment.isManuallyAdjusted();
			}
		});
	}

	public int getUnadjustedCount() {
		return count(new Filter() {
			public boolean allows(SourceRoadSegment segment) {
				return !segment.isAdjusted();
			}
		});
	}

	public double getAutoMatchedLength() {
		return totalLength(new Filter() {
			public boolean allows(SourceRoadSegment segment) {
				return segment.isMatched() && !segment.isManuallyMatched();
			}
		});
	}

	public double getAutoAdjustedLength() {
		return totalLength(new Filter() {
			public boolean allows(SourceRoadSegment segment) {
				return segment.isAdjusted() && !segment.isManuallyAdjusted();
			}
		});
	}

	public double getUnadjustedLength() {
		return totalLength(new Filter() {
			public boolean allows(SourceRoadSegment segment) {
				return !segment.isAdjusted();
			}
		});
	}

	public double getManuallyMatchedLength() {
		return totalLength(new Filter() {
			public boolean allows(SourceRoadSegment segment) {
				return segment.isMatched() && segment.isManuallyMatched();
			}
		});
	}

	public double getManuallyAdjustedLength() {
		return totalLength(new Filter() {
			public boolean allows(SourceRoadSegment segment) {
				return segment.isAdjusted() && segment.isManuallyAdjusted();
			}
		});
	}

	private int count(Filter filter) {
		int count = 0;
		for (Iterator i = network.getGraph().edgeIterator(); i.hasNext();) {
			SourceRoadSegment segment = (SourceRoadSegment) i.next();
			if (filter.allows(segment)) {
				count++;
			}
		}
		return count;
	}

	private double totalLength(Filter filter) {
		double totalLength = 0;
		for (Iterator i = network.getGraph().edgeIterator(); i.hasNext();) {
			SourceRoadSegment segment = (SourceRoadSegment) i.next();
			if (filter.allows(segment)) {
				//Instead of #getApparentLine use #getLine which the other
				//metrics use because it is constant [Jon Aquino 2004-11-08]
				totalLength += segment.getLine().getLength();
			}
		}
		return totalLength;
	}

	private interface Filter {
		public boolean allows(SourceRoadSegment segment);
	}
}