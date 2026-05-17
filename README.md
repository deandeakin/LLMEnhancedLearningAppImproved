# LLM-Enhanced Learning Assistant

## Overview
LLM-Enhanced Learning Assistant is an Android mobile application built in Java using Android Studio.  
The app is designed to support students by generating short learning activities powered by an LLM.

## Features
- User Registration and Login
  - Users can create an account, log in, and log out.
  - Session state is stored locally using SharedPreferences.

- Interest Selection
  - Users select learning interests such as Algorithms, Data Structures, Cyber Security, and others.
  - These interests are used to generate personalized questions and learning support through the LLM.

- AI-Generated Tasks
  - The app requests a personalized task from the backend.
  - Each generated task contains:
    - a topic
    - a short lesson summary
    - two multiple-choice questions
    - answer options and correct answers

- AI Learning Utilities
  - Generate Hint: provides a hint for the current question.
  - Explain My Answer: explains the selected answer using AI-generated feedback.

- Results Screen
  - After completing both questions, the app displays:
    - Question 1 result
    - Question 2 result
    - an overall summary
  - Submitted answers are saved locally for history and profile statistics.

- Profile Screen
  - Users can view their saved profile details, selected interests, current account plan, and learning statistics.
  - The profile displays:
    - total questions answered
    - correctly answered questions
    - incorrectly answered questions

- Learning History
  - The app stores completed learning interactions locally using Room.
  - Users can view a history of previously answered questions.
  - Each history item shows the topic, question, selected answer, correct answer, and result.

- Profile Sharing
  - Users can share their learning profile using Android's built-in share sheet.
  - The shared profile includes the user's learning stats and current account plan.
  - Users can also generate a QR code containing their profile summary.

- Account Upgrade
  - Users can view and select different account plans.
  - Paid plans use a Google Pay test-mode.
  - After a successful test payment response, the selected plan is saved locally using SharedPreferences.
  - No real payment is processed.

- Recent Activity
  - The latest activity is shown on the dashboard.

## Built With
- Java
- Android Studio
- XML
- SharedPreferences
- Room Database
- RecyclerView
- Retrofit
- Gson
- Google Pay API
- ZXing QR Code Library
- Node.js backend
- Gemini API

## Backend Note
This Android project communicates with a separate backend server using Retrofit. The backend is responsible for:
- receiving task generation and learning feedback requests
- calling the Gemini API
- returning structured JSON responses to the Android app

The backend is kept separate from this Android Studio project, and API keys are not included in this repository.

## Google Pay Note
The account upgrade feature uses Google Pay in test mode. This is used to demonstrate the purchasing workflow without processing real payments. After a successful test payment response, the app saves the selected account plan locally.

## How to Run
1. Open the project in Android Studio
2. Sync the Gradle files
3. Start the separate backend server
4. Run the app using an emulator or Android device

## Backend
This app expects a backend running locally at `http://10.0.2.2:3000/`

This address is intended for the Android emulator to communicate with a backend running on the development machine.

## Future Changes
- Replace the current local account system with proper Room-based user storage
- Refactor the task structure into a more object-oriented design for easier expansion and maintenance
- Expand account plans so different plans unlock different learning features
- Add optional backend support for public profile links and cloud-synced history

## Author
Dean Kennedy  
s224318581
