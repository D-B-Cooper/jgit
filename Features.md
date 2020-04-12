## JGit
### Feature 1
  #### Story A - Add Pull Command Line Command
   - Task 1: Add a pull command to the CLI, callable with "jgit pull"
   - Task 2: Add remote and remote branch arguments to pull command
   - Task 3: Add a test option to pull command, which outputs raw data about pull (fetch and merge outputs)
   - Task 4: Add rebase options to pull command 
   - Task 5: Add fast forward options to pull command 
   - Task 6: Add standardized output for pull command
   
  #### Story B - Add Abort to Merge Command
   - Task 1: Add "--abort" option to merge command 
   - Task 2: Update merge CLI command to use abort
  
  #### Story C - Add Continue to Merge Command
   - Task 1: Add "--continue" option to merge command 
   - Task 2: Update merge CLI command to use abort
   
  #### Story D - Add "Merges" to Rebase Command
   - Task 1: Add "--rebase-merges" as an option for rebase interface
   - Task 2: Add functionality to merges option
   - Task 3: Update other commands that implement rebase command
   - Task 4: Update docs to show --preserve-merges
   
  #### Story E - Add "-help" option to common commands
   - Task 1: Add "-help" option to fetch command that outputs the usage and options.
   - Task 2: Add "-help" option to merge command that outputs the usage and options.
   - Task 3: Add "-help" option to push command that outputs the usage and options.
   - Task 4: Add "-help" option to pull command that outputs the usage and options.
   - Task 5: Add "-help" option to add command that outputs the usage and options.
   - Task 6: Add "-help" option to clone command that outputs the usage and options.
   - Task 7: Add "-help" option to commit command that outputs the usage and options.
   - Task 8: Add "-help" option to branch command that outputs the usage and options.
