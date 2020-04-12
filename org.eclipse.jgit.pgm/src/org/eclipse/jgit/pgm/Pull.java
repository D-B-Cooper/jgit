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
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.kohsuke.args4j.Argument;

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
	 * Run method creates new pull using specified commands. References: Req.
	 * 1.0, Req. 1.1
	 */
	@Override
	protected void run() throws IOException {
		try (Git git = new Git(db)) {
			PullCommand pull = git.pull();
			pull.setRemote(remote);

			if (remoteBranchName != null)
				pull.setRemoteBranchName(remoteBranchName);

			pull.call();

		} catch (GitAPIException e) {
			throw die(e.getMessage(), e);
		}
	}
}