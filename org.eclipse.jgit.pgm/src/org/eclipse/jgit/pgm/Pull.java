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
//import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BranchConfig.BranchRebaseMode;
import org.eclipse.jgit.lib.Constants;
//import org.eclipse.jgit.lib.ObjectId;
//import org.eclipse.jgit.lib.ObjectReader;
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
		outw.println("Pull Added: " + remote);
		
		try(Git git = new Git(db)) {
			PullCommand pull = git.pull();
			pull.setRemote(remote);
			pull.setFastForward(ff);
			pull.setRebase(pullRebaseMode);
			
			outw.println("Pull Added: " + pull.getRemote() + ",   " + ff.toString() + ",   " + pullRebaseMode.toString());
			try {
			pull.call();
			} catch (Exception e) {
				outw.println("Failure:  " + e.toString());
			}
			outw.println("Success");
		} catch ( IOException e) {
			throw die(e.getMessage(), e);
		}
	}
}
