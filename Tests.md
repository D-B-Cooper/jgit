Test Merge:

REQUIREMENT 2.0
M1) Merge will properly exit if no ref is entered
--Enter command like "jgit merge"
--- Expected output is a warning about misuse of the command, and --help notice


REQUIREMENT 2.0
M2) Try to abort without an active merge
--With no active merge: "jgit merge --abort"'
--- Expected output is a warning about no active merge, and --help notice

REQUIREMENT 2.0
M3) Abort an active merge
-- With an active merge (with conflicts): "jgit merge --abort"
--- Expected output is a notification about a successful abort. Files will be restored to the state before the merge attempt


Test Pull (unless specified, each of these commands require the remote branch has a new change to pull)

REQUIREMENT 1.1, 1.5
P1) Pull with specified branch
-- "jgit pull origin RemoteBranchName"
--- Expected output is a confirmation of pull


REQUIREMENT 1.0, 1.5
P2) Pull without specifying branch and remote
-- "jgit pull"
--- Expected output is a confirmation of pull

REQUIREMENT 1.0, 1.5
P2.5) Pull without specifying branch and remote, with conflict
-- "jgit pull"
--- Expected output is a merge conflict

REQUIREMENT 1.2, 1.5
P3) Pull with specified branch, with the test output
-- "jgit pull origin RemoteBranchName --test"
--- Expected output is a confirmation of pull, with test output

REQUIREMENT 1.2, 1.5
P3.5) Pull with specified branch, with the test output, and a merge conflict
-- With merge conflict, use "jgit pull origin RemoteBranchName --test"
--- Expected output is a merge conflict, and test output

REQUIREMENT 1.5
P5) Pull with no new changes on remote
-- With no changes to remote: "jgit pull"
--- Expected output is "Already up to date"

REQUIREMENT 1.2, 1.5
P6) Pull with no new changes on remote, using test
-- With no changes to remote: "jgit pull origin RemoteBranchName --test"
--- Expected output is "Already up to date" with test output

NOTE: Rebase has very little error handling due to code outside of Pull. 
When attempting a rebase command, reference git documentation for what a rebase is meant to do:
https://git-scm.com/docs/git-rebase
https://git-scm.com/docs/git-pull

REQUIREMENT 1.3, 1.5
P7) Pull with rebase
-- "jgit pull origin RemoteBranchName -r"
--- Expected output is a confirmation of pull using rebase method

REQUIREMENT 1.3, 1.5
P7.5) Pull with rebase, with conflict
-- With a conflict setup, use "jgit pull origin RemoteBranchName -r"
--- Expected output is a rebase conflict

REQUIREMENT 1.3, 1.5
P7.6) Pull with rebase, with no files to commit
-- With a conflict setup, use "jgit pull origin RemoteBranchName -r"
--- Expected output is "Already up to date"

REQUIREMENT 1.3, 1.5
P8) Pull with rebase preserve
-- "jgit pull origin RemoteBranchName --rebase-preserve"
--- Expected output is a confirmation of pull using rebase method

REQUIREMENT 1.3, 1.5
P8.5) Pull with rebase preserve, with conflict
-- With a conflict setup, "jgit pull origin RemoteBranchName --rebase-preserve"
--- Expected output is a rebase conflict

REQUIREMENT 1.3, 1.5
P8.6) Pull with rebase preserve, with no files to commit
-- With a conflict setup, "jgit pull origin RemoteBranchName --rebase-preserve"
--- Expected output is "Already up to date"

REQUIREMENT 1.3, 1.5
P9) Pull with rebase interactive
-- "jgit pull origin RemoteBranchName --rebase-interactive"
--- Expected output is a confirmation of pull using rebase method

REQUIREMENT 1.3, 1.5
P9.5) Pull with rebase interactive, with conflict
-- With a conflict setup, "jgit pull origin RemoteBranchName --rebase-interactive"
--- Expected output is a rebase conflict

REQUIREMENT 1.3, 1.5
P9.6) Pull with rebase interactive, with no files to commit
-- With a conflict setup, "jgit pull origin RemoteBranchName rebase-interactive"
--- Expected output is "Already up to date"

REQUIREMENT 1.4, 1.5
P10) Pull with Fast-forward
-- "jgit pull origin RemoteBranchName --ff"
--- Expected output is a confirmation of pull using recursive merge

REQUIREMENT 1.4, 1.5
P10.5) Pull with Fast-forward
-- "jgit pull origin RemoteBranchName --ff"
--- Expected output is "Already up to date"

REQUIREMENT 1.4, 1.5
P11) Pull with no Fast-forward
-- "jgit pull origin RemoteBranchName --no-ff"
--- Expected output is a confirmation of pull using recursive merge

REQUIREMENT 1.4, 1.5
P11.5) Pull with no Fast-forward, already up to date
-- With no changes in remote, run "jgit pull origin RemoteBranchName --no-ff"
--- Expected output is "Already up to date"

REQUIREMENT 1.4, 1.5
P12) Pull with Fast-forward only
-- "jgit pull origin RemoteBranchName --ff-only"
--- Expected output is a confirmation of pull

REQUIREMENT 1.4, 1.5
P12.5) Pull with Fast-forward only
-- "jgit pull origin RemoteBranchName --ff-only"
--- Expected output is "Already up to date"
	