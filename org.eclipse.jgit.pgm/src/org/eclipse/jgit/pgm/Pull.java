/*
 * Developed 2020, Ian Mahaffy
 *
 * Heavily referenced and copied from code by the following:
 *
 * Copyright (C) 2010, Christian Halstrick <christian.halstrick@sap.com>
 * Copyright (C) 2010, Mathias Kinzler <mathias.kinzler@sap.com>
 * Copyright (C) 2016, Laurent Delaigue <laurent.delaigue@obeo.fr> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */


package org.eclipse.jgit.pgm;

//import static java.lang.Character.valueOf;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

//import java.text.MessageFormat;
//import java.util.ArrayList;
//import java.util.List;
//
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
//import org.eclipse.jgit.api.MergeCommand.FastForwardMode.Merge;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BranchConfig.BranchRebaseMode;
import org.eclipse.jgit.merge.ResolveMerger.MergeFailureReason;
import org.eclipse.jgit.pgm.internal.CLIText;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.lib.Constants;
//import org.eclipse.jgit.lib.Ref;
//import org.eclipse.jgit.lib.TextProgressMonitor;
//import org.eclipse.jgit.pgm.internal.CLIText;
//import org.eclipse.jgit.transport.PushResult;
//import org.eclipse.jgit.transport.RefSpec;
//import org.eclipse.jgit.transport.RemoteRefUpdate;
//import org.eclipse.jgit.transport.RemoteRefUpdate.Status;
//import org.eclipse.jgit.transport.Transport;
//import org.eclipse.jgit.transport.URIish;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;



@Command(common = true, usage = "usage_pullChangesFromRemoteRepositories")
class Pull extends TextBuiltin {

	//Will default to "origin"
	@Argument(index = 0, metaVar = "metaVar_uriish")
	private String remote = Constants.DEFAULT_REMOTE_NAME;

	//Will default to "origin"
	@Argument(index = 1, metaVar = "metaVar_uriish")
	private String remoteBranchName = null;

	private BranchRebaseMode pullRebaseMode = BranchRebaseMode.NONE;

	@Option(name = "--rebase", aliases = { "-r" })
	void pullRebaseMode(@SuppressWarnings("unused") final boolean ignored) {
		pullRebaseMode = BranchRebaseMode.REBASE;
	}

	private FastForwardMode ff = FastForwardMode.FF;

	@Option(name = "--ff", usage = "usage_mergeFf")
	void ff(@SuppressWarnings("unused") final boolean ignored) {
		ff = FastForwardMode.FF;
	}

	@Option(name = "--no-ff", usage = "usage_mergeNoFf")
	void noff(@SuppressWarnings("unused") final boolean ignored) {
		ff = FastForwardMode.NO_FF;
	}

	@Option(name = "--ff-only", usage = "usage_mergeFfOnly")
	void ffonly(@SuppressWarnings("unused") final boolean ignored) {
		ff = FastForwardMode.FF_ONLY;
	}

	/** {@inheritDoc} */
	@Override
	protected void run() throws IOException {

		try(Git git = new Git(db)) {
			PullCommand pull = git.pull();
			pull.setRemote(remote);
			pull.setFastForward(ff);
			pull.setRebase(pullRebaseMode);

			if(remoteBranchName != null)
				pull.setRemoteBranchName(remoteBranchName);


			PullResult results = pull.call();
			printPullResult(results);


	} catch (GitAPIException e) {
		throw die(e.getMessage(), e);
	}
	}

	/**
	 *
	 * @param results
	 *            PullResult to be printed
	 * @throws IOException
	 */
	@SuppressWarnings("nls")
	private void printPullResult(final PullResult results) throws IOException {
		// outw.println(
		// results.toString() + "\n end \n" + results.isSuccessful());

		// printTest(results);
		if (!results.isSuccessful()) {
			outw.println(getPullErrors(results));
			return;
		}
		outw.println();
		StringBuilder sb = new StringBuilder();

		outw.println(MessageFormat.format(CLIText.get().fromURI,
				results.getFetchResult().getURI().toString()));

		// Prints out a visual for the fetch path
		for (TrackingRefUpdate u : results.getFetchResult()
				.getTrackingRefUpdates()) {
			final String src = abbreviateRef(u.getRemoteName(), false);
			final String dst = abbreviateRef(u.getLocalName(), true);
			outw.format("	%-10s -> %s", src, dst);
			outw.println();
		}
		// Add Merge status to SB
		sb.append(results.getMergeResult().getMergeStatus().toString());
		sb.append("Pull Successful");
		outw.println(sb.toString());
		// printTest(results);

	}

	@SuppressWarnings("nls")
	private String getPullErrors(final PullResult results) throws IOException {
		StringBuilder sb = new StringBuilder();

		if (results.getFetchResult().getURI() != null)
			outw.println(MessageFormat.format(CLIText.get().fromURI,
					results.getFetchResult().getURI().toString()));
		else
			outw.println("No URI found for Fetch Result\n");

		for (TrackingRefUpdate u : results.getFetchResult()
				.getTrackingRefUpdates()) {
			final String src = abbreviateRef(u.getRemoteName(), false);
			final String dst = abbreviateRef(u.getLocalName(), true);
			outw.format(" %-10s -> %s", src, dst);
			outw.println();
		}
		// Print Merge results
		if (results.getMergeResult().getMergeStatus() != null) {
			// if (results.getMergeResult().getMergeStatus()
			// .toString() == "Failed") {
			// outw.println(CLIText.get().mergeFailed);
			// if (results.getMergeResult().getFailingPaths() != null)
			// outw.println("Problem with files: " + results
			// .getMergeResult().getFailingPaths().toString());
			// outw.println(results.getMergeResult().getMergeStatus().);
			// }
			for (Map.Entry<String, MergeFailureReason> entry : results
					.getMergeResult().getFailingPaths().entrySet())
				switch (entry.getValue()) {
				case DIRTY_WORKTREE:
				case DIRTY_INDEX:
					outw.println(CLIText.get().dontOverwriteLocalChanges);
					outw.println("        " + entry.getKey());
					break;
				case COULD_NOT_DELETE:
					outw.println(CLIText.get().cannotDeleteFile);
					outw.println("        " + entry.getKey());
					break;
				}

			outw.println("Merge Results: "
					+ results.getMergeResult().getMergeStatus().toString()
					+ "\n");
		}
		// Print merge conflicts, if any
		if (results.getMergeResult().getConflicts() != null)
			outw.println("Merge conflict in the files:  "
					+ results.getMergeResult().getConflicts().toString()
					+ "\nAutomatic mergess failed; fix conflicts and then commit the results\n");
		return sb.toString();

	}

	// @SuppressWarnings("nls")
	// private void printTest(final PullResult results) throws IOException {
	// if (results.getFetchResult() != null) {
	// outw.println("\n\n********* FETCH RESULTS **************\n\n");
	//
	// for (TrackingRefUpdate u : results.getFetchResult()
	// .getTrackingRefUpdates()) {
	// final String src = abbreviateRef(u.getRemoteName(), false);
	// final String dst = abbreviateRef(u.getLocalName(), true);
	// outw.format(" %-10s -> %s", src, dst);
	// outw.println();
	// }
	//
	// outw.println("getFetchResult: " + results.getFetchResult());
	//
	// outw.println("getFetchResult.toString: "
	// + results.getFetchResult().toString());
	//
	// outw.println("getFetchResult.getMessages: "
	// + results.getFetchResult().getMessages());
	//
	// outw.println("getFetchResult.getTrackingRefUpdates().toString(): "
	// + results.getFetchResult().getTrackingRefUpdates()
	// .toString());
	//
	// outw.println("getFetchedFrom: " + results.getFetchedFrom() + "\n");
	//
	// }
	//
	// if (results.getMergeResult() != null) {
	// outw.println("\n\n********* MERGE RESULTS **************\n\n");
	//
	// outw.println("getMergeResult: " + results.getMergeResult());
	//
	// outw.println("getMergeResult.toString: "
	// + results.getMergeResult().toString());
	// try {
	// outw.println("getMergeResult.getMergedCommits: "
	// + results.getMergeResult().getMergedCommits());
	// } catch (Exception e) {
	// outw.println("Exception caught: " + e.toString());
	// }
	//
	// try {
	// outw.println(
	// "getMergeResult.getMergeResult.getBase().toString(): "
	// + results.getMergeResult().getBase()
	// .toString());
	// } catch (Exception e) {
	// outw.println("Exception caught: " + e.toString());
	// }
	// try {
	// outw.println(
	// "getMergeResult.getMergeResult.getFailingPaths().toString(): "
	// + results.getMergeResult().getFailingPaths()
	// .toString());
	// } catch (Exception e) {
	// outw.println("Exception caught: " + e.toString());
	// }
	//
	// try {
	// outw.println(
	// "getMergeResult.getMergeResult.getMergeStatus().toString(): "
	// + results.getMergeResult().getMergeStatus()
	// .toString());
	// } catch (Exception e) {
	// outw.println("Exception caught: " + e.toString());
	// }
	//
	// try {
	// outw.println("getMergeResult.getConflicts().toString(): "
	// + results.getMergeResult().getConflicts().toString());
	// } catch (Exception e) {
	// outw.println("Exception caught: " + e.toString());
	// }
	//
	// try {
	// outw.println("getMergeResult.getNewHead().toString(): "
	// + results.getMergeResult().getNewHead().toString());
	// } catch (Exception e) {
	// outw.println("Exception caught: " + e.toString());
	// }
	//
	// try {
	// outw.println(
	// "getMergeResult.getCheckoutConflicts().toString(): "
	// + results.getMergeResult()
	// .getCheckoutConflicts().toString());
	// } catch (Exception e) {
	// outw.println("Exception caught: " + e.toString());
	// }
	//
	// }
	//
	// if (results.getRebaseResult() != null) {
	//
	// outw.println("\n\n********* REBASE RESULTS **************\n\n");
	//
	// outw.println("Rebase: " + results.getRebaseResult() + "\n");
	// }
	//
	// }
}

