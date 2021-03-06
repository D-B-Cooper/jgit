/*
 * Copyright (C) 2014 Google Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.eclipse.jgit.pgm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.junit.JGitTestUtil;
import org.eclipse.jgit.lib.CLIRepositoryTestCase;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;

public class RepoTest extends CLIRepositoryTestCase {
	private Repository defaultDb;
	private Repository notDefaultDb;
	private Repository groupADb;
	private Repository groupBDb;

	private String rootUri;
	private String defaultUri;
	private String notDefaultUri;
	private String groupAUri;
	private String groupBUri;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		defaultDb = createWorkRepository();
		try (Git git = new Git(defaultDb)) {
			JGitTestUtil.writeTrashFile(defaultDb, "hello.txt", "world");
			git.add().addFilepattern("hello.txt").call();
			git.commit().setMessage("Initial commit").call();
		}

		notDefaultDb = createWorkRepository();
		try (Git git = new Git(notDefaultDb)) {
			JGitTestUtil.writeTrashFile(notDefaultDb, "world.txt", "hello");
			git.add().addFilepattern("world.txt").call();
			git.commit().setMessage("Initial commit").call();
		}

		groupADb = createWorkRepository();
		try (Git git = new Git(groupADb)) {
			JGitTestUtil.writeTrashFile(groupADb, "a.txt", "world");
			git.add().addFilepattern("a.txt").call();
			git.commit().setMessage("Initial commit").call();
		}

		groupBDb = createWorkRepository();
		try (Git git = new Git(groupBDb)) {
			JGitTestUtil.writeTrashFile(groupBDb, "b.txt", "world");
			git.add().addFilepattern("b.txt").call();
			git.commit().setMessage("Initial commit").call();
		}

		resolveRelativeUris();
	}

	@Test
	public void testMissingPath() throws Exception {
		try {
			execute("git repo");
			fail("Must die");
		} catch (Die e) {
			// expected, requires argument
		}
	}

	/**
	 * See bug 484951: "git repo -h" should not print unexpected values
	 *
	 * @throws Exception
	 */
	@Test
	public void testZombieHelpArgument() throws Exception {
		String[] output = execute("git repo -h");
		String all = Arrays.toString(output);
		assertTrue("Unexpected help output: " + all,
				all.contains("jgit repo"));
		assertFalse("Unexpected help output: " + all,
				all.contains("jgit repo VAL"));
	}

	@Test
	public void testAddRepoManifest() throws Exception {
		StringBuilder xmlContent = new StringBuilder();
		xmlContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
			.append("<manifest>")
			.append("<remote name=\"remote1\" fetch=\".\" />")
			.append("<default revision=\"master\" remote=\"remote1\" />")
			.append("<project path=\"foo\" name=\"")
			.append(defaultUri)
			.append("\" groups=\"a,test\" />")
			.append("<project path=\"bar\" name=\"")
			.append(notDefaultUri)
			.append("\" groups=\"notdefault\" />")
			.append("<project path=\"a\" name=\"")
			.append(groupAUri)
			.append("\" groups=\"a\" />")
			.append("<project path=\"b\" name=\"")
			.append(groupBUri)
			.append("\" groups=\"b\" />")
			.append("</manifest>");
		writeTrashFile("manifest.xml", xmlContent.toString());
		StringBuilder cmd = new StringBuilder("git repo --base-uri=\"")
			.append(rootUri)
			.append("\" --groups=\"all,-a\" \"")
			.append(db.getWorkTree().getAbsolutePath())
			.append("/manifest.xml\"");
		execute(cmd.toString());

		File file = new File(db.getWorkTree(), "foo/hello.txt");
		assertFalse("\"all,-a\" doesn't have foo", file.exists());
		file = new File(db.getWorkTree(), "bar/world.txt");
		assertTrue("\"all,-a\" has bar", file.exists());
		file = new File(db.getWorkTree(), "a/a.txt");
		assertFalse("\"all,-a\" doesn't have a", file.exists());
		file = new File(db.getWorkTree(), "b/b.txt");
		assertTrue("\"all,-a\" has have b", file.exists());
	}

	private void resolveRelativeUris() {
		// Find the longest common prefix ends with "/" as rootUri.
		defaultUri = defaultDb.getDirectory().toURI().toString();
		notDefaultUri = notDefaultDb.getDirectory().toURI().toString();
		groupAUri = groupADb.getDirectory().toURI().toString();
		groupBUri = groupBDb.getDirectory().toURI().toString();
		int start = 0;
		while (start <= defaultUri.length()) {
			int newStart = defaultUri.indexOf('/', start + 1);
			String prefix = defaultUri.substring(0, newStart);
			if (!notDefaultUri.startsWith(prefix) ||
					!groupAUri.startsWith(prefix) ||
					!groupBUri.startsWith(prefix)) {
				start++;
				rootUri = defaultUri.substring(0, start) + "manifest";
				defaultUri = defaultUri.substring(start);
				notDefaultUri = notDefaultUri.substring(start);
				groupAUri = groupAUri.substring(start);
				groupBUri = groupBUri.substring(start);
				return;
			}
			start = newStart;
		}
	}
}
