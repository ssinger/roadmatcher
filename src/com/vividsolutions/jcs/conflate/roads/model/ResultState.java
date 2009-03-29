package com.vividsolutions.jcs.conflate.roads.model;

import java.io.Serializable;
import java.util.HashMap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.util.Blackboard;

public class ResultState implements Serializable {

    private String name;
    private static HashMap nameToResultStateMap = new HashMap();
    private ResultState(String name) {
        this.name = name;
        nameToResultStateMap.put(name, this);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }

    public static final ResultState PENDING = new ResultState("Pending");

    public static final ResultState INCONSISTENT = new ResultState(
            "Inconsistent");

    public static final ResultState INTEGRATED = new ResultState("Integrated");

    public static class Description implements Serializable {


        private String comment;

        private ResultState resultState;

        private Coordinate intersection;

        public Description(ResultState resultState) {
            this.resultState = resultState;
        }

        private Blackboard blackboard = null;

        public Blackboard getBlackboard() {
            if (blackboard == null) {
                blackboard = new Blackboard();
            }
            return blackboard;
        }

        public String getComment() {
            return comment;
        }

        public ResultState getResultState() {
            return resultState;
        }

        public Description put(String key, Object value) {
            getBlackboard().put(key, value);
            return this;
        }

        public Object get(String key) {
            return getBlackboard().get(key);
        }

        public Description addComment(String comment) {
            this.comment = this.comment == null ? comment : this.comment + " "
                    + comment;
            return this;
        }
    }
    /**
     * @see http://www.javaworld.com/javaworld/javatips/jw-javatip122.html
     */
    private Object readResolve() {
        return nameToResultStateMap.get(name);
    }    
}
