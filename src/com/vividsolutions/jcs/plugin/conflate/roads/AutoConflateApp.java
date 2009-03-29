package com.vividsolutions.jcs.plugin.conflate.roads;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import bsh.Interpreter;

import com.vividsolutions.jcs.conflate.roads.ConflationSession;
import com.vividsolutions.jcs.conflate.roads.model.SourceRoadSegment;
import com.vividsolutions.jcs.plugin.issuelog.IssueLog;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.workbench.model.LayerManager;

public class AutoConflateApp {
	public static void main(String[] args) throws FileNotFoundException,
			IOException, InstantiationException, IllegalAccessException {
		if (args.length != 1) {
			System.out.println("Usage: autoconflate SOURCEPACKAGE");
			return;
		}
		ConflationSession session = importSourcePackage(new File(args[0]));
		if (!NewSessionPlugIn.validationErrorMessages(session).isEmpty()) {
			for (Iterator i = NewSessionPlugIn.validationErrorMessages(session)
					.iterator(); i.hasNext();) {
				String errorMessage = (String) i.next();
				System.err.println(errorMessage);
			}
			System.exit(1);
		}
		saveSession(autoConflate(session), new File(args[0]).getParentFile());
		System.out.println("Done");
	}

	private static void saveSession(ConflationSession session, File directory)
			throws FileNotFoundException, IOException {
		System.out.println("Saving session as "
				+ outputFilename(session, directory) + ". . .");
		AbstractSaveSessionPlugIn.save(session, IssueLog.instance(
				new LayerManager()).getIssues(), outputFilename(session,
				directory), 0, new DummyTaskMonitor());
	}

	private static File outputFilename(ConflationSession session, File directory) {
		return SaveSessionAsPlugIn.defaultFilename(session, directory);
	}

	private static ConflationSession autoConflate(
			final ConflationSession session) throws InstantiationException,
			IllegalAccessException {
		System.out.println("AutoMatching . . .");
		session.autoMatch(new DummyTaskMonitor());
		System.out.println("Updating result states . . .");
		session.updateResultStates(new DummyTaskMonitor());
		System.out.println("AutoAdjusting . . .");
		autoAdjust(session);
		return session;
	}

	private static void autoAdjust(final ConflationSession session)
			throws InstantiationException, IllegalAccessException {
		new AutoAdjustOp().autoAdjust(session.getRoadSegments(),
				new DummyTaskMonitor(), consistencyConfiguration()
						.getAutoAdjuster(), session, new Block() {
					public Object yield(Object adjustmentObject) {
						final Adjustment adjustment = (Adjustment) adjustmentObject;
						session.doAutomatedProcess(new Block() {
							public Object yield() {
								for (int i = 0; i < adjustment
										.getRoadSegments().size(); i++) {
									((SourceRoadSegment) adjustment
											.getRoadSegments().get(i))
											.setApparentLine(((LineString) adjustment
													.getNewApparentLines().get(
															i)));
								}
								Transaction.updateAffectedSegments(adjustment
										.getRoadSegments(), session,
										new DummyTaskMonitor());
								return null;
							}
						});
						return null;
					}
				});
	}

	private static ConsistencyConfiguration consistencyConfiguration()
			throws InstantiationException, IllegalAccessException {
		return ((ConsistencyConfiguration) ConsistencyConfiguration.DEFAULT_CLASS
				.newInstance());
	}

	private static ConflationSession importSourcePackage(File sourcePackage)
			throws IOException, InstantiationException, IllegalAccessException {
		System.out.println("Importing source package . . .");
		return new SourcePackageImporter().importSourcePackage(sourcePackage,
				consistencyConfiguration(), new DummyTaskMonitor());
	}
}