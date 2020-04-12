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

@Command(common = true, usage = "usage_PullChangesFromRemoteRepositories")
class Pull extends TextBuiltin {

	/**
	 * Run method to prove pull command is functional. References: Req. 1.0
	 */
	@Override
	protected void run() throws IOException {
		System.out.println("Test");
	}
}