/*
 * Developed 2020, Ian Mahaffy
 *
 * Heavily referenced from code by the following:
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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseResult;
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
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

@Command(common = true, usage = "usage_PullChangesFromRemoteRepositories")
class Pull extends TextBuiltin {

	/**
	 * Arguement for the remote repository. Will default to origin.
	 *
	 * References: Req. 1.1
	 */
	@Argument(index = 0, metaVar = "metaVar_uriish")
	private String remote = Constants.DEFAULT_REMOTE_NAME;

	/**
	 * Arguement for the remote branch.
	 *
	 * References: Req. 1.1
	 */
	@Argument(index = 1, metaVar = "metaVar_uriish")
	private String remoteBranchName = null;

	/**
	 * Variable that holds the default rebase mode.
	 *
	 * References: Req. 1.3
	 */
	private BranchRebaseMode pullRebaseMode = BranchRebaseMode.NONE;

	/**
	 * Option to set the rebase mode to true.
	 *
	 * References: Req. 1.3
	 *
	 * @param ignored
	 */
	@Option(name = "--rebase", aliases = { "-r" })
	void RebaseMode(@SuppressWarnings("unused")
	final boolean ignored) {
		pullRebaseMode = BranchRebaseMode.REBASE;
	}

	/**
	 * Option to set the rebase mode to preserve.
	 *
	 * References: Req. 1.3
	 *
	 * @param ignored
	 */
	@Option(name = "--rebase-preserve")
	void PreserveRebase(@SuppressWarnings("unused")
	final boolean ignored) {
		pullRebaseMode = BranchRebaseMode.PRESERVE;
	}

	/**
	 * Option to set the rebase mode to interactive.
	 *
	 * References: Req. 1.3
	 *
	 * @param ignored
	 */
	@Option(name = "--rebase-interactive")
	void InteractiveRebase(@SuppressWarnings("unused")
	final boolean ignored) {
		pullRebaseMode = BranchRebaseMode.INTERACTIVE;
	}

	/**
	 * Option to set the rebase mode to false, which is default.
	 *
	 * References: Req. 1.3
	 *
	 * @param ignored
	 */
	@Option(name = "--rebase-false", aliases = { "--no-rebase" })
	void NoRebase(@SuppressWarnings("unused")
	final boolean ignored) {
		pullRebaseMode = BranchRebaseMode.NONE;
	}

	/**
	 * Variable that holds the default fast forward mode.
	 *
	 * References: Req. 1.4
	 */
	private FastForwardMode ff = FastForwardMode.FF;

	/**
	 * Option to set the rebase mode to Fast Forward, which is default.
	 *
	 * References: Req. 1.4
	 *
	 * @param ignored
	 */
	@Option(name = "--ff", usage = "usage_mergeFf")
	void ff(@SuppressWarnings("unused")
	final boolean ignored) {
		ff = FastForwardMode.FF;
	}

	/**
	 * Option to set the rebase mode to false.
	 *
	 * References: Req. 1.4
	 *
	 * @param ignored
	 */
	@Option(name = "--no-ff", usage = "usage_mergeNoFf")
	void noff(@SuppressWarnings("unused")
	final boolean ignored) {
		ff = FastForwardMode.NO_FF;
	}

	/**
	 * Option to set the rebase mode to Fast Forward Only.
	 *
	 * References: Req. 1.4
	 *
	 * @param ignored
	 */
	@Option(name = "--ff-only", usage = "usage_mergeFfOnly")
	void ffonly(@SuppressWarnings("unused")
	final boolean ignored) {
		ff = FastForwardMode.FF_ONLY;
	}

	/**
	 * Option to print out test output.
	 *
	 * References: Req. 1.2
	 */
	@Option(name = "--test", aliases = { "-t" })
	private boolean showTest;

	/**
	 * Variable that holds the referenced remote string, used for testing.
	 *
	 * References: Req. 1.2
	 */
	private String ref;

	/**
	 * Variable that holds a default merge strategy. Used for printing results
	 * and testing.
	 *
	 * References: Req. 1.2
	 */
	private MergeStrategy mergeStrategy = MergeStrategy.RECURSIVE;

	/**
	 * Run method creates new pull using specified commands, and setting the
	 * options accordingly.
	 *
	 * References: Req. 1.0, Req. 1.1, Req. 1.2, Req. 1.3, Req. 1.4
	 */
	@Override
	protected void run() throws IOException {
		try (Git git = new Git(db)) {
			PullCommand pull = git.pull();
			pull.setRemote(remote);
			pull.setFastForward(ff);
			pull.setRebase(pullRebaseMode);

			if (remoteBranchName != null)
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
	 * Prints out the output for the pull request. Used to show user what the
	 * results of the pull attempt are.
	 *
	 * References: Req. 1.5
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

	/**
	 * Prints out the output for the pull request errors. Used to show user what
	 * the results of the failed pull attempt are.
	 *
	 * References: Req. 1.5
	 *
	 * @param results
	 *            PullResult to be printed
	 * @throws IOException
	 */
	@SuppressWarnings("nls")
	private void getPullErrors(final PullResult results) throws IOException {

		if (results.getFetchResult().getURI() != null)
			outw.println(MessageFormat.format(CLIText.get().fromURI,
					results.getFetchResult().getURI().toString()));
		else
			outw.println("No URI found for Fetch Result\n");
		try {
		for (TrackingRefUpdate u : results.getFetchResult()
				.getTrackingRefUpdates()) {
			final String src = abbreviateRef(u.getRemoteName(), false);
			final String dst = abbreviateRef(u.getLocalName(), true);
			outw.format(" %-10s -> %s", src, dst);
			outw.println();
		}
		} catch (Exception e) {
			System.out.println(e);
		}
		// Print Merge results
		try {
			if (results.getMergeResult() != null) {
				printMergeResults(results.getMergeResult());
			}
		} catch (Exception e) {
			// Do nothing
		}
		try {
			if (results.getRebaseResult() != null) {
				printRebaseResult(results.getRebaseResult());
			}
		} catch (Exception e) {
			// Do nothing
		}
	}

	/**
	 * Prints out messages based on the rebase result. Used to get meaningful
	 * descriptions of errors, and show success information
	 *
	 * References Req. 1.5
	 *
	 * @param result
	 *            RebaseResult to be referenced
	 */
	@SuppressWarnings("nls")
	private void printRebaseResult(RebaseResult result) {
		switch (result.getStatus()) {
		case OK:
			System.out.println("Rebase Successful");
			break;
		case ABORTED:
			System.out.println("Rebase Aborted");
			break;
		case CONFLICTS:
			System.out.println("Conflict in files:" + result.getConflicts());
			break;
		case FAILED:
			System.out.println("Rebase failed; the original HEAD was restored");
			break;
		case NOTHING_TO_COMMIT:
			System.out.println("There is nothing to commit");
			break;
		case STOPPED:
			System.out.println(
					"Rebase stopped due to a conflict; must either abort or resolve or skip");
			break;
		case UNCOMMITTED_CHANGES:
			System.out.println(
					"The repository contains uncommitted changes and the rebase is not a fast-forward");
			break;
		case UP_TO_DATE:
			System.out.println(CLIText.get().alreadyUpToDate);
			break;
		default:
			System.out
					.println("Rebase result: " + result.getStatus().toString());
			break;

		}
	}

	/**
	 * Code pulled from Merge.java
	 *
	 * Prints out messages based on the merge result. Used to get meaningful
	 * descriptions of errors, and show success information
	 *
	 * References Req. 1.5
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
					outw.println(MessageFormat.format(CLIText.get().updating,
							oldId, newId));
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
					outw.println(MessageFormat.format(
							CLIText.get().mergeConflict, collidingPath));
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
				outw.println(
						CLIText.get().mergeWentWellStoppedBeforeCommitting);
				break;
			case MERGED_SQUASHED:
			case FAST_FORWARD_SQUASHED:
			case MERGED_SQUASHED_NOT_COMMITTED:
				outw.println(CLIText.get().mergedSquashed);
				outw.println(
						CLIText.get().mergeWentWellStoppedBeforeCommitting);
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

	/**
	 * Code pulled from Merge.java
	 *
	 * Used to check if the merge was successful
	 *
	 * References Req. 1.5
	 *
	 * @param oldHead
	 * @param src
	 *
	 * @return boolean value representing if the merge was successful
	 * @throws IOException
	 */
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

	/**
	 * Prints the information of the pull command. Used for testing and
	 * troubleshooting purposes.
	 *
	 * References: Req. 1.2, 1.3, 1.4
	 *
	 * @param pull
	 *            PullCommand to be examined and printed out
	 * @throws IOException
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

	/**
	 * Prints the information of the pull result. Used for testing and
	 * troubleshooting purposes.
	 *
	 * References: Req. 1.2, 1.3, 1.4
	 *
	 * @param results
	 *            PullResult to be examined and printed out
	 * @throws IOException
	 */
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