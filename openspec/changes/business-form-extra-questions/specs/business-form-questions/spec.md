# Spec Delta

## Purpose

Defines the question flow of the new-business bottom sheet: which questions are asked, in what order, what input each accepts, and how the answers are persisted on the business when the user submits the form.

## ADDED Requirements

### Requirement: Cleanliness question with three choices
After the changing-table questions, the new-business form SHALL show a cleanliness question with exactly three single-choice options: Spotless, It's ok, Needs cleaning.

#### Scenario: Cleanliness question appears after changing-table questions
- **WHEN** the user navigates past the changing-table location question in the new-business form
- **THEN** a cleanliness question is displayed with the three options Spotless, It's ok, and Needs cleaning

#### Scenario: Selecting a cleanliness option
- **WHEN** the user selects one of the three cleanliness options
- **THEN** exactly one option is selected and the selection is recorded for saving

### Requirement: Diaper pail question
After the cleanliness question, the new-business form SHALL show a diaper pail question with two single-choice options: Yes and No.

#### Scenario: Diaper pail question appears after cleanliness question
- **WHEN** the user navigates past the cleanliness question in the new-business form
- **THEN** a diaper pail question is displayed with Yes and No options

#### Scenario: Selecting a diaper pail option
- **WHEN** the user selects Yes or No on the diaper pail question
- **THEN** exactly one option is selected and the selection is recorded for saving

### Requirement: Free-text extra note question
After the diaper pail question, the new-business form SHALL show a free-text page where the user can type an optional extra note. The note SHALL be allowed to remain empty.

#### Scenario: Extra note page appears last
- **WHEN** the user navigates past the diaper pail question in the new-business form
- **THEN** a free-text input page is displayed as the last question, with a hint indicating an extra note can be added

#### Scenario: Typing a note
- **WHEN** the user types text into the extra note input
- **THEN** the text is recorded for saving

#### Scenario: Leaving the note empty
- **WHEN** the user leaves the extra note input empty and submits the form
- **THEN** the form submits successfully and no note is stored

### Requirement: Answers persisted on the business
When the user taps "Add business", the form SHALL save the answers onto the new business: the cleanliness choice SHALL be stored as one of `spotless`, `ok`, or `needs_cleaning` (null when the question was skipped), the diaper pail choice SHALL be stored as a boolean, and the extra note SHALL be stored as the business description.

#### Scenario: All questions answered
- **WHEN** the user answers every question and taps "Add business"
- **THEN** the saved business has the cleanliness value, the diaper pail boolean, and the note text set accordingly

#### Scenario: Some questions skipped
- **WHEN** the user skips one or more of the new questions and taps "Add business"
- **THEN** the saved business stores a null cleanliness, false diaper pail, and null description for the skipped answers, and the app does not crash

### Requirement: Cleanliness status displayed on business details
When a saved business is displayed, its cleanliness status SHALL be shown as a colored chip: `spotless` SHALL display as "Clean" with a green background, `ok` SHALL display as "Clean enough" with a yellow background, and `needs_cleaning` SHALL display as "Dirty" with a red background. A business with no cleanliness value SHALL NOT show a cleanliness chip.

#### Scenario: Spotless business displayed
- **WHEN** a business with cleanliness `spotless` is displayed
- **THEN** a "Clean" chip with a green background is shown

#### Scenario: Ok business displayed as clean enough with yellow background
- **WHEN** a business with cleanliness `ok` is displayed
- **THEN** a "Clean enough" chip with a yellow background is shown

#### Scenario: Needs cleaning business displayed
- **WHEN** a business with cleanliness `needs_cleaning` is displayed
- **THEN** a "Dirty" chip with a red background is shown

#### Scenario: Business without cleanliness answer displayed
- **WHEN** a business with a null cleanliness value is displayed
- **THEN** no cleanliness chip is shown

### Requirement: Navigation across all questions
The form SHALL present the five questions as ordered pages. The user SHALL be able to go back to previous questions and skip forward past any question. The last page SHALL NOT show a skip button.

#### Scenario: Skip moves forward through all questions
- **WHEN** the user taps Skip on any question except the last
- **THEN** the next question page is displayed

#### Scenario: Back returns to the previous question
- **WHEN** the user taps Back on any question except the first
- **THEN** the previous question page is displayed

#### Scenario: Last page has no skip button
- **WHEN** the user reaches the extra note page
- **THEN** no skip button is shown on that page

### Requirement: Auto-advance to the next question on answer
When the user selects an option on a single-choice question, the form SHALL automatically move to the next question. The changing-table location question SHALL NOT auto-advance, because it accepts multiple answers; the user SHALL leave it via Skip or Back.

#### Scenario: Answering a single-choice question advances
- **WHEN** the user selects any option on a single-choice question that is not the last page
- **THEN** the form automatically displays the next question page

#### Scenario: Changing-table location question does not auto-advance
- **WHEN** the user selects one or more options on the changing-table location question
- **THEN** the form stays on that question until the user taps Skip or Back

### Requirement: Stored data remains loadable after the schema change
Businesses stored before the cleanliness field existed SHALL remain loadable from the JSON export file, and the database SHALL remain readable after the schema change, without crashing the app.

#### Scenario: Old JSON export without cleanliness loads
- **WHEN** the app loads a JSON export file that does not contain the cleanliness field (and may still contain the legacy `isClean` boolean)
- **THEN** the businesses load successfully with a null cleanliness value and the legacy key is ignored

#### Scenario: Database upgrade does not crash
- **WHEN** the app starts with a database created by an older schema version
- **THEN** the database is migrated automatically and the app starts normally
