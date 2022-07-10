# Budget App

This app helps you and your family keep track of expenses. It allows users to enter their expenses and generates an xlsx file for every month. It combines the records from all the users to show how much your family has spent and can generate an xlsx for individual and collective expenses

## Installation
You will need Android Studio installed on your machine.
```sh
git clone
```
After cloning you can open this folder in android studio. In the root directory you have to create a file named `local.properties` (if it does not exist) and add the following variables to the file (do not erase existing contents of the file if it exists by default)

`local.properties`
```text
APP_ID=BACK_4_APP_APP_ID
CLIENT_KEY=BACK_4_APP_CLIENT_KEY
SERVER=BACK_4_APP_SERVER_URL
```
To get the APP_ID, CLIENT_KEY and SERVER you have to create an account in Back4App. The servers for this is hosted on Back4App. Once you create an account you have to create a new app