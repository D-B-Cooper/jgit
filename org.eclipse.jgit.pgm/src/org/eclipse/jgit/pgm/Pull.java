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
import org.eclipse.jgit.api.MergeResult;
//import org.eclipse.jgit.api.MergeCommand.FastForwardMode.Merge;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BranchConfig.BranchRebaseMode;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger.MergeFailureReason;
import org.eclipse.jgit.pgm.internal.CLIText;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
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

	@Argument(index = 1, metaVar = "metaVar_uriish")
	private String remoteBranchName = null;

	private BranchRebaseMode pullRebaseMode = BranchRebaseMode.NONE;

	// TODO Uncomment after Rebase Implementation
	// @Option(name = "--rebase", aliases = { "-r" })
	// void pullRebaseMode(@SuppressWarnings("unused") final boolean ignored) {
	// pullRebaseMode = BranchRebaseMode.REBASE;
	// }

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

	@Option(name = "--test", aliases = { "-t" })
	private boolean showTest;

	private MergeStrategy mergeStrategy = MergeStrategy.RECURSIVE;

	private String ref;

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

			// sets the ref variable
			try {
			ref = pull.getRemote() + "/" + pull.getRemoteBranchName(); //$NON-NLS-1$
			} catch (Exception e) {
				throw die(e.getMessage(), e);
			}
			if (showTest)
				printTest(pull);

			printPullResult(results);


	} catch (GitAPIException e) {
		throw die(e.getMessage(), e);
	}
	}

	/**
	 * Passed the PullResults of a PullCommand that has been called.
	 *
	 * @param results
	 *            PullResult to be printed
	 * @throws IOException
	 */
	@SuppressWarnings("nls")
	private void printPullResult(final PullResult results) throws IOException {
		if (showTest)
			printTest(results);

		if (!results.isSuccessful()) {
			getPullErrors(results);
			return;
		}

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

		// Print Merge results
		try {
			printMergeResults(results.getMergeResult());
		} catch (Exception e) {
			outw.println("Error: " + e.toString());
		}
	}

	@SuppressWarnings("nls")
	private void getPullErrors(final PullResult results) throws IOException {

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
			printMergeResults(results.getMergeResult());
		}

	}

	/**
	 * Code pulled from Merge.java
	 *
	 * @param result
	 *            MergeResult to be referenced
	 */
	private void printMergeResults(MergeResult result) {
		try {
		final ObjectId src = db.resolve(ref + "^{commit}"); //$NON-NLS-1$
		if (src == null) {
			throw die(MessageFormat
					.format(CLIText.get().refDoesNotExistOrNoCommit, ref));
		}
		Ref oldHead = db.exactRef(Constants.HEAD);
		if (oldHead == null) {
			throw die(CLIText.get().onBranchToBeBorn);
		}
		switch (result.getMergeStatus()) {
		case ALREADY_UP_TO_DATE:
			outw.println(CLIText.get().alreadyUpToDate);
			break;
		case FAST_FORWARD:
			ObjectId oldHeadId = oldHead.getObjectId();
			if (oldHeadId != null) {
				String oldId = oldHeadId.abbreviate(7).name();
				String newId = result.getNewHead().abbreviate(7).name();
				outw.println(MessageFormat.format(CLIText.get().updating, oldId,
						newId));
			}
			outw.println(result.getMergeStatus().toString());
			break;
		case CHECKOUT_CONFLICT:
			outw.println(CLIText.get().mergeCheckoutConflict);
			for (String collidingPath : result.getCheckoutConflicts()) {
				outw.println("\t" + collidingPath); //$NON-NLS-1$
			}
			outw.println(CLIText.get().mergeCheckoutFailed);
			break;
		case CONFLICTING:
			for (String collidingPath : result.getConflicts().keySet())
				outw.println(MessageFormat.format(CLIText.get().mergeConflict,
						collidingPath));
			outw.println(CLIText.get().mergeFailed);
			break;
		case FAILED:
			for (Map.Entry<String, MergeFailureReason> entry : result
					.getFailingPaths().entrySet())
				switch (entry.getValue()) {
				case DIRTY_WORKTREE:
				case DIRTY_INDEX:
					outw.println(CLIText.get().dontOverwriteLocalChanges);
					outw.println("        " + entry.getKey()); //$NON-NLS-1$
						outw.println("Commit your local changes"); //$NON-NLS-1$
					break;
				case COULD_NOT_DELETE:
					outw.println(CLIText.get().cannotDeleteFile);
					outw.println("        " + entry.getKey()); //$NON-NLS-1$
					break;
				}
			break;
		case MERGED:
			MergeStrategy strategy = isMergedInto(oldHead, src)
					? MergeStrategy.RECURSIVE
					: mergeStrategy;
			outw.println(MessageFormat.format(CLIText.get().mergeMadeBy,
					strategy.getName()));
			break;
		case MERGED_NOT_COMMITTED:
			outw.println(CLIText.get().mergeWentWellStoppedBeforeCommitting);
			break;
		case MERGED_SQUASHED:
		case FAST_FORWARD_SQUASHED:
		case MERGED_SQUASHED_NOT_COMMITTED:
			outw.println(CLIText.get().mergedSquashed);
			outw.println(CLIText.get().mergeWentWellStoppedBeforeCommitting);
			break;
		case ABORTED:
			throw die(CLIText.get().ffNotPossibleAborting);
		case NOT_SUPPORTED:
			outw.println(MessageFormat.format(
					CLIText.get().unsupportedOperation, result.toString()));
		}
		} catch (IOException e) {
			throw die(e.getMessage(), e);
		}
	}

	private boolean isMergedInto(Ref oldHead, AnyObjectId src)
			throws IOException {
		try (RevWalk revWalk = new RevWalk(db)) {
			ObjectId oldHeadObjectId = oldHead.getPeeledObjectId();
			if (oldHeadObjectId == null)
				oldHeadObjectId = oldHead.getObjectId();
			RevCommit oldHeadCommit = revWalk.lookupCommit(oldHeadObjectId);
			RevCommit srcCommit = revWalk.lookupCommit(src);
			return revWalk.isMergedInto(oldHeadCommit, srcCommit);
		}
	}

	/*
	 * Prints the information of the pull command
	 */
	@SuppressWarnings("nls")
	private void printTest(final PullCommand pull) throws IOException {
		outw.println(
				"\n***************** Pull Command Details ***************\n");

		StringBuilder enteredOptions = new StringBuilder();
		enteredOptions.append("****** Entered Options *****\n");
		if (remote != null)
			enteredOptions.append("Remote:  " + remote + ";  ");
		else
			enteredOptions.append("Remote:  !RETURNED NULL;  ");

		if (remoteBranchName != null)
			enteredOptions
					.append("Remote Branch:  " + remoteBranchName + ";  ");
		else
			enteredOptions.append(
					"Remote Branch:  !RETURNED NULL, default will be used;  ");

		if (pullRebaseMode.toString() != null)
			enteredOptions
					.append("Rebase Mode: " + pullRebaseMode.toString() + "; ");
		else
			enteredOptions.append("Rebase Mode: !RETURNED NULL; ");

		if (ff != null)
			enteredOptions
					.append("Fast Forward Mode:  " + ff.toString() + ";  ");
		else
			enteredOptions.append("Fast Forward Mode:  !RETURNED NULL;  ");

		if (mergeStrategy != null)
			enteredOptions.append(
					"Merge Strategy:  " + mergeStrategy.getName() + ";  ");
		else
			enteredOptions.append("Merge Strategy:  !RETURNED NULL;  ");

		if (ref != null)
			enteredOptions.append("Internal ref Variable:  " + ref + ";  ");
		else
			enteredOptions.append("Internal ref Variable:  !RETURNED NULL;  ");

		enteredOptions.append("\n\n****************************\n");

		outw.println(enteredOptions.toString());

		if (pull.toString() != null)
			outw.println("To String:  " + pull.toString());

		if (pull.getRemote() != null)
			outw.println("Remote:  " + pull.getRemote());
		else
			outw.println("Remote: NO REMOTE FOUND");

		if (pull.getRemoteBranchName() != null)
			outw.println("Remote Branch:   " + pull.getRemoteBranchName());
		else
			outw.println("Remote Branch:  NO REMOTE BRANCH FOUND");

		outw.println(
				"\n*****************************************************\n\n");

	}

	@SuppressWarnings("nls")
	private void printTest(final PullResult results) throws IOException {
		outw.println(
				"\n***************** Pull Result Options ***************\n");

		outw.println("Pull Results Successful:  " + results.isSuccessful());

		if (results.toString() != null)
			outw.println("ToString:  " + results.toString());
		else
			outw.println("ToString:   NONE FOUND");

		if (results.getFetchResult() != null)
			outw.println(
					"Fetch Result:  " + results.getFetchResult().toString());
		else
			outw.println("Fetch Result:   NONE FOUND");

		if (results.getFetchedFrom() != null)
			outw.println("Fetched From:  " + results.getFetchedFrom());
		else
			outw.println("Fetched From:   NONE FOUND");

		if (results.getMergeResult() != null)
			outw.println(
					"Merge Result:  " + results.getMergeResult().toString());
		else
			outw.println("Merge Result:   NONE FOUND");

		if (results.getRebaseResult() != null)
			outw.println(
					"Rebase Result: " + results.getRebaseResult().toString());
		else
			outw.println("Rebase Result: NONE FOUND");

		outw.println(
				"\n*****************************************************\n\n");

	}


}

