# Clear Branch Command

This command sequence helps you safely clean up feature branches that have been merged into main.

## Steps

1. Check for uncommitted changes in current feature branch
```bash
git status
```
If there are any changes, abort the operation.

2. Switch to main branch and fetch latest changes
```bash
git checkout main
git fetch origin main
```

3. Check if feature branch is merged into main and delete if merged
```bash
# Replace FEATURE_BRANCH with your branch name
git branch --merged main | grep FEATURE_BRANCH
if [ $? -eq 0 ]; then
    git branch -d FEATURE_BRANCH
    echo "Feature branch has been deleted"
else
    echo "Feature branch is not merged into main. Skipping deletion."
fi
```

## Usage

1. Make sure you're on the feature branch you want to check
2. Run the commands in sequence
3. If any step fails, review the output and take appropriate action 