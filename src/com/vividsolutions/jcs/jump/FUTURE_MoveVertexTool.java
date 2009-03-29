package com.vividsolutions.jcs.jump;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.MoveVertexTool;
public class FUTURE_MoveVertexTool extends MoveVertexTool {
    public FUTURE_MoveVertexTool(EnableCheckFactory checkFactory) {
        super(checkFactory);
    }
    private Envelope _vertexBuffer(Coordinate c)
            throws NoninvertibleTransformException {
        return (Envelope) FUTURE_LangUtil.invokePrivateMethod("vertexBuffer",
                this, MoveVertexTool.class, new Object[]{c},
                new Class[]{Coordinate.class});
    }
    public void moveVertices(Coordinate initialLocation,
            Coordinate finalLocation) throws Exception {
        final Envelope oldVertexBuffer = _vertexBuffer(initialLocation);
        final Coordinate newVertex = finalLocation;
        ArrayList transactions = new ArrayList();
        for (Iterator i = getPanel().getSelectionManager()
                .getLayersWithSelectedItems().iterator(); i.hasNext();) {
            Layer layerWithSelectedItems = (Layer) i.next();
            if (!layerWithSelectedItems.isEditable()) {
                continue;
            }
            transactions.add(_createTransaction(layerWithSelectedItems,
                    oldVertexBuffer, newVertex));
        }
        commit(transactions);
    }
    protected void commit(Collection transactions) {
        EditTransaction.commit(transactions);
    }
    private EditTransaction _createTransaction(Layer layer,
            final Envelope oldVertexBuffer, final Coordinate newVertex) {
        return (EditTransaction) FUTURE_LangUtil.invokePrivateMethod(
                "createTransaction", this, MoveVertexTool.class, new Object[]{
                        layer, oldVertexBuffer, newVertex}, new Class[]{
                        Layer.class, Envelope.class, Coordinate.class});
    }
}