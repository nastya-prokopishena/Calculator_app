# Calculator App  

## Project Description

This project is an **Android calculator application** developed as part of the **Mobile Device Programming** course.  
The main goal of the project is to demonstrate practical skills in **mobile application development using Kotlin and Jetpack Compose**, as well as understanding of UI design, state management, and responsive layouts on Android devices.

The application implements multiple screens and calculator logic using a modern declarative UI approach.

---

## Functional Requirements

The application provides the following functionality:

### Start Screen
- Navigation to:
  - Simple Calculator
  - Advanced Calculator
  - About screen
- Exit application option

### Simple Calculator
- Basic arithmetic operations:
  - Addition
  - Subtraction
  - Multiplication
  - Division
- Decimal number input
- Sign change (+/−)
- Percentage calculation
- Clear input
- Result calculation using the equals button

### Advanced Calculator
- Extended mathematical operations:
  - Power
  - Square
  - Square root
  - Trigonometric functions (sin, cos, tan)
  - Logarithmic functions (ln, log)
- Binary and unary operations
- Error handling for invalid input

### About Screen
- Displays application and author information

---

## Technical Implementation

### Architecture
- Single-activity architecture (`MainActivity`)
- UI built entirely with **Jetpack Compose**
- Screen navigation implemented using state (`enum class Screen`)

### State Management
- `rememberSaveable` is used to preserve:
  - Current input
  - Stored values
  - Selected operator
  - Screen state during configuration changes (e.g. rotation)

### UI Adaptation
The interface dynamically adapts to:
- Screen orientation (portrait / landscape)
- Device type (phone / tablet)

This includes:
- Automatic font size adjustment
- Dynamic button sizes
- Adaptive spacing and padding

### Custom Components
- **AutoResizeDisplay**
  - Automatically adjusts text size to prevent overflow
  - Ensures correct display of large numbers

---

## Technologies Used

- **Programming Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Platform:** Android

---

## How to Run the Application

1. Open the project in **Android Studio**
2. Synchronize Gradle files
3. Run the application on:
   - Android Emulator  
   - or a physical Android device

