package com.vividsolutions.jcs.jump;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.JUMPReader;
import com.vividsolutions.jump.io.JUMPWriter;
import com.vividsolutions.jump.io.ShapefileReader;
import com.vividsolutions.jump.io.ShapefileWriter;
import com.vividsolutions.jump.io.datasource.DelegatingCompressedFileHandler;
import com.vividsolutions.jump.io.datasource.StandardReaderWriterFileDataSource;
public class FUTURE_StandardReaderWriterFileDataSource {
    private static class ClassicReaderWriterFileDataSource extends
            StandardReaderWriterFileDataSource {
        //Copy of
        // StandardReaderWriterFileDataSource.ClassicReaderWriterFileDataSource
        //[Jon Aquino 2004-05-04]
        public ClassicReaderWriterFileDataSource(JUMPReader reader,
                JUMPWriter writer, String[] extensions) {
            super(new DelegatingCompressedFileHandler(reader,
                    toEndings(extensions)), writer, extensions);
            this.extensions = extensions;
        }
    }
    public static class Shapefile extends ClassicReaderWriterFileDataSource {
        public Shapefile() {
            super(new ShapefileReader(), new ShapefileWriter() {
                public GeometryCollection makeSHAPEGeometryCollection(
                        FeatureCollection fc) throws Exception {
                    //Avoid "Could not determine shapefile type - data is
                    //either all GeometryCollections or empty" exception
                    //[Jon Aquino 2004-05-04]
                    return fc.isEmpty() ? new GeometryFactory()
                            .createGeometryCollection(new Geometry[]{}) : super
                            .makeSHAPEGeometryCollection(fc);
                }
            }, new String[]{"shp"});
        }
    }
}