package com.vividsolutions.jcs.plugin.conflate.roads;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import bsh.Interpreter;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.util.Block;

public class ExportResultApp {
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Usage: exportresult SESSIONFILE OUTPUTFILE");
			return;
		}
		exportResult(session(new File(args[0])), new File(args[1]));
		System.out.println("Done");
	}

	private static void exportResult(ConflationSession session,
			File outputFilename) throws IOException {
		System.out.println("Exporting result package as " + outputFilename
				+ ". . .");
		new ResultPackageExporter().exportResultPackage(session,
				(FeatureCollection) session.getBlackboard().get(
						AbstractSaveSessionPlugIn.ISSUES_KEY), outputFilename,
				new DummyTaskMonitor());
	}

	private static ConflationSession session(File file) throws Exception {
		System.out.println("Opening session . . .");
		return OpenRoadMatcherSessionPlugIn.open(file, new DummyTaskMonitor());
	}
}