## Software Requirements
### Functional Requirements
  #### User Story A - As a JGit command line user, I want to be able to use a pull command, so I do not need to use fetch and merge separately
   - Req. 1.0: The executable JGit CLI shall include a pull command.
   - Req. 1.1: The JGit CLI pull command shall accept specified remote and remote branch names.
   - Req. 1.2: The JGit CLI pull command shall accept a test option that outputs additional options and output from command.
   - Req. 1.3: The JGit CLI pull command shall accept a rebase option ("--rebase" "-r").
   - Req. 1.4: The JGit CLI pull command shall accept fast forward options ("--ff", "--no-ff", "--ff-only").
   - Req. 1.5: The JGit CLI pull command shall output information about the fetch and merge statuses.

  #### User Story B - As a JGit command line user, I want to be able to abort an active merge, so I do not need to manually cancel a merge.
   - Req. 2.0: The JGit CLI merge command shall accept an abort option to abort an active merge ("--abort").

  #### User Story C - As a JGit command line user, I want to be able to continue an active merge, so I can easily proceed with a merge.
   - Req. 2.1: The JGit CLI merge command shall accept a continue option to continue with an in progress merge ("--continue").
  
  #### User Story D - As a JGit user, I want to be able to utilize the "merges" option on rebase, so I do not need to use the depreciated alternative.
   - Req. 3.0: The JGit rebase implementation shall include the merges option
   - Req. 3.1: JGit commands that implement the rebase option shall include the merges option

  #### User Story E - As a JGit command line user, I want to be able to use a help option of common commands, so I am able to see usages and possible options.
   - Req. 4.0: The JGit CLI shall include a "-help" for common commands containing usage and options.
   - Req. 4.1: The JGit CLI fetch command shall include a "-help" option that outputs usage and options.
   - Req. 4.2: The JGit CLI merge command shall include a "-help" option that outputs usage and options.
   - Req. 4.3: The JGit CLI push command shall include a "-help" option that outputs usage and options.
   - Req. 4.4: The JGit CLI pull command shall include a "-help" option that outputs usage and options.
   - Req. 4.5: The JGit CLI add command shall include a "-help" option that outputs usage and options.
   - Req. 4.6: The JGit CLI clone command shall include a "-help" option that outputs usage and options.
   - Req. 4.7: The JGit CLI commit command shall include a "-help" option that outputs usage and options.
   - Req. 4.8: The JGit CLI branch command shall include a "-help" option that outputs usage and options.
