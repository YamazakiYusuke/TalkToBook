# TalkToBook Screen Transition Diagram

## Screen Flow Overview

```mermaid
graph TD
    A[Splash Screen] --> B[Main Screen]
    
    B --> C[Recording Screen]
    B --> D[Document List Screen]
    B --> E[Settings Screen]
    
    C --> F[Processing Screen]
    F --> G[Text View Screen]
    
    D --> G
    D --> H[Document Detail Screen]
    D --> M[Document List Screen - Selection Mode]
    
    M --> N[Document Merge Screen]
    N --> H
    
    G --> I[Text Edit Screen]
    G --> D
    G --> B
    
    H --> J[Chapter List Screen]
    J --> K[Chapter Edit Screen]
    
    K --> J
    I --> G
    E --> B
    
    %% Voice Command Transitions
    B -.Voice Command.-> C
    G -.Voice Command.-> I
    C -.Voice Command.-> B
    
    %% Error States
    F --> L[Error Screen]
    L --> C
    L --> B
```

## Detailed Screen Transitions

```mermaid
stateDiagram-v2
    [*] --> SplashScreen
    SplashScreen --> MainScreen
    
    MainScreen --> RecordingScreen: Tap Record Button
    MainScreen --> DocumentListScreen: Tap Documents
    MainScreen --> SettingsScreen: Tap Settings
    
    RecordingScreen --> ProcessingScreen: Stop Recording
    RecordingScreen --> MainScreen: Cancel/Voice Command
    
    ProcessingScreen --> TextViewScreen: Conversion Success
    ProcessingScreen --> ErrorScreen: Conversion Failed
    
    DocumentListScreen --> DocumentDetailScreen: Select Document
    DocumentListScreen --> DocumentListSelectionMode: Tap Select Button
    DocumentListScreen --> MainScreen: Back
    
    DocumentListSelectionMode --> DocumentMergeScreen: Tap Merge (2+ selected)
    DocumentListSelectionMode --> DocumentListScreen: Cancel Selection
    
    DocumentMergeScreen --> DocumentDetailScreen: Confirm Merge
    DocumentMergeScreen --> DocumentListSelectionMode: Cancel
    
    DocumentDetailScreen --> ChapterListScreen: View Chapters
    DocumentDetailScreen --> TextViewScreen: Open Document
    
    TextViewScreen --> TextEditScreen: Edit Text/Voice Command
    TextViewScreen --> DocumentListScreen: Save & Exit
    TextViewScreen --> MainScreen: Home
    
    ChapterListScreen --> ChapterEditScreen: Select Chapter
    ChapterEditScreen --> ChapterListScreen: Save/Cancel
    
    TextEditScreen --> TextViewScreen: Save/Cancel
    
    SettingsScreen --> MainScreen: Back
    
    ErrorScreen --> RecordingScreen: Retry
    ErrorScreen --> MainScreen: Home
```

## User Journey Flow

```mermaid
journey
    title TalkToBook User Journey
    section Starting the App
      Open App: 5: User
      View Splash Screen: 5: User
      Arrive at Main Screen: 5: User
    
    section Recording Voice
      Tap Record Button: 5: User
      Start Speaking: 5: User
      View Recording Indicator: 5: User
      Stop Recording: 5: User
      Wait for Processing: 3: User
    
    section Reviewing Text
      View Converted Text: 5: User
      Edit Text if Needed: 4: User
      Save to Document: 5: User
    
    section Managing Documents
      Open Document List: 5: User
      Select Document: 5: User
      Organize Chapters: 4: User
      Export Document: 5: User
    
    section Merging Documents
      Enter Selection Mode: 5: User
      Tap Documents in Order: 5: User
      View Order Numbers: 5: User
      Tap Merge Button: 5: User
      Confirm Merge Order: 5: User
      Enter New Title: 4: User
      View Merged Document: 5: User
```

## Navigation Components

```mermaid
graph LR
    subgraph "Bottom Navigation"
        BN1[Home]
        BN2[Documents]
        BN3[Settings]
    end
    
    subgraph "Voice Commands"
        VC1[Start Recording]
        VC2[Stop Recording]
        VC3[Next Chapter]
        VC4[Go Back]
        VC5[Save]
        VC6[Read Aloud]
    end
    
    subgraph "Floating Action Buttons"
        FAB1[Record]
        FAB2[Voice Command]
    end
```

## Screen States

```mermaid
graph TD
    subgraph "Main Screen States"
        MS1[Default] --> MS2[Recording Available]
        MS2 --> MS3[No Network]
        MS3 --> MS1
    end
    
    subgraph "Recording Screen States"
        RS1[Ready] --> RS2[Recording]
        RS2 --> RS3[Paused]
        RS3 --> RS2
        RS2 --> RS4[Stopped]
    end
    
    subgraph "Processing Screen States"
        PS1[Converting] --> PS2[Success]
        PS1 --> PS3[Failed]
        PS3 --> PS4[Retry]
        PS4 --> PS1
    end
    
    subgraph "Document List Screen States"
        DL1[Normal Mode] --> DL2[Selection Mode]
        DL2 --> DL3[Documents Selected]
        DL3 --> DL4[Merge Available]
        DL4 --> DL1
        DL2 --> DL1
    end
```

## Accessibility Flow

```mermaid
graph TD
    A[Any Screen] --> B{TalkBack Active?}
    B -->|Yes| C[Voice Guidance]
    B -->|No| D[Visual Interface]
    
    C --> E[Spoken Feedback]
    C --> F[Gesture Navigation]
    
    D --> G[Touch Navigation]
    D --> H[Visual Feedback]
    
    E --> I[Action Confirmation]
    F --> I
    G --> I
    H --> I
```

## Error Handling Flow

```mermaid
flowchart TD
    A[User Action] --> B{Network Available?}
    B -->|No| C[Show Offline Alert]
    B -->|Yes| D{API Call}
    
    D -->|Success| E[Process Result]
    D -->|401 Error| F[Invalid API Key Alert]
    D -->|429 Error| G[Rate Limit Alert]
    D -->|500 Error| H[Server Error Alert]
    D -->|Timeout| I[Connection Timeout Alert]
    
    C --> J[Suggest Retry Later]
    F --> K[Contact Support]
    G --> L[Wait and Retry]
    H --> L
    I --> L
    
    L --> M{Retry?}
    M -->|Yes| A
    M -->|No| N[Return to Previous Screen]
```

## Document Merge Feature Details

```mermaid
flowchart TD
    A[Document List Screen] --> B[Tap Select Button]
    B --> C[Selection Mode Active]
    C --> D[User Taps Documents]
    D --> E{Document Selected?}
    E -->|Yes| F[Add Number Badge]
    E -->|No| G[Remove from Selection]
    F --> H{2+ Documents Selected?}
    H -->|Yes| I[Show Merge Button]
    H -->|No| J[Hide Merge Button]
    I --> K[Tap Merge Button]
    K --> L[Document Merge Screen]
    L --> M[Display Selected Documents in Order]
    M --> N[Enter New Document Title]
    N --> O[Confirm Merge]
    O --> P[Process Merge]
    P --> Q[Create New Document]
    Q --> R[Navigate to Document Detail]
    
    G --> H
    C --> S[Cancel Selection]
    S --> A
```

## Notes

- All transitions include appropriate animations for senior users (minimal and clear)
- Voice commands are available on main screens for hands-free navigation
- Error states always provide clear recovery paths
- Back navigation is consistently available via gesture or voice command
- Auto-save ensures no data loss during any transition
- Document merge feature uses tap order for intuitive ordering (numbered badges show selection order)
- Merge operation creates a new document while preserving originals