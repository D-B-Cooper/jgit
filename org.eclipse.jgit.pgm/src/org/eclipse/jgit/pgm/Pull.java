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
import org.eclipse.jgit.lib.Constants;
//import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
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
				try (ObjectReader reader = db.newObjectReader()) {
					//printPullResult(reader, result.getURI(), result);
				outw.println("Output:  " + results.toString()); //$NON-NLS-1$
				if (results.getFetchResult() != null)
					outw.println("Fetch:  " + results.getFetchResult() + "\n"); //$NON-NLS-1$//$NON-NLS-2$
				if (results.getMergeResult() != null)
					outw.println("Merge:  " + results.getMergeResult() + "\n"); //$NON-NLS-1$//$NON-NLS-2$
				if (results.getRebaseResult() != null)
					outw.println(
							"Rebase:  " + results.getRebaseResult() + "\n"); //$NON-NLS-1$//$NON-NLS-2$
			} catch ( IOException e) {
			throw die(e.getMessage(), e);
			}


//			try {
//			pull.call();
//			} catch (Exception e) {
//				outw.println("Failure:  " + e.toString());
//			}
		} catch (GitAPIException e) {
			throw die(e.getMessage(), e);
		}
	}

//	private void printPullResult(final ObjectReader reader, //final URIish uri,
//			final PullResult result) throws IOException {
//		shownURI = false;
//		boolean everythingUpToDate = true;
//
//
//
//
//		// at first, print up-to-date ones...
//		for (RemoteRefUpdate rru : result.getRemoteUpdates()) {
//			if (rru.getStatus() == Status.UP_TO_DATE) {
//				if (verbose)
//					printRefUpdateResult(reader, uri, result, rru);
//			} else
//				everythingUpToDate = false;
//		}
//
//		for (RemoteRefUpdate rru : result.getRemoteUpdates()) {
//			// ...then successful updates...
//			if (rru.getStatus() == Status.OK)
//				printRefUpdateResult(reader, uri, result, rru);
//		}
//
//		for (RemoteRefUpdate rru : result.getRemoteUpdates()) {
//			// ...finally, others (problematic)
//			if (rru.getStatus() != Status.OK
//					&& rru.getStatus() != Status.UP_TO_DATE)
//				printRefUpdateResult(reader, uri, result, rru);
//		}
//
//		AbstractFetchCommand.showRemoteMessages(errw, result.getMessages());
//		if (everythingUpToDate)
//			outw.println(CLIText.get().everythingUpToDate);
//	}
}
