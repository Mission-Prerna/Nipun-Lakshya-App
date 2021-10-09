# Nipun Lakshya Android application

![Nipun Lakshya Core Flows](./images/core_flows.png "Nipun Lakshya Core Flows")

The Nipun Lakshya Android Application is used by parents, teachers, examiners and mentors. With the
app parents & teachers can help students prepare for assessments. Mentors can perform assessments
using the app.

The application is predominantly written in Java with newer elements in Kotlin. The app extensively
uses a modified version of ODK to collect data. The app uses APIs for easier access and storage of
some of our data elements. The app talks to our backend service for authentication and access
control.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Objective

The Nipun Lakshya app is a thermometer for the state & actors to analyse the proficiency of their
students in foundational literacy and numeracy. It is an effort towards
the [Nipun Bharat](https://nipunbharat.education.gov.in/) initiative of the Govt. Of India.

## App internals
* [ODK Collect](https://docs.getodk.org/collect-intro/)
* [Hasura](https://hasura.io/) 
* Firebase 
* Jetpack components - ViewModel, LiveData 
* Libraries - Retrofit, OkHttp, Realm, Rx 
* Languages - Java, Kotlin, XML

## Installation

### 1. Clone the repo and submodules

Clone the repo and also update the submodules
`git submodule update --recursive --remote`

### 2. Setup a NL backend instance

Follow instructions in
the [sandbox-deployment](https://github.com/Mission-Prerna/sandbox-deployment) repo to setup the BE
system to run the app.

**Note: You can skip this and go to step 3.a directly**

### 3. Create a local.properties file

Refer [sample.local.properties](/sample.local.properties) and create a local.properties.

You can replace the creds with the endpoints you have created in step 2.

#### 3.a. Use the sandbox endpoints instead

We have created a sandbox with the entire BE system deployed. You can copy the contents
of [sandbox.properties](/sandbox.properties) to local.properties for using the sandbox environment &
easier setup of the app.

### 4. Create a Firebase project

Create a [new project on Firebase](https://console.firebase.google.com/project/_/settings/general).

Remember to check "Enable Google Analytics for this project" in the Step 2.

#### 4.a. Create & place a project google-services.json file

1. Open the Firebase project created in the above step.
2. Register an Android app with package name `org.samagra.missionPrerna`
3. [Download & Place](https://support.google.com/firebase/answer/7015592?hl=en#android&zippy=%2Cin-this-article)
   the `google-services.json` in `app` folder.

#### 4.b. Create & place an odk google-services.json file

1. Add another Android app to the same Firebase project
2. Add package name as `org.odk.collect.android`
3. Download & place the `google-services.json` in `collect_app` folder

### 5. Run the app & contribute back!

Run the app, enjoy and [contribute](./CONTRIBUTING.md)!

## Test credentials on sandbox

You can use the following credentials to login into the app when pointing to our sandbox environment
as mentioned in point 3.a. above.

- Teacher - 9999999999
- Examiner - 9999999998
- Mentor - 9999999997

Enter OTP as `9876` for all the above test users.

## Build tested on

* OS - macOS Ventura 13
* Android Studio Version - Android Studio Electric Eel | 2022.1.1 Patch 2
* Android Gradle Plugin Version - v7.4.2
* Gradle Version - v7.5
* Kotlin Version - v1.6.21