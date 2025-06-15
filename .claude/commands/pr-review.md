# PR Review and Merge Process

Execute the complete process of reviewing and merging GitHub pull requests.

## Steps

1. **Review PR Details**
```bash
gh pr view [$PR_NUMBER] --repo YamazakiYusuke/TalkToBook

2. Check Related Issues
# Check issues linked to the PR
gh pr view [$PR_NUMBER] --repo YamazakiYusuke/TalkToBook --json body,title | jq -r '.body' | grep -E
"(closes|fixes|resolves) #[0-9]+"

# Review details of the corresponding issue
gh issue view [ISSUE_NUMBER] --repo YamazakiYusuke/TalkToBook
3. Review Code Diff
gh pr diff [$PR_NUMBER] --repo YamazakiYusuke/TalkToBook
4. Conduct Review
- Requirements Fulfillment: Check if Acceptance Criteria from related issues are met
- Code Quality: Evaluate from architecture, security, and best practices perspectives
- Implementation Scope: Verify tasks defined in issues are properly implemented
- Testing: Check test coverage against issue requirements
- Point out specific improvements or recommendations if any
- Make overall assessment and decide on Approval/Request Changes/Comment
5. Add LGTM Comment (if approved)
gh pr comment [$PR_NUMBER] --repo YamazakiYusuke/TalkToBook --body "LGTM!

✅ **[Assessment Summary]**

**Issue Requirements Met:**
- [List fulfillment status of issue requirements]

**Code Quality:**
[List specific positive points]

**Ready to merge!**"
6. Merge PR
gh pr merge [$PR_NUMBER] --repo YamazakiYusuke/TalkToBook --merge
7. Delete Branch
# Get branch name
gh pr view [$PR_NUMBER] --repo YamazakiYusuke/TalkToBook --json headRefName

# Delete remote branch
git push origin --delete [branch-name]

Review Criteria

Issue Requirements Check

- Acceptance Criteria: Are all acceptance conditions satisfied?
- Task Completion: Are tasks defined in the issue completed?
- Scope: Does implementation scope match issue requirements?
- Dependencies: Are dependent issues properly handled?

Code Quality Check

- Architecture: Clean Architecture, MVVM, appropriate dependencies
- Code Quality: Readability, maintainability, testability
- Security: API key management, permission handling, data protection
- Performance: Memory usage, processing efficiency
- Best Practices: Compliance with Android development guidelines
- Testing: Unit tests, integration test adequacy

Output Format

Review results should be output in the following format:

## 📋 Issue Requirements Review
**Related Issue**: #[NUMBER] - [TITLE]
**Acceptance Criteria**: ✅/❌ [Fulfillment status of each condition]
**Task Completion**: ✅/❌ [Completion status of each task]

## ✅ Strengths
[List positive points]

## 🔍 Areas for Improvement
[Specific improvement points]

## 📝 Recommendations
[Recommendations with code examples]

## ✅ Overall Assessment
[Overall evaluation and Approval Status]

Usage example:
review PR https://github.com/owner/repo/pull/25
use gh command

