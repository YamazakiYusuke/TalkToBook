# Issue Type
<!--
Select from the following:
- [ ] Bug
- [x] Feature
- [ ] Enhancement
- [ ] Documentation
- [ ] Refactoring
- [ ] Testing
-->

# Priority/Severity
<!--
Select from the following:
- [ ] Critical
- [x] High
- [ ] Medium
- [ ] Low
-->

# Overview
<!--
Describe the overview of the work
Clearly state what needs to be done
-->

Implement Google Analytics 4 (GA4) with Firebase Analytics integration for the TalkToBook Android application to track user engagement, voice recording usage, transcription accuracy, and senior-friendly accessibility metrics.

# Details
<!--
task 
Describe the details of the work
First, create a branch using git worktree.
```
git worktree add <path-to-working-tree> <branch-name>
```
Complete the work by creating a PR on GitHub
-->

**Task Overview:**
Integrate Firebase Analytics with GA4 to provide comprehensive analytics for elderly users (65+) using the TalkToBook voice-to-text application.

**Implementation Steps:**

1. **Firebase Project Setup**
   - Create/configure Firebase project for TalkToBook
   - Generate and integrate `google-services.json` configuration file
   - Link Firebase project to GA4 property

2. **Dependencies and Configuration**
   - Add Firebase Analytics dependencies to `app/build.gradle.kts`
   - Configure Google Services plugin in project-level and app-level build files
   - Update project configuration for Firebase integration

3. **Analytics Manager Implementation**
   - Create centralized `AnalyticsManager` class using Hilt dependency injection
   - Implement methods for tracking voice recording events, transcription accuracy, document creation
   - Add senior-friendly analytics tracking (accessibility features, usage patterns)

4. **Integration with Existing Architecture**
   - Inject `AnalyticsManager` into ViewModels (RecordingViewModel, DocumentViewModel, etc.)
   - Implement event tracking in appropriate lifecycle methods
   - Add user property tracking for age groups and accessibility levels

5. **Privacy and Compliance**
   - Implement user consent mechanism for analytics data collection
   - Add GDPR compliance features with opt-out functionality
   - Ensure transparent data usage disclosure

6. **Custom Events Implementation**
   - Voice recording events (start, completion, duration)
   - Transcription events (accuracy, language, success rate)
   - Document management events (creation, editing, export)
   - Accessibility feature usage tracking
   - User engagement metrics for elderly users

**Branch Creation:**
```bash
git worktree add ../ga4-implementation feature/ga4-firebase-analytics
```

# Acceptance Criteria
<!--
Clearly define completion conditions
Example:
- [ ] Feature A is implemented
- [ ] Tests are added
- [ ] Documentation is updated
-->

- [ ] Firebase Analytics SDK successfully integrated with project
- [ ] `google-services.json` configuration file properly placed and configured
- [ ] `AnalyticsManager` class implemented with Hilt dependency injection
- [ ] Custom events implemented for voice recording, transcription, and document operations
- [ ] Senior-friendly analytics tracking implemented (accessibility features, age groups)
- [ ] Privacy compliance features implemented (user consent, opt-out)
- [ ] Analytics integration added to all relevant ViewModels
- [ ] Debug mode configuration for testing environments
- [ ] Unit tests written for `AnalyticsManager` class
- [ ] Integration verified using Firebase DebugView
- [ ] GA4 real-time reports showing data flow
- [ ] Build passes without errors (`./gradlew build`)
- [ ] Unit tests pass (`./gradlew test`)
- [ ] Lint checks pass (`./gradlew lint`)

# Testing Requirements
<!--
Specify whether unit test implementation is required
- [ ] Unit test implementation required
- [ ] Unit test implementation not required
-->

- [x] Unit test implementation required

**Testing Scope:**
- Unit tests for `AnalyticsManager` class methods
- Mock Firebase Analytics calls in test environment
- Verify event parameters and user properties are correctly set
- Test privacy compliance methods (consent handling, opt-out)
- Integration tests for ViewModel analytics integration

# Dependencies
<!--
Other issues or tasks this work depends on
Example:
- Depends on #123
- Blocked by #456
-->

- Requires Google Analytics account with admin access
- Requires Firebase project setup (can be done during implementation)
- No blocking dependencies on existing issues

# Reference Documents
**IMPORTANT: Always refer to Reference Documents first before starting work**

- CLAUDE.md
- docs/ga4-implementation-guide.md
- docs/specification.md (for senior-friendly design requirements)
- Firebase Analytics Documentation: https://firebase.google.com/docs/analytics
- GA4 for Mobile Apps: https://support.google.com/analytics/answer/9304153

# Working Branch
<!-- Use git worktree -->

```bash
git worktree add ../ga4-implementation feature/ga4-firebase-analytics
cd ../ga4-implementation
```

# Task Checklist
<!--
Specific work steps
Example:
- [ ] Create branch with git worktree
- [ ] Implement functionality
- [ ] Add tests
- [ ] Update documentation
- [ ] Create PR
- [ ] Receive review
- [ ] Merge
-->

## Setup Phase
- [ ] Create branch with git worktree: `feature/ga4-firebase-analytics`
- [ ] Set up Firebase project and generate `google-services.json`
- [ ] Add Firebase Analytics dependencies to build files
- [ ] Configure Google Services plugin

## Implementation Phase
- [ ] Create `AnalyticsManager` class in `data/analytics/` directory
- [ ] Implement Hilt module for `AnalyticsManager` dependency injection
- [ ] Add analytics integration to `RecordingViewModel`
- [ ] Add analytics integration to `DocumentViewModel`
- [ ] Add analytics integration to `TranscriptionViewModel`
- [ ] Implement custom events for voice recording operations
- [ ] Implement custom events for transcription operations
- [ ] Implement custom events for document management
- [ ] Add senior-friendly analytics tracking methods
- [ ] Implement privacy compliance features (consent, opt-out)

## Testing Phase
- [ ] Write unit tests for `AnalyticsManager` class
- [ ] Write unit tests for analytics integration in ViewModels
- [ ] Test Firebase Analytics integration using DebugView
- [ ] Verify custom events in GA4 real-time reports
- [ ] Test privacy compliance features
- [ ] Run full test suite: `./gradlew test`
- [ ] Run lint checks: `./gradlew lint`
- [ ] Run build verification: `./gradlew build`

## Documentation and Review Phase
- [ ] Update CLAUDE.md with analytics configuration if needed
- [ ] Create PR with detailed description of implementation
- [ ] Include testing verification screenshots in PR
- [ ] Request code review from team members
- [ ] Address review feedback and update implementation
- [ ] Merge PR after approval

## Verification Phase
- [ ] Verify GA4 data collection in production environment
- [ ] Monitor Firebase Analytics console for data flow
- [ ] Validate custom dimensions and metrics setup
- [ ] Confirm senior-friendly analytics are tracking correctly