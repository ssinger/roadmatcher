package com.vividsolutions.jcs.conflate.roads.model;

import com.vividsolutions.jcs.conflate.roads.ErrorMessages;
import com.vividsolutions.jcs.jump.FUTURE_StringUtil;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;

public class ScriptedPrecedenceRuleEngine implements PrecedenceRuleEngine {

	//Make Interpreter transient -- this somehow fixes the following
	//deserialization ClassNotFoundException (outside the IDE):
	//java.lang.ClassNotFoundException:
	// com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment
	//at java.net.URLClassLoader$1.run(Unknown Source)
	//at java.security.AccessController.doPrivileged(Native Method)
	//at java.net.URLClassLoader.findClass(Unknown Source)
	//at java.lang.ClassLoader.loadClass(Unknown Source)
	//[Jon Aquino 2004-06-03]
	private transient Interpreter interpreter;

	public SourceRoadSegment chooseReference(SourceRoadSegment a,
			SourceRoadSegment b) {
		try {
			getInterpreter().set("a", a);
			getInterpreter().set("b", b);
			return (SourceRoadSegment) getInterpreter().eval(
					"chooseReference(a, b);");
		} catch (EvalError e) {
			if (!(e instanceof TargetError)) {
				throw new RuntimeException(e);
			}
			throw new RuntimeException(
					FUTURE_StringUtil
							.substitute(
									ErrorMessages.scriptedPrecedenceRuleEngine_evalError,
									new String[] { ((TargetError) e)
											.getTarget().getMessage() }), e);
		}
	}

	private Interpreter getInterpreter() throws EvalError {
		if (interpreter == null) {
			interpreter = new Interpreter();
			interpreter.eval(script);
		}
		return interpreter;
	}

	private String script;

	public String getScript() {
		return script;
	}

	public ScriptedPrecedenceRuleEngine setScript(String script) {
		this.script = script;
		return this;
	}
}