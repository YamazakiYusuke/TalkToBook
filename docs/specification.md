# TalkToBook Application Specification

## 1. Executive Summary

### 1.1 Problem Statement
My grandmother is writing a book but struggling to make progress due to the difficulty of the task. Writing by hand or typing places a significant physical burden on elderly people, hindering their creative activities.

### 1.2 Solution Overview
We will solve this challenge by creating a smartphone app called "TalkToBook" that records spoken content as text and organizes it into written documents. Voice input will enable elderly users to easily create and edit text.

### 1.3 Key Benefits
- Reduced physical burden of writing through voice input
- Simple and user-friendly interface
- Protection of work through auto-save functionality
- Simplified operation through voice commands

## 2. User Requirements

### 2.1 Target Users
- **Primary User**: Elderly individuals (especially 65+) who want to write books or documents
- **User Characteristics**:
  - Difficulty or burden with typing or handwriting
  - Capable of basic smartphone operation
  - May have physical constraints such as declining vision or hand tremors
  - Limited technical knowledge

### 2.2 User Personas
**Primary Persona: Mrs. Hanako Tanaka (75 years old)**
- Wants to write her autobiography
- Hands get tired from extended typing
- Uses smartphone to communicate with family
- Has difficulty seeing small text

### 2.3 User Stories
1. As a user, I want my spoken content to be automatically converted to text
2. As a user, I want to operate the app easily with large buttons
3. As a user, I want peace of mind knowing my work is automatically saved
4. As a user, I want to control the app with voice commands
5. As a user, I want clear notification when the app cannot be used offline
6. As a user, I want to merge multiple recording sessions into a single document by tapping them in order
7. As a user, I want to organize my documents into chapters for better structure

### 2.4 Use Cases
1. **Voice Recording and Conversion**
   - User presses the record button
   - Records spoken content
   - Automatically converts to text
   - Displays results on screen

2. **Text Editing**
   - Review converted text
   - Provide voice corrections as needed
   - Organize and edit text

3. **Document Management**
   - Combine multiple recording sessions into one document
   - Organize chapters and paragraphs
   - Save completed documents

4. **Document Merging**
   - Enter selection mode in document list
   - Tap documents in desired order (numbered badges appear)
   - View merge preview with selected order
   - Enter title for merged document
   - Confirm merge operation

5. **Chapter Organization**
   - View list of chapters in a document
   - Create new chapters
   - Edit chapter titles and content
   - Reorder chapters within document

## 3. Functional Requirements

### 3.1 Core Features

#### 3.1.1 Voice Recording
- One-tap recording start/stop
- Visual feedback during recording (large recording indicator)
- Recording time display
- Pause and resume functionality

#### 3.1.2 Speech-to-Text Conversion
- High-accuracy speech recognition using OpenAI Whisper API
- Conversion optimized for Japanese language
- Real-time or batch processing conversion
- Speaker adaptation for improved conversion accuracy

#### 3.1.3 Text Organization
- Automatic paragraph separation
- Chapter and section creation functionality
- Text reordering
- Simple editing features (delete, insert, modify)

#### 3.1.4 Auto-Save
- Automatic save every 5 seconds
- Immediate save upon change detection
- Visual display of save status
- Data recovery functionality

#### 3.1.5 Voice Commands
- Basic commands like "Start recording" and "Stop recording"
- Navigation commands like "Next chapter" and "Go back"
- Operation commands like "Save" and "Read aloud"

#### 3.1.6 Document Merge Functionality
- Selection mode for document list with clear visual indicators
- Tap-to-select interface with numbered order badges (1, 2, 3...)
- Merge preview showing selected documents in order
- Custom title input for merged document
- Original documents preserved after merge
- Progress indicator during merge process

#### 3.1.7 Chapter Management
- Chapter list view with clear hierarchy
- Create, edit, and delete chapters
- Drag-and-drop or numbered reordering
- Chapter title and content editing
- Navigation between chapters

### 3.2 User Interface Requirements
- Minimum font size: 18pt
- Minimum button size: 48dp x 48dp
- High contrast color scheme
- Combined use of simple icons and labels
- Voice feedback functionality

### 3.3 Accessibility Requirements
- TalkBack support
- Magnification display support
- Voice guidance
- Vibration feedback
- Color-blind friendly color schemes

## 4. Non-Functional Requirements

### 4.1 Performance Requirements
- Voice recording latency: Maximum 100ms
- Text conversion time: Within 2x recording time
- App startup time: Within 3 seconds
- Auto-save completion: Within 1 second

### 4.2 Security Requirements
- Local storage encryption
- Secure API key management
- User data privacy protection
- Deletion of temporary audio files

### 4.3 Usability Requirements
- Access to main features within 3 taps
- Error messages displayed in plain Japanese
- Operation undo functionality
- Tutorial feature

### 4.4 Reliability Requirements
- Crash rate: Below 0.1%
- Data loss rate: 0% (through auto-save)
- Network connection monitoring and appropriate error handling

## 5. Technical Specifications

### 5.1 Platform and Technology Stack
- **Platform**: Android (Minimum API Level 24 / Android 7.0)
- **Programming Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: Jetpack Compose
- **Dependency Injection**: Hilt
- **Database**: Room
- **Network**: Retrofit + OkHttp

### 5.2 System Architecture
```
┌─────────────────┐
│  Presentation   │  - Activities/Fragments
│     Layer       │  - ViewModels
│                 │  - Composables
├─────────────────┤
│   Domain        │  - Use Cases
│    Layer        │  - Domain Models
│                 │  - Repository Interfaces
├─────────────────┤
│    Data         │  - Repository Implementations
│    Layer        │  - Local Data Sources (Room)
│                 │  - Remote Data Sources (API)
└─────────────────┘
```

### 5.3 API Integration

#### 5.3.1 OpenAI API Specifications
- **Endpoint**: Whisper API for speech-to-text
- **Authentication**: Bearer token
- **Request Format**: 
  ```
  - Audio format: MP3, M4A, WAV
  - Maximum file size: 25MB
  - Language: ja (Japanese)
  ```
- **Response Handling**: JSON with transcribed text
- **Error Codes**: 
  - 401: Invalid API key
  - 429: Rate limit exceeded
  - 500: Server error

#### 5.3.2 Error Handling
- Exponential backoff for retries
- Offline queue for pending transcriptions
- User-friendly error messages
- Fallback to cached results when possible

### 5.4 Data Storage

#### 5.4.1 Local Storage Schema
```kotlin
@Entity
data class Recording(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val audioFilePath: String,
    val transcribedText: String?,
    val status: TranscriptionStatus
)

@Entity
data class Document(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val content: String
)

@Entity
data class Chapter(
    @PrimaryKey val id: String,
    val documentId: String,
    val orderIndex: Int,
    val title: String,
    val content: String
)
```

#### 5.4.2 Future Cloud Migration Plan
- Phase 1: Local SQLite with Room
- Phase 2: Sync with Firebase Firestore
- Phase 3: Full cloud storage with offline caching
- Data migration tools and backwards compatibility

### 5.5 Network Requirements
- **Connectivity**: Required for speech-to-text conversion operations
- **Offline capabilities**: Document viewing, editing, and management work offline
- **Minimum bandwidth**: 1 Mbps (when online)
- **Offline detection**: Immediate UI feedback
- **Connection monitoring**: Real-time status updates

## 6. User Interface Design

### 6.1 Design Principles
1. **Simplicity First**: Maximum functionality with minimum elements
2. **Large Touch Targets**: All interactive elements 48dp or larger
3. **High Contrast**: WCAG AA compliant contrast ratios
4. **Clear Feedback**: Immediate feedback for all operations
5. **Consistent Layout**: Predictable UI element placement

### 6.2 Screen Layouts

#### 6.2.1 Main Screen
- Large recording button (occupying 1/3 of screen)
- Recording history list (large font)
- Simple navigation bar

#### 6.2.2 Recording Screen
- Clear display of recording status
- Waveform visualizer
- Large stop button
- Elapsed time display

#### 6.2.3 Text View Screen
- Readable font and size
- Simple editing toolbar
- Voice command button

#### 6.2.4 Document List Screen
- List of all documents with large, readable titles
- Creation date and time display
- Select button for entering selection mode
- Clear visual distinction between normal and selection modes

#### 6.2.5 Document List Screen (Selection Mode)
- Checkbox or selection indicator for each document
- Numbered badges showing selection order (1, 2, 3...)
- Merge button (appears when 2+ documents selected)
- Cancel selection button
- Selected count display

#### 6.2.6 Document Merge Screen
- Preview of documents to be merged in order
- Large, editable title field for new document
- Visual representation of merge order
- Confirm and Cancel buttons
- Progress indicator during merge

#### 6.2.7 Chapter List Screen
- Hierarchical list of chapters
- Chapter numbers and titles
- Add new chapter button
- Edit indicators for each chapter
- Clear navigation to parent document

#### 6.2.8 Chapter Edit Screen
- Large text input for chapter title
- Content editing area with voice input option
- Save and Cancel buttons
- Delete chapter option with confirmation

### 6.3 Navigation Flow
```
Main Screen → Recording Screen → Processing Screen → Text View Screen
     ↑                                                      ↓
     └──────────── Document List ←──────────────────────────┘
                        ↓
                Selection Mode → Document Merge Screen
                        ↓
                Document Detail → Chapter List → Chapter Edit
```

### 6.4 Senior-Friendly Design Elements
- Combined use of icons and text labels
- Minimal animations
- Confirmation dialogs to prevent misoperation
- Clear transitions between screens
- Help button placed on each screen

## 7. Data Management

### 7.1 Data Models
- **Audio Data**: Temporary audio files (deleted after recording)
- **Text Data**: Persistent text data
- **Metadata**: Timestamps, edit history
- **User Preferences**: Font size, color theme

### 7.2 Storage Strategy
- Audio files: Temporary directory in internal storage
- Text data: Room database
- Settings: SharedPreferences
- Cache: Maximum 100MB

### 7.3 Backup and Recovery
- Automatic backup: Daily
- Manual backup: Available anytime
- Restore functionality: Simple UI
- Data export: Text file format

## 8. Testing Requirements

### 8.1 Unit Testing
- ViewModels logic testing
- Repository layer testing
- Use Cases testing
- Coverage target: 80% or higher

## 9. Deployment and Maintenance

### 9.1 Deployment Strategy
- Distribution via Google Play Store
- Phased rollout (5% → 25% → 50% → 100%)
- Feature validation through A/B testing

### 9.2 Update Mechanism
- Automatic updates recommended
- Important update notifications
- Post-update tutorials

### 9.3 Support and Maintenance
- In-app help center
- Frequently Asked Questions (FAQ)
- Email support
- Regular bug fix releases

## 10. Future Enhancements

### 10.1 Cloud Storage Integration
- Phase 1: Optional cloud backup
- Phase 2: Real-time synchronization
- Phase 3: Multi-device support
- Phase 4: Collaborative editing features

### 10.2 Additional Features
- PDF/EPUB export
- Image insertion functionality
- Enhanced text-to-speech features
- AI-powered text proofreading assistance
- Voice memo functionality

### 10.3 Platform Expansion
- iOS version development
- Web version consideration
- Tablet optimization

## 11. Appendices

### 11.1 Glossary
- **Speech-to-Text (STT)**: Voice recognition technology
- **Auto-Save**: Automatic save functionality
- **Voice Command**: Voice-operated commands
- **Accessibility**: Ease of use for all users, including those with disabilities

### 11.2 References
- Android Accessibility Guidelines
- OpenAI API Documentation
- Material Design Guidelines for Accessibility
- WCAG 2.1 Guidelines

### 11.3 Revision History
- v1.0 (2024-12-06): Initial version created
- v1.1 (2024-12-06): English translation completed
- v1.2 (2024-12-13): Added document merge functionality and chapter management features