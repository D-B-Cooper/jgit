## JGit
### Feature 1
  #### Story A - Add Pull Command Line Command ( https://git-scm.com/docs/git-pull )
   - Task 1: Add a pull command to the CLI, callable with "jgit pull"
   - Task 2: Add remote and remote branch arguments to pull command
   - Task 3: Add a test option to pull command, which outputs raw data about pull (fetch and merge outputs)
   - Task 4: Add rebase options to pull command 
   - Task 5: Add fast forward options to pull command 
   - Task 6: Add standardized output for pull command
   
  #### Story B - Add Abort to Merge Command ( https://git-scm.com/docs/git-merge )
   - Task 1: Add "--abort" option to merge command 
   - Task 2: Update merge CLI command to use abort
  
  #### Story C - Add Continue to Merge Command ( https://git-scm.com/docs/git-merge )
   - Task 1: Add "--continue" option to merge command 
   - Task 2: Update merge CLI command to use abort
   
  #### Story D - Add "Merges" to Rebase Command ( https://git-scm.com/docs/git-rebase )
   - Task 1: Add "--rebase-merges" as an option for rebase interface
   - Task 2: Add functionality to merges option
   - Task 3: Update other commands that implement rebase command
   - Task 4: Update docs to show --preserve-merges
  
