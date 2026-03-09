An application that assists students with managing their academic tasks and class schedules, stay updated with official campus announcements.

This is to help students manage their academic responsibilities more efficiently while making sure that they won't have to worry about missing a task or school related events.


Team Leader - Limjoco, Ian Rovic D.
Git Manager - Leonor, Vic Adrie John
UI/UX Developer - Lucido, Kenneth Ivan
Feature Developer - Lovete, Ryan Ric
QA/Documentor - Lumio, John Paul

Git Workflow

feature branches -> develop -> main


Project Structure

app/src/main/java/com/example/smartcompanionapp/
│
├── data/                          # Data Layer (Repositories & Sources)
│   ├── database/                  # Local Storage (Room)
│   │   ├── authentication/        # User accounts DB
│   │   │   ├── AuthDao.kt
│   │   │   ├── AuthDatabase.kt
│   │   │   └── UserEntity.kt
│   │   ├── tasks/                 # Tasks & Deadlines DB
│   │   │   ├── TaskDao.kt
│   │   │   └── TaskDatabase.kt
│   │   └── announcement/          # News & Announcements DB
│   │       └── AppDatabase.kt
│   ├── model/                     # Data Entities
│   │   ├── Task.kt
│   │   ├── Announcement.kt
│   │   └── Models.kt              # General user/role models
│   ├── repository/                
│   │   ├── AuthRepository.kt
│   │   ├── TaskRepository.kt
│   │   ├── AnnouncementRepository.kt
│   │   └── UserRepository.kt
│   └── session/                   # Session persistence
│       └── SessionManager.kt      # SharedPreferences logic
│
├── domain/                        # Domain Layer
│   ├── TaskUiState.kt             # Sealed classes for Task UI states
│   └── TaskIntent.kt              # Sealed classes for User actions
│
├── intent/                        # Intent Contracts
│   └── DashboardContract.kt       # Dashboard State/Intent definitions
│
├── ui/                            # UI Layer (Presentation)
│   ├── navigation/                # Compose Navigation
│   │   ├── Navigation.kt          # NavHost & Screen routes
│   │   └── Screen.kt              # Route definitions
│   ├── screens/                   # App Screens (Composables)
│   │   ├── DashboardScreen.kt
│   │   ├── TaskScreen.kt
│   │   ├── ScheduleScreen.kt
│   │   ├── Login&SignupScreen.kt
│   │   ├── AllAnnouncementsScreen.kt
│   │   ├── CampusInformationScreen.kt
│   │   ├── SettingsScreen.kt
│   │   └── NotificationScreen.kt
│   └── theme/                     # App Styling
│       ├── Color.kt               # Brand colors & gradients
│       ├── Theme.kt               # Material 3 setup
│       └── Type.kt                # Typography
│
├── viewmodel/                     # ViewModels
│   ├── TaskViewModel.kt
│   ├── TaskViewModelFactory.kt
│   ├── DashboardViewModel.kt
│   └── AuthViewModel.kt
│
└── MainActivity.kt             
