package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jcs.jump.FUTURE_AbstractBasicFeature;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.util.LangUtil;

/**
 * Attributes are the same as those of the original Feature, with the exception
 * of the geometry, which may be different.
 */
public class SourceFeature implements Feature, Serializable {
	public static abstract class ConflationAttribute {
		private String shapefileName;

		private AttributeType type;

		public ConflationAttribute(String name, String shapefileName,
				AttributeType type) {
			this.name = name;
			this.type = type;
			this.shapefileName = shapefileName;
			Assert.isTrue(shapefileName.length() <= 10);
		}

		public ConflationAttribute(String name, AttributeType type) {
			this(name, name, type);
		}

		public String getName() {
			return name;
		}

		public abstract Object value(SourceRoadSegment roadSegment);

		private String name;

		public String getShapefileName() {
			return shapefileName;
		}

		public AttributeType getType() {
			return type;
		}
	}

	public SourceFeature(SourceRoadSegment roadSegment) {
		this.roadSegment = roadSegment;
	}

	public Object clone() {
		return clone(true);
	}

	public Feature clone(boolean deep) {
		return FUTURE_AbstractBasicFeature.clone(this, true);
	}

	public int compareTo(Object o) {
		return roadSegment.getOriginalFeature().compareTo((Feature) o);
	}

	public Object getAttribute(int i) {
		for (Iterator j = CONFLATION_ATTRIBUTES.iterator(); j.hasNext();) {
			ConflationAttribute conflationAttribute = (ConflationAttribute) j
					.next();
			if (i == getSchema().getAttributeIndex(
					conflationAttribute.getName())) {
				return conflationAttribute.value(roadSegment);
			}
		}
		return roadSegment.getOriginalFeature().getAttribute(
				i - CONFLATION_ATTRIBUTES.size());
	}

	public Object getAttribute(String name) {
		return getAttribute(getSchema().getAttributeIndex(name));
	}

	public Object[] getAttributes() {
		List attributes = new ArrayList();
		for (Iterator i = CONFLATION_ATTRIBUTES.iterator(); i.hasNext();) {
			ConflationAttribute conflationAttribute = (ConflationAttribute) i
					.next();
			attributes.add(conflationAttribute.value(roadSegment));
		}
		attributes.addAll(Arrays.asList(roadSegment.getOriginalFeature()
				.getAttributes()));
		return attributes.toArray();
	}

	public double getDouble(int attributeIndex) {
		return roadSegment.getOriginalFeature().getDouble(attributeIndex);
	}

	public Geometry getGeometry() {
		return roadSegment.getApparentLine();
	}

	public int getID() {
		return id;
	}

	public int getInteger(int attributeIndex) {
		return roadSegment.getOriginalFeature().getInteger(attributeIndex);
	}

	public SourceRoadSegment getRoadSegment() {
		return roadSegment;
	}

	public FeatureSchema getSchema() {
		return roadSegment.getNetwork().getFeatureCollection()
				.getFeatureSchema();
	}

	public String getString(int attributeIndex) {
		//Conform to JavaDoc for Feature#getString [Jon Aquino 11/28/2003]
		return LangUtil.ifNull(getAttribute(attributeIndex), "").toString();
	}

	public String getString(String attributeName) {
		return getString(getSchema().getAttributeIndex(attributeName));
	}

	public void setAttribute(int attributeIndex, Object newAttribute) {
		throw new UnsupportedOperationException();
	}

	public void setAttribute(String attributeName, Object newAttribute) {
		throw new UnsupportedOperationException();
	}

	public void setAttributes(Object[] attributes) {
		throw new UnsupportedOperationException();
	}

	public void setGeometry(Geometry geometry) {
		//User might use MoveVertexTool on geometry then hit undo.
		//MoveVertexTool will set geometry to clone of original geometry,
		//and "adjusted" symbology will still appear. Hence the logic
		//below to use the original line if it is equal to geometry.
		//[Jon Aquino 2004-02-18]
		getRoadSegment()
				.setApparentLine(
						(LineString) (geometry.equalsExact(getRoadSegment()
								.getLine()) ? getRoadSegment().getLine()
								: geometry));
		//Use #equalsExact rather than #equals(Geometry),
		//to allow InsertVertexTool to work.
		//[Jon Aquino 2004-02-18]
		getRoadSegment().getNetwork().getSession().getRoadsEventFirer()
				.fireGeometryModifiedExternally(getRoadSegment());
	}

	public void setSchema(FeatureSchema schema) {
		throw new UnsupportedOperationException();
	}

	private int id = FeatureUtil.nextID();

	private SourceRoadSegment roadSegment;

	public static FeatureSchema createSchema(FeatureSchema schema) {
		FeatureSchema newSchema = new FeatureSchema();
		for (Iterator i = CONFLATION_ATTRIBUTES.iterator(); i.hasNext();) {
			ConflationAttribute conflationAttribute = (ConflationAttribute) i
					.next();
			newSchema.addAttribute(conflationAttribute.getName(),
					conflationAttribute.getType());
		}
		for (int i = 0; i < schema.getAttributeCount(); i++) {
			newSchema.addAttribute(schema.getAttributeName(i), schema
					.getAttributeType(i));
		}
		return newSchema;
	}

	public static List CONFLATION_ATTRIBUTES = Collections
			.unmodifiableList(Arrays.asList(new ConflationAttribute[] {
					new ConflationAttribute("MaxDist", AttributeType.DOUBLE) {
						public Object value(SourceRoadSegment roadSegment) {
							return roadSegment.getMaxDistance();
						}
					},
					new ConflationAttribute("AdjSize", AttributeType.DOUBLE) {
						public Object value(SourceRoadSegment roadSegment) {
							return new Double(roadSegment.getAdjustmentSize());
						}
					},
					new ConflationAttribute("TrimmedDist", "TrimDist",
							AttributeType.DOUBLE) {
						public Object value(SourceRoadSegment roadSegment) {
							return roadSegment.getTrimmedDistance();
						}
					},
					new ConflationAttribute("Length", AttributeType.DOUBLE) {
						public Object value(SourceRoadSegment roadSegment) {
							return new Double(roadSegment
									.getApparentLineLength());
						}
					},
					new ConflationAttribute("Nearness", AttributeType.DOUBLE) {
						public Object value(SourceRoadSegment roadSegment) {
							return roadSegment.getNearnessFraction();
						}
					},
					new ConflationAttribute("Comment", AttributeType.STRING) {
						public Object value(SourceRoadSegment roadSegment) {
							return roadSegment.getComment();
						}
					},
					new ConflationAttribute("Adjusted", AttributeType.STRING) {
						public Object value(SourceRoadSegment roadSegment) {
							return roadSegment.isAdjusted() ? "Y" : "N";
						}
					},
					new ConflationAttribute("AdjAngDel", AttributeType.DOUBLE) {
						public Object value(SourceRoadSegment roadSegment) {
							return new Double(roadSegment
									.getAdjustmentAngleDelta());
						}
					},
					new ConflationAttribute("SourceState", "SrcState",
							AttributeType.STRING) {
						public Object value(SourceRoadSegment roadSegment) {
							return roadSegment.getState().getName();
						}
					},
					new ConflationAttribute("ResultState", "ResState",
							AttributeType.STRING) {
						public Object value(SourceRoadSegment roadSegment) {
							return roadSegment.getResultState() != null ? roadSegment
									.getResultState().getName()
									: null;
						}
					},
					new ConflationAttribute("SplitStart", AttributeType.STRING) {
						public Object value(SourceRoadSegment roadSegment) {
							return roadSegment instanceof SplitRoadSegment
									&& ((SplitRoadSegment) roadSegment)
											.isSplitAtStart() ? "Y" : "N";
						}
					},
					new ConflationAttribute("SplitEnd", AttributeType.STRING) {
						public Object value(SourceRoadSegment roadSegment) {
							return roadSegment instanceof SplitRoadSegment
									&& ((SplitRoadSegment) roadSegment)
											.isSplitAtEnd() ? "Y" : "N";
						}
					},
					new ConflationAttribute("ManualMatch", "ManMatch",
							AttributeType.STRING) {
						public Object value(SourceRoadSegment roadSegment) {
							return roadSegment.isManuallyMatched() ? "Y" : "N";
						}
					},
					new ConflationAttribute("ManualAdj", "ManAdj",
							AttributeType.STRING) {
						public Object value(SourceRoadSegment roadSegment) {
							return roadSegment.isManuallyAdjusted() ? "Y" : "N";
						}
					},
					new ConflationAttribute("ManualAdjCount", "ManAdjCnt",
							AttributeType.INTEGER) {
						public Object value(SourceRoadSegment roadSegment) {
							return new Integer(roadSegment
									.getManualAdjustmentCount());
						}
					},
					new ConflationAttribute("MatchOrientation", "MatchOrien",
							AttributeType.STRING) {
						public Object value(SourceRoadSegment roadSegment) {
							if (roadSegment.isMatchOrientationSame() == null) {
								return null;
							}
							return roadSegment.isMatchOrientationSame()
									.booleanValue() ? "Y" : "N";
						}
					},
					new ConflationAttribute("Reviewed", AttributeType.STRING) {
						public Object value(SourceRoadSegment roadSegment) {
							return roadSegment.isReviewed() ? "Y" : "N";
						}
					},
					new ConflationAttribute("UpdateTime", AttributeType.STRING) {
						public Object value(SourceRoadSegment roadSegment) {
							return dateFormatter.format(roadSegment
									.getUpdateTime());
						}
					}, }));

	private static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
}