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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BranchConfig.BranchRebaseMode;
import org.eclipse.jgit.lib.Constants;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

@Command(common = true, usage = "usage_PullChangesFromRemoteRepositories")
class Pull extends TextBuiltin {

	/**
	 * Arguement for the remote repository. Will default to origin. References:
	 * Req. 1.1
	 */
	@Argument(index = 0, metaVar = "metaVar_uriish")
	private String remote = Constants.DEFAULT_REMOTE_NAME;

	/**
	 * Arguement for the remote branch. References: Req. 1.1
	 */
	@Argument(index = 1, metaVar = "metaVar_uriish")
	private String remoteBranchName = null;

	/**
	 * Variable that holds the default rebase mode. References: Req. 1.3
	 */
	private BranchRebaseMode pullRebaseMode = BranchRebaseMode.NONE;

	/**
	 * Option to set the rebase mode to true. References: Req. 1.3
	 *
	 * @param ignored
	 */
	@Option(name = "--rebase", aliases = { "-r" })
	void RebaseMode(@SuppressWarnings("unused")
	final boolean ignored) {
		pullRebaseMode = BranchRebaseMode.REBASE;
	}

	/**
	 * Option to set the rebase mode to preserve. References: Req. 1.3
	 *
	 * @param ignored
	 */
	@Option(name = "--rebase-preserve")
	void PreserveRebase(@SuppressWarnings("unused")
	final boolean ignored) {
		pullRebaseMode = BranchRebaseMode.PRESERVE;
	}

	/**
	 * Option to set the rebase mode to interactive. References: Req. 1.3
	 *
	 * @param ignored
	 */
	@Option(name = "--rebase-interactive")
	void InteractiveRebase(@SuppressWarnings("unused")
	final boolean ignored) {
		pullRebaseMode = BranchRebaseMode.INTERACTIVE;
	}

	/**
	 * Option to set the rebase mode to false, which is default. References:
	 * Req. 1.3
	 *
	 * @param ignored
	 */
	@Option(name = "--rebase-false", aliases = { "--no-rebase" })
	void NoRebase(@SuppressWarnings("unused")
	final boolean ignored) {
		pullRebaseMode = BranchRebaseMode.NONE;
	}

	/**
	 * Variable that holds the default fast forward mode. References: Req. 1.4
	 */
	private FastForwardMode ff = FastForwardMode.FF;

	/**
	 * Option to set the rebase mode to Fast Forward, which is default.
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
	 * Option to set the rebase mode to false. References: Req. 1.4
	 *
	 * @param ignored
	 */
	@Option(name = "--no-ff", usage = "usage_mergeNoFf")
	void noff(@SuppressWarnings("unused")
	final boolean ignored) {
		ff = FastForwardMode.NO_FF;
	}

	/**
	 * Option to set the rebase mode to Fast Forward Only. References: Req. 1.4
	 *
	 * @param ignored
	 */
	@Option(name = "--ff-only", usage = "usage_mergeFfOnly")
	void ffonly(@SuppressWarnings("unused")
	final boolean ignored) {
		ff = FastForwardMode.FF_ONLY;
	}

	/**
	 * Option to print out test output. References: Req. 1.2
	 */
	@Option(name = "--test", aliases = { "-t" })
	private boolean showTest;

	/**
	 * Variable that holds the referenced remote string, used for testing.
	 * References: Req. 1.2
	 */
	private String ref;

	/**
	 * Run method creates new pull using specified commands, and setting the
	 * options accordingly. References: Req. 1.0, Req. 1.1, Req. 1.2, Req. 1.3,
	 * Req. 1.4
	 */
	@Override
	protected void run() throws IOException {
		try (Git git = new Git(db)) {
			PullCommand pull = git.pull();

			pull.setRemote(remote);

			if (remoteBranchName != null)
				pull.setRemoteBranchName(remoteBranchName);

			pull.setRebase(pullRebaseMode);
			pull.setFastForward(ff);

			PullResult result = pull.call();

			try {
				ref = pull.getRemote() + "/" + pull.getRemoteBranchName(); //$NON-NLS-1$
			} catch (Exception e) {
				throw die(e.getMessage(), e);
			}

			if (showTest) {
				printTest(pull);
				printTest(result);
			}

		} catch (GitAPIException e) {
			throw die(e.getMessage(), e);
		}
	}

	/**
	 * Prints the information of the pull command. Used for testing and
	 * troubleshooting purposes. References: Req. 1.2, 1.3, 1.4
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
	 * troubleshooting purposes. References: Req. 1.2, 1.3, 1.4
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