# Spec Delta

## ADDED Requirements

### Requirement: High chair question with three choices
After the diaper pail question and before the extra note page, the new-business form SHALL show a high chair question with exactly three single-choice options: Multiple high chairs, One high chair, No high chair. The answer SHALL be stored as one of `multiple`, `single`, or `none` (null when the question was skipped).

#### Scenario: High chair question appears between diaper pail and extra note
- **WHEN** the user navigates past the diaper pail question in the new-business form
- **THEN** a high chair question is displayed with the three options Multiple high chairs, One high chair, and No high chair

#### Scenario: Selecting a high chair option
- **WHEN** the user selects one of the three high chair options
- **THEN** exactly one option is selected and the selection is recorded for saving

#### Scenario: Skipping the high chair question
- **WHEN** the user skips the high chair question and taps "Add business"
- **THEN** the saved business has a null high chair value and the app does not crash

## MODIFIED Requirements

### Requirement: Free-text extra note question
After the high chair question, the new-business form SHALL show a free-text page where the user can type an optional extra note. The note SHALL be allowed to remain empty.

#### Scenario: Extra note page appears last
- **WHEN** the user navigates past the high chair question in the new-business form
- **THEN** a free-text input page is displayed as the last question, with a hint indicating an extra note can be added

#### Scenario: Typing a note
- **WHEN** the user types text into the extra note input
- **THEN** the text is recorded for saving

#### Scenario: Leaving the note empty
- **WHEN** the user leaves the extra note input empty and submits the form
- **THEN** the form submits successfully and no note is stored

### Requirement: Answers persisted on the business
When the user taps "Add business", the form SHALL save the answers onto the new business: the cleanliness choice SHALL be stored as one of `spotless`, `ok`, or `needs_cleaning` (null when the question was skipped), the diaper pail choice SHALL be stored as `true` for Yes, `false` for No, or null when the question was skipped, the high chair choice SHALL be stored as one of `multiple`, `single`, or `none` (null when the question was skipped), and the extra note SHALL be stored as the business description.

#### Scenario: All questions answered
- **WHEN** the user answers every question and taps "Add business"
- **THEN** the saved business has the cleanliness value, the diaper pail boolean, the high chair value, and the note text set accordingly

#### Scenario: Some questions skipped
- **WHEN** the user skips one or more of the new questions and taps "Add business"
- **THEN** the saved business stores a null cleanliness, null diaper pail, null high chair value, and null description for the skipped answers, and the app does not crash

### Requirement: Navigation across all questions
The form SHALL present the six questions as ordered pages. The user SHALL be able to go back to previous questions and skip forward past any question. The last page SHALL NOT show a skip button.

#### Scenario: Skip moves forward through all questions
- **WHEN** the user taps Skip on any question except the last
- **THEN** the next question page is displayed

#### Scenario: Back returns to the previous question
- **WHEN** the user taps Back on any question except the first
- **THEN** the previous question page is displayed

#### Scenario: Last page has no skip button
- **WHEN** the user reaches the extra note page
- **THEN** no skip button is shown on that page
